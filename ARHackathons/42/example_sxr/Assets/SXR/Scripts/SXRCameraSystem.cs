using UnityEngine;
using UnityEngine.UI;
using System.Collections;
using System;
using System.Collections.Generic;
using System.Linq;
using UnityEngine.Events;
using System.Runtime.InteropServices;

namespace Sxr {

    /// <summary>
    /// A SXR camera system that is available in the game. It provides multiple(include multiple layered) camera feature.
    /// It can be used by preparing SXR menu from enditor (automatically registering SXRCamera hierarchy)
    /// Or attaching SXRCameraRig prefab directly onto unity Camera hierarchy in the scene.
    /// SXRCameraSystem supports statically instantiated Unity Camera from the scene now.
    /// </summary>
    public class SXRCameraSystem : SXRMonoBehaviour<SXRCameraSystem> {
        // Debugging purpose
        public static bool DEBUG_SXR_MULTICAMERA = false;

        // Enable local key handling
        public static bool ENABLE_LOCAL_KEY_DISPATCH = false;

        public enum SXRCameraSystemStatus {
            Active,
            Transitioning,
            Inactive
        }

        public enum UnityAntialiasingLevel {
            X2Sample = 2, X4Sample = 4, X8Sample = 8
        }

#if UNITY_ANDROID && !UNITY_EDITOR
    public enum RenderEventID
    {
        Rendered = 0,
        Pause = 100,
        Resume = 101,
        RenderInitialized = 102,
        EndRendering = 103,
        RenderInitializedExtCamera = 104,
		RenderUpdatedExtCamera = 105,
    }
#endif

        /// <summary>
        /// SXR camera settings.
        /// </summary>
        public class SXRCameraSettingFlag {
            private string name;
            private object value = false;
            public string Name { get { return this.name; } set { this.name = value; } }
            public object Value { get { return this.value; } set { this.value = value; } }
        }

        #region inspector members
        // Following global SXRCameraSetting will override QualitySettings (i.e. AntialiasingLevel)
        [SerializeField]
        [Tooltip("Use FixedUpdate for physics based camera tracking.")]
        public bool useFixedUpdate = false;

        [SerializeField]
        [Tooltip("Use ChromaticAberration.")]
        public bool useChromaticAberration = true;

        [SerializeField]
        [Tooltip("Use Antialiasing.")]
        public bool useAntialiasing = true;

        [SerializeField]
        [Tooltip("Use AntialiasingLevel..")]
        public UnityAntialiasingLevel antialiasingLevel = UnityAntialiasingLevel.X2Sample;

        [SerializeField]
        [Tooltip("Use Multiple camera.")]
        public bool useMultipleCamera = false;

        [SerializeField]
        [Tooltip("AR Camera Mode")]
        public bool ARCameraMode = false;

        [SerializeField]
        [Tooltip("AR Background Video")]
        public bool ARBackgroundVideo = false;

        #endregion inspector members

        private Camera defaultCamera = null;
        private Camera currentCamera = null;
        private Camera nextCamera = null;

        public Camera DefaultCamera { get { return defaultCamera; } }
        public Camera CurrentCamera { get { return currentCamera; } }
        public Camera NextCamera { get { return nextCamera; } }

        private SXRCameraGroup defaultCameraGroup;
        private SXRCameraGroup currentCameraGroup = null;
        private SXRCameraGroup nextCameraGroup = null;
        private SXRCameraGroup currentTransitionHostGroup = null;

        private List<SXRCameraGroup> cameraGroupList = new List<SXRCameraGroup>();
        private Queue<SXRCamera> cameraComponentToAdd = new Queue<SXRCamera>();
        private Queue<SXRCamera> cameraComponentToRemove = new Queue<SXRCamera>();
        private Dictionary<string, int> cameraGroupLookup = new Dictionary<string, int>();

        private SXRCameraSystemStatus cameraSystemStatus = SXRCameraSystemStatus.Inactive;
        public SXRCameraSystemStatus GetSXRSystemStatus { get { return cameraSystemStatus; } }

        // Use SXR material to set UI layer canvas(image and text) to front of other objects using single camera
        private Material sxrUIMaterial = null;

        // Global camera settings
        private List<SXRCameraSettingFlag> cameraSettingsList = new List<SXRCameraSettingFlag>();
        private Dictionary<string, object> cameraSettingsListLookup = new Dictionary<string, object>();

