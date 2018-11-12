using UnityEngine;
using System;
using System.Collections;
using System.Collections.Generic;
using System.Runtime.InteropServices;

namespace Sxr {

    public class SXRExternalCamera : SXRMonoBehaviour<SXRExternalCamera> {
        #region inspector members
        [SerializeField]
        [Tooltip("Use External camera in the scene. (experiment)")]
        public bool useExternalCamera = true;

        [SerializeField]
        [Tooltip("Use ARuco in the scene. (experiment)")]
        public bool useARuco = false;

        [SerializeField]
        [Tooltip("Choose ARuco texture format in the scene. (experiment)")]
        public ARUCO_TextureFormat textureFormat = ARUCO_TextureFormat.RGBA32;

        [SerializeField]
        [Tooltip("Choose preview width in the scene. (experiment)")]
        public int previewWidth = 640;

        [SerializeField]
        [Tooltip("Choose preview height in the scene. (experiment)")]
        public int previewHeight = 480;

        [SerializeField]
        [Tooltip("Angle of external camera from eye (experiment)")]
        public int cameraAngleDiff = -25;
        #endregion inspector members

        Texture2D extCameraTexture;
        Texture2D extCameraNativeTexture;
        private int nativeTextureID;
        private const bool DEBUG_EXT = false;

        // Supported TextureFormat for ARuco now
        public enum ARUCO_TextureFormat {
            RGB24 = TextureFormat.RGB24,
            RGBA32 = TextureFormat.RGBA32,
            ARGB32 = TextureFormat.ARGB32,
            UNKNOWN
        }

        public enum STATUS {
            IDLE = 0,
            INIT,
            READY,
            RUNNING,
            PROCESSING,
            STOP
        };
        private STATUS extCameraStatus;
        private STATUS prevExtCameraStatus;
        private static SXRExternalCamera instance;

        public Texture2D TargetTexture { get { return this.extCameraTexture; } }
        public Texture2D TargetNativeTexture { get { return this.extCameraNativeTexture; } }
        public delegate void previewCallbackDelegate(IntPtr buffer, long size);
        public STATUS CurrentStatus { get { return this.extCameraStatus; } }

#if UNITY_ANDROID && !UNITY_EDITOR
    [DllImport ("sxr-plugin")]
    private static extern IntPtr PureGetRenderEventFunc();

    [DllImport ("sxr-plugin")]
    private static extern void PureExtCameraPreviewCallback(previewCallbackDelegate fp);

    SXRFrameRegulator fr;
#endif

        // manage preview callback buffer
        private byte[] frameBufData;
        private long frameBufSize;

        // ARuco 
        private STATUS arucoStatus;
        private byte[] frameBuffer;
        private IntPtr framePtr;
        private long frameSize;

        /// <summary>
        /// Awake monobehaviour.
        /// </summary>
        void Awake() {
            if (DEBUG_EXT) {
                Debug.Log("SXRExternalCamera::Awake() useExternalCamera = " + useExternalCamera);
            }
        }

        /// <summary>
        /// Enable monobehaviour.
        /// </summary>
        void OnEnable() {
        }

        /// <summary>
        /// Disabled monobehaviour.
        /// </summary>
        void OnDisable() {
            if (useExternalCamera) {
#if UNITY_ANDROID && !UNITY_EDITOR
            if (fr.isReady() && (extCameraStatus == STATUS.RUNNING)) {
                // Stop external camera
                fr.stopExtCamera ();
                extCameraStatus = arucoStatus = STATUS.STOP;
            }
#endif
            }
        }

        /// <summary>
        /// Destroy monobehaviour.
        /// </summary>
        void OnDestroy() {
            extCameraStatus = arucoStatus = STATUS.IDLE;
        }

        /// <summary>
        /// Start monobehaviour.
        /// </summary>
        void Start() {
            if (DEBUG_EXT) {
                Debug.Log("SXRExternalCamera::Start() useExternalCamera=" + useExternalCamera);
            }

            if (useExternalCamera) {
#if UNITY_ANDROID && !UNITY_EDITOR
            fr = SXRManager.Instance.getFrameRegulator();
            if (useExternalCamera) {
                if (fr.isReady()) {
                    // Set external camera mode
                    SXRConfiguration.Instance.SetExternalCameraMode(useExternalCamera);
                    // OpenGL texture generation should be processed in the render thread
                    GL.IssuePluginEvent(PureGetRenderEventFunc(), (int) SXRCameraSystem.RenderEventID.RenderInitializedExtCamera);
                    // Update status
                    extCameraStatus = STATUS.INIT;
                }
            }
#endif
            }
        }

