using UnityEngine;
using UnityEngine.EventSystems;
using System;
using System.Xml;
using System.Collections;
using UnityEditor;

namespace Sxr {

    [CustomEditor(typeof(SXRCameraSystem))]
    [CanEditMultipleObjects]
    public class SXREditor : Editor {
        #region members
        private SerializedProperty useFixedUpdateField;
        private SerializedProperty useChromaticAberrationField;
        private SerializedProperty useAntialiasingField;
        private SerializedProperty antialiasingLevelField;
        private SerializedProperty useMultipleCameraField;
        private SerializedProperty ARCameraModeField;
        private SerializedProperty ARBackgroundVideoField;

        public static string titleMessage = "Camera already set",
                             bodyMessage = "Your camera already have SXRCamera script, " +
            "if you really want to configure this camera, remove this script",
                             confirmationMessage = "ok";

        //SXRCameraRig build parameters
        public struct SXREyeParameters {
            public float camerasOffset;
            public string name;

            public SXREyeParameters(string name) {
                // Use initial value as hard-coded
                // It will be updated with valid ones from frame regulator at the run-time
                camerasOffset = 0.062f;
                this.name = name;
            }

        };

        public static SXREyeParameters rightEyeParameters, leftEyeParameters;

        #endregion members

        #region methods
        public static void CreateCamera(GameObject obj, GameObject camRig, SXREyeParameters parameters) {
            float ipdOffset = 0.0f;
            GameObject newObj = new GameObject("SXR" + parameters.name + "Camera");
            Camera cam = newObj.AddComponent<Camera>();
            // we inherit the settings from main camera (not from camRig), including clearFlags and cullingMask.
            cam.CopyFrom(obj.GetComponent<Camera>());
            cam.transform.parent = camRig.transform;

            Debug.Log("SXREditor::CreateCamera cam.targetTexture  Resources.Load " + "SXRCameraRenderTexture" + parameters.name);
            cam.targetTexture = (RenderTexture)Resources.Load("SXRCameraRenderTexture" + parameters.name);

            if (parameters.name == "Left") {
                ipdOffset = parameters.camerasOffset / 2 * -1;
                newObj.AddComponent<SXRLeftEye>();
                newObj.AddComponent<SXRCameraRig>();
                obj.GetComponent<SXRCamera>().SetLeftTexture(cam.targetTexture);
                obj.GetComponent<SXRCamera>().SetLeftCamera(cam);
            } else {
                ipdOffset = parameters.camerasOffset / 2;
                newObj.AddComponent<SXRRightEye>();
                newObj.AddComponent<SXRCameraRig>();
                obj.GetComponent<SXRCamera>().SetRightTexture(cam.targetTexture);
                obj.GetComponent<SXRCamera>().SetRightCamera(cam);
            }
            cam.transform.localPosition = new Vector3(ipdOffset, 0, 0);
        }

        void OnEnable() {
            // Setup the SerializedProperties.
            useFixedUpdateField = serializedObject.FindProperty("useFixedUpdate");
            useChromaticAberrationField = serializedObject.FindProperty("useChromaticAberration");
            useAntialiasingField = serializedObject.FindProperty("useAntialiasing");
            antialiasingLevelField = serializedObject.FindProperty("antialiasingLevel");
            useMultipleCameraField = serializedObject.FindProperty("useMultipleCamera");
            ARCameraModeField = serializedObject.FindProperty("ARCameraMode");
            ARBackgroundVideoField = serializedObject.FindProperty("ARBackgroundVideo");
        }

        public override void OnInspectorGUI() {
            if (UpdateProperties() == false) {
                return;
            }

            DrawPropertyGUI();
        }

        private bool UpdateProperties() {
            try {
                useFixedUpdateField = serializedObject.FindProperty("useFixedUpdate");
                useChromaticAberrationField = serializedObject.FindProperty("useChromaticAberration");
                useAntialiasingField = serializedObject.FindProperty("useAntialiasing");
                antialiasingLevelField = serializedObject.FindProperty("antialiasingLevel");
                useMultipleCameraField = serializedObject.FindProperty("useMultipleCamera");
                ARCameraModeField = serializedObject.FindProperty("ARCameraMode");
                ARBackgroundVideoField = serializedObject.FindProperty("ARBackgroundVideo");
            } catch (Exception e) {
                Debug.Log(e.Message);
                return false;
            }

            return true;
        }

        public static void SetSXRScriptExecutionOrder(bool enable) {
            // Retrieve valid script
            MonoScript script = MonoScript.FromMonoBehaviour(SXRCameraSystem.Instance);
            if (script != null) {
                int order = MonoImporter.GetExecutionOrder(script);
                if (enable && order == 0) {
                    MonoImporter.SetExecutionOrder(script, -100);
                } else if (!enable && order != 0) {
                    MonoImporter.SetExecutionOrder(script, 0);
                }
            }
        }

        private void DrawPropertyGUI() {
            EditorGUI.BeginChangeCheck();
            {
                EditorGUILayout.PropertyField(useFixedUpdateField);
                EditorGUILayout.PropertyField(useChromaticAberrationField);
                EditorGUILayout.PropertyField(useAntialiasingField);
                if (useAntialiasingField.boolValue == true) {
                    EditorGUILayout.PropertyField(antialiasingLevelField);
                }
                EditorGUILayout.PropertyField(useMultipleCameraField);
                EditorGUILayout.PropertyField(ARCameraModeField);
                EditorGUILayout.PropertyField(ARBackgroundVideoField);
            }

            if (EditorGUI.EndChangeCheck()) {
                serializedObject.ApplyModifiedProperties();
            }
        }

