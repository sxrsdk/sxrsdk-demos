using UnityEngine;
using UnityEngine.VR;
using System.Runtime.InteropServices;

namespace Sxr {//TODO: namespace SxrInternal {

    public static class SXRControllerPluginIF {

        [StructLayout(LayoutKind.Sequential)]
        struct SxrVector2 {
            public float x;
            public float y;
        }

        [StructLayout(LayoutKind.Sequential)]
        struct SxrVector3 {
            public float x;
            public float y;
            public float z;
        }

        [StructLayout(LayoutKind.Sequential)]
        struct SxrQuaternion {
            public float x;
            public float y;
            public float z;
            public float w;
        }

        [StructLayout(LayoutKind.Sequential)]
        public struct SxrControllerState {
            public ConnectionState connectionState;

            public Quaternion orientation;
            public Vector3 position;
            public Vector3 gyro;
            public Vector3 acceleration;

            public int touchState;
            public Vector2 touchPosition;
            public bool touchUp;// touch ended
            public bool touchDown;// touch started

            // Refer to the in-source comments such as getButton*() functions below for details
            public int buttonBackState;
            public bool buttonBackUp;
            public bool buttonBackDown;

            public int buttonHomeState;
            public bool buttonHomeUp;
            public bool buttonHomeDown;

            public int buttonVolumeUpState;
            public bool buttonVolumeUpUp;
            public bool buttonVolumeUpDown;

            public int buttonVolumeDownState;
            public bool buttonVolumeDownUp;
            public bool buttonVolumeDownDown;

            public int buttonTouchClickState;
            public bool buttonTouchClickUp; // if hardware supported, different from touch up/down
            public bool buttonTouchClickDown;

            public int buttonTriggerState;
            public bool buttonTriggerUp;
            public bool buttonTriggerDown;

            public void setPose(Vector3 position, Quaternion orientation) {
                if (DEBUG_PER_FRAME) {
                    Debug.LogFormat("SXRControllerPluginIF setPose orientation:{0} {1} {2} {3} ",
                       orientation.x,
                       orientation.y,
                       orientation.z,
                       orientation.w);
                    Debug.LogFormat("SXRControllerPluginIF setPose position: {0} {1} {2}",
                        position.x, position.y, position.z);
                }

                this.position = position;
                this.orientation = orientation;
            }
        }
        // -----------------------------------------------------------------------
        private const string dllSxr = "sxr-plugin";

        [DllImport(dllSxr)]
        private static extern SxrQuaternion PureGetOrientation(ControllerId controllerId);

        [DllImport(dllSxr)]
        private static extern SxrVector3 PureGetPosition(ControllerId controllerId);

        [DllImport(dllSxr)]
        private static extern SxrVector3 PureGetGyro(ControllerId controllerId);

        [DllImport(dllSxr)]
        private static extern SxrVector3 PureGetAcceleration(ControllerId controllerId);

        [DllImport(dllSxr)]
        private static extern SxrVector2 PureGetTouchPosition(ControllerId controllerId);

        [DllImport(dllSxr)]
        private static extern int PureGetTouchUp(ControllerId controllerId);

        [DllImport(dllSxr)]
        private static extern int PureGetTouchDown(ControllerId controllerId);

        [DllImport(dllSxr)]
        private static extern int PureGetTouchState(ControllerId controllerId);

        [DllImport(dllSxr)]
        private static extern int PureGetButtonUp(ControllerId controllerId, int buttonId);

        [DllImport(dllSxr)]
        private static extern int PureGetButtonDown(ControllerId controllerId, int buttonId);

        [DllImport(dllSxr)]
        private static extern int PureGetButtonState(ControllerId controllerId, int buttonId);

        [DllImport(dllSxr)]
        private static extern int PureGetConnectionState(ControllerId controllerId);

        [DllImport(dllSxr)]
        private static extern int PureStartController(ControllerId controllerId);

        [DllImport(dllSxr)]
        private static extern int PureStopController(ControllerId controllerId);

        [DllImport(dllSxr)]
        private static extern void PureSetHeadOrientation(ControllerId controllerId, SxrQuaternion sxrHeadOri);