        // Locking camera with original scene view via Escape key
        public static bool FreezeCamera { get { return LockCamera; } set { LockCamera = value; } }
        private int currentCameraIndex = 0;
        private bool depressedLastFrame = false;
        private float timeBetweenTaps = 0.2f;

#if UNITY_ANDROID && !UNITY_EDITOR
    private RenderEventID cameraRenderStatus = RenderEventID.Pause;
    public RenderEventID CameraRenderStatus { get { return cameraRenderStatus; } }

    // SXR Pure interfaces
    private SXRManager sxrm;

    [DllImport ("sxr-plugin")]
    private static extern IntPtr PureGetRenderEventFunc();
#endif

        #region constructors
        protected override void Awake() {
            base.Awake();

            Debug.Log("SXRCameraSystem:Awake");

#if UNITY_ANDROID && !UNITY_EDITOR
        Debug.Log("SXRCameraSystem::Awake vSyncCount = 0;");

        // Don't want Unity's vsync as the triggering will be handled by SXR plugin
        QualitySettings.vSyncCount = 0; // VSync must be disabled for this to work (set vSyncCount to 0)
        // Override QualitySettings parameter from SXRCameraSystem's AA setting for SXR
        QualitySettings.antiAliasing = (int)antialiasingLevel;
        Application.targetFrameRate = 60; // can be set bigger, but it will be controlled SXR plugin

        // Prepare sxr instance - need to instantiate before events, like onResume
        sxrm = SXRManager.Instance;

        // Initialize global camera settings
        InitializeCameraSettings();

        // SXR plugin Version
        string sxrfVersion = sxrm.getFrameRegulator().getVersion();
        Debug.Log("SXR plugin version : " + sxrfVersion + "." + SXRPluginVersion.BUILD_ID+ " " + SXRPluginVersion.BUILD_TIMESTAMP);

        // SXRCameraSystem is taking care of active/inactive cameras only if multiple camera option is selected
        if (useMultipleCamera) {

            if (DEBUG_SXR_MULTICAMERA) {
                Debug.Log("SXRCameraSystem:Awake - Camera.allCamerasCount: " + Camera.allCamerasCount);
            }

            // First, register active cameras to container from the scene
            if (Camera.allCamerasCount > 0) {
                // Find MainCamera and register it first
                GameObject go = GameObject.FindGameObjectWithTag("MainCamera");
                if (go != null) {
                    Camera mainCam = go.GetComponent<Camera>();

                    if (mainCam != null &&
                            !IsCameraRegistered(mainCam.name) &&
                            mainCam.clearFlags != CameraClearFlags.Depth) {
                        defaultCamera = mainCam;
                    }
                }

                if (defaultCamera != null) {
                    currentCamera = defaultCamera;
                    UpdateCameraGroupList(defaultCamera, cameraGroupList.Count);
                }

                // Register other cameras except MainCamera
                foreach (Camera cam in Camera.allCameras) {
                    // Register cameras except SXR stero camera
                    if (cam.gameObject.name == "SXRLeftCamera" ||
                            cam.gameObject.name == "SXRRightCamera" ||
                            IsCameraRegistered(cam.name)) {
                        continue;
                    }

                    UpdateCameraGroupList(cam, cameraGroupList.Count);
                }
            }

            // Invalidate current active camera group components
            // Active Multi-layered camera should be registered ahead
            InvalidateCameraGroups();

            // Second, register rest of inactive cameras as well
            // Normally application de-activate game objects which contains camera component initially
            // But those camera group will be used at runtime
            Camera[] cameras = Resources.FindObjectsOfTypeAll(typeof(Camera)) as Camera[];
            foreach (Camera cam in cameras) {
                // Register cameras except SXR stero camera and already registered camera
                if (cam.gameObject.name == "SXRLeftCamera" ||
                        cam.gameObject.name == "SXRRightCamera" ||
                        cam.gameObject.name == "SXRCameraRig" ||
                        IsCameraRegistered(cam.name)) {
                    continue;
                }
                UpdateCameraGroupList(cam, cameraGroupList.Count);
            }

            // Finalize camera components from the statically and dynamically registered of the scene
            if (cameraGroupList.Count > 0) {
                // Invalidate current camera group components once more
                InvalidateCameraGroups();

                // Set current and default camera group
                defaultCameraGroup = currentCameraGroup = cameraGroupList[currentCameraIndex];
                if (defaultCamera == null) {
                    defaultCamera = currentCamera = defaultCameraGroup.GroupHeadCamera.MainCamera;
                }

                // Enable current camera group component
                currentCameraGroup.SetGroupEnable(true);

                // Invalidate UI components in the scene
                SetUIElementsToFront();
            }
        }
#endif
        }
        #endregion constructors

