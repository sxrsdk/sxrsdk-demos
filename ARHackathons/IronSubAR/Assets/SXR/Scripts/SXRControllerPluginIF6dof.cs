using UnityEngine;
using UnityEngine.VR;
using System.Runtime.InteropServices;

namespace Sxr {//TODO: namespace SxrInternal {

    public static class SXRControllerPluginIF6dof {
        //controller event queue size
        public const int SIZE_EVENT_QUEUE = 10;

        [StructLayout(LayoutKind.Sequential)]
        struct SxrControllerPoseRaw {
            public long time_stamp;//reserved
            public float x;
            public float y;
            public float z;
            public float w;
            public float gyro_x;
            public float gyro_y;
            public float gyro_z;
            public float p_x;
            public float p_y;
            public float p_z;
            //velocity and acceleration
            public float v_x;
            public float v_y;
            public float v_z;
            public float a_x;
            public float a_y;
            public float a_z;
            public float angularv_x;
            public float angularv_y;
            public float angularv_z;
            public float angulara_x;
            public float angulara_y;
            public float angulara_z;
        }

        [StructLayout(LayoutKind.Sequential)]
        public struct EventRaw {
            public int device_id;
            public int touch_x;
            public int touch_y;
            public int source;
            public int events;
            public int extra;
            public int available;
        };

        [StructLayout(LayoutKind.Sequential)]
        struct SxrControllerEventRaw {
            public int n_events; // the count set from native
            public long time_stamp;//reserved

            public EventRaw ev0; //TODO:IntPtr and Marshal.AllocHGlobal()
            public EventRaw ev1;
            public EventRaw ev2;
            public EventRaw ev3;
            public EventRaw ev4;
            public EventRaw ev5;
            public EventRaw ev6;
            public EventRaw ev7;
            public EventRaw ev8;
            public EventRaw ev9;
        }

        [StructLayout(LayoutKind.Sequential)]
        public class SxrControllerState6dof {
            public ConnectionState6dof connectionState;

            public ControllerPose controllerPose; // depending on the caller, it will be controller 1 or 2

            public ControllerEvent controllerEvent; // the last event in global. Will contain the controller 1 or 2 in the event data

            public ControllerEvent[] controllerEventAll // all events accumulated in this frame cycle.
                                                = new ControllerEvent[0]; // avoid null for app's convenience

            public SxrControllerState6dof() {
            }

            public SxrControllerState6dof(SxrControllerState6dof state) {
                this.connectionState = state.connectionState;
                this.controllerPose = state.controllerPose;
                this.controllerEvent = state.controllerEvent;
                this.controllerEventAll = state.controllerEventAll;
            }
        }

        // -----------------------------------------------------------------------

        private const string dllSxr = "sxr-plugin";

        [DllImport(dllSxr)]
        private static extern SxrControllerPoseRaw PureGetControllerPose6dof(ControllerId6dof controllerId);

        [DllImport(dllSxr)]
        private static extern SxrControllerEventRaw PureGetControllerEvent6dof();

        [DllImport(dllSxr)]
        private static extern int PureGetConnectionState6dof(ControllerId6dof controllerId);

        // -----------------------------------------------------------------------

        private const bool DEBUG_PER_FRAME = false;

        private static bool recenterPressed = true; // true when 1) first entered, 2) or Home button pressed (to be)
        private static SxrControllerState6dof mStateController1Save;

