using UnityEngine;
using UnityEngine.VR;
using System.Collections.Generic;

namespace Sxr {

    public enum ConnectionState6dof {
        Connected = 1,
        Other = 9
    }

    public struct ControllerPose {
        public long timeStamp;
        public Quaternion orientation;
        public Vector3 position;
        public Vector3 gyro;
        public Vector3 velocity;///value of velocity, in m/s
        public Vector3 acceleration;///value of acceleration, in m/s2
        public Vector3 angularVelocity;///value of Angular velocity, in RAD/s
        public Vector3 angularAcceleration;//value of Angular acceleration, in RAD/s2
    }

    public enum EventSource6dof {
        TvrSourceInvalid = (-1),
        TvrSourceHome = (0),
        TvrSourceBack = (1),
        TvrSourceTrigger = (2),
        TvrSourceTouch = (3),
        TvrSourceGripLeft = (4),
        TvrSourceGripRight = (5),
        TvrSourceAPP = (6),
        TvrSourceTouchPad = (7),
    }

    public enum Events6dof {
        TvrEventInvalid = -1,
        TvrEventPressed = 0,
        TvrEventReleased = 1,
    }

    public struct ControllerEvent {
        public long timeStamp;
        public int deviceId;
        public int touchX;
        public int touchY;
        public EventSource6dof source;
        public Events6dof events;
        public int extra;
        public int available;
        public void Set(long timeStamp, int deviceId, int touchX, int touchY, int source, int events, int extra, int available) {
            this.timeStamp = timeStamp;
            this.deviceId = deviceId;
            this.touchX = touchX;
            this.touchY = touchY;
            this.source = (EventSource6dof)source;// maps to it 1 to 1, without explicit conversion
            this.events = (Events6dof)events;
            this.extra = extra;
            this.available = available;
        }
    }

    public class ControllerState : SXRControllerPluginIF6dof.SxrControllerState6dof {
        public ControllerState(SXRControllerPluginIF6dof.SxrControllerState6dof state) : base(state) { }

        // further interfaces for app
        public ControllerId6dof mControllerId;
    }

    public class SXRSixdofController : MonoBehaviour, ControllerInterface6dof {
        private const bool DEBUG_PER_FRAME = false;

        private List<ControllerInterface6dof> mControllers;

        public ControllerId6dof mControllerId = ControllerId6dof.TVRController1; // set from the scene

        public GameObject mControllerModel;

        private bool mControllerConnected = false; // not active by default, until state changed to connected

        private SXRControllerPluginIF6dof.SxrControllerState6dof mState = new SXRControllerPluginIF6dof.SxrControllerState6dof();//not null for initial

        private SXRSixdofController() {
            Debug.Log("SXRSixdofController constructed");
        }

        /// <summary>
        /// controllerId() - returns the controller id bound to this
        /// </summary>
        public ControllerId6dof controllerId() {
            return mControllerId;
        }

        public void setListener(ControllerListener6dof listener) {
        }

        public void start() {
        }

        public void stop() {
        }

        public bool isConnected() {
            return getState().connectionState == ConnectionState6dof.Connected;
        }

        void Start() {

            Debug.Log("SXRSixdofController Start. mControllerId=" + mControllerId);

            // add myself to the manager
            SXRManager sxrm = SXRManager.Instance;
            SXRControllerManager6dof controllerMgr = sxrm.getControllerManager6dof();
            if (controllerMgr != null) {
                Debug.Log("SXRSixdofController::Start() adding controller");
                controllerMgr.AddController(this);
            }

            // some init on the controller state
            if (mControllerModel != null) {
                mControllerModel.SetActive(mControllerConnected);
            }
        }

        void Update() {
            if (DEBUG_PER_FRAME) {
                Debug.Log("SXRSixdofController Update=" + mControllerId);
            }

#if UNITY_ANDROID && !UNITY_EDITOR

        // give a chance to each controller for state update. the sxr controller prefab can be positioned differently, e.g. scene top or under player.
        update();

        // update the model state
        UpdateControllerModel(isConnected());

#endif
        }

