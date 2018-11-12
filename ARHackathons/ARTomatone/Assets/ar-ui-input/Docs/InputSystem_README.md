## SXR Input System
The SXR Input system is designed to work with native Unity UI components placed in world space, as well as normal 3D elements using the Unity Events system.

Developers can use either gaze or controller pointer for selecting objects. Objects must have a 3D collider in order to be recognized by the SXR Input system. 

Objects can also register for global events such as trigger presses or touchpad events regardless of selection.
#### Setup

1. Set up camera for SXR Camera Rig
	* Menu -> SXR -> Prepare VR Camera...
1. Add a new game object to your scene and add the SXR Controller Manager to that object.
1. Add the Physics Raycaster to the SXRCameraRig game object.
1. Find the SXREventSystem game object in the camera rig.
1. Remove the SXR Input Module, and replace it with MPS.SXRInputModule. The correct input module will have a checkbox for keyboard emulation.
1. Add the SXRCamerRig with the Physics Raycaster to the “Gaze Raycaster” property
1. Set which layers should be ignored by the raycaster.
1. Set which controller you'd like to use as the primary pointer. This can be changed in run-time, and it should be GAZE by default.
1. Set whether the trigger, touchpad button, or both, will be used for click events in the Unity UI system.


#####Optional: Set up controller input
1. Add a new game object that will represent the controller position. Typically you'll want to register for controller orientation events so that the game object's rotation matches the controller.
1. Add a Controller Raycaster to the game object.
1. Add the Controller Raycaster to the "Controller Raycaster" property.
1. Set which layers should be ignored by the raycaster.

#### Implementation

###### Using with UI Elements
UI elements will need a 3D collider added to the base game object, and must be in World Space. Otherwise they'll work as expected. Currently only button UI has been extensively tested, and scroll hasn't been implemented at all.