        #region methods
        /// <summary>
        /// Start SXRCameraSystem.
        /// </summary>
        protected override void Start() {
            // Update SXR camera system status
            if (currentCameraGroup != null) {
                cameraSystemStatus = SXRCameraSystemStatus.Active;
            }
        }

        /// <summary>
        /// Enable as the life cycle.
        /// </summary>
        void OnEnable() {
            Debug.Log("SXRCameraSystem:OnEnable - " + gameObject.FullPath());
        }

        /// <summary>
        /// Disable as the life cycle.
        /// </summary>
        void OnDisable() {
            Debug.Log("SXRCameraSystem::OnDisable - " + gameObject.FullPath());
        }

        /// <summary>
        /// Normal update.
        /// </summary>
        void Update() {
            if (useFixedUpdate == false) {
                if (DEBUG_SXR_MULTICAMERA) {
                    if (currentCamera != null) {
                        Debug.Log("SXRCameraSystem:Update - " + currentCamera.name + ", deltaTime: " + Time.deltaTime);
                    } else {
                        Debug.Log("SXRCameraSystem:Update, deltaTime: " + Time.deltaTime);
                    }
                }

                // Debugging purpose, basically application should handle key events by their own scenarios.
                if (ENABLE_LOCAL_KEY_DISPATCH) {
                    HandleKeys();
                }

                DoCameraSystemUpdate(Time.deltaTime);
            }
        }

        /// <summary>
        /// Fixed update for physics.
        /// </summary>
        void FixedUpdate() {
#if UNITY_ANDROID && !UNITY_EDITOR
        if (useFixedUpdate) {
            if (DEBUG_SXR_MULTICAMERA) {
                if (currentCamera != null) {
                    Debug.Log("SXRCameraSystem:FixedUpdate - " + currentCamera.name + ", deltaTime: " + Time.fixedDeltaTime);
                } else {
                    Debug.Log("SXRCameraSystem:FixedUpdate, deltaTime: " + Time.deltaTime);
                }
            }

            DoCameraSystemUpdate(Time.fixedDeltaTime);
        }
#endif
        }

        /// <summary>
        // Note. Requiring manage specific key input MonoBehaviour(e.g. SXRInput/SXRInputModule) to take care of all key events
        // this func. is only testing purpose for switching or freezing cameras
        /// </summary>
        private void HandleKeys() {
            if (SXRCameraSystem.Instance == null) {
                return;
            }

            // On Android, check for specific actions
            // tap or Mouse0 for switching camera
            // back(escape) for freezing camera view (Optional)
            if (Application.platform == RuntimePlatform.Android) {
                if (SXRInput.tapEvent || Input.GetKeyDown(KeyCode.Space) || Input.GetMouseButtonDown(0)) {
                    if (!depressedLastFrame && (Time.time > timeBetweenTaps)) {
                        // Debugging purpose
                        cameraSystemStatus = SXRCameraSystemStatus.Transitioning;
                    }
                    depressedLastFrame = true;
                } else if (Input.GetKey(KeyCode.Escape)) {
                    if (!depressedLastFrame && (Time.time > timeBetweenTaps)) {
                        // Toggle freezing camera
                        SetFreezeCamera(!GetFreezeCamera());
                    }
                    depressedLastFrame = true;
                } else {
                    depressedLastFrame = false;
                }
            }
        }

        /// <summary>
        /// Initialize SXR camera settings which are linked with SXR editor settings.
        /// </summary>
        private void InitializeCameraSettings() {
            // Create or update settings parameters
            SetCameraSettingFlag("UseFixedUpdate", useFixedUpdate);
            SetCameraSettingFlag("UseChromaticAberration", useChromaticAberration);
            SetCameraSettingFlag("UseAntialiasing", useAntialiasing);
            SetCameraSettingFlag("AntialiasingLevel", antialiasingLevel);
            SetCameraSettingFlag("UseMultipleCamera", useMultipleCamera);

            // AR related 
            SetCameraSettingFlag("ARCameraMode", ARCameraMode);
            SetCameraSettingFlag("ARBackgroundVideo", ARBackgroundVideo);
        }