        public void update() {
            mState = SXRControllerPluginIF6dof.updateControllerAll(controllerId()); // update once per frame

            if (DEBUG_PER_FRAME) {
                Debug.LogFormat("handleControllerEvent: {0} {1} {2} {3} {4} {5} {6} {7}",
                    getState().controllerEvent.timeStamp,
                    getState().controllerEvent.deviceId,
                    getState().controllerEvent.touchX,
                    getState().controllerEvent.touchY,
                    getState().controllerEvent.source,
                    getState().controllerEvent.events,
                    getState().controllerEvent.extra,
                    getState().controllerEvent.available);

                Debug.LogFormat("connectionState: {0}",
                    getState().connectionState);
            }

            updateControllerTransform();
        }

        /// <summary>
        /// isTouching() - true if there was a touching event on a controller. a convenience function.
        /// </summary>
        public bool isTouching() {
            // can be replace to use once mCurrState.touchState is supported
            if (getState().controllerEvent.touchX != 0 || getState().controllerEvent.touchY != 0) {
                return true;
            }
            return false;
        }

        /// <summary>
        /// getState() - gets the current state read from the plugin
        /// </summary>
        public SXRControllerPluginIF6dof.SxrControllerState6dof getState() {
            return mState;
        }

        /// <summary>
        /// getControllerState() - gets the current state read from the plugin
        /// </summary>
        public ControllerState getControllerState() {
            ControllerState controllerState; // be an atomic variable or replace mState to avoid confusion
            controllerState = new ControllerState(mState);
            controllerState.mControllerId = this.mControllerId;

            return controllerState;
        }

        /// <summary>
        /// getEvent() - gets the last event, global to the connected controllers
        /// </summary>
        public ControllerEvent getEvent() {
            if (DEBUG_PER_FRAME) {
                Debug.LogFormat("getEvent: {0} {1} {2} {3} {4} {5} {6} {7}",
                    mState.controllerEvent.timeStamp,
                    mState.controllerEvent.deviceId,
                    mState.controllerEvent.touchX,
                    mState.controllerEvent.touchY,
                    mState.controllerEvent.source,
                    mState.controllerEvent.events,
                    mState.controllerEvent.extra,
                    mState.controllerEvent.available);
            }
            return mState.controllerEvent;
        }

        /// <summary>
        /// getEventLocal() - gets the last event from this controller in this frame cycle
        /// </summary>
        public ControllerEvent getEventLocal() {
            int size = mState.controllerEventAll.Length;
            if (DEBUG_PER_FRAME) {
                Debug.LogFormat("getEventLocal: mControllerId=" + mControllerId + ", size=" + size);
            }

            ControllerEvent controllerEventLocal = SXRControllerPluginIF6dof.controllerNoEvent(); // clear the default

            for (int cnt = size - 1; cnt >= 0 && mState.controllerEventAll[cnt].available == 1; cnt--) {//look up in reverse order
                if (mState.controllerEventAll[cnt].deviceId == (int)mControllerId) {//mine
                    controllerEventLocal = mState.controllerEventAll[cnt];
                    break;
                }
            }
            if (DEBUG_PER_FRAME) {
                Debug.LogFormat("getEventLocal: {0} {1} {2} {3} {4} {5} {6} {7}",
                    controllerEventLocal.timeStamp,
                    controllerEventLocal.deviceId,
                    controllerEventLocal.touchX,
                    controllerEventLocal.touchY,
                    controllerEventLocal.source,
                    controllerEventLocal.events,
                    controllerEventLocal.extra,
                    controllerEventLocal.available);
            }
            return controllerEventLocal;
        }