        /// <summary>
        /// Update monobehaviour.
        /// </summary>
        void Update() {
#if UNITY_ANDROID && !UNITY_EDITOR
        if (fr.isReady() && (extCameraStatus == STATUS.INIT || extCameraStatus == STATUS.READY)) {
            // Validate native SurfaceTexture ID
            nativeTextureID = fr.getExtCameraNativeTextureID();
            if (nativeTextureID > 0) {
                if (extCameraTexture == null) {
                    // Create base Texture2D texture
                    extCameraTexture = new Texture2D(previewWidth, previewHeight,
                        (useARuco ? (TextureFormat)textureFormat : TextureFormat.ARGB32) , false);

                    // Create native external texture from sxr plugin
                    extCameraNativeTexture = Texture2D.CreateExternalTexture(previewWidth, previewHeight,
                                (useARuco ? (TextureFormat)textureFormat : TextureFormat.ARGB32), false, true, new IntPtr(nativeTextureID));

                    // Link native texture to base Texture2D
                    extCameraTexture.UpdateExternalTexture (extCameraNativeTexture.GetNativeTexturePtr ());

                    // Use native external camera texture as quad's main texture
                    gameObject.GetComponent<Renderer> ().material.mainTexture = extCameraTexture;

                    // Create preview buffer with 4 channels format (i.e. RGBA32)
                    frameBufData = new byte[extCameraTexture.height * extCameraTexture.width * 4]; // 1228800 of RGBA, 921600 of RGB24

                    // Register preview callback function, this callback will be held from other thread.
                    PureExtCameraPreviewCallback(new previewCallbackDelegate(this.previewCallback));

                    // Update status
                    extCameraStatus = STATUS.READY;
                    if (DEBUG_EXT) {
                        Debug.Log("SXRExternalCamera Update(), extCamNativeTexture SufaceTextureID="  + extCameraNativeTexture.GetNativeTexturePtr ());
                    }
                }

                // Start if SurfaceTexture is ready
                if (fr.startExtCamera ()) {
                    extCameraStatus = STATUS.RUNNING;
                }
                return;
            }
        }

        if (extCameraStatus == STATUS.RUNNING) {
            // OpenGL texture update should be processed in the render thread
            GL.IssuePluginEvent(PureGetRenderEventFunc(), (int) SXRCameraSystem.RenderEventID.RenderUpdatedExtCamera);
        }
#endif
        }

        public byte[] GetPreviewBuffer() {
            return frameBufData;
        }

        public void CopyPreviewBuffer(IntPtr bufferPtr, int size) {
            Marshal.Copy(bufferPtr, frameBufData, 0, (int)size);
            frameBufSize = size;
        }

        public long GetPreviewBufferSize() {
            return frameBufSize;
        }

        private void previewCallback(IntPtr bufferPtr, long size) {
            if (SXRExternalCamera.Instance.CurrentStatus == STATUS.RUNNING) {
                if (DEBUG_EXT) {
                    Debug.Log("SXRExternalCamera:: previewCallback, bufferPtr= " + bufferPtr + ", Size= " + (int)size);
                }

                if (bufferPtr != null && size != 0) {
                    // Copy native buffer data to local buffer for post-processing
                    SXRExternalCamera.Instance.CopyPreviewBuffer(bufferPtr, (int)size);
                }
            }
        }

        public void StartCamera() {
#if UNITY_ANDROID && !UNITY_EDITOR
        if (fr.isReady() && (extCameraStatus == STATUS.INIT || extCameraStatus == STATUS.READY || extCameraStatus == STATUS.STOP)) {
            fr.startExtCamera ();
            extCameraStatus = STATUS.RUNNING;
        }
#endif
        }

        public void StopCamera() {
#if UNITY_ANDROID && !UNITY_EDITOR
         if (fr.isReady() && (extCameraStatus == STATUS.RUNNING)) {
             // Stop external camera
             fr.stopExtCamera ();
             extCameraStatus = STATUS.STOP;
         }
#endif
        }
    }
}