        /// <summary>
        /// Update SXR camera system.
        /// </summary>
        private void DoCameraSystemUpdate(float deltaTime) {
            if (currentCameraGroup == null) {
                return;
            }

            // Debugging purpose
            if (ENABLE_LOCAL_KEY_DISPATCH) {
                if (cameraSystemStatus == SXRCameraSystemStatus.Transitioning) {
                    Debug.Log("SXRCameraSystem - in Transitioning");
                    // Note. if it is required for changing status of next camera component while in transitioning
                    // (for example) nextCameraComponent.Update();

                    if (currentCameraGroup != null) {
                        // Stop current rendering camera group while in transitioning
                        if (currentCameraGroup.IsEnabled) {
                            currentCameraGroup.SetGroupEnable(false);
                        }
                    }
                }
            }
        }

        /// <summary>
        /// Late update.
        /// </summary>
        void LateUpdate() {
            if (DEBUG_SXR_MULTICAMERA) {
                if (currentCamera != null) {
                    Debug.Log("SXRCameraSystem:LateUpdate - " + currentCamera.name + ", deltaTime: " + Time.deltaTime);
                } else {
                    Debug.Log("SXRCameraSystem:LateUpdate, deltaTime: " + Time.deltaTime);
                }
            }

            // Debugging purpose
            if (ENABLE_LOCAL_KEY_DISPATCH) {
                if (cameraSystemStatus == SXRCameraSystemStatus.Transitioning) {
                    int target_index = currentCameraIndex + 1;
                    if (target_index > cameraGroupList.Count - 1) {
                        target_index = 0;
                    }

                    SXRCameraGroup group = cameraGroupList[target_index];
                    if (group != null && group.IsGameObjectEnabled) {
                        currentCameraIndex = target_index;

                        // Start to switch camera
                        SwitchCameraGroup(group);

                        if (DEBUG_SXR_MULTICAMERA) {
                            Debug.Log("SXRCameraSystem:Switched on " + currentCamera.name + " -> " + nextCamera.name);
                        }

                        // Finally transition of camera
                        HandleCameraGroupTransition();
                    } else {
                        currentCameraGroup.SetGroupEnable(true);
                        cameraSystemStatus = SXRCameraSystemStatus.Active;
                    }
                }
            }
        }

        /// <summary>
        /// Notify active camera status.
        /// </summary>
        public void NotifyStatus(SXRCamera camera, bool status) {
            SXRCameraGroup group = null;

            if (DEBUG_SXR_MULTICAMERA) {
                Debug.Log("SXRCameraSystem::NotifyStatus- " + camera.MainCamera.name + ", Status-" + status);
            }

            // SXRCameraSystem is taking care of events with SXRCamera classes only if multiple camera option is selected
            if (useMultipleCamera) {
                if (camera.SXRCurrentGroup == null) {
                    // Try to find current group
                    group = GetCameraGroup(camera.name);
                    if (group == null) {
                        Debug.Log("SXRCameraSystem::NotifyStatus - No group assigned for " + camera.name);
                        // TBD: In Case of using SXRCameraPrefab, there was no chance to register SXRCamera yet
                        return;
                    } else {
                        camera.SXRCurrentGroup = group;
                    }
                } else {
                    group = camera.SXRCurrentGroup;
                }

                if (!group.IsGameObjectEnabled && status) {
                    Debug.Log("SXRCameraSystem:NotifyStatus - game object of " + camera.MainCamera.name + " is not enabled from the scene, returned!");
                    return;
                }

                if (status) {
                    // Check current camera group
                    if (currentCameraGroup != group) {
                        // Set current status to transitioning
                        cameraSystemStatus = SXRCameraSystemStatus.Transitioning;

                        // Switch new camera group
                        SwitchCameraGroup(group);

                        // Finally transition of camera
                        HandleCameraGroupTransition();
                    } else {
                        group.SetGroupEnable(status);
                    }
                } else {
                    // Passing by onDisable() of the specific SXRCamera
                    if (group.IsEnabled) {
                        group.SetGroupEnable(status);
                    }
                }
            }
        }

