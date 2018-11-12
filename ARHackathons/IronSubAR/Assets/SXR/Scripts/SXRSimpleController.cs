using UnityEngine;
using System.Collections;
using System.Runtime.InteropServices;

namespace Sxr {

    public class SXRSimpleController : ControllerInterface {
        private const bool DEBUG_PER_FRAME = false;

        private ControllerListener mListener;

        private UnityEngine.Quaternion mQuaternion = new UnityEngine.Quaternion();
        private Vector3 mGyro = new Vector3();
        static SXRSimpleController instance;

        public SXRSimpleController() {
            instance = this;
            //Debug.Log("SXRSimpleController instance created.");
        }

        public ControllerId controllerId() {
            return ControllerId.SamsungSimpleController;
        }

        public void setListener(ControllerListener listener) {

        }
        public void start() {
            //depreated. SXRControllerPluginIF.startController(controllerId());
        }

        public void stop() {
            //depreated. SXRControllerPluginIF.stopController(controllerId());
        }

        public void update() {
            SXRControllerPluginIF.updateControllerAll(controllerId());

            updateControllerTransform();
        }

        public bool isTouching() {
            // can be replace to use once mCurrState.touchState is supported
            if (getState().touchPosition.x != 0 || getState().touchPosition.y != 0) {
                return true;
            }
            return false;
        }

        public bool isConnected() {
            return getState().connectionState == ConnectionState.Connected;
        }

        public SXRControllerPluginIF.SxrControllerState getState() {
            return SXRControllerPluginIF.getState();
        }

        const bool APPLY_HEAD_ORI_NATIVE = true; // pose from native plugin
        const bool APPLY_HEAD_POS_NATIVE = true;

        void updateControllerTransform() {
            SXRControllerPluginIF.SxrControllerState controllerState = getState();
            Quaternion ori = controllerState.orientation;
            Vector3 pos = controllerState.position;

            var controllerObject = GameObject.Find("ControllerModel");//TODO: Public ControllerS
            if (controllerObject != null) {
                if (APPLY_HEAD_ORI_NATIVE) {
                    controllerObject.transform.localRotation =
                        new Quaternion(-ori.x, -ori.y, ori.z, ori.w);//new Quaternion(-ori.x, -ori.y, -ori.z, ori.w); was from controller ver1.0 bug

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

                if (APPLY_HEAD_POS_NATIVE) {
                    controllerObject.transform.localPosition =
                    new Vector3(pos.x, pos.y, -pos.z);
                    // controller ver 1.2 fix: ArmModel position for 1) out of view frustrum around -180, 2) recentering not following y-axis head rotation
                    // - Camera rot is over/under compensated. Arm Model needs upgrade.
                    // - Should stay forward facing by the cam rotation already

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

    }
}