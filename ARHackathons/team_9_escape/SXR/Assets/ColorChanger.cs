using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using MPS.XR;

public class ColorChanger : MonoBehaviour, ISXRControllerTriggerHandler,
	ISXRControllerTouchpadButtonHandler, 
	ISXRControllerTouchpadHandler, ISXRControllerGyroHandler
{
    public bool triggerClicked;
    public bool triggerDown;
    public bool triggerUp;

	[SerializeField]
	public GameObject cube1;
	public GameObject cube2;
	public GameObject cube3;

	public Color[] colors;

	//void OnTouchpadButtonDown(SXRInputModule.TouchpadDirection direction){
		
	//	Debug.Log(direction);

	//}
	
	//
	//public void OnTouchpadButtonDown(TouchpadDirection direction)
	//{
	//	if (useTouchpadButtonForClick)
	//	{
	//		downEvent = true;
	//	}
	//}
	//

    public void OnTriggerClicked()
    {
		//Fetch the Renderer from the GameObject
		Renderer rend = cube1.GetComponent<Renderer>();

		//Set the main Color of the Material to green
		rend.material.shader = Shader.Find("_Color");
		rend.material.SetColor("_Color", colors[0]);

        triggerClicked = true;
        Debug.Log("trigger clicked");
        //        throw new System.NotImplementedException();
    }

    public void OnTriggerDown()
    {
        triggerDown = true;
		cube1.active = false;
        Debug.Log("trigger down");
    }

    public void OnTriggerUp()
    {
        triggerUp = true;
		cube1.active = true;
        Debug.Log("trigger up");
        //        throw new System.NotImplementedException();
        Vector3 r = Random.insideUnitSphere;
        cube1.GetComponent<MeshRenderer>().sharedMaterial.color = new Color(Mathf.Abs(r.x), Mathf.Abs(r.y), Mathf.Abs(r.z));
		//	Debug.Log ("r is ", r); 
		//	Debug.Log(" Mathf.Abs(r.x) is ", Mathf.Abs(r.x));
		//	Debug.Log(" Mathf.Abs(r.y) is ", Mathf.Abs(r.y));
		//	Debug.Log(" Mathf.Abs(r.z) is ", Mathf.Abs(r.z));
	}

	public void OnTouchpadButtonUp(SXRInputModule.TouchpadDirection dir)
	{
		if (dir == SXRInputModule.TouchpadDirection.LEFT) {
			cube1.active = true;
		}
		if (dir == SXRInputModule.TouchpadDirection.UP) {
			cube2.active = true;
		}
		if (dir == SXRInputModule.TouchpadDirection.RIGHT) {
			cube3.active = true;
		}
		Debug.Log("Game Controller Touchpad up! direction="+dir);
	}

	public void OnTouchpadButtonDown(SXRInputModule.TouchpadDirection dir)
	{
		if (dir == SXRInputModule.TouchpadDirection.LEFT) {
			cube1.active = false;
		}
		if (dir == SXRInputModule.TouchpadDirection.UP) {
			cube2.active = false;
		}
		if (dir == SXRInputModule.TouchpadDirection.RIGHT) {
			cube3.active = false;
		}		
		Debug.Log("Game Controller Touchpad down! direction="+dir);
	}

	public void OnTouchpadClicked(SXRInputModule.TouchpadDirection dir)
	{
		Debug.Log("Game Controller Touchpad clicked! direction="+dir);
	}

	public void OnTouchpadOn(SXRInputModule.TouchpadDirection direction)
	{
		Debug.Log("touchpad touch on! direction="+direction);
	}

	public void OnTouchpadOff(SXRInputModule.TouchpadDirection direction)
	{
		Debug.Log("touchpad touch off! direction="+direction);
	}

	public void OnTouchpadMove(Vector2 position)
	{
		Debug.Log("touchpad movement! position="+position.x+", "+position.y);
	}

	public void OnTouchpadSwipe(SXRInputModule.SwipeDirection swipeDirection)
	{
		Debug.Log("touchpad swipe! direction="+swipeDirection);
	}

	public void OnOrientation(Quaternion orientation)
	{
		Debug.Log("Orientation event! orientation="+orientation.ToString());
	}

	// public void MatchingColors()
	// {
	//	if (cube1 == 1)
	// }

    // Start is called before the first frame update
    void Start()
    {
        SXRInputModule.Register(this, gameObject);
    }

    // Update is called once per frame
    void Update()
    {
        
    }
}
