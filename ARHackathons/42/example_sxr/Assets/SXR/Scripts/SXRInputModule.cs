using UnityEngine;
using UnityEngine.EventSystems;
using UnityEngine.UI;
using System.Collections;

namespace Sxr {

    public class SXRInputModule : BaseInputModule {

        private PointerEventData lookData;
        public static GameObject selectedObject;

        private PointerEventData GetLookPointerEventData() {
            Vector2 lookPosition;
            lookPosition.x = Screen.width / 2;
            lookPosition.y = Screen.height / 2;
            if (lookData == null) {
                lookData = new PointerEventData(eventSystem);
            }
            lookData.Reset();
            lookData.delta = Vector2.zero;
            lookData.position = lookPosition;
            lookData.scrollDelta = Vector2.zero;
            eventSystem.RaycastAll(lookData, m_RaycastResultCache);
            lookData.pointerCurrentRaycast = FindFirstRaycast(m_RaycastResultCache);
            m_RaycastResultCache.Clear();
            return lookData;
        }

        public override void Process() {
            //TODO: this will be called on every frame
            PointerEventData lookData = GetLookPointerEventData();

            HandlePointerExitAndEnter(lookData, lookData.pointerCurrentRaycast.gameObject);

            if (SXRInput.tapEvent) {
                Debug.Log(string.Format("SXRInput Process SXRInput.tapEvent"));

                eventSystem.SetSelectedGameObject(null);
                selectedObject = lookData.pointerCurrentRaycast.gameObject;
                if (selectedObject != null) {
                    GameObject go = lookData.pointerCurrentRaycast.gameObject;
                    ExecuteEvents.ExecuteHierarchy(go, lookData, ExecuteEvents.submitHandler);

                }
            }
        }
    }
}