using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class Note : MonoBehaviour
{
	private int startFrame;
	// Use this for initialization
	void Start ()
	{
		startFrame = Time.frameCount;
	}
	
	// Update is called once per frame
	void Update () {
		gameObject.transform.Translate(.04f,0f,0f);
		if (Time.frameCount - startFrame > 1000&&startFrame>200) {
			GameObject.Destroy(gameObject);
		}
	}
}
