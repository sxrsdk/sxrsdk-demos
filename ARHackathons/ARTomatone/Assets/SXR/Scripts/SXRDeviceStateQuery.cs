using UnityEngine;
using System.Collections;
using UnityEngine.VR;
using System.Runtime.InteropServices;

namespace Sxr {

    public class SXRDeviceStateQuery {

        // -----------------------------------------------------------------------
        private const string dllSxr = "sxr-plugin";

        [DllImport(dllSxr)]
        private static extern int PureGetCVTrackingState();

        [DllImport(dllSxr)]
        private static extern void PureResetCVTracking();
        // -----------------------------------------------------------------------

        private const bool DEBUG_PER_FRAME = true;

        private static SXRDeviceStateQuery mInstance;

        public static SXRDeviceStateQuery Instance {
            get {
                if (mInstance == null) {
                    mInstance = new SXRDeviceStateQuery();
                }
                return mInstance;
            }
        }

        private SXRDeviceStateQuery() {
            //Debug.Log("SXRDeviceStateQuery constructed.");
        }

        public int//TODO enum 
            GetCVTrackingState() {
            int state = PureGetCVTrackingState();
            return state;
        }

        public void ResetCVTracking() {
            PureResetCVTracking();
        }

        //	Multiple Markers hashmap  

        //	Computer Vision Framerate
        
        public void UpdateStateAll() {
            if (DEBUG_PER_FRAME) {
                Debug.LogFormat("SXRDeviceStateQuery UpdateStateAll");
            }
            //TODO: can be batched to reduce call overhead
            // mState = PureUpdateStateAll(); // update once per frame
        }

    }
}