        /// <summary>
        /// getEventLocalAll() - gets all the events from this controller in this frame cycle
        /// </summary>
        public ControllerEvent[] getEventLocalAll() {
            int size = mState.controllerEventAll.Length;
            if (DEBUG_PER_FRAME) {
                Debug.LogFormat("getEventLocalAll: mControllerId=" + mControllerId + ", size=" + size);
            }

            //extract count
            int cnt_local = 0;
            for (int cnt = 0; cnt < size && mState.controllerEventAll[cnt].available == 1; cnt++) {
                if (DEBUG_PER_FRAME) {
                    Debug.LogFormat("getEventLocalAll if: {0} {1}",
                        (ControllerId6dof)mState.controllerEventAll[cnt].deviceId, mControllerId);
                }

                if (mState.controllerEventAll[cnt].deviceId == (int)mControllerId) {//mine
                    cnt_local += 1;
                } else {
                    // do nothing
                }

                if (DEBUG_PER_FRAME) {
                    Debug.LogFormat("getEventLocalAll: {0} {1} {2} {3} {4} {5} {6} {7}",
                        mState.controllerEventAll[cnt].timeStamp,
                        mState.controllerEventAll[cnt].deviceId,
                        mState.controllerEventAll[cnt].touchX,
                        mState.controllerEventAll[cnt].touchY,
                        mState.controllerEventAll[cnt].source,
                        mState.controllerEventAll[cnt].events,
                        mState.controllerEventAll[cnt].extra,
                        mState.controllerEventAll[cnt].available);
                }
            }

            //extract data
            ControllerEvent[] controllerEventLocal = new ControllerEvent[cnt_local];
            for (int cnt = 0, localCnt = 0; cnt < size && mState.controllerEventAll[cnt].available == 1; cnt++) {
                if (mState.controllerEventAll[cnt].deviceId == (int)mControllerId) {//mine
                    controllerEventLocal[localCnt++] = mState.controllerEventAll[cnt];
                } else {
                    // do nothing
                }
            }

            if (DEBUG_PER_FRAME) {
                for (int cnt = 0; cnt < controllerEventLocal.Length; cnt++) {
                    Debug.LogFormat("getEventLocalAll controllerEventLocal: {0} {1} {2} {3} {4} {5} {6} {7}",
                        controllerEventLocal[cnt].timeStamp,
                        controllerEventLocal[cnt].deviceId,
                        controllerEventLocal[cnt].touchX,
                        controllerEventLocal[cnt].touchY,
                        controllerEventLocal[cnt].source,
                        controllerEventLocal[cnt].events,
                        controllerEventLocal[cnt].extra,
                        controllerEventLocal[cnt].available);
                }
            }

            return controllerEventLocal;
        }

        const bool APPLY_ORI_NATIVE = true; // pose from native plugin
        const bool APPLY_POS_NATIVE = true;

        private static Quaternion sModelRotation = new Quaternion(); // sModelRotation.SetEulerRotation(): uses radians, init once is enough
        private static double PI = 3.141592653589793D;

