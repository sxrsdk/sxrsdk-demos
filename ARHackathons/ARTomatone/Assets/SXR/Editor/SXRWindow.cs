using UnityEngine;
using UnityEditor;
using System.Collections;

namespace Sxr {

    public class SXRWindow : EditorWindow {

        bool groupEnabled;


        public static bool unityAntialiasing = false;
        public static SXRCameraSystem.UnityAntialiasingLevel antialiasingLevel = SXRCameraSystem.UnityAntialiasingLevel.X2Sample;
        bool brokenPipe = false;
        bool isCamera = false;
        bool preparedCameraExists = false;

        private GameObject root;
        private SXRCamera sxrCamera;

        [MenuItem("SXR/Prepare XR Camera...")]
        static void Init() {

            SXRWindow window = (SXRWindow)EditorWindow.GetWindow(typeof(SXRWindow));
            window.Show();
        }

        void Update() {
            Repaint();
        }

        void OnSelectionChange() {
            // Load current active GameObject and SXRCamera hierarchy if it has
            if (Selection.activeGameObject != null) {
                preparedCameraExists = false;
                brokenPipe = false;

                // Find current root GameObject for this camera
                root = Selection.activeGameObject.transform.root.gameObject;

                // Check SXRCamera hierarchy
                // Note. recommend to load single or few scenes from the editor, 
                // if you load multiple scenes then editor will be very slow response to 
                // render and repaint whole of cameras and GUI from multiple scenes together
                sxrCamera = root.GetComponentInChildren<SXRCamera>();
                if (!sxrCamera) {
                    brokenPipe = true;
                } else {
                    // Verify SXRCamera hierarchy
                    Transform transform = sxrCamera.gameObject.transform.Find("SXRCameraRig");
                    if (transform != null) {
                        GameObject go = transform.gameObject;
                        if (!go) {
                            brokenPipe = true;
                        } else {
                            if (!go.GetComponent<SXRCameraRig>()) {
                                brokenPipe = true;
                            }
                            if (!go.transform.Find("SXRRightCamera")) {
                                brokenPipe = true;
                            }
                            if (!go.transform.Find("SXRLeftCamera")) {
                                brokenPipe = true;
                            }
                        }
                    }
                }

                if (sxrCamera && !brokenPipe) {
                    brokenPipe = false;
                    preparedCameraExists = true;
                }

                // Reset settings
                unityAntialiasing = false;
                antialiasingLevel = SXRCameraSystem.UnityAntialiasingLevel.X2Sample;

                if (preparedCameraExists) {
                    // Sync editor settings with current SXRCamera
                    if (!sxrCamera.useGlobalSetting) {
                        unityAntialiasing = sxrCamera.useAntialiasing;
                        antialiasingLevel = sxrCamera.antialiasingLevel;
                        if (!unityAntialiasing) {
                            sxrCamera.useGlobalSetting = true;
                        }
                    }
                }
            } else {
                preparedCameraExists = false;
                brokenPipe = false;
                sxrCamera = null;
            }
        }