        /// <summary>
        /// Swtich SXR camera.
        /// </summary>
        public void SwitchSXRCamera(string name) {
            int value = 0;

            // Find registered camera group and trans
            if (cameraGroupLookup.TryGetValue(name, out value)) {
                // Set Transitioning status
                cameraSystemStatus = SXRCameraSystemStatus.Transitioning;

                // Find current camera group
                SXRCameraGroup group = cameraGroupList[value];
                if (group.IsGameObjectEnabled) {
                    // Start to switch camera
                    SwitchCameraGroup(group);
                } else {
                    Debug.Log("SXRCameraSystem:SwitchSXRCamera - game object of " + name + " is not enabled from the scene");
                    return;
                }

                if (DEBUG_SXR_MULTICAMERA) {
                    Debug.Log("SXRCameraSystem:Switched on " + currentCamera.name + " -> " + nextCamera.name);
                }

                // Finally transition of camera
                HandleCameraGroupTransition();
            } else {
                Debug.Log("There is no camera to switch for " + name);
            }
        }

        /// <summary>
        /// Switch to SXR camera group.
        /// </summary>
        public void SwitchCameraGroup(SXRCameraGroup group) {
            ActivateTransitionGroup(group, group);
        }

        /// <summary>
        /// Finalize SXR camera group transition.
        /// </summary>
        private void HandleCameraGroupTransition() {
            if (cameraSystemStatus == SXRCameraSystemStatus.Transitioning) {
                if (nextCamera == null || nextCameraGroup == null) {
                    return;
                }

                if (DEBUG_SXR_MULTICAMERA) {
                    Debug.Log("SXRCameraSystem::HandleCameraTransition - DISABLE current Camera Group...");
                }

                // Turn down current camera group
                // OnDisable() of previous SXRCamera will handle it
                //currentCameraGroup.SetGroupEnable(false);

                // Update new camera group component
                currentCameraGroup = nextCameraGroup;
                nextCameraGroup = null;

                if (nextCamera != currentCamera) {
                    if (DEBUG_SXR_MULTICAMERA) {
                        Debug.Log("SXRCameraSystem::HandleCameraTransition from (" + currentCamera.name + ") to (" + nextCamera.name + ")");
                    }

                    //currentCamera.enabled = false;
                    currentCamera = nextCamera;
                    //currentCamera.enabled = false;
                    nextCamera = null;
                }

                currentTransitionHostGroup = null;
                currentCameraGroup.SetGroupEnable(true);
                cameraSystemStatus = SXRCameraSystemStatus.Active;
            }
        }


        /// <summary>
        /// Activate new SXR camera group.
        /// </summary>
        private void ActivateTransitionGroup(SXRCameraGroup toGroup, SXRCameraGroup transitionHost) {
            if (toGroup == null || transitionHost == null) {
                Debug.Log("Invalid trnasition of camera group");
                return;
            }

            nextCameraGroup = toGroup;
            nextCamera = (nextCameraGroup.GroupHeadCamera.MainCamera != null) ? nextCameraGroup.GroupHeadCamera.MainCamera : defaultCamera;
            currentTransitionHostGroup = transitionHost;

            if (nextCamera != null) {
                if (currentTransitionHostGroup == nextCameraGroup) {
                    // Verify nextCamera and group
                    currentTransitionHostGroup.InvalidateCameraGroup(nextCamera, cameraSettingsList);
                }

                if (currentCamera == null) {
                    currentCamera = defaultCamera = nextCamera;
                }
            }
        }

        /// <summary>
        /// Register new sxr camera rig and stereo cameras hierarchy to the given root camera.
        /// </summary>
        public SXRCamera RegisterCamera(Camera rootCam, List<SXRCameraSettingFlag> settings) {
            SXRCamera cam = null;

            if (rootCam != null) {
                // Verify SXRScript
                cam = rootCam.GetComponent<SXRCamera>();
                if (cam == null) {
                    // Attach new SXRCamera script to Unity Camera GameObject
                    cam = rootCam.gameObject.AddComponent(typeof(SXRCamera)) as SXRCamera;
                }

                cam.Initialize(rootCam, settings);
            }

            return cam;
        }

