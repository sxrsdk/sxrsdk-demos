using UnityEngine;
using System.Collections;

namespace Sxr {

    public class SXRSwipeEvent {
        private const bool DEBUG_PER_FRAME = false;

        public enum SwipeType { SwipeUp, SwipeDown, SwipeRight, SwipeLeft, none };
        public SwipeType swipeType = new SwipeType();
        public SXRSwipeEvent() {
            swipeType = SwipeType.none;
        }

        public void SetSwipe(SwipeType swipe) {
            if (DEBUG_PER_FRAME) {
                Debug.Log("SXRSwipeEvent::SetSwipe  swipeType=" + swipe);
            }
            swipeType = swipe;
        }
    }
}