###### Using with 3D Elements
3D elements must have a collider for gaze interactivity. Objects can implement the [Unity Event System Interfaces](https://docs.unity3d.com/ScriptReference/EventSystems.ISubmitHandler.html). At the moment, ISubmitHandler, IPointerEnterHandler and IPointerExitHandler have been tested.

###### Using Global Events
Scripts can register for events from the controller, regardless of whether they're selected or not. Scripts must register with the SXR Plugin when initialized if they want global events.
```c#
	void Start () {
		SXRInputModule.Register(this, gameObject);
	}
```
**Event Handlers**
* ISXRControllerTriggerHandler
	* Handles controller trigger events.

```c#
		// when trigger is released
		void OnTriggerUp();
		// when trigger is pressed 
		void OnTriggerDown();
		// when trigger is pressed and released within a time threshold.
		void OnTriggerClicked();
``` 
* ISXRControllerTouchpadButtonHandler
	* Handles touchpad button events, and sends where on touchpad the event occurred.

```c#	
		//when touchpad button is released. Touchpad direction is the approximate placement of the finger when removed from pad.
		void OnTouchpadButtonUp(SXRInputModule.TouchpadDirection direction);
		
		//when touchpad button is pushed. Touchpad direction is the approximate placement of the finger when pushed on the pad.		
		void OnTouchpadButtonDown(SXRInputModule.TouchpadDirection direction);

		//when touchpad button is released within a certain time threshold. Touchpad direction is the approximate placement of the finger when removed from pad.
		void OnTouchpadClicked(SXRInputModule.TouchpadDirection direction);
```
* ISXRControllerTouchpadHandler
	* Handles touchpad events

```c#
		//when finger is placed on touchpad. Touchpad direction is the approximate placement of the finger when placed.
		void OnTouchpadOn(SXRInputModule.TouchpadDirection direction);
		
		//when finger is removed from touchpad. Touchpad direction is the approximate placement of the finger when removed.
		void OnTouchpadOff(SXRInputModule.TouchpadDirection direction);
		
		//event occurs on every frame when finger is on touchpad. Vector2 is position of finger on touchpad, normalized to 0.0 - 1.0.
		void OnTouchpadMove(Vector2 position);
		
		//event occurs when finger is removed from touchpad, and represents direction of finger movement from start to finish.
		void OnTouchpadSwipe(SXRInputModule.SwipeDirection swipeDirection);
```

* ISXRControllerGyroHandler
	* Handles gyro orientation of controller

```c#
	//event occurs on every frame and represents the orientation of the controller.
	void OnOrientation(Quaternion orientation);
```
* ISXRControllerBackHandler
	* Handles back button. This may be overridden by the system in future development.
	
```c#
		//when back button is released
		void OnBackButtonUp();
		
		//when back button is pressed
		void OnBackButtonDown();
		
		//when back button is released within a time threshold.
		void OnBackButtonClicked();
```
* IPointerHitPositionHandler
	* Handles world position of the collision between the ray from the pointer and the selected object. This can be used for placing reticles.	

```c#
		//when pointer has selected an object, this is the world position of where the pointer is hitting the object.
		
		void OnPointerHitPosition(GameObject selectedObject, Vector3 hitPosition, Vector3 hitNormal);
		
		//when pointer has started hitting object
		void OnPointerHitStart(GameObject selectedObject);
		
		//when pointer has stopped hitting object.
		void OnPointerHitEnd(GameObject deselectedObject);
```

###### Example script - SXREventSystemTest
```c#
using MPS.XR;
using UnityEngine;

//simple test of many of the SXR Input Module handlers
public class SXREventSytemTest : MonoBehaviour, 
	ISXRControllerTriggerHandler, 
	ISXRControllerTouchpadButtonHandler, 
	ISXRControllerTouchpadHandler, ISXRControllerGyroHandler {


	public void Start()
	{
		SXRInputModule.Register(this, gameObject);
	}
	
	public void OnTriggerUp()
	{
		print("Game Controller Trigger up!");
	}

	public void OnTriggerDown()
	{
		print("Game Controller Trigger Down!");
	}

	public void OnTriggerClicked()
	{
		print("Game Controller trigger clicked!");
	}

	public void OnTouchpadButtonUp(SXRInputModule.TouchpadDirection dir)
	{
		print("Game Controller Touchpad up! direction="+dir);
	}

	public void OnTouchpadButtonDown(SXRInputModule.TouchpadDirection dir)
	{
		print("Game Controller Touchpad down! direction="+dir);
	}

	public void OnTouchpadClicked(SXRInputModule.TouchpadDirection dir)
	{
		print("Game Controller Touchpad clicked! direction="+dir);
	}

	public void OnTouchpadOn(SXRInputModule.TouchpadDirection direction)
	{
		print("touchpad touch on! direction="+direction);
	}

	public void OnTouchpadOff(SXRInputModule.TouchpadDirection direction)
	{
		print("touchpad touch off! direction="+direction);
	}

	public void OnTouchpadMove(Vector2 position)
	{
		print("touchpad movement! position="+position.x+", "+position.y);
	}

	public void OnTouchpadSwipe(SXRInputModule.SwipeDirection swipeDirection)
	{
		print("touchpad swipe! direction="+swipeDirection);
	}

	public void OnOrientation(Quaternion orientation)
	{
		print("Orientation event! orientation="+orientation.ToString());
	}
}
```
###### Example Script - HitPositionTest

```c#
using MPS.XR;
using UnityEngine;
//uses a 3D object as a representation of where pointer is hitting selected object
public class HitPositionTest : MonoBehaviour, IPointerHitPositionHandler {

	public void Start()
	{
		SXRInputModule.Register(this, gameObject);
	}
	
	public void OnPointerHitPosition(GameObject selectedObject, Vector3 hitPosition, Vector3 hitNormal)
	{
		transform.position = hitPosition;
		transform.eulerAngles = hitNormal;
	}

	public void OnPointerHitStart(GameObject selectedObject)
	{
		if (selectedObject != null)
		{
			GetComponent<Renderer>().enabled = true;
		}
	}

	public void OnPointerHitEnd(GameObject deselectedObject)
	{
		if (deselectedObject != null)
		{
			GetComponent<Renderer>().enabled = false;
		}
	}
}

```
###### Example Script - GazeButtonTest

```c#
using UnityEngine;
using UnityEngine.EventSystems;

public class GazeButtonTest : MonoBehaviour, IPointerClickHandler, IPointerEnterHandler, IPointerExitHandler
{

	public bool is3D = false;

	public void OnPointerClick(PointerEventData eventData)
	{
		print("button Clicked!");
	}

	public void OnPointerEnter(PointerEventData eventData)
	{
		if (is3D)
		{
			GetComponent<Renderer>().material.color = Color.cyan;
		}
		//print("pointer entered!");
	}

	public void OnPointerExit(PointerEventData eventData)
	{
		if (is3D)
		{
			GetComponent<Renderer>().material.color = Color.white;
		}
		//print("pointer exited!");
	}
}

```
