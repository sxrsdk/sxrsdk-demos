
// on OpenGL ES there is no way to query texture extents from native texture id
#if UNITY_ANDROID && !UNITY_EDITOR
#define UNITY_GLES_RENDERER
#endif

using UnityEngine;
using System;
using System.Xml;
using System.Collections;
using System.Collections.Generic;
using System.Runtime.InteropServices;

namespace Sxr {

    /// <summary>
    /// A SXR camera that is rendering class linked with SXR pure backend.
    /// </summary>
    [DisallowMultipleComponent]
    public class SXRCamera : MonoBehaviour {
        private const bool DEBUG_PER_FRAME = false;

        /// <summary>
        /// SXR pre-defined eye parameters
        /// </summary>
        private struct SXREyeParameters {
            public float camerasOffset;
            public string name;

            public SXREyeParameters(string name) {
                SXRFrameRegulator fr = SXRManager.Instance.getFrameRegulator();
                camerasOffset = fr.getInterpupillaryDistance();
                this.name = name;
            }
        };

        #region inspector members
        [SerializeField]
        [Tooltip("Use SXRCameraSystem global settings.")]
        public bool useGlobalSetting = true;

        [SerializeField]
        [Tooltip("Use FixedUpdate for physics based camera tracking.")]
        public bool useFixedUpdate = false;

        [SerializeField]
        [Tooltip("Use ChromaticAberration.")]
        public bool useChromaticAberration = true;

        [SerializeField]
        [Tooltip("Use Antialiasing.")]
        public bool useAntialiasing = false;

        [SerializeField]
        [Tooltip("Use AntialiasingLevel..")]
        public SXRCameraSystem.UnityAntialiasingLevel antialiasingLevel = SXRCameraSystem.UnityAntialiasingLevel.X2Sample;

        [SerializeField]
        [Tooltip("Use Multiple cameras in the scene.")]
        public bool useMultipleCamera = false;

        [SerializeField]
        [Tooltip("AR Camera Mode")]
        public bool ARCameraMode = false;

        [SerializeField]
        [Tooltip("AR Background Video")]
        public bool ARBackgroundVideo = false;

        #endregion inspector members

        // The size of eye texture buffer size (the default w x h)
        // Can be changed by the app developers through app settings.xml, not through changing these constants in codes/inspector directly
        private int eyeBufferTexW = 1024; 
        private int eyeBufferTexH = 1024;
        private float cameraAspect;

        // Current root camera/camera rig
        private Camera mainCamera = null;
        private GameObject mainCameraRig = null;

        // Local parameters
        private bool renderEnabled = false;

        // SXR Camera settings
        private List<SXRCameraSystem.SXRCameraSettingFlag> settingsList;
        private bool lateUpdateSettings = false;

        #region properties
        public Camera MainCamera { get { return mainCamera; } set { mainCamera = value; } }
        public bool IsCameraActive { get { return mainCamera.enabled; } }
        public bool IsCameraRendering { get { return renderEnabled; } set { renderEnabled = value; } }

        [HideInInspector]
        public bool IsInActiveGroup = false;

        public SXRCameraGroup SXRCurrentGroup;

        public Camera leftCamera, rightCamera;
        private GameObject objectSXRCameraRig;

        // use WaitForEndOfFrame coroutine to do our PostRender tasks after all cameras rendered
        private static WaitForEndOfFrame waitForEndOfFrame = new WaitForEndOfFrame();

        // Frame is set to render with pass of an Update call
        private bool frameRendered = false;
        private bool isInCoroutine = false;
        private bool cameraPropertyInitialized = false;

        public RenderTexture[] rightTexture;
        public RenderTexture[] leftTexture;

        public int[] texIdLeft = new int[SXRFrameRegulator.BUFFER_NUM];
        public int[] texIdRight = new int[SXRFrameRegulator.BUFFER_NUM];

        SXRManager sxrm;
        SXRFrameRegulator fr;

        // Keep a copy of the executing script
        private IEnumerator coroutine = null;

        // For debugging
        static SXRCamera sSXRCamera;
        static int sDrawCount;

        /// </summary>

        //TODO: move all Pure*() signatures to SXRConfigurationIF and make calls through that IF
#if UNITY_GLES_RENDERER
    [DllImport ("sxr-plugin")]
    private static extern void PureInitializer();