        /// <summary>
        /// Unregister current sxr rig and stereo camera hierarchy from the given root camera.
        /// </summary>
        public bool UnRegisterCamera(SXRCamera sxrCamera) {
            if (sxrCamera != null) {
                if (sxrCamera.MainCamera != null) {
                    Component.Destroy(sxrCamera);
                    return true;
                }
            }

            return false;
        }

#if UNITY_ANDROID && !UNITY_EDITOR
    /// <summary>
    /// Handle pause/resume based upon activity life cycle.
    /// </summary>
    void OnApplicationPause(bool pauseStatus)
    {
        Debug.Log("OnApplicationPause(): " + pauseStatus);

        // This will be serialized in the event queue with the same thread.
        // Another approach can be using Player's Activity class, but it can
        // interrupt/intervene the on-going gl thread in the middle of drawing
        // in a sudden way. By queuing the Pause/Resume events, it would best have
        // chance of smoothly handling SXR’s internal states, including thread
        // shutdown.
        if (pauseStatus) {
            GL.IssuePluginEvent(PureGetRenderEventFunc(), (int)RenderEventID.Pause);
            if (sxrm != null) {
                sxrm.onPause();
            }
            cameraRenderStatus = RenderEventID.Pause;
        } else {
            // Clear the previous render buffer if it exists
            GL.Clear(true, true, Color.clear);

            if (sxrm != null) {
                sxrm.getFrameRegulator().isResumeInProgress = true;
                sxrm.onResume();
            }
            GL.IssuePluginEvent(PureGetRenderEventFunc(), (int)RenderEventID.Resume);
            cameraRenderStatus = RenderEventID.Resume;
        }
    }

    /// <summary>
    /// Handle focus based upon activity life cycle.
    /// </summary>
    void OnApplicationFocus(bool focusStatus)
    {
        Debug.Log("OnApplicationFocus(): " + focusStatus);
        if (focusStatus) {
            // Disable screen dimming
            Screen.sleepTimeout = SleepTimeout.NeverSleep;
        } else {
            // Set screen dimming as system default
            Screen.sleepTimeout = SleepTimeout.SystemSetting;
        }
    }
#endif

        /// <summary>
        /// Get SXR camera setting class from the given setting tag.
        /// </summary>
        public SXRCameraSettingFlag GetCameraSettingByName(string name) {
            foreach (SXRCameraSettingFlag flag in cameraSettingsList) {
                if (flag.Name == name) {
                    return flag;
                }
            }

            return null;
        }

        /// <summary>
        /// Get setting bool value from the setting tag.
        /// </summary>
        public bool GetCameraSettingFlagBool(string flagName) {
            return cameraSettingsListLookup.Get<bool>(flagName);
        }

        /// <summary>
        /// Get setting int value from the setting tag.
        /// </summary>
        public int GetCameraSettingFlagInt(string flagName) {
            return cameraSettingsListLookup.Get<int>(flagName);
        }

        /// <summary>
        /// Update SXR camera setting.
        /// </summary>
        public void SetCameraSettingFlag(string name, object value) {
            if (cameraSettingsList != null) {
                if (cameraSettingsListLookup.ContainsKey(name)) {
                    cameraSettingsListLookup[name] = value;
                } else {
                    cameraSettingsList.Add(new SXRCameraSettingFlag() { Name = name, Value = value });
                    cameraSettingsListLookup.Add(name, value);
                }
            }
        }

        /// <summary>
        /// Get current SXR camera settings.
        /// </summary>
        public List<SXRCameraSettingFlag> GetCameraSettingFlag() {
            return cameraSettingsList;
        }

        /// <summary>
        /// Get all SXR camera group.
        /// </summary>
        public List<SXRCameraGroup> GetAllCameraGroup() {
            if (cameraGroupList != null && cameraGroupList.Count > 0) {
                return cameraGroupList;
            }

            return null;
        }

        /// <summary>
        /// Get SXR camera group.
        /// </summary>
        public SXRCameraGroup GetCameraGroup(string name) {
            SXRCameraGroup group = null;

            int index = 0;

            if (string.IsNullOrEmpty(name)) { return null; }

            if (cameraGroupLookup.TryGetValue(name, out index)) {
                group = cameraGroupList[index];
            }

            return group;
        }

        /// <summary>
        /// Invalidate current SXR camera group.
        /// </summary>
        public void InvalidateCameraGroups() {
            SXRCameraGroup group = null;
            SXRCamera component = null;

            if (DEBUG_SXR_MULTICAMERA) {
                Debug.Log("SXRCameraSyste::InvalidateCameraGroups");
            }

            if (cameraComponentToAdd.Count > 0) {
                // Deque and register all of cameras
                do {
                    component = cameraComponentToAdd.Dequeue();
                    if (component != null) {
                        UpdateCameraGroupList(component.MainCamera, cameraGroupList.Count);
                    }
                } while (cameraComponentToAdd.Count != 0);
            }

            if (cameraComponentToRemove.Count > 0) {
                do {
                    // Deque and remove registered camera
                    component = cameraComponentToRemove.Dequeue();
                    if (component != null) {
                        group = component.SXRCurrentGroup;
                        if (group != null) {
                            cameraGroupLookup.Remove(component.MainCamera.name);
                            cameraGroupList.Remove(group);
                        }
                    }
                } while (cameraComponentToRemove.Count != 0);
            }
        }

