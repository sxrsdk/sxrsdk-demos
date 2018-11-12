using UnityEngine;
using System.Collections;
using System;
using UnityEditor;
using UnityEditorInternal;

namespace Sxr {

    [CustomEditor(typeof(SXRExternalCamera))]
    [CanEditMultipleObjects]
    public class SXRExternalCameraEditor : Editor {
        #region members
        private SerializedProperty useExternalCameraField;
        private SerializedProperty textureFormatField;
        private SerializedProperty previewWidthField;
        private SerializedProperty previewHeightField;
        private SerializedProperty cameraAngleDiffField;
        #endregion members

        #region methods
        void OnEnable() {
            // Setup the SerializedProperties.
            this.useExternalCameraField = serializedObject.FindProperty("useExternalCamera");
            this.textureFormatField = serializedObject.FindProperty("textureFormat");
            this.previewWidthField = serializedObject.FindProperty("previewWidth");
            this.previewHeightField = serializedObject.FindProperty("previewHeight");
            this.cameraAngleDiffField = serializedObject.FindProperty("cameraAngleDiff");
            //Debug.Log("[OnEnable]this.useExternalCameraField.boolValue=" + this.useExternalCameraField.boolValue);
        }

        public override void OnInspectorGUI() {
            if (UpdateProperties() == false) {
                return;
            }

            DrawPropertyGUI();
        }

        private bool UpdateProperties() {
            try {
                this.useExternalCameraField = serializedObject.FindProperty("useExternalCamera");
                this.textureFormatField = serializedObject.FindProperty("textureFormat");
                this.previewWidthField = serializedObject.FindProperty("previewWidth");
                this.previewHeightField = serializedObject.FindProperty("previewHeight");
                this.cameraAngleDiffField = serializedObject.FindProperty("cameraAngleDiff");
                //Debug.Log("this.useExternalCameraField.boolValue=" + this.useExternalCameraField.boolValue);
            } catch (Exception e) {
                Debug.Log(e.Message);
                return false;
            }

            return true;
        }
        private void DrawPropertyGUI() {
            EditorGUI.BeginChangeCheck();
            {
                EditorGUILayout.PropertyField(this.useExternalCameraField);
                if (useExternalCameraField.boolValue == true) {
                    EditorGUILayout.PropertyField(textureFormatField);
                }
                EditorGUILayout.PropertyField(this.previewWidthField);
                EditorGUILayout.PropertyField(this.previewHeightField);
                EditorGUILayout.PropertyField (this.cameraAngleDiffField);
            }

            if (EditorGUI.EndChangeCheck()) {
                this.serializedObject.ApplyModifiedProperties();
            }
        }
        #endregion methods
    }
}