    [DllImport ("sxr-plugin")]
    private static extern void PureSetLeftTextureFromPlugin(int bufferId, int textureId, int w, int h); // w, h: currently not used but through native texture getty

    [DllImport ("sxr-plugin")]
    private static extern void PureSetRightTextureFromPlugin(int bufferId, int textureId, int w, int h);

    [DllImport ("sxr-plugin")]
    private static extern void PureSetBufferCnt(long bufCnt);

    [DllImport ("sxr-plugin")]
    private static extern void PureDebugFunc(int cmd);

    [DllImport ("sxr-plugin")]
    private static extern IntPtr PureGetRenderEventFunc();

#endif
        #endregion properties

        #region constructors
        void Awake() {
            Debug.Log("SXRCamera::Awake");

            // prepare sxr instance - need to instantiate before events, like onResume
            if (sxrm == null) {
                sxrm = SXRManager.Instance;
                Debug.Log("SXRCamera::Awake sxr instance created.");
            }

            // Move Application's QualitySetting to SXRCameraSystem Awake()
        }
        #endregion constructors

        #region methods
        /// <summary>
        /// Initialize eye textures.
        /// </summary>
        private void initTextureArray() {
            // always do reinit, does not rely on the static array from UnityEditor
            rightTexture = new RenderTexture[SXRFrameRegulator.BUFFER_NUM];
            leftTexture = new RenderTexture[SXRFrameRegulator.BUFFER_NUM];
        }

        /// <summary>
        /// Coroutine for rendering of SXR pure backend.
        /// </summary>
        private IEnumerator FrameSubmissionCoroutine() {
            while (true) {
                yield return waitForEndOfFrame;

                if (!renderEnabled) {
                    yield break;
                }

                if (!SXRFrameRegulator.EARLY_FRAME_SUBMISSION && frameRendered) {
                    fr.SubmitFrame();
                }
                if (!SXRFrameRegulator.DELAYED_EYEBUFFER_GET && frameRendered) {
                    fr.incBufferIdx();
                }
            }
        }

        void Start() {
            if (SXRCameraSystem.Instance == null) {
                Debug.LogError("SXRCameraSystem instance is not exist in the scene!");
                return;
            }
            Debug.Log("SXRCamera::Start " + gameObject.FullPath());
            if (sxrm != null) {
                fr = sxrm.getFrameRegulator();
            }
            sSXRCamera = this;

            // validate settings with SXRCameraSystem
            if (lateUpdateSettings && useGlobalSetting) {
                UpdateCameraSettings(SXRCameraSystem.Instance.GetCameraSettingFlag());
                lateUpdateSettings = false;
            }

#if UNITY_GLES_RENDERER
        sxrm.onResume();

        initTextureArray();

        int nativeValue = SXRConfigurationIF.GetResolutionWidth();
        if (nativeValue > 0) 
            eyeBufferTexW = nativeValue;
        nativeValue = SXRConfigurationIF.GetResolutionHeight();
        if (nativeValue > 0) 
            eyeBufferTexH = nativeValue;
        Debug.Log("SXRCamera::Start creating eye textures with resolution of " + eyeBufferTexW + "x" + eyeBufferTexH);
#endif // UNITY_GLES_RENDERER

        // aspect ratio (width divided by height)
        cameraAspect = (float)eyeBufferTexW / (float)eyeBufferTexH;

        if (!useAntialiasing) {
            antialiasingLevel = 0;
            QualitySettings.antiAliasing = 0;
         }
        Debug.Log("SXRCamera::Start QualitySettings.antiAliasing " + QualitySettings.antiAliasing);

#if UNITY_GLES_RENDERER
        for (int i = 0; i < SXRFrameRegulator.BUFFER_NUM; ++i)
        {
            // texture will be created with width x height, and depth bit
            leftTexture[i] = new RenderTexture(eyeBufferTexW, eyeBufferTexH, 24);//in Unity, only 24 bit depth has stencil buffer
            rightTexture[i] = new RenderTexture(eyeBufferTexW, eyeBufferTexH, 24);

            leftTexture[i].antiAliasing = rightTexture[i].antiAliasing = (QualitySettings.antiAliasing == 0) ? 1 : QualitySettings.antiAliasing;
            texIdLeft[i] = texIdRight[i] = 0;
        }

        SXRConfiguration.Instance.SetChromaticAberration(useChromaticAberration);

        // Start render submission coroutine
        if (coroutine == null) {
            coroutine = FrameSubmissionCoroutine();
        }

        if (!isInCoroutine) {
            StartCoroutine(coroutine);
            isInCoroutine = true;
        }
#endif // UNITY_GLES_RENDERER

        }


