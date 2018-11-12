/* Copyright 2018 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.EventSystems;


namespace  MPS.XR
{
	public class SXRInputModule : BaseInputModule, ISXRControllerTouchpadButtonHandler, ISXRControllerTriggerHandler
	{
		public enum TouchpadDirection
		{
			UP,
			DOWN,
			RIGHT,
			LEFT,
			CENTER,
			UNKNOWN
		}

		public enum SwipeDirection
		{
			UP,
			DOWN,
			RIGHT,
			LEFT,
			UNKNOWN
		}

		public enum PointerDevice
		{
			GAZE,
			CONTROLLER
		}

		public enum TouchpadDevice
		{
			CONTROLLER,
			HEADSET
		}

		//todo Headset values are placeholder. Replace with real values once known
		public static readonly Dictionary<TouchpadDevice, Vector2> TouchpadMultipliers =
			new Dictionary<TouchpadDevice, Vector2>
		{
			{TouchpadDevice.CONTROLLER, new Vector2(314,314)},
			{TouchpadDevice.HEADSET, new Vector2(314,50)}
		};

		private SimpleControllerInput controllerInput;
		public bool keyboardEmulation;
		public bool useMouseClick;
		public bool useTriggerForClick;
		public bool useTouchpadButtonForClick;

		private PointerEventData lookData;

		private GameObject selectedObject;
		private GameObject previousSelectedObject;
		private static List<GameObject> RegisteredHitPositionHandlers = new List<GameObject>();

		public BaseRaycaster gazeRaycaster;
		public BaseRaycaster controllerRayCaster;

		public PointerDevice primaryPointer = PointerDevice.GAZE;

		public float clickTimeThreshold = 0.5f;

		public override void ActivateModule()
		{
			base.ActivateModule();
			controllerInput = new SimpleControllerInput(keyboardEmulation);
			controllerInput.controllerClickThreshold = clickTimeThreshold;
			Register(this, gameObject); //register to the controller input so we can use standard Unity Events.
			print("SXR Input Module Activated!");
			selectedObject = gameObject;
			previousSelectedObject = gameObject;
		}

		public override void DeactivateModule()
		{
			base.DeactivateModule();
			RegisteredHitPositionHandlers.Clear();
			controllerInput.Destroy();
			controllerInput = null;
		}

		//register for global events
		public static void Register(IEventSystemHandler handler, GameObject go)
		{   
			if (handler is IPointerHitPositionHandler)
			{
				RegisteredHitPositionHandlers.Add(go);
			}
			SimpleControllerInput.Register(handler, go);
		}

		private void GetLookPointerEventData() {
			Vector2 lookPosition;
			lookPosition.x = Screen.width/2;
			lookPosition.y = Screen.height/2;
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
		}

		private bool clickEvent;
		private bool downEvent;
		private bool upEvent;
		private void ResetEvents()
		{
			clickEvent = false;
			downEvent = false;
			upEvent = false;
		}

		public override void Process()
		{
			ResetEvents();
			CheckSelectedPointer();
			GetLookPointerEventData();
			selectedObject = lookData.pointerCurrentRaycast.gameObject;
			controllerInput.Process(selectedObject);
			HandlePointerExitAndEnter(lookData,selectedObject);
			HandlePointerHit();
			if (useMouseClick)
			{
				CheckMouseClick();
			}
			GameObject go = selectedObject;
			if (clickEvent)
			{
				ExecuteEvents.ExecuteHierarchy(go, lookData, ExecuteEvents.pointerClickHandler);
			}
			if (downEvent)
			{
				ExecuteEvents.ExecuteHierarchy(go, lookData, ExecuteEvents.pointerDownHandler);
			}
			if (upEvent)
			{
				ExecuteEvents.ExecuteHierarchy(go, lookData, ExecuteEvents.pointerUpHandler);
			}
		}

		private void HandlePointerHit()
		{
			bool objectChanged = (selectedObject != null && previousSelectedObject == null) ||
				(selectedObject == null && previousSelectedObject != null);
			if (!objectChanged && selectedObject != null && previousSelectedObject != null)
			{
				objectChanged = !selectedObject.Equals(previousSelectedObject);
			}

			if (selectedObject != null)
			{
				Vector3 hitpos = lookData.pointerCurrentRaycast.worldPosition;
				Vector3 hitNormal = lookData.pointerCurrentRaycast.worldNormal;
				ExecuteEvents.Execute<IPointerHitPositionHandler>(selectedObject, lookData,
					(target, eventData) => { target.OnPointerHitPosition(selectedObject, hitpos, hitNormal); });
				foreach (var hoverHandler in RegisteredHitPositionHandlers)
				{
					if (!hoverHandler.Equals(selectedObject))
					{
						ExecuteEvents.Execute<IPointerHitPositionHandler>(hoverHandler, lookData,
							(target, eventData) => { target.OnPointerHitPosition(selectedObject, hitpos, hitNormal); });
					}
				}
			}

			if (objectChanged)
			{
				if (previousSelectedObject != null)
				{
					ExecuteEvents.Execute<IPointerHitPositionHandler>(previousSelectedObject, lookData,
						(target, eventData) => { target.OnPointerHitEnd(previousSelectedObject); });
				}

				if (selectedObject != null)
				{
					ExecuteEvents.Execute<IPointerHitPositionHandler>(selectedObject, lookData,
						(target, eventData) => { target.OnPointerHitStart(selectedObject); });
				}

				foreach (var hoverHandler in RegisteredHitPositionHandlers)
				{
					if (previousSelectedObject == null || !hoverHandler.Equals(previousSelectedObject))
					{
						ExecuteEvents.Execute<IPointerHitPositionHandler>(hoverHandler, lookData,
							(target, eventData) => { target.OnPointerHitEnd(previousSelectedObject); });
					}

					if (selectedObject == null || !hoverHandler.Equals(selectedObject))
					{
						ExecuteEvents.Execute<IPointerHitPositionHandler>(hoverHandler, lookData,
							(target, eventData) => { target.OnPointerHitStart(selectedObject); });
					}
				}
			}

			previousSelectedObject = selectedObject;
		}

		private void CheckSelectedPointer()
		{
			if (primaryPointer == PointerDevice.CONTROLLER)
			{
				if (controllerRayCaster != null && !controllerRayCaster.enabled)
				{
					controllerRayCaster.enabled = true;
				}
			}
			else
			{
				if (controllerRayCaster != null && controllerRayCaster.enabled)
				{
					controllerRayCaster.enabled = false;
				}
			}
			if (primaryPointer == PointerDevice.GAZE)
			{
				if (!gazeRaycaster.enabled)
				{
					gazeRaycaster.enabled = true;
				}
			}
			else
			{
				if (gazeRaycaster.enabled)
				{
					gazeRaycaster.enabled = false;
				}
			}
		}

		public void OnTouchpadClicked(TouchpadDirection direction)
		{
			if (useTouchpadButtonForClick)
			{
				clickEvent = true;
			}
		}

		public void OnTriggerClicked()
		{
			if (useTriggerForClick)
			{
				clickEvent = true;
			}
		}

		private float mouseDownTime = 0;
		private void CheckMouseClick()
		{
			if (Input.GetMouseButtonDown(0))
			{
				mouseDownTime = Time.unscaledTime;
			}

			if (Input.GetMouseButtonUp(0) && (Time.unscaledTime - mouseDownTime < 0.5f))
			{
				clickEvent = true;
			}
		}

		public void OnTouchpadButtonDown(TouchpadDirection direction)
		{
			if (useTouchpadButtonForClick)
			{
				downEvent = true;
			}
		}

		public void OnTouchpadButtonUp(TouchpadDirection direction)
		{
			if (useTouchpadButtonForClick)
			{
				upEvent = true;
			}
		}

		public void OnTriggerDown()
		{
			if (useTriggerForClick)
			{
				downEvent = true;
			}
		}

		public void OnTriggerUp()
		{
			if (useTriggerForClick)
			{
				upEvent = true;
			}
		}
	}

}