        // -----------------------------------------------------------------------
        private const bool DEBUG_PER_FRAME = false;

        private static SxrControllerState currState = new SxrControllerState();
        private static Transform headTransform;
        private static bool recenterPressed = true; // true when 1) first entered, 2) or Home button pressed (to be)
        private static Quaternion syncRot;

        public static SxrControllerState getState() {
            return currState;
        }

        public static void updateControllerAll(ControllerId controllerId) {
            getPose(controllerId);

            currState.gyro = getGyro(controllerId);
            currState.acceleration = getAcceleration(controllerId);

            currState.touchState = getTouchState(controllerId);
            currState.touchPosition = getTouchPosition(controllerId);
            currState.touchUp = getTouchUp(controllerId);
            currState.touchDown = getTouchDown(controllerId);

            currState.buttonBackState = getButtonState(controllerId, (int)ButtonId.Back);
            currState.buttonBackUp = getButtonUp(controllerId, (int)ButtonId.Back);
            currState.buttonBackDown = getButtonDown(controllerId, (int)ButtonId.Back);

            currState.buttonHomeState = getButtonState(controllerId, (int)ButtonId.Home);
            currState.buttonHomeUp = getButtonUp(controllerId, (int)ButtonId.Home);
            currState.buttonHomeDown = getButtonDown(controllerId, (int)ButtonId.Home);

            currState.buttonVolumeUpState = getButtonState(controllerId, (int)ButtonId.VolumeUp);
            currState.buttonVolumeUpUp = getButtonUp(controllerId, (int)ButtonId.VolumeUp);
            currState.buttonVolumeUpDown = getButtonDown(controllerId, (int)ButtonId.VolumeUp);

            currState.buttonVolumeDownState = getButtonState(controllerId, (int)ButtonId.VolumeDown);
            currState.buttonVolumeDownUp = getButtonUp(controllerId, (int)ButtonId.VolumeDown);
            currState.buttonVolumeDownDown = getButtonDown(controllerId, (int)ButtonId.VolumeDown);

            currState.buttonTouchClickState = getButtonState(controllerId, (int)ButtonId.TouchClick);
            currState.buttonTouchClickUp = getButtonUp(controllerId, (int)ButtonId.TouchClick);
            currState.buttonTouchClickDown = getButtonDown(controllerId, (int)ButtonId.TouchClick);

            currState.buttonTriggerState = getButtonState(controllerId, (int)ButtonId.Trigger);
            currState.buttonTriggerUp = getButtonUp(controllerId, (int)ButtonId.Trigger);
            currState.buttonTriggerDown = getButtonDown(controllerId, (int)ButtonId.Trigger);

            currState.connectionState = (ConnectionState)getConnectionState(controllerId);
        }

        private static void getPose(ControllerId controllerId) {
            SxrQuaternion sxrOrientation = PureGetOrientation(controllerId);
            SxrVector3 sxrPosition = PureGetPosition(controllerId);

            Quaternion orientation = new Quaternion(sxrOrientation.x, sxrOrientation.y, sxrOrientation.z, sxrOrientation.w);
            Vector3 position = new Vector3(sxrPosition.x, sxrPosition.y, sxrPosition.z);

            currState.setPose(position, orientation);
        }

        public static void currTransform(ControllerId controllerId, Transform transform) {
            headTransform = transform;  // NOTE: this is for local-use test. Normally will be fetched from native later.

            // TODO: enable this UnityEngine.VR.VRSettings InputTracking. 
            // Until then use this eye transform param as head orientation.
            const bool use_VR_InputTracking = false;
            if (use_VR_InputTracking) {
                //VRSettings.enabled = true;
                Debug.Log("use_VR_InputTracking VRSettings.enabled= " + UnityEngine.XR.XRSettings.enabled);
                Debug.Log("InputTracking.disablePositionalTracking= " + UnityEngine.XR.InputTracking.disablePositionalTracking);

                Vector3 headDirection = UnityEngine.XR.InputTracking.GetLocalRotation(UnityEngine.XR.XRNode.Head)
                    * Vector3.forward;
                Quaternion headOri = Quaternion.FromToRotation(Vector3.forward, headDirection);
                setHeadOrientation(controllerId, headOri);//InputTracking.GetLocalRotation(VRNode.Head)
            } else {
                Vector3 headDirection =
                    transform.localRotation * Vector3.forward;//transform.forward;

                // the xy rotation - assuming the arm model expects forward direction
                Quaternion headOri = Quaternion.FromToRotation(Vector3.forward, headDirection);

                setHeadOrientation(controllerId, headOri);//transform.localRotation
            }
        }