        /// <summary>
        /// Enabled monobehaviour.
        /// </summary>
        void OnEnable() {
            Debug.Log("SXRCamera:OnEnable " + gameObject.FullPath());

#if UNITY_ANDROID && !UNITY_EDITOR
        // Activate notification system only if multiple camera option is used
        if (useMultipleCamera) {
            // Let SXRCameraSystem knows which group is transitioned, mainCamera and group should be set ahead
            // Also wait until gropu head SXRCamera trigger to send notification
            if (SXRCurrentGroup != null && !IsCameraRendering) {
                SXRCameraSystem.Instance.NotifyStatus(this, true);
            }
        } else {
            // Initialize single SXR camera itself
            Initialize();
        }
#endif
        }

        /// <summary>
        /// Disabled monobehaviour.
        /// </summary>
        void OnDisable() {
            Debug.Log("SXRCamera::OnDisable " + gameObject.FullPath());

#if UNITY_ANDROID && !UNITY_EDITOR
        // Activate notification system only if multiple camera option is used
        if (useMultipleCamera) {
            // Stop current rendering camera group while in transitioning
            if (renderEnabled) {
                SXRCameraSystem.Instance.NotifyStatus(this, false);
            }

            // Exit renderer coroutine
            if (coroutine != null) {
                StopCoroutine(coroutine);
                coroutine = null;
                isInCoroutine = false;
            }

            renderEnabled = false;
        } else {
            SetSXRCameraStatus(false);
        }

        // Clean buffers
        for (int i = 0; i < SXRFrameRegulator.BUFFER_NUM; ++i) {
            if (rightTexture[i] != null && rightTexture[i].IsCreated()) {
                rightTexture[i].Release();
            }
            if (leftTexture[i] != null && leftTexture[i].IsCreated()) {
                leftTexture[i].Release();
            }
        }
#endif
        }

        void setSxrCameras() {
#if UNITY_GLES_RENDERER
        gameObject.GetComponent<Camera> ().targetTexture = null;

        // we want to enforce flags of all the SXR created cameras.
        // but we don't touch other cameras created by the application, if any.

        if (objectSXRCameraRig == null) { // not initialized yet
            GameObject objectSXRLeftCamera, objectSXRRightCamera;

            if (!(gameObject.GetComponent<Camera>()) ||
                !(objectSXRCameraRig = GameObject.Find("SXRCameraRig")) ||
                !(objectSXRLeftCamera = GameObject.Find("SXRLeftCamera")) ||
                !(objectSXRRightCamera = GameObject.Find("SXRRightCamera"))
            ) {
                Debug.Log("SXRCamera::Start detected a missing SXR Camera component!"); 
            } else {
                if (DEBUG_PER_FRAME) {
                    Debug.Log("SXRCamera::Start SXRCamera? " + gameObject.GetComponent<Camera>().enabled);   
                    Debug.Log("SXRCamera::Start SXRCameraRig? " + objectSXRCameraRig.GetComponent<Camera>().enabled); 
                    Debug.Log("SXRCamera::Start SXRRightEye? " + objectSXRLeftCamera.GetComponent<Camera>().enabled); 
                    Debug.Log("SXRCamera::Start SXRLeftEye? " + objectSXRRightCamera.GetComponent<Camera>().enabled); 
                    Debug.Log("SXRCamera::Start QualitySettings.antiAliasing? "  +  QualitySettings.antiAliasing);
                }

                gameObject.GetComponent<Camera>().enabled = false;
                objectSXRCameraRig.GetComponent<Camera>().enabled = false;
                objectSXRLeftCamera.GetComponent<Camera>().enabled = true;
                objectSXRRightCamera.GetComponent<Camera>().enabled = true;

                // attach the OnPostRender() script onto the camera
                if (objectSXRLeftCamera.GetComponent<SXRCameraRig>() == null) {
                    Debug.Log("GetComponent<SXRCameraRig>() == null, adding..." );
                    objectSXRLeftCamera.gameObject.AddComponent<SXRCameraRig>();
                    objectSXRRightCamera.gameObject.AddComponent<SXRCameraRig>();
                }

            }
        }

        if (DEBUG_PER_FRAME) {
            int count = Camera.allCamerasCount; //count = Camera.allCameras.Length;
            Debug.Log("allCamerasCount " + count + " cameras");

            foreach (Camera c in Camera.allCameras)
            {
                Debug.Log("c.gameObject.name:" + c.gameObject.name);
                Debug.Log("c.gameObject.active:" + c.gameObject.activeSelf);
            }
        }

#endif
        }