        /// <summary>
        /// Add/Update SXR camera group.
        /// </summary>
        public void UpdateCameraGroupList(Camera cam, int index) {
            int value = 0;

            if (cam != null) {
                if (cameraGroupLookup.TryGetValue(cam.name, out value)) {
                    Debug.Log(cam.name + " is already registered.");
                    return;
                } else {
                    SXRCamera component = RegisterCamera(cam, cameraSettingsList);
                    if (component != null) {
                        cameraGroupList.Add(new SXRCameraGroup(component));
                        cameraGroupLookup.Add(cam.name, index);
                        if (DEBUG_SXR_MULTICAMERA) {
                            Debug.Log("UpdateCameraGroupList, Registered new SXRCamera(" + cam.name + "), InstanceID(" + cam.GetInstanceID() + ")");
                        }
                    } else {
                        Debug.Log(cam.name + " is not registered. unknown error!");
                    }
                }
            }
        }

        /// <summary>
        /// Add/Update SXR camera lookup dictionary.
        /// </summary>
        public void SetCameraComponentsLookup(string name, int index) {
            int value = 0;

            if (cameraGroupLookup.TryGetValue(name, out value)) {
                cameraGroupLookup[name] = index;
            } else {
                cameraGroupLookup.Add(name, index);
            }
        }

        /// <summary>
        /// Is give camera registered already? or not.
        /// </summary>
        public bool IsCameraRegistered(string name) {
            int value = 0;

            if (cameraGroupLookup.TryGetValue(name, out value)) {
                return true;
            }

            return false;
        }

        public bool IsCameraTransitioning() {
            return cameraSystemStatus == SXRCameraSystemStatus.Transitioning;
        }

        /// <summary>
        /// Is give camera registered already? or not.
        /// </summary>
        public bool IsCameraRegisteredInstance(string name, int instanceID) {
            int value = 0;

            if (cameraGroupLookup.TryGetValue(name, out value)) {
                SXRCameraGroup group = cameraGroupList[value];
                int iID = group.GroupHeadCamera.MainCamera.GetInstanceID();
                if (iID == instanceID) {
                    return true;
                }
            }

            return false;
        }

        /// <summary>
        /// Attach new SXR camera as dynamically (for example, using prefab).
        /// Input GameObject already contains camera objects inside with position information
        /// </summary>
        public bool RegisterSXRCamera(GameObject go) {
            if (go == null) {
                return false;
            }

            bool IsRegistered = IsCameraRegistered(go.name);
            if (IsRegistered) {
                if (DEBUG_SXR_MULTICAMERA) {
                    Debug.Log(go.name + " is already registered.");
                }
                return false;
            }

            Camera cam = go.GetComponent<Camera>();
            if (cam != null) {
                // Register new camera to container
                UpdateCameraGroupList(cam, cameraGroupList.Count);
            }

            return IsCameraRegistered(go.name);
        }

        /// <summary>
        /// Attach new SXR camera as dynamically.
        /// </summary>
        public bool RegisterSXRCamera(string name, string tag) {
            if (string.IsNullOrEmpty(name)) { return false; }

            bool IsRegistered = IsCameraRegistered(name);
            if (IsRegistered) {
                if (DEBUG_SXR_MULTICAMERA) {
                    Debug.Log(name + " is already registered.");
                }
                return false;
            } else {
                GameObject go = new GameObject(name);
                Camera cam = go.AddComponent<Camera>();
                cam.gameObject.transform.rotation = Quaternion.FromToRotation(new Vector3(0, 0, 0), new Vector3(0, 0, 1));
                cam.gameObject.transform.position = new Vector3(0, 0, 0);
                cam.gameObject.transform.localPosition = Vector3.zero;
                cam.gameObject.transform.localRotation = Quaternion.identity;
                cam.gameObject.transform.localScale = Vector3.one;

                go.AddComponent<AudioListener>();
                go.AddComponent<GUILayer>();
                go.AddComponent<FlareLayer>();

                // Initially disable audio listener
                AudioListener audioListener = go.GetComponent(typeof(AudioListener)) as AudioListener;
                if (audioListener != null) {
                    audioListener.enabled = false;
                }

                // Register new camera to container
                UpdateCameraGroupList(cam, cameraGroupList.Count);
            }

            return IsCameraRegistered(name);
        }

