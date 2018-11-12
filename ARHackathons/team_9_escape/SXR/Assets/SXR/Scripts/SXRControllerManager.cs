using UnityEngine;
using UnityEngine.VR;
using System.Collections.Generic;

namespace Sxr {

    public interface ControllerInterface {
        void setListener(ControllerListener listener);
        void start();
        void update();
        bool isConnected();
        ControllerId controllerId();
    }

    public interface ControllerListener {
        // this ControllerListener interface is a place holder for the future, for the unified
        void onConnected(ControllerInterface controller);
        void onDisconnected(ControllerInterface controller);
    }

    public enum ControllerId {
        SamsungSimpleController = 1,
        //SamsungNextController = 2,
    }

    public enum ButtonId {
        None = 0,
        Back = 1,
        Home = 2,
        VolumeUp = 3,
        VolumeDown = 4,
        TouchClick = 5, // another event from touch-up/down if triggered
        Trigger = 6,
    }

    public enum ConnectionState {
        Connected = 1,
        Other = 9
    }

    // SXRControllerManager - can be linked together with the legacy SXRInput when about to unitifed api
    public class SXRControllerManager : MonoBehaviour, ControllerListener {
        private const bool DEBUG_PER_FRAME = false;

        private static SXRControllerManager mInstance;

        private List<ControllerInterface> mControllers;

        private SXRSimpleController mSimpleController;

        public GameObject mControllerModel;

        private bool mControllerConnected = false; // not active by default, until state changed to connected


        public static SXRControllerManager Instance {
            get {
                if (mInstance == null) {
                    // Unity does not allow to instantiate a class that is inherited from MonoBehaviour
                    // instantiate SXRControllerManager without C# new keyword 
                    mInstance = GameObject.FindObjectOfType<SXRControllerManager>();
                }
                return mInstance;
            }
        }

        private SXRControllerManager() {
            mControllers = new List<ControllerInterface>();
            mSimpleController = new SXRSimpleController();
            addController(mSimpleController);
            //Debug.Log("SXRControllerManager constructed.");
        }

        private void addController(ControllerInterface controller) {
            mControllers.Add(controller);
            controller.setListener(this);
            controller.start();
        }

        public SXRSimpleController getSimpleController() {
            return mSimpleController;
        }

        void Start() {
            // some init on the controller state
            if (mControllerModel != null) {
                mControllerModel.SetActive(mControllerConnected);
            }
        }

        void Update() {
            foreach (ControllerInterface controller in mControllers) {
                if (DEBUG_PER_FRAME) {
                    Debug.Log("SXRControllerManager::Update() type = " + controller.controllerId());
                }

#if UNITY_ANDROID && !UNITY_EDITOR
            // the head transform
            SXRControllerPluginIF.currTransform(controller.controllerId(), 
                SXRManager.Instance.getFrameRegulator().getTransform()
                );

            // give a chance to each controller for state update. the sxr controller prefab can be positioned differently, e.g. scene top or under player.
            controller.update();

            // update the model state
            UpdateControllerModel(controller.isConnected());
#endif
            }
        }

        public void onConnected(ControllerInterface controller) {
            UpdateControllerModel(controller.isConnected());
        }

        public void onDisconnected(ControllerInterface controller) {
            UpdateControllerModel(controller.isConnected());
        }

        void UpdateControllerModel(bool controllerConnected) {
            if (DEBUG_PER_FRAME) {
                Debug.Log("UpdateControllerModel (mInstance.mControllerModel == null): " + (mInstance.mControllerModel == null));
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