        public static SxrControllerState6dof updateControllerAll(ControllerId6dof controllerId) {
            if (DEBUG_PER_FRAME) {
                Debug.LogFormat("SXRControllerPluginIF updateControllerAll controllerId = " + controllerId);
            }

            // currState: no storing in this static class if more than 1 controller instances
            SxrControllerState6dof currState = new SxrControllerState6dof();

            // POSE
            SxrControllerPoseRaw sxrControllerPose = PureGetControllerPose6dof(controllerId);
            currState.controllerPose.timeStamp = sxrControllerPose.time_stamp;
            currState.controllerPose.orientation = new Quaternion(sxrControllerPose.x, sxrControllerPose.y, sxrControllerPose.z, sxrControllerPose.w);
            currState.controllerPose.position = new Vector3(sxrControllerPose.p_x, sxrControllerPose.p_y, sxrControllerPose.p_z);
            currState.controllerPose.gyro = new Vector3(sxrControllerPose.gyro_x, sxrControllerPose.gyro_y, sxrControllerPose.gyro_z);
            // velocity
            currState.controllerPose.velocity = new Vector3(sxrControllerPose.v_x, sxrControllerPose.v_y, sxrControllerPose.v_z);
            currState.controllerPose.acceleration = new Vector3(sxrControllerPose.a_x, sxrControllerPose.a_y, sxrControllerPose.a_z);
            currState.controllerPose.angularVelocity = new Vector3(sxrControllerPose.angularv_x, sxrControllerPose.angularv_y, sxrControllerPose.angularv_z);
            currState.controllerPose.angularAcceleration = new Vector3(sxrControllerPose.angulara_x, sxrControllerPose.angulara_y, sxrControllerPose.angulara_z);

            if (DEBUG_PER_FRAME) {
                Debug.LogFormat("SXRControllerPluginIF updateControllerAll currState.controllerPose.timeStamp = " + sxrControllerPose.time_stamp);
            }

            // EVENT
            if (controllerId == 0) { // only once per frame.
                SxrControllerEventRaw sxrControllerEvent = new SxrControllerEventRaw();
                sxrControllerEvent = PureGetControllerEvent6dof();
                if (DEBUG_PER_FRAME) {
                    Debug.LogFormat("SXRControllerPluginIF updateControllerAll sxrControllerEvent.n_events = " + sxrControllerEvent.n_events);
                }

                /*
                    * Whatever there is in the data, always fetches regardless of controller id (== controllerId) by just just copying it.
                    * The app should check event data only if the flag says event has arrived (available == 1). If the flag is off, 
                    * event data should not be read as it is not meaningful and data is not defined. Code snippet that uses SXRSixdofController 
                    * as exampled in ControllerBoard.cs of SXRFIO-TetheredVR sample app
                    */
                currState.controllerEventAll = new ControllerEvent[sxrControllerEvent.n_events]; // array size is variable and only number of acutual events

                copyRawToArray(currState.controllerEventAll, sxrControllerEvent.ev0, 0, sxrControllerEvent.n_events);
                copyRawToArray(currState.controllerEventAll, sxrControllerEvent.ev1, 1, sxrControllerEvent.n_events);
                copyRawToArray(currState.controllerEventAll, sxrControllerEvent.ev2, 2, sxrControllerEvent.n_events);
                copyRawToArray(currState.controllerEventAll, sxrControllerEvent.ev3, 3, sxrControllerEvent.n_events);
                copyRawToArray(currState.controllerEventAll, sxrControllerEvent.ev4, 4, sxrControllerEvent.n_events);
                copyRawToArray(currState.controllerEventAll, sxrControllerEvent.ev5, 5, sxrControllerEvent.n_events);
                copyRawToArray(currState.controllerEventAll, sxrControllerEvent.ev6, 6, sxrControllerEvent.n_events);
                copyRawToArray(currState.controllerEventAll, sxrControllerEvent.ev7, 7, sxrControllerEvent.n_events);
                copyRawToArray(currState.controllerEventAll, sxrControllerEvent.ev8, 8, sxrControllerEvent.n_events);
                copyRawToArray(currState.controllerEventAll, sxrControllerEvent.ev9, 9, sxrControllerEvent.n_events);

                // the last one in the queue
                if (sxrControllerEvent.n_events > 0) {
                    currState.controllerEvent = currState.controllerEventAll[sxrControllerEvent.n_events - 1];
                } else {
                    currState.controllerEvent = controllerNoEvent();
                }

                //save for controller2 for common ones such as controller event
                mStateController1Save = currState;
                mStateController1Save.controllerEventAll = currState.controllerEventAll;
            } else {
                currState.controllerEvent = mStateController1Save.controllerEvent;
                currState.controllerEventAll = mStateController1Save.controllerEventAll;
            }

            // STATE
            currState.connectionState = (ConnectionState6dof)getConnectionState(controllerId);

            return currState;
        }

        public static ControllerEvent controllerNoEvent() {
            ControllerEvent controllerEvent = new ControllerEvent();
            controllerEvent.Set(0L,
                -1,
                0,
                0,
                -1,
                -1,
                -1,
                (int)Events6dof.TvrEventInvalid);

            return controllerEvent;
        }

        private static void copyRawToArray(ControllerEvent[] arr, EventRaw ev, int idx, int n_events) {
            if (idx < n_events) {
                arr[idx].Set(0L,
                    ev.device_id,
                    ev.touch_x,
                    ev.touch_y,
                    ev.source,
                    ev.events,
                    ev.extra,
                    ev.available);
            }
        }

        private static int getConnectionState(ControllerId6dof controllerId) {
            int connectionState = PureGetConnectionState6dof(controllerId);
            return connectionState;
        }

    }
}