        void updateControllerTransform() {
            SXRControllerPluginIF6dof.SxrControllerState6dof controllerState = getState();
            Quaternion ori = controllerState.controllerPose.orientation;
            Vector3 pos = controllerState.controllerPose.position;
            Vector3 gyro = controllerState.controllerPose.gyro;

            Vector3 velocity = controllerState.controllerPose.velocity;
            Vector3 acceleration = controllerState.controllerPose.acceleration;
            Vector3 angularVelocity = controllerState.controllerPose.angularVelocity;
            Vector3 angularAcceleration = controllerState.controllerPose.angularAcceleration;

            if (DEBUG_PER_FRAME) {
                Debug.LogFormat("controllerState ori: {0} {1} {2} {3}",
                    ori.x, ori.y, ori.z, ori.w);
                Debug.LogFormat("controllerState gyro: {0} {1} {2}",
                    gyro.x, gyro.y, gyro.z);
                Debug.LogFormat("controllerState pos: {0} {1} {2}",
                    pos.x, pos.y, pos.z);

                Debug.LogFormat("controllerState velocity: {0} {1} {2}",
                    velocity.x, velocity.y, velocity.z);
                Debug.LogFormat("controllerState acceleration: {0} {1} {2}",
                    acceleration.x, acceleration.y, acceleration.z);
                Debug.LogFormat("controllerState angularVelocity: {0} {1} {2}",
                    angularVelocity.x, angularVelocity.y, angularVelocity.z);
                Debug.LogFormat("controllerState angularAcceleration: {0} {1} {2}",
                    angularAcceleration.x, angularAcceleration.y, angularAcceleration.z);       
            }

            const bool APPLY_UNITY_AXIS_CONVERSION = true;

            var controllerObject = mControllerModel;// GameObject.Find("ControllerModel"); not efficient, name can be the same for the two controllers
            if (controllerObject != null) {
                if (APPLY_ORI_NATIVE) {
                    // Unity's conversion
                    if (APPLY_UNITY_AXIS_CONVERSION) {//Left-handed
                        ori = new Quaternion(-ori.x, -ori.y, ori.z, ori.w);//new Quaternion(-ori.x, -ori.y, -ori.z, ori.w); was from controller ver1.0 bug
                    }

                    controllerObject.transform.localRotation =
                         new Quaternion(ori.x, ori.y, ori.z, ori.w);

                    if (DEBUG_PER_FRAME) {
                        Debug.LogFormat("controllerObject rotation: {0} {1} {2} {3}",
                            controllerObject.transform.localRotation.x, controllerObject.transform.localRotation.y, controllerObject.transform.localRotation.z, controllerObject.transform.localRotation.w);
                        Debug.LogFormat("controllerObject rotation eulerAngles {0} {1} {2}",
                            controllerObject.transform.localRotation.eulerAngles.x,
                            controllerObject.transform.localRotation.eulerAngles.y,
                            controllerObject.transform.localRotation.eulerAngles.z
                            );
                    }
                }

                // the default position on the scene
                controllerObject.transform.localPosition =
                    new Vector3(0f, 0f, 0f); //identity

                if (APPLY_POS_NATIVE) {
                    // Unity's conversion
                    if (APPLY_UNITY_AXIS_CONVERSION) {//Left-handed
                        controllerObject.transform.localPosition =
                            new Vector3(pos.x, pos.y, -pos.z); // or (-pos.x, -pos.y, pos.z);
                    }

                    if (DEBUG_PER_FRAME) {
                        Debug.LogFormat("updateControllerTransform localPosition position: {0} {1} {2}",
                            controllerObject.transform.localPosition.x, controllerObject.transform.localPosition.y, controllerObject.transform.localPosition.z);
                        Debug.LogFormat("updateControllerTransform world position: {0} {1} {2}",
                            controllerObject.transform.position.x, controllerObject.transform.position.y, controllerObject.transform.position.z);
                    }
                }
            } else {
                if (DEBUG_PER_FRAME) {
                    Debug.Log("updateControllerTransform (controllerObject == null)!");
                }
                // do nothing
            }
        }

        public void onConnected(ControllerInterface6dof controller) {
            UpdateControllerModel(controller.isConnected());
        }

        public void onDisconnected(ControllerInterface6dof controller) {
            UpdateControllerModel(controller.isConnected());
        }

        void UpdateControllerModel(bool controllerConnected) {
            if (DEBUG_PER_FRAME) {
                //Debug.Log("UpdateControllerModel (mInstance.mControllerModel == null): " + (mInstance.mControllerModel == null));
                if (mControllerModel != null) {
                    Debug.Log("UpdateControllerModel mControllerModel.active: " + mControllerModel.active);
                }
            }

            // show or hide contoller model depending on the connection state
            if ((controllerConnected != mControllerConnected)) {
                Debug.Log("UpdateControllerModel state changed to: " + controllerConnected);

                if (mControllerModel != null) {
                    mControllerModel.SetActive(controllerConnected);
                }
                mControllerConnected = controllerConnected;
            }
        }
    }
}