        /// <summary>
        /// Attach new SXR camera which is inherited from parent camera dynamically.
        /// </summary>
        public bool RegisterSXRCamera(string name, Camera parent, bool inherit) {
            Camera newCamera = null;

            if (string.IsNullOrEmpty(name)) { return false; }

            if (parent != null) {
                if (inherit) {
                    newCamera = (Camera)Camera.Instantiate(parent);
                } else {
                    newCamera = (Camera)Camera.Instantiate(
                                        parent,
                                        new Vector3(0, 0, 0),
                                        Quaternion.FromToRotation(new Vector3(0, 0, 0), new Vector3(0, 0, 1)));
                }

                if (newCamera != null) {
                    newCamera.name = name;

                    // Initially disable audio listener
                    AudioListener audioListener = newCamera.GetComponent(typeof(AudioListener)) as AudioListener;
                    if (audioListener != null) {
                        audioListener.enabled = false;
                    }

                    // Register new camera to container
                    UpdateCameraGroupList(newCamera, cameraGroupList.Count);
                    return true;
                }
            }

            return IsCameraRegistered(name);
        }

        /// <summary>
        /// Attach new SXR camera as dynamically.
        /// Input GameObject already contains camera objects inside with position information
        /// </summary>
        public bool UnregisterSXRCamera(GameObject go) {
            if (go == null) {
                return false;
            }

            bool IsRegistered = IsCameraRegistered(go.name);
            if (IsRegistered) {
                SXRCameraGroup group = GetCameraGroup(go.name);
                if (group != null) {
                    cameraGroupLookup.Remove(go.name);
                    cameraGroupList.Remove(group);
                }
            } else {
                return false;
            }

            return !IsCameraRegistered(go.name);
        }

        /// <summary>
        /// Detache/Remove SXR camera hierarchy from the given root camera.
        /// </summary>
        public bool UnregisterSXRCamera(string name) {
            if (string.IsNullOrEmpty(name)) { return false; }

            bool IsRegistered = IsCameraRegistered(name);
            if (IsRegistered) {
                SXRCameraGroup group = GetCameraGroup(name);
                if (group != null) {
                    cameraGroupLookup.Remove(name);
                    cameraGroupList.Remove(group);

                    // Re-indexing cameragroup lookup
                    cameraGroupLookup.Clear();
                    int index = 0;
                    foreach (SXRCameraGroup grp in cameraGroupList) {
                        cameraGroupLookup.Add(grp.GroupHeadCamera.MainCamera.name, index);
                        index++;
                    }
                }
            } else {
                return false;
            }

            return !IsCameraRegistered(name);
        }

        /// <summary>
        /// Set Freeze camera view.
        /// </summary>
        public void SetFreezeCamera(bool enable) {
            SXRCameraSystem.FreezeCamera = enable;
        }

        /// <summary>
        /// Get Freeze camera status.
        /// </summary>
        public bool GetFreezeCamera() {
            return SXRCameraSystem.FreezeCamera;
        }

        /// <summary>
        /// Set UI Canvas (which contains Image or Text object) to infront of any other camera object rendering.
        /// There is other solution with enabling another camera for UI layer, but it causes performance degradation in the mobile device.
        /// </summary>
        private void SetUIElementsToFront() {
            // Find valid material resource
            try {
                sxrUIMaterial = Resources.Load("Materials/UIOptionalZTest", typeof(Material)) as Material;

                if (sxrUIMaterial != null) {
                    // Find all UI canvases from the scene
                    Canvas[] canvases = FindObjectsOfType(typeof(Canvas)) as Canvas[];

                    foreach (var canvas in canvases) {
                        Image[] sprites = canvas.GetComponentsInChildren<Image>();
                        Text[] labels = canvas.GetComponentsInChildren<Text>();
                        if (sprites.Length > 0 || labels.Length > 0) {
                            foreach (var sprite in sprites) {
                                sprite.material = sxrUIMaterial;
                            }

                            foreach (var label in labels) {
                                label.material = sxrUIMaterial;
                            }
                        }
                    }
                } else {
                    Debug.Log("Couldn't find SXRUIElementZOrder material at Resources/Materials/SXRUIElementZOrder.mat");
                }
            } catch (Exception e) {
                Debug.Log(e.Message);
            }
        }
        #endregion methods
    }
}