        /// <summary>
        /// Normal update.
        /// </summary>
        void Update() {
            if (DEBUG_PER_FRAME) {
                Debug.Log("SXRCamera::Update. Camera.enabled? " + gameObject.GetComponent<Camera>().enabled + ", leftCamera.enabled? " + leftCamera.enabled);
            }

            // set Sxr Cameras on every update - enforce it to override any app's change
            // TBD: it is not needed anymore
            setSxrCameras();

            if (!cameraPropertyInitialized) {
                // Validate camera property (i.e FOV)
                SetDefaultVRProperties(gameObject.GetComponent<Camera>());
                SetDefaultVRProperties(leftCamera);
                SetDefaultVRProperties(rightCamera);

                // app settings such as IPD will be available at this point with native initialization
                float ipd = sxrm.getFrameRegulator().getInterpupillaryDistance();
                Debug.Log("SXRCamera:: ipd=" + ipd);

                Vector3 pos = leftCamera.transform.localPosition;
                leftCamera.transform.localPosition = new Vector3(ipd / 2 * -1, pos.y, pos.z);
                pos = rightCamera.transform.localPosition;
                rightCamera.transform.localPosition = new Vector3(ipd / 2, pos.y, pos.z);

                cameraPropertyInitialized = true;
            }
#if UNITY_GLES_RENDERER

            if (fr.isResumeInProgress) {// query only if this is in progress for overhead
                bool resumed = SXRConfigurationIF.QueryPluginResumed();          
                if (DEBUG_PER_FRAME) {
                    Debug.Log("resumed = " + resumed);
                }
                if (resumed) {
                    fr.isResumeInProgress = false;
                    // fall through
                } else {
                    frameRendered = false;
                    return;
                }
            }

            if (useFixedUpdate == false) {
                if (renderEnabled) {
                    // begin the frame
                    SXRFrameRegulator.Instance.beginFrame(); // for debug
                    SensorManager sensor = sxrm.getSensorManager();

                    // get the next buffer idx; no need to be earlier in lifecycle than this and just before the first use should be enough.
                    if (SXRFrameRegulator.DELAYED_EYEBUFFER_GET) {
                        if (frameRendered)
                            fr.incBufferIdx();
                    }

                    int bufIdx = fr.getBufferIdx();

                    if (!fr.isReady()) {
                        Debug.Log("FR not ready, skip..");
                        return;
                    }

                    if (SXRFrameRegulator.USE_PREDICTION) {
                        // Predict and save
                        SensorManager.SxrPose pose = fr.getDrawPose(); // predicted rot for the head, and position data

                        UnityEngine.Quaternion rot = new UnityEngine.Quaternion();
                        rot.w = pose.w;
                        rot.x = pose.x;
                        rot.y = pose.y;
                        rot.z = pose.z;

                        UnityEngine.Vector3 pos = new UnityEngine.Vector3();
                        if (objectSXRCameraRig != null) {
                            objectSXRCameraRig.transform.localRotation = convertSensorToCamera(rot);
                            if (pose.p_x != 0.0f && pose.p_y != 0.0f && pose.p_z != 0.0f) {
                                //If x y z all 0, do not set from native. Let it use the constant values in the scene or c#

                                //local pos
                                UnityEngine.Vector3 localPos = convertSensorToCameraPos(pose.p_x, pose.p_y, pose.p_z);

                                objectSXRCameraRig.transform.localPosition = localPos;
                            }
                        }
        
                        if (DEBUG_PER_FRAME) {
                            Debug.Log(string.Format("getDrawPose rot {0} {1} {2} {3}", rot.w, rot.x, rot.y, rot.z));
                            Debug.Log(string.Format("getDrawPose pos {0} {1} {2}", pose.p_x, pose.p_y, pose.p_z));
                            Debug.Log(string.Format("new pos {0} {1} {2}", pos.x, pos.y, pos.z));
                            //fr.queryBufferCountStatus(fr.getBufferCount() - 1); // of prev frame
                        }
                    }

                    leftCamera.targetTexture = leftTexture[bufIdx];
                    rightCamera.targetTexture = rightTexture[bufIdx];
               

                    for (int i = 0; i < SXRFrameRegulator.BUFFER_NUM; i++) {
                        if (texIdLeft[i] == 0) {
                            texIdLeft[i] = leftTexture[i].GetNativeTextureID();
                        }
                        if (texIdRight[i] == 0) {
                            texIdRight[i] = rightTexture[i].GetNativeTextureID();
                        }

                        if (DEBUG_PER_FRAME) {
                            Debug.Log("PureSetLeftTextureFromPlugin i=" + i + ", GetNativeTextureID()=" + texIdLeft[i]);
                            Debug.Log("PureSetRightTextureFromPlugin i=" + i + ", GetNativeTextureID()=" + texIdRight[i]);
                        }

                        PureSetLeftTextureFromPlugin(i, texIdLeft[i], leftTexture[i].width, leftTexture[i].height);
                        PureSetRightTextureFromPlugin(i, texIdRight[i], rightTexture[i].width, rightTexture[i].height);
                    }

                    if (SXRFrameRegulator.TEST_DRAW_TO_SHOW_SYNC) {
                        if (sDrawCount++ % 10 != 0) {
                            SXRFrameRegulator.Instance.endFrame(); // for debug
                            return;
                        }
                        PureDebugFunc(1 /* glFlush */);
                        System.Threading.Thread.Sleep(500);
                    }

                    fr.saveTransform(objectSXRCameraRig.transform); // use post predicted pos
                    frameRendered = true;
                }
            }
#endif
        }

