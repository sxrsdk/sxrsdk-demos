using UnityEngine;
using UnityEngine.EventSystems;
using System;
using System.Xml;
using System.Collections;
using UnityEditor;
using UnityEditorInternal;

namespace Sxr {

    [CustomEditor(typeof(SXRCamera))]
    [CanEditMultipleObjects]
    public class SXRCameraEditor : Editor {
        #region members
        // Location prefeb @ Assets/SXR/Resources/SXREventSystem.prefab
        // Set value as default : FixedUpdate(false), Antialising(true), ChromaticAberration(true)
        private SerializedProperty useGlobalSettingField;
        private SerializedProperty useFixedUpdateField;
        private SerializedProperty useChromaticAberrationField;
        private SerializedProperty useAntialiasingField;
        private SerializedProperty antialiasingLevelField;

        static XmlDocument mSXRConfig;
        public static string titleMessage = "Camera already set",
                bodyMessage = "Your camera already have SXRCamera script, " +
                "if you really want to configure this camera, remove this script",
                confirmationMessage = "ok";
        #endregion members

        #region methods
        void OnEnable() {
            // Setup the SerializedProperties.
            this.useGlobalSettingField = serializedObject.FindProperty("useGlobalSetting");
            this.useFixedUpdateField = serializedObject.FindProperty("useFixedUpdate");
            this.useChromaticAberrationField = serializedObject.FindProperty("useChromaticAberration");
            this.useAntialiasingField = serializedObject.FindProperty("useAntialiasing");
            this.antialiasingLevelField = serializedObject.FindProperty("antialiasingLevel");
        }

        public override void OnInspectorGUI() {
            if (UpdateProperties() == false) {
                return;
            }

            DrawPropertyGUI();
        }

        private bool UpdateProperties() {
            try {
                this.useGlobalSettingField = serializedObject.FindProperty("useGlobalSetting");
                this.useFixedUpdateField = serializedObject.FindProperty("useFixedUpdate");
                this.useChromaticAberrationField = serializedObject.FindProperty("useChromaticAberration");
                this.useAntialiasingField = serializedObject.FindProperty("useAntialiasing");
                this.antialiasingLevelField = serializedObject.FindProperty("antialiasingLevel");
            } catch (Exception e) {
                Debug.Log(e.Message);
                return false;
            }

            return true;
        }
        private void DrawPropertyGUI() {
            EditorGUI.BeginChangeCheck();
            {
                EditorGUILayout.PropertyField(this.useGlobalSettingField);
                if (useGlobalSettingField.boolValue == false) {
                    EditorGUILayout.PropertyField(this.useFixedUpdateField);
                    EditorGUILayout.PropertyField(this.useChromaticAberrationField);
                    EditorGUILayout.PropertyField(this.useAntialiasingField);
                    if (useAntialiasingField.boolValue == true) {
                        EditorGUILayout.PropertyField(this.antialiasingLevelField);
                    }
                }
            }

            if (EditorGUI.EndChangeCheck()) {
                this.serializedObject.ApplyModifiedProperties();
            }
        }
        #endregion methods
    }
}
