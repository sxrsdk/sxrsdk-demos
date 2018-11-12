using UnityEngine;
using System.Collections.Generic;
using System.Linq;

namespace Sxr {

    /// <summary>
    /// SXR camera group.
    /// </summary>
    public class SXRCameraGroup {
        private bool groupEnabled = false;
        private SXRCamera groupHeadCamera;

        public SXRCamera GroupHeadCamera { get { return groupHeadCamera; } }
        public bool IsEnabled { get { return groupEnabled; } }
        public bool IsGameObjectEnabled { get { return groupHeadCamera.gameObject.activeSelf; } }

        // Tracking chldren camera's status from the scene
        public Dictionary<string, bool> SXRGroupCameraStatusLookup = new Dictionary<string, bool>();

        /// <summary>
        /// Constructor.
        /// </summary>
        public SXRCameraGroup(SXRCamera root) {
            // Set group head camera
            groupHeadCamera = root;

            // Inform to group head camera which group it belongs to
            groupHeadCamera.SXRCurrentGroup = this;

            // Instantiate dictionary to keep the status of cameras in the camera group
            SXRGroupCameraStatusLookup.Add(groupHeadCamera.MainCamera.name, groupHeadCamera.MainCamera.enabled);
        }

        /// <summary>
        /// Invalidate current camera group component.
        /// </summary>
        public void InvalidateCameraGroup(Camera cam, List<SXRCameraSystem.SXRCameraSettingFlag> settings) {
            // Invalidate group cameras
            if (cam != null) {
                if (groupHeadCamera.MainCamera != cam) {
                    groupHeadCamera.MainCamera = cam;
                }
            }

            // Initialize SXRCamera
            groupHeadCamera.Initialize(cam, settings);
        }

        /// <summary>
        /// Enable or disable current camera group.
        /// </summary>
        public void SetGroupEnable(bool enable) {
            if (groupEnabled == enable) {
                Debug.Log(groupHeadCamera.MainCamera.name + " is already set as " + enable);
                return;
            }

            groupEnabled = enable;
            groupHeadCamera.SetSXRCameraStatus(enable);
        }

        public bool IsInActiveGroup(SXRCamera camera) {
            return SXRCameraSystem.Instance.CurrentCamera == camera.SXRCurrentGroup.GroupHeadCamera.MainCamera;
        }
    }
}