        /// <summary>
        /// Initialize SXR camera as single use (i.e. use SXRCamera.prefab).
        /// </summary>
        public void Initialize() {
            Camera eyeL = null, eyeR = null;

            Camera cam = GetComponent<Camera>();
            if (cam != null) {
                mainCamera = cam;

                mainCameraRig = cam.gameObject.GetChild("SXRCameraRig");
                if (mainCameraRig == null) {
                    // Create new camera rig with stereo sxr camera
                    mainCameraRig = CreateCameraRig(cam);
                }

                if (!leftCamera || !rightCamera) {
                    // Invalidate stereo cameras
                    GameObject go = mainCameraRig.GetChild("SXRLeftCamera");
                    if (go == null) {
                        eyeL = CreateCamera(cam, mainCameraRig, new SXREyeParameters("Left"));
                    } else {
                        eyeL = leftCamera != null ? leftCamera : go.GetComponent<Camera>();
                    }
                    SetLeftCamera(eyeL);
                    SetLeftTexture(eyeL.targetTexture);

                    go = mainCameraRig.GetChild("SXRRightCamera");
                    if (go == null) {
                        eyeR = CreateCamera(cam, mainCameraRig, new SXREyeParameters("Right"));
                    } else {
                        eyeR = rightCamera != null ? rightCamera : go.GetComponent<Camera>();
                    }
                    SetRightCamera(eyeR);
                    SetRightTexture(eyeR.targetTexture);
                }

                // Disable main camera for performance
                mainCamera.enabled = false;

                // set initial SXRCamera settings
                if (settingsList == null) {
                    UpdateCameraSettings(SXRCameraSystem.Instance.GetCameraSettingFlag());
                }

                SetSXRCameraStatus(true);
            }
        }

        /// <summary>
        /// Initialize SXR camera from SXRCameraSystem (which supports multiple camera).
        /// </summary>
        public void Initialize(Camera cam, List<SXRCameraSystem.SXRCameraSettingFlag> settings) {
            if (GetComponent<Camera>() != null) {
                if (mainCamera != cam) {
                    mainCamera = cam;
                }

                mainCameraRig = cam.gameObject.GetChild("SXRCameraRig");
                if (mainCameraRig == null) {
                    // Create new camera rig with stereo sxr camera
                    mainCameraRig = CreateCameraRig(cam);
                }

                // Disable main camera for performance
                mainCamera.enabled = false;

                if (renderEnabled) {
                    SetSXRCameraStatus(false);
                }

                // set initial SXRCamera settings
                if (settingsList == null) {
                    UpdateCameraSettings(settings);
                }
            }
        }

