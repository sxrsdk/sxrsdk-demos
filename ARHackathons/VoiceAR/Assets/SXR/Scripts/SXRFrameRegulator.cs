#if UNITY_ANDROID && !UNITY_EDITOR
#define UNITY_GLES_RENDERER
#endif
using UnityEngine;
using System.Collections;
using UnityEngine.UI;
using System;
using System.Runtime.InteropServices;

namespace Sxr {

    // This is the Unity's interface into Frame Regulator implementation
    public class SXRFrameRegulator {
#if UNITY_ANDROID && !UNITY_EDITOR

    [DllImport ("sxr-plugin")]
    private static extern long PureGetCurrentTime();

    [DllImport ("sxr-plugin")]
    private static extern IntPtr PureGetRenderEventFunc();

    [DllImport ("sxr-plugin")]
    private static extern void PureGameInitialized(int numEyeBuffers);

    [DllImport ("sxr-plugin")]
    private static extern int PureGetNextBufferIdx();

    [DllImport ("sxr-plugin")]
    private static extern void PureSetBufferCnt(long bufCnt);
#endif

        private const bool DEBUG_PER_FRAME = false;

        public const bool TEST_DRAW_TO_SHOW_SYNC = false;

        // Specify the number of draw buffers: need to be consistent with native and Java
        public const int BUFFER_NUM = 4;
        public const bool USE_PREDICTION = true;

        // Before drawing each frame, wait for signal from eye-show.
        // TODO: This can reduce FPS and needs to be REVISITed
        public const bool WAIT_FOR_PULSE = false;

        // Submit a frame before gl finish of rendering. 
        public const bool EARLY_FRAME_SUBMISSION = false; // Not a recommended way by Unity, so side effects are unknown. Verify if any visual side effects, such as visual artifacts on games objects and GUI, before turning it on.

        // The point where it waits for the next eye buffer; it can be delayed in the gamethread if there are no visual artifacts
        public const bool DELAYED_EYEBUFFER_GET = false;
        
        // SXR configuration file in StreamingAssets
        private const string sSxrXmlFile = "Xml/gvr.xml";
        private const bool sEnableFrameRegulator = true;

        // Singleton
        private static SXRFrameRegulator sInstance;
        private bool bPluginRenderReady = false;
        public bool isResumeInProgress = false;

        // Buffer management- buffer count or index can be controlled in the form of queue in FR. bufferIdxLatest is 
        // the last acquired index that is availabe for eye drawing.
        private int bufferIdxLatest;

        // Prediction
        private Transform mHeadTransform; // head transform saved

        private SXRFrameRegulator() {
            bufferIdxLatest = 0; // this is for initial one time. reset is not needed as it is fetched.
        }

        public static SXRFrameRegulator Instance {
            get {
                if (sInstance == null) {
                    sInstance = new SXRFrameRegulator();
                }
                return sInstance;
            }
        }

        // API
        Text txtDebug = null;
        public void setDebug(Text debugText) {
            txtDebug = debugText;
        }

        // Initialize the Frame Regulator and establish links with Framework
        public void initialize() {
            Debug.Log("SXRFrameRegulator::initialize");
            linkToPlugin();
        }

        // Start the view. If FR is enabled, it runs the ShowEye thread; if not, it
        // enables the distorter.
        public bool start() {
            return SXRConfigurationIF.Start();
        }

        // Stop the view. If FR is enabled, stops the ShowEye thread; if not, it does nothing.
        public void stop() {
            SXRConfigurationIF.Stop();
        }


        public bool isReady() {
            // ready to render, including both Java obj init and plugin view init
            return bPluginRenderReady;
        }

        public void beforeDraw() {
            SXRConfigurationIF.BeforeDraw(getBufferIdx());
        }

        public bool startExtCamera() {
            return SXRConfigurationIF.startExtCamera();
        }

        public void stopExtCamera() {
            SXRConfigurationIF.stopExtCamera();
        }

        public string getVersion() {
#if UNITY_ANDROID && !UNITY_EDITOR
        return SXRConfigurationIF.GetVersion();
#else
            return "0.0.0";
#endif
        }

        public float getFovY() {
#if UNITY_ANDROID && !UNITY_EDITOR
        return SXRConfigurationIF.GetFovY();
#else
            return 93.0f;// default device param with no specific device types
#endif
        }

        public float getEyeHeight() {
            return SXRConfigurationIF.GetEyeHeight();
        }

        public float getInterpupillaryDistance() {
#if UNITY_ANDROID && !UNITY_EDITOR
        return SXRConfigurationIF.GetInterpupillaryDistance();
#else
            return 0.062f;// default device param with no specific device types
#endif
        }

