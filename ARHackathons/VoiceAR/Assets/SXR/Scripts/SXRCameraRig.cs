#if UNITY_ANDROID && !UNITY_EDITOR
#define UNITY_GLES_RENDERER
#endif

using UnityEngine;
using System;
using System.Collections;
using System.Collections.Generic;
using System.Runtime.InteropServices;

namespace Sxr {

    [DisallowMultipleComponent]

    public class SXRCameraRig : MonoBehaviour {
        SXRManager sxrm;
        SXRFrameRegulator fr;
        private Camera activeCamera;

        UnityEngine.Quaternion curRotation = new UnityEngine.Quaternion();

#if UNITY_ANDROID && !UNITY_EDITOR
    [DllImport ("sxr-plugin")]
    private static extern IntPtr PureGetRenderEventFunc();
#endif

        void Start() {
            // Initialize SXR Plugin
            Debug.Log("SXRCameraRig::Start()");
            sxrm = SXRManager.Instance;
            fr = sxrm.getFrameRegulator();

            // Retrieve current active camera which is mapped with this rig
            if (activeCamera == null) {
                activeCamera = gameObject.GetComponent<Camera>();
            }
        }

        void Update() {
#if UNITY_ANDROID && !UNITY_EDITOR
        if (activeCamera == null) {
            return;
        }

        if (activeCamera.enabled) {
            if (!SXRFrameRegulator.USE_PREDICTION) {
                SensorManager sensor = sxrm.getSensorManager();
                curRotation.w = -sensor.w;
                curRotation.x = sensor.x;
                curRotation.y = sensor.y;
                curRotation.z = -sensor.z;

                // Update current sensor data
                transform.localRotation = curRotation;
            }
        }
#endif
        }


        void OnPreRender() {
#if UNITY_ANDROID && !UNITY_EDITOR

        // currently do nothing

#endif
        }

        void OnPostRender() {
#if UNITY_ANDROID && !UNITY_EDITOR
        if (this.activeCamera == null) {
            return;
        }

        // Send post render event when the camera render is finished
        if (this.activeCamera.enabled) {
            // called at the end of eye rendering, currently no need of left/right or buffer idx
            GL.IssuePluginEvent(PureGetRenderEventFunc(), (int)SXRCameraSystem.RenderEventID.EndRendering);
        }
#endif
            if (SXRFrameRegulator.EARLY_FRAME_SUBMISSION && gameObject.name.Contains("SXRRightCamera")) {
                fr.SubmitFrame();
            }
        }
    }
}