        /// <summary>
        /// Set SXR camera status.
        /// </summary>
        public void SetSXRCameraStatus(bool enable) {
            float cameraOffset = 0;

            // Only apply with status transition
            if (IsCameraActive == enable && renderEnabled == enable) {
                Debug.Log("SXRCamera:SetSXRCameraStatus - returned " + enable + ", " + gameObject.FullPath());
                return;
            }

            renderEnabled = enable;

            // Note. it probably affect performance w/ enabled root camera together (e.g. 3-cameras are working together, Root, L, R)
            // but root camera status should be set as it is for applicaiton
            //mainCamera.enabled = enable;

            // Retrieve SXRManager instance even if SXRCamera is not awaked yet
            if (sxrm == null) {
                sxrm = SXRManager.Instance;
            }

            // Update SXRCameraRig's transform
            if (mainCameraRig != null) {
                SetCameraRigTransform(mainCameraRig, mainCamera);
            }

            if (sxrm != null) {
                fr = sxrm.getFrameRegulator();
            }

            if (leftCamera != null) {
                leftCamera.enabled = enable;
            }

            if (rightCamera != null) {
                rightCamera.enabled = enable;
            }

            // Rendering coroutine sync with SXRCamera life cycle
            if (enable) {
                if (coroutine != null && !isInCoroutine) {
                    StartCoroutine(coroutine);
                    isInCoroutine = true;
                }
                // Apply main camera's orientation to be based as native timewarp calculatoin when camera group is transitioned
                leftCamera.transform.rotation = rightCamera.transform.rotation = mainCamera.transform.rotation;
            } else {
                if (coroutine != null) {
                    StopCoroutine(coroutine);
                    coroutine = null;
                    isInCoroutine = false;
                }
            }
        }

        /// <summary>
        /// Update SXR camera settings.
        /// Normally SXRCamera setting will be inherited from overall SXRCameraSystems settings,
        /// but specific parameters for this SXRCamera can be kept if it is required.
        /// </summary>
        private void UpdateCameraSettings(List<SXRCameraSystem.SXRCameraSettingFlag> settings) {
            if (settings != null && settings.Count > 0) {
                settingsList = settings;

                // Use global settings from SXRCameraSystem if it is required
                if (useGlobalSetting) {
                    foreach (SXRCameraSystem.SXRCameraSettingFlag flag in settings) {
                        if (flag.Name == "UseFixedUpdate") {
                            useFixedUpdate = (bool)flag.Value;
                        }

                        if (flag.Name == "UseChromaticAberration") {
                            useChromaticAberration = (bool)flag.Value;
                        }

                        if (flag.Name == "UseAntialiasing") {
                            useAntialiasing = (bool)flag.Value;
                        }

                        if (flag.Name == "AntialiasingLevel") {
                            antialiasingLevel = (SXRCameraSystem.UnityAntialiasingLevel)flag.Value;
                        }

                        if (flag.Name == "UseMultipleCamera") {
                            useMultipleCamera = (bool)flag.Value;
                        }

                        if (flag.Name == "ARCameraMode") {
                            ARCameraMode = (bool)flag.Value;
                        }

                        if (flag.Name == "ARBackgroundVideo") {
                            ARBackgroundVideo = (bool)flag.Value;
                        }
                    }
                }
            } else {
                //Debug.Log ("Sync as late settings with SXRCameraSystem");
                lateUpdateSettings = true;
            }
        }

        /// <summary>
        /// Set right texture.
        /// </summary>
        public void SetRightTexture(RenderTexture tex) {
            initTextureArray();
            rightTexture[0] = tex;
        }

        /// <summary>
        /// Set left texture.
        /// </summary>
        public void SetLeftTexture(RenderTexture tex) {
            initTextureArray();
            leftTexture[0] = tex;
        }

        /// <summary>
        /// Set right eye camera.
        /// </summary>
        public void SetRightCamera(Camera cam) {
            rightCamera = cam;
        }

        /// <summary>
        /// Set left eye camera.
        /// </summary>
        public void SetLeftCamera(Camera cam) {
            leftCamera = cam;
        }