        void OnGUI() {
            brokenPipe = false; preparedCameraExists = false;

            // Check GUI and SXRCamera status
            if (Selection.activeGameObject != null) {
                // Find current root GameObject for this camera
                root = Selection.activeGameObject.transform.root.gameObject;

                // Check SXRCamera hierarchy
                // Note. recommend to load single or few scenes from the editor, 
                // if you load multiple scenes then editor will be very slow response to 
                // render and repaint whole of cameras and GUI from multiple scenes together
                sxrCamera = root.GetComponentInChildren<SXRCamera>();
                if (!sxrCamera) {
                    brokenPipe = true;
                } else {
                    // Verify SXRCamera hierarchy
                    Transform transform = sxrCamera.gameObject.transform.Find("SXRCameraRig");
                    if (transform != null) {
                        GameObject go = transform.gameObject;
                        if (!go) {
                            brokenPipe = true;
                        } else {
                            if (!go.GetComponent<SXRCameraRig>()) {
                                brokenPipe = true;
                            }
                            if (!go.transform.Find("SXRRightCamera")) {
                                brokenPipe = true;
                            }
                            if (!go.transform.Find("SXRLeftCamera")) {
                                brokenPipe = true;
                            }
                        }
                    }
                }
            }

            if (sxrCamera && !brokenPipe) {
                brokenPipe = false;
                preparedCameraExists = true;

#if UNITY_5
			if (PlayerSettings.virtualRealitySupported == true)
				PlayerSettings.virtualRealitySupported = false;
		

				
#endif
                PlayerSettings.defaultInterfaceOrientation = UIOrientation.LandscapeLeft;
            }
            if (Selection.activeGameObject == null || Selection.activeGameObject.GetComponent<Camera>() == null) {
                GUI.enabled = false;
                isCamera = false;
            } else {
                isCamera = true;
                GUI.enabled = true;
            }

            if (!brokenPipe) {
                GUI.enabled = false;
            }

            GUILayout.Label("SXR Camera Configuration", EditorStyles.boldLabel);

            unityAntialiasing = EditorGUILayout.Toggle("Use Unity Antialiasing ", unityAntialiasing);
            if (unityAntialiasing) {
                antialiasingLevel = (SXRCameraSystem.UnityAntialiasingLevel)EditorGUILayout.EnumPopup("Unity Antialiasing Level", antialiasingLevel);
            }

            if (brokenPipe && isCamera) {

                GUI.enabled = true;
#if UNITY_5
			GUILayout.Label ("This camera is not prepared for SXR use, please prepare it", EditorStyles.helpBox);
#else
                GUILayout.Label("This camera is not prepared for SXR use, please prepare it", EditorStyles.whiteLabel);
#endif



                if (GUILayout.Button("Prepare XR Camera...")) {

                    SXREditor.PrepareCamera(Selection.activeGameObject.GetComponent<Camera>());

                    // Sync with SXRCamera 
                    if (unityAntialiasing) {
                        if (sxrCamera == null) {
                            sxrCamera = Selection.activeGameObject.GetComponent<SXRCamera>();
                        }
                        sxrCamera.useGlobalSetting = false;
                        sxrCamera.useAntialiasing = unityAntialiasing;
                        sxrCamera.antialiasingLevel = antialiasingLevel;
                    }
                }
            }
            if (preparedCameraExists) {
                // Sync editor settings with current SXRCamera
                if (!sxrCamera.useGlobalSetting) {
                    unityAntialiasing = sxrCamera.useAntialiasing;
                    antialiasingLevel = sxrCamera.antialiasingLevel;
                } else {
                    unityAntialiasing = false;
                }

#if UNITY_5
			GUILayout.Label ("There's already one prepared camera", EditorStyles.helpBox);
#else
                GUILayout.Label("There's already one prepared camera", EditorStyles.whiteLabel);
#endif

                GUI.enabled = true;
                GUILayout.Label("SXR Prepared Camera");
                EditorGUILayout.ObjectField(sxrCamera, typeof(SXRCamera), true);
                if (GUILayout.Button("Unprepare XR Camera...")) {

                    SXREditor.UnprepareCamera(sxrCamera.gameObject);
                }
#if UNITY_5
			GUILayout.Label ("All elements inside 'SXRCameraRig' are going to be removed", EditorStyles.helpBox);
#else
                GUILayout.Label("All elements inside 'SXRCameraRig' are going to be removed", EditorStyles.whiteLabel);
#endif
            } else {
                if (!isCamera)
#if UNITY_5
			GUILayout.Label ("Please select a camera", EditorStyles.helpBox);
#else
                    GUILayout.Label("Please select a camera", EditorStyles.whiteLabel);
#endif

            }
        }
    }
}