        public static void PrepareCamera(Camera cam) {
            PlayerSettings.allowedAutorotateToPortraitUpsideDown = false;
            PlayerSettings.allowedAutorotateToPortrait = false;
            PlayerSettings.allowedAutorotateToLandscapeRight = false;
            InitializeParameters();

            // Find current active scene GameObject and attach SXR scripts in it
            // Note. In case of Multi Scene Editing, SXR scripts will work separately at each scene
            GameObject root = cam.gameObject.transform.root.gameObject;
            EventSystem eventSystem = root.GetComponentInChildren<EventSystem>();
            if (eventSystem) {
                if (!eventSystem.gameObject.GetComponent<SXRInput>()) {
                    eventSystem.gameObject.AddComponent<SXRInput>();
                }
                if (!eventSystem.gameObject.GetComponent<SXRInputModule>()) {
                    eventSystem.gameObject.AddComponent<SXRInputModule>();
                }
                if (!eventSystem.gameObject.GetComponent<SXRCameraSystem>()) {
                    eventSystem.gameObject.AddComponent<SXRCameraSystem>();
                }
            } else {
                GameObject obj = (GameObject)Instantiate(Resources.Load("SXREventSystem"));
                obj.name = obj.name.Replace("(Clone)", "");
                obj.transform.parent = root.transform;
            }

            CreateCameras(cam.gameObject, CreateCameraRig(cam.gameObject));
            PlayerSettings.defaultInterfaceOrientation = UIOrientation.LandscapeLeft;

            // We keep the main camera settings from application, including clearFlags and cullingMask
        }

        public static void InitializeParameters() {

            rightEyeParameters = new SXREyeParameters("Right");
            leftEyeParameters = new SXREyeParameters("Left");
        }

        public static void CreateCameras(GameObject cam, GameObject camRig) {

            if (!cam.gameObject.GetComponent<SXRCamera>()) {
                cam.gameObject.AddComponent<SXRCamera>();
            }

            if (!cam.gameObject.transform.Find("SXRLeftCamera")) {
                CreateCamera(cam.gameObject, camRig, leftEyeParameters);
            } else {

                cam.gameObject.GetComponent<SXRCamera>().SetLeftTexture(cam.gameObject.transform.Find("SXRLeftCamera").GetComponent<Camera>().targetTexture);
            }
            if (!cam.gameObject.transform.Find("SXRRightCamera")) {
                CreateCamera(cam.gameObject, camRig, rightEyeParameters);


            } else {

                cam.gameObject.GetComponent<SXRCamera>().SetRightTexture(cam.gameObject.transform.Find("SXRRightCamera").GetComponent<Camera>().targetTexture);
            }

            cam.gameObject.GetComponent<SXRCamera>().useAntialiasing = SXRWindow.unityAntialiasing;
            cam.gameObject.GetComponent<SXRCamera>().antialiasingLevel = SXRWindow.antialiasingLevel;
        }

        static GameObject CreateCameraRig(GameObject obj) {

            GameObject cameraRig = new GameObject("SXRCameraRig");
            cameraRig.gameObject.AddComponent<SXRCameraRig>();
            //Camera to be used exclusivelly for UI events, rendering will be disabled
            cameraRig.gameObject.AddComponent<Camera>().enabled = false;

            cameraRig.transform.parent = obj.transform;

            // Use pre-defined initial parameters in the editor
            // Actual head model parameters will be updated with valid ones at the run-time
            float headModelDepth = 0.0805f;
            float headModelHeight = 0.075f;
            cameraRig.transform.localPosition = new Vector3(0, -headModelHeight, headModelDepth);
            return cameraRig;
        }

        public static void UnprepareCamera(GameObject obj) {
            if (!obj)
                return;

            GameObject root = obj.transform.root.gameObject;

            SXRCamera sxrCamera = obj.GetComponent<SXRCamera>();
            SXRCameraRig sxrRig = obj.GetComponentInChildren<SXRCameraRig>();
            SXRRightEye rightEye = obj.GetComponentInChildren<SXRRightEye>();
            SXRLeftEye leftEye = obj.GetComponentInChildren<SXRLeftEye>();

            if (sxrCamera) {
                DestroyImmediate(sxrCamera);
            }
            if (sxrRig) {
                DestroyImmediate(sxrRig.gameObject);
            }
            if (rightEye) {
                DestroyImmediate(rightEye.gameObject);
            }
            if (leftEye) {
                DestroyImmediate(leftEye.gameObject);
            }

            // Event system is still valid if any sxrCamera exists in active/inactive GameObjects
            // FindObjectOfType returns active object only, so use FindObjectsOfTypeAll
            Transform sxrEventSystem = root.transform.Find("SXREventSystem");
            if (sxrEventSystem) {
                DestroyImmediate(sxrEventSystem.gameObject);
            } else {
                EventSystem eventSystem = root.GetComponentInChildren<EventSystem>();
                if (eventSystem) {
                    SXRInput sxrInput = eventSystem.GetComponent<SXRInput>();
                    SXRInputModule sxrInputModule = eventSystem.GetComponent<SXRInputModule>();
                    SXRCameraSystem sxrCameraSystem = eventSystem.GetComponent<SXRCameraSystem>();
                    if (sxrInput) {
                        DestroyImmediate(sxrInput);
                    }
                    if (sxrInputModule) {
                        DestroyImmediate(sxrInputModule);
                    }
                    if (sxrCameraSystem) {
                        DestroyImmediate(sxrCameraSystem);
                    }
                }
            }
        }


        public static void UnprepareAllSceneCameras() {



        }
        #endregion methods
    }
}