        /// <summary>
        /// Set default VR properties.
        /// </summary>
        public void SetDefaultVRProperties(Camera cam) {
            // In case of registering inactive cameras, Awake() is not called.
            if (sxrm == null) {
                sxrm = SXRManager.Instance;
            }

            if (!SXRCameraSystem.Instance.ARCameraMode) {
                SetDefaultVRPropertiesForVR(cam, fr);
            } else {
                SetDefaultVRPropertiesForAR(cam, fr);
            }
        }

        private void SetDefaultVRPropertiesForVR(Camera cam, SXRFrameRegulator fr) {
            //fov
            cam.fieldOfView = fr.getFovY();
            //rect
            cam.rect = new Rect(0, 0, 1, 1);
            //Aspect Ratio - in Unity, by default the aspect ratio is automatically calculated from the screen's aspect ratio.
            //Must use the Aspect Ratio set when the eye texture is created
            cam.aspect = cameraAspect;

            PrintDefaultVRProperties("SXRCamera:SetDefaultVRPropertiesForVR", cam, fr);
        }

        private void SetDefaultVRPropertiesForAR(Camera cam, SXRFrameRegulator fr) {
            //fov
            //cam.fieldOfView = 93.0f;// 55.0f; // Enlarged for testing. Fov can be quite different for AR glasses, Mira, etc. 
            cam.fieldOfView = fr.getFovY();
            //rect
            cam.rect = new Rect(0, 0, 1, 1);
            //Aspect Ratio - in Unity, by default the aspect ratio is automatically calculated from the screen's aspect ratio.
            //Must use the Aspect Ratio set when the eye texture is created
            cam.aspect = cameraAspect;

            PrintDefaultVRProperties("SXRCamera:SetDefaultVRPropertiesForAR", cam, fr);
        }

        private void PrintDefaultVRProperties(String tag, Camera cam, SXRFrameRegulator fr) {
            if (true/*DEBUG_PER_FRAME*/) {
                Debug.Log(tag + " - cam.fieldOfView  " + cam.fieldOfView);
                Debug.Log(tag + " - cam.aspec " + cam.aspect); // by front eye texture
                Debug.Log(tag + " - fr.getAspectRatio() " + fr.getAspectRatio()); // native, set by the display resolution
            }
        }

        /// <summary>
        /// Get SXRCameraRig transform.
        /// </summary>
        public Transform GetRoot() {
            return transform.Find("SXRCameraRig");
        }

        /// <summary>
        /// Destroy game object.
        /// </summary>
        void OnDestroy() {
            Debug.Log("SXRCamera::OnDestroy");
        }

        /// <summary>
        /// Set SXR camerarig property from the root camera.
        /// </summary>
        public void SetCameraRigTransform(GameObject cameraRig, Camera cam) {
            float headModelDepth = 0.0805F;
            float headModelHeight = 0.075F;

            if (cameraRig != null && cam != null) {
                // Use pre-defined values which depends on framework settings of device
                if (sxrm != null) {
                    headModelDepth = sxrm.getFrameRegulator().getHeadModelDepth();
                    headModelHeight = sxrm.getFrameRegulator().getHeadModelHeight();
                }

                cameraRig.transform.parent = cam.gameObject.transform;
                cameraRig.transform.localPosition = new Vector3(0, -headModelHeight, headModelDepth);
            }
        }

        /// <summary>
        /// Create SXR camera for specific eye.
        /// </summary>
        private Camera CreateCamera(Camera camera, GameObject camRig, SXREyeParameters parameters) {
            float ipdOffset = 0.0f;

            // Create new SXRCamera for each eye
            GameObject go = new GameObject("SXR" + parameters.name + "Camera");

            // Add Camera component
            Camera sxrCam = go.AddComponent<Camera>();

            // Inherit camera pameters from root camera
            sxrCam.CopyFrom(camera);

            sxrCam.targetTexture = (RenderTexture)Resources.Load("SXRCameraRenderTexture" + parameters.name);

            // Set stereo offset for each eye
            if (parameters.name == "Left") {
                ipdOffset = parameters.camerasOffset / 2 * -1;
                go.AddComponent<SXRLeftEye>();
                go.AddComponent<SXRCameraRig>();
            } else {
                ipdOffset = parameters.camerasOffset / 2;
                go.AddComponent<SXRRightEye>();
                go.AddComponent<SXRCameraRig>();
            }

            // Set intial parameters
            sxrCam.transform.parent = camRig.transform;
            sxrCam.transform.localPosition = new Vector3(ipdOffset, 0, 0);
            sxrCam.enabled = false;

            // Set default VR properties
            // Note. if SXRPlugin is not ready at this moment then it will be verified at SXRCamera::Start()
            SetDefaultVRProperties(sxrCam);

            return sxrCam;
        }