        public float getHeadModelDepth() {
#if UNITY_ANDROID && !UNITY_EDITOR
        return SXRConfigurationIF.GetHeadModelDepth();
#else
            return 0.0805f;// default device param with no specific device types
#endif
        }

        public float getHeadModelHeight() {
#if UNITY_ANDROID && !UNITY_EDITOR
        return SXRConfigurationIF.GetHeadModelHeight();
#else
            return 0.075f;// default device param with no specific device types
#endif
        }

        public float getAspectRatio() {
#if UNITY_ANDROID && !UNITY_EDITOR
        return SXRConfigurationIF.GetAspectRatio();
#else
            return 1.027778f;// default device param with no specific device types
#endif
        }

        public int getExtCameraNativeTextureID() {
#if UNITY_ANDROID && !UNITY_EDITOR
        return SXRConfigurationIF.GetExtCameraNativeTextureID();
#else
            return 0;
#endif
        }

        public void incBufferIdx() {
#if UNITY_ANDROID && !UNITY_EDITOR
        int idx = PureGetNextBufferIdx();
        if (DEBUG_PER_FRAME) {
            Debug.Log("incBufferIdx bufferIdxLatest=" + bufferIdxLatest + ", new idx=" + idx);
        }
        bufferIdxLatest = idx;

#endif
        }

        public int getBufferIdx() {
            return bufferIdxLatest;
        }

        // Must match SXRPlugin.java
        enum MsgId {
            QUATERNION = 1,
            POLL_SENSOR = 2,
            FLASH_SCREEN = 90,
            CLEAR_BUFFER = 91,
        };

        public SensorManager.SxrPose getDrawPose() {
            SensorManager.SxrPose predictedPose = SensorManager.Instance.getDrawPose();

            if (DEBUG_PER_FRAME) {
                Debug.Log(string.Format("predict Sensor Quat w:{0} x:{1} y:{2} z:{3}", predictedPose.w, predictedPose.x,
                    predictedPose.y, predictedPose.z));
            }

            return predictedPose;
        }

        public void saveTransform(Transform transform) {
            mHeadTransform = transform;

            if (DEBUG_PER_FRAME) {
                Debug.Log(string.Format("saveTransform Camera Quat w:{0} x:{1} y:{2} z:{3}",
                mHeadTransform.localRotation.w, mHeadTransform.localRotation.x, mHeadTransform.localRotation.y, mHeadTransform.localRotation.z));
            }
        }

        public Transform getTransform() {
            return mHeadTransform;
        }

        public void beginFrame() {
        }

        public void endFrame() {
        }

        public bool queryBufferCountStatus(long bufCnt) {
            // query if the buffer count wait is done to the plugin native
            // can be called from game thread
#if UNITY_ANDROID && !UNITY_EDITOR
        return SXRConfigurationIF.QueryBufferCountStatus(bufCnt);
#else
            return false;
#endif
        }

        public void SubmitFrame() {
            if (isReady() // && frameRendered
                ) {
                if (DEBUG_PER_FRAME) {
                    Debug.Log("frame submitting, getBufferIdx  idx " + getBufferIdx());
                }
#if UNITY_GLES_RENDERER
                // to avoid racing condition, this should be close to the RenderEventID.Rendered, at least after OnPostRender
                long bufidxLong = getBufferIdx();
                PureSetBufferCnt(bufidxLong);

                GL.IssuePluginEvent(PureGetRenderEventFunc(), (int) SXRCameraSystem.RenderEventID.Rendered);

                endFrame(); // for debug
                if (DEBUG_PER_FRAME) {
                    Debug.Log("SubmitFrame() done.");
                }
#endif
            }
        }

        // linking to the SXR Plugin low-end, either java or native
        private void linkToPlugin() {
            // at this point, the sxr plugin instance should be ready
            Debug.Log("linkToPlugin begin");

            string result = "Not android";

#if UNITY_ANDROID && !UNITY_EDITOR

        // the game engine initialized, notify sxr framework to do some init on this thread
        // it can be multi-threaded depending on the PlayerSettings.MTRendering
        PureGameInitialized(BUFFER_NUM);

        //TODO: consider combining with PureGameInitialized() for robust call sequence
        bool initOK = SXRConfigurationIF.InitializePluginRender(sSxrXmlFile);
        result = "OK: " + initOK.ToString();

        // issue init on the rendering thread, async
        // implicit for now. GL.IssuePluginEvent(PureGetRenderEventFunc(), (int)RenderEventID.RenderInitialized);
        Debug.Log("linkToPlugin and initial handshaking succ.");

        // plugin render ready
        bPluginRenderReady = true;

#endif

            if (DEBUG_PER_FRAME) {
                if (txtDebug != null) {
                    txtDebug.text = result;
                }

                Debug.Log("SXRFrameRegulator: " + result);
            }
        }
    }
}