        private static void setHeadOrientation(ControllerId controllerId, Quaternion headOri) {
            //deliver it to the Plugin native for controller's arm/body model
            //conversion to right-handed maybe needed if sxr Arm Model will already Left-handed inside 

            SxrQuaternion sxrHeadOri;

            sxrHeadOri.x = -headOri.x;
            sxrHeadOri.y = -headOri.y;
            sxrHeadOri.z = headOri.z;//-headOri.z; was from controller ver1.0 bug
            sxrHeadOri.w = headOri.w;
            if (DEBUG_PER_FRAME) {
                Debug.LogFormat("SXRControllerPluginIF setHeadOrientation sxrHeadOri:{0} {1} {2} {3} ",
                   sxrHeadOri.x,
                   sxrHeadOri.y,
                   sxrHeadOri.z,
                   sxrHeadOri.w);
                Debug.LogFormat("SXRControllerPluginIF setHeadOrientation eulerAngles {0} {1} {2}",
                    headOri.eulerAngles.x,
                    headOri.eulerAngles.y,
                    headOri.eulerAngles.z);
            }
            PureSetHeadOrientation(controllerId, sxrHeadOri);
        }

        private static Vector3 getGyro(ControllerId controllerId) {
            SxrVector3 sxrGyro = PureGetGyro(controllerId);
            return new Vector3(sxrGyro.x, sxrGyro.y, sxrGyro.z);
        }

        private static Vector3 getAcceleration(ControllerId controllerId) {
            SxrVector3 sxrAcceleration = PureGetAcceleration(controllerId);
            return new Vector3(sxrAcceleration.x, sxrAcceleration.y, sxrAcceleration.z);
        }

        private static Vector2 getTouchPosition(ControllerId controllerId) {
            SxrVector2 sxrTouchPosition = PureGetTouchPosition(controllerId);
            return new Vector2(sxrTouchPosition.x, sxrTouchPosition.y);
        }

        private static bool getTouchUp(ControllerId controllerId) {
            return PureGetTouchUp(controllerId) > 0; // up event
        }

        private static bool getTouchDown(ControllerId controllerId) {
            return PureGetTouchDown(controllerId) > 0; // down event
        }

        private static int getTouchState(ControllerId controllerId) {
            return PureGetTouchState(controllerId); // touchState & ISTOUCH_PRESSED;
        }

        // getButtonUp - returns true if button up event just happened. Currently not supported and always returning false.
        private static bool getButtonUp(ControllerId controllerId, int buttonId) {
            return PureGetButtonUp(controllerId, buttonId) > 0;
        }

        // getButtonDown - returns true if button down event just happened, otherwise false. Currently it returns true if stay pressed too.
        private static bool getButtonDown(ControllerId controllerId, int buttonId) {
            return PureGetButtonDown(controllerId, buttonId) > 0;
        }

        // getButtonState - returns 1 if event for the specified button is supported, otherwise 0. It depends on the given controller Id such as Simple Controller. In the future, usage will be extended to querying the state of pressed/not pressed.
        private static int getButtonState(ControllerId controllerId, int buttonId) {
            return PureGetButtonState(controllerId, buttonId);
        }

        private static int getConnectionState(ControllerId controllerId) {
            int connectionState = PureGetConnectionState(controllerId);
            return connectionState;// STARTED/STOPPED/BT_CONNECTED/ERROR
        }

        /* Deprecated. these are handled at lower level in the plugin */
        public static bool startController(ControllerId controllerId) {
            return PureStartController(controllerId) > 0;
        }

        /* Deprecated. these are handled at lower level in the plugin */
        public static bool stopController(ControllerId controllerId) {
            return PureStopController(controllerId) > 0;
        }

    }
}