        /// <summary>
        /// Create SXRCameraRig.
        /// </summary>
        public GameObject CreateCameraRig(Camera cam) {
            // Create SXRCameraRig
            GameObject cameraRig = new GameObject("SXRCameraRig");
            cameraRig.gameObject.AddComponent<SXRCameraRig>();
            //Camera to be used exclusivelly for UI events, rendering will be disabled
            cameraRig.gameObject.AddComponent<Camera>().enabled = false;

            // Align SXRCameraRig properties
            SetCameraRigTransform(cameraRig, cam);

            // Create SXR stere cameras
            CreateCameras(cam, cameraRig);

            return cameraRig;
        }

        /// <summary>
        /// Create SXR stereo caemra.
        /// </summary>
        public void CreateCameras(Camera cam, GameObject camRig) {
            Camera eyeL = null, eyeR = null;
            GameObject go = null;
            SXRCamera sxrScript = null;

            if (DEBUG_PER_FRAME) {
                Debug.Log("CreateCameras for rig " + camRig.name + " attached to " + cam.name);
            }

            sxrScript = cam.gameObject.GetComponent<SXRCamera>();
            if (!sxrScript) {
                sxrScript = cam.gameObject.AddComponent<SXRCamera>();
            }

            // Setup stereo cameras
            go = camRig.GetChild("SXRLeftCamera");
            if (go == null) {
                eyeL = CreateCamera(cam, camRig, new SXREyeParameters("Left"));
            } else {
                eyeL = leftCamera != null ? leftCamera : go.GetComponent<Camera>();
            }
            sxrScript.SetLeftCamera(eyeL);
            sxrScript.SetLeftTexture(eyeL.targetTexture);

            go = camRig.GetChild("SXRRightCamera");
            if (go == null) {
                eyeR = CreateCamera(cam, camRig, new SXREyeParameters("Right"));
            } else {
                eyeR = rightCamera != null ? rightCamera : go.GetComponent<Camera>();
            }
            sxrScript.SetRightCamera(eyeR);
            sxrScript.SetRightTexture(eyeR.targetTexture);

            sxrScript.useAntialiasing = useGlobalSetting ? SXRCameraSystem.Instance.useAntialiasing : useAntialiasing;
            sxrScript.antialiasingLevel = useGlobalSetting ? SXRCameraSystem.Instance.antialiasingLevel : antialiasingLevel;
            sxrScript.useMultipleCamera = useGlobalSetting ? SXRCameraSystem.Instance.useMultipleCamera : useMultipleCamera;
        }

        /// <summary>
        /// Transform SXR orientation to Unity form.
        /// </summary>
        public static UnityEngine.Quaternion convertSensorToCamera(UnityEngine.Quaternion from) {
            if (DEBUG_PER_FRAME) {
                Debug.Log("SXRCamera::convertSensorToCamera");
            }
            return SensorManager.toCamera(from);
        }

        /// <summary>
        /// Transform SXR position to Unity form.
        /// </summary>
        public static UnityEngine.Vector3 convertSensorToCameraPos(float x, float y, float z) {
            if (DEBUG_PER_FRAME) {
                Debug.Log("SXRCamera::convertSensorToCameraPos");
            }
            return SensorManager.toCameraPos(x, y, z);
        }

        // For debug
        public static SXRCamera getDebugInstance() {
            return sSXRCamera;
        }

        public void debugClearBuffer(int bufferIdx) {
            RenderTexture current = RenderTexture.active;

            RenderTexture.active = leftTexture[bufferIdx];
            GL.Clear(true, true, Color.red);

            RenderTexture.active = rightTexture[bufferIdx];
            GL.Clear(true, true, Color.red);

            RenderTexture.active = current;
        }
        #endregion methods
    }
}
