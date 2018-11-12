using UnityEngine;
using System.Collections;

namespace Sxr {

    public class SXRInput : MonoBehaviour {
        private const bool DEBUG_PER_FRAME = false;

        //static variable to check tap event
        public static bool tapEvent, longPressEvent;
        //static variable to check swipe event;
        public static SXRSwipeEvent swipeEvent;
        //manager
        private SXRManager sxrm;

        private float longPressTimer = 0;

        public float xThreshold = 0.05f, yThreshold = 0.05f;
        private static Vector3 touchLastPos;

        void Start() {
            sxrm = SXRManager.Instance;

            //Create Swipe Event Instance
            SXRInput.swipeEvent = new SXRSwipeEvent();
        }

        void Update() {
            // hmd's sensor/input update
            updateHMDInput();
        }

        void updateHMDInput() {
            //Reset Click Every Frame
            tapEvent = false;
            //Reset LongPress Every Frame
            longPressEvent = false;
            //Reset swipe event
            swipeEvent.SetSwipe(SXRSwipeEvent.SwipeType.none);

            if (Input.GetButtonDown("Fire1")) {
                longPressTimer = Time.time;
                touchLastPos = Input.mousePosition;
            }
            if (Input.GetButtonUp("Fire1")) {
                if (DEBUG_PER_FRAME) {
                    Debug.Log("SXRInput::Update Fire1 GetButtonUp");
                }
                if (touchLastPos != Input.mousePosition) {
                    //check for new swipe events
                    HandleSwipe();
                }
                if (swipeEvent.swipeType != SXRSwipeEvent.SwipeType.none)
                    return;
                if (longPressTimer + .7f < Time.time)
                    longPressEvent = true;//Longpress is true
                else
                    tapEvent = true;//Tap is true
            }
        }

        void HandleSwipe() {
            float xDiff = (touchLastPos.x - Input.mousePosition.x) / Screen.width;
            float yDiff = (touchLastPos.y - Input.mousePosition.y) / Screen.height;
            // check threshold for both axis
            if (Mathf.Abs(xDiff) < xThreshold && Mathf.Abs(yDiff) < yThreshold) {
                if (DEBUG_PER_FRAME) {
                    Debug.Log("SXRInput::HandleSwipe threshold");
                }
                return;
            } else {
                if (DEBUG_PER_FRAME) {
                    Debug.Log("SXRInput::HandleSwipe threshold else");
                }
            }

            //if Swipe is identified, disable click 
            tapEvent = false;

            if (Mathf.Abs(xDiff) > Mathf.Abs(yDiff)) {
                if (xDiff > 0) {
                    swipeEvent.SetSwipe(SXRSwipeEvent.SwipeType.SwipeRight);
                } else if (xDiff < 0) {
                    swipeEvent.SetSwipe(SXRSwipeEvent.SwipeType.SwipeLeft);
                }
            } else {
                if (yDiff > 0) {
                    swipeEvent.SetSwipe(SXRSwipeEvent.SwipeType.SwipeDown);
                } else if (yDiff < 0) {
                    swipeEvent.SetSwipe(SXRSwipeEvent.SwipeType.SwipeUp);
                }
            }
            //store 
            touchLastPos = Input.mousePosition;
        }
    }
}
