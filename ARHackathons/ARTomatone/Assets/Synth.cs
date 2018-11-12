using System.Collections;
using System.Collections.Generic;
using MPS.XR;
using UnityEngine;

[RequireComponent(typeof (AudioSource))]
public class Synth : MonoBehaviour, ISXRControllerTriggerHandler, ISXRControllerGyroHandler,
	ISXRControllerTouchpadHandler,ISXRControllerTouchpadButtonHandler,ISXRControllerVolumeHandler,
	ISXRControllerBackHandler,ISXRControllerHomeHandler

{
	public GameObject mouth;
	public GameObject face;
	public GameObject angleObject;
	public GameObject note;
	public AudioReverbFilter reverb;
	public AudioEchoFilter echo;
	public GameObject cameraRig, recenterer;
	public bool useMouse;

	public bool grayBackground;
	private bool lastGrayBackground;
	public Camera cam1, cam2;
	public GameObject ground;

	private float volume = .05f;
	private int noteCount;

	private float mouthStartScale;
	private float mouthSmallScale;

	private bool triggerDown,hadButton,hasClip;
	private float controllerPitch, mousePitch;

	private AudioSource audioSource;

	private Quaternion controllerNeutral=Quaternion.identity,lastControllerRotation;

	// Use this for initialization
	void Start () {
		Debug.Log("synth start up");
		mouthStartScale = mouth.transform.localScale.x;
		mouthSmallScale = mouthStartScale * .3f;
		
		SXRInputModule.Register(this, gameObject);

		audioSource = GetComponent<AudioSource>();
	}
	
	// Update is called once per frame
	void Update () {
		var q = angleObject.transform.rotation;
		var objPitch = getPitch(q);
		
		var x = Input.GetAxis("Mouse X");
		var y = Input.GetAxis("Mouse Y");
		if (useMouse && (x != 0 || y != 0)) {
			Debug.Log("Mouse X:" + x + " Y:" + y);
			mousePitch += y*.2f;
		}
		var pitch = controllerPitch + objPitch + mousePitch;
		if (hasClip) {
			audioSource.pitch = 1 + pitch;
		}
		else {
			audioSource.pitch = pitch*.7f + 3f;
		}

		var p = face.transform.localPosition;
		p.y = pitch;
		face.transform.localPosition = p;

		var b = Input.GetMouseButton(0) || triggerDown;
		float s = b ? mouthStartScale : mouthSmallScale;
		mouth.transform.localScale = new Vector3(s, s, s);
		audioSource.volume = b ? 1f : .3f;

		if (b&&!hadButton) {
			var newNote = GameObject.Instantiate(note);
			newNote.SetActive(true);
			newNote.transform.position = mouth.transform.position;
			noteCount++;

			Color c;
			if ((noteCount & 1) == 1) {
				c=Color.red;
			}
			else {
				c=Color.green;
			}
			var meshRenderer = newNote.GetComponent<MeshRenderer>();
			var mat = meshRenderer.material;
			mat.color = c;
			
			if (hasClip) {
				audioSource.Play();
			}
		}

		if (Input.GetMouseButtonDown(1)) {
			OnTouchpadClicked(SXRInputModule.TouchpadDirection.CENTER);
		}

		hadButton = b;

		if (grayBackground != lastGrayBackground) {
			UpdateBackground();
			lastGrayBackground = grayBackground;
		}
	}

	void OnAudioFilterRead(float[] data, int channels){
		if (!hasClip) {
			var len = data.Length;
			var halfData = len >> 1;
			var v = volume;
			for (int i = 0; i < len; i++) {
				var flag = i & 128;
				if (flag > 0) {
					data[i] = v;
				} else {
					data[i] = -v;
				}
			}
		}
		//Debug.Log("cb " + len);
	}

	public void OnTriggerUp()
	{
		triggerDown = false;
	}

	public void OnTriggerDown()
	{
		triggerDown = true;
	}

	public void OnTriggerClicked()
	{
	}

	public void OnOrientation(Quaternion orientation)
	{
		lastControllerRotation = orientation;
		var invNeutral = Quaternion.Inverse(controllerNeutral);
		orientation = invNeutral * orientation;
		controllerPitch = getPitch(orientation);
		angleObject.transform.rotation = orientation;
	}

	float getPitch(Quaternion q)
	{
		var v = q * Vector3.forward;
		var xz = v;
		xz.y = 0;
		var y = v.y;
		var xzLen = xz.magnitude;
		var pitch = Mathf.Atan2(y, xzLen);
//		Debug.Log("pitch:"+ pitch);

		return pitch;
		// will need to test, think angle should be in [-Pi,Pi]
	}

	public void OnTouchpadOn(SXRInputModule.TouchpadDirection direction)
	{
	}

	public void OnTouchpadOff(SXRInputModule.TouchpadDirection direction)
	{
	}

	public void OnTouchpadMove(Vector2 position)
	{
		echo.delay = position.x*200f+300f;
	}

	public void OnTouchpadSwipe(SXRInputModule.SwipeDirection swipeDirection)
	{
	}

	public void OnTouchpadButtonUp(SXRInputModule.TouchpadDirection direction)
	{
		echo.enabled = false;
	}

	public void OnTouchpadButtonDown(SXRInputModule.TouchpadDirection direction)
	{
		echo.enabled = true;
	}

	public void OnTouchpadClicked(SXRInputModule.TouchpadDirection direction)
	{
		if (hasClip) {
			audioSource.clip = null;
			hasClip = !hasClip;
			audioSource.Play();
		}
		else {
			Debug.Log("recording_clip");

			var devices = Microphone.devices;
			Debug.Log("num mics"+devices.Length);
			if (devices.Length > 0) {
				var device1 = devices[0];
				Debug.Log("recording with " + device1);
				var clip=Microphone.Start(device1, false, 3, 44100);
				audioSource.clip = clip;
				hasClip = !hasClip;
			}
			else {
				Debug.Log("no_microphone");
			}
		}
	}

	public void OnVolumeUpClicked()
	{
		volume *= 1.2f;
	}

	public void OnVolumeDownClicked()
	{
		volume *= .8f;
	}

	public void OnBackButtonUp()
	{
	}

	public void OnBackButtonDown()
	{
		controllerNeutral = lastControllerRotation;
		var r = cameraRig.transform.localRotation;
		recenterer.transform.localRotation = Quaternion.Inverse(r);
	}

	public void OnBackButtonClicked()
	{
	}

	public void OnHomeClicked()
	{
		grayBackground = !grayBackground;
//		UpdateBackground();
	}

	void UpdateBackground()
	{
		Color c = grayBackground ? Color.gray : Color.black;
		cam1.backgroundColor = c;
		cam2.backgroundColor = c;
		ground.SetActive(grayBackground);
	}
}
