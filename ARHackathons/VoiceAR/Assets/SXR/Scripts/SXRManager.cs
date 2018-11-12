using UnityEngine;
using System.Collections;

namespace Sxr {

    public class SXRManager {
        private static SXRManager sInstance;

        private SensorManager mSensorManager;
        private SXRControllerManager mControllerManager;
        private SXRControllerManager6dof mControllerManager6dof;

        // FR configuration
        private bool mEnableFrameRegulator = true;
        private SXRFrameRegulator mFrameRegulator;

        private SXRManager() {
            mSensorManager = SensorManager.Instance;
            mFrameRegulator = SXRFrameRegulator.Instance;
            //mControllerManager = SXRControllerManager.Instance;
            //mControllerManager6dof = SXRControllerManager6dof.Instance;

            // FR init and start
            if (mEnableFrameRegulator) {
                mFrameRegulator.initialize();
            }
        }

        public void onPause() {
            mSensorManager.pause();
        }

        public void onResume() {
            mSensorManager.resume();
        }

        public static SXRManager Instance {
            get {
                if (sInstance == null) {
                    Debug.Log(" SXRManager Instance  newly created.");
                    sInstance = new SXRManager();
                }
                return sInstance;
            }
        }

        public SensorManager getSensorManager() {
            return mSensorManager;
        }

        public SXRFrameRegulator getFrameRegulator() {
            return mFrameRegulator;
        }

        public SXRControllerManager getControllerManager() {
            return SXRControllerManager.Instance; //call it again in case reset
        }

        public SXRControllerManager6dof getControllerManager6dof() {
            return SXRControllerManager6dof.Instance; //call it again in case reset
        }

        // API

        // Update stereo camera settings with main camera settings.
        // An application should call this function after it changes
        // main camera settings.
        public void updateStereoCameraSettings() {
            // TODO
        }
    }
}
