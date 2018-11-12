using UnityEngine;
using UnityEngine.VR;
using System.Collections.Generic;

namespace Sxr {

    public interface ControllerInterface6dof {
        void setListener(ControllerListener6dof listener);
        void start();
        void update();
        bool isConnected();
        ControllerId6dof controllerId();
    }

    public interface ControllerListener6dof {
        // this ControllerListener6dof interface is a place holder for the future, for the unified
        void onConnected(ControllerInterface6dof controller);
        void onDisconnected(ControllerInterface6dof controller);
    }

    public enum ControllerId6dof {
        TVRController1 = 0, // matches to the idx of lower library
        TVRController2 = 1,
    }


    public class SXRControllerManager6dof : MonoBehaviour {
        private const bool DEBUG_PER_FRAME = false;

        private static SXRControllerManager6dof sInstance;

        private List<ControllerInterface6dof> mControllers;

        // a single instance in the scene that can traverse multiple controllers.
        public static SXRControllerManager6dof Instance {
            get {
                if (sInstance == null) {
                    // Unity does not allow to instantiate a class that is inherited from MonoBehaviour
                    // instantiate SXRControllerManager6dof without C# new keyword 
                    sInstance = GameObject.FindObjectOfType<SXRControllerManager6dof>(); // one-time find, reseting by default on new scenes; the only instance that attached to the single game object ControllerManager6dof in the scene
                    Debug.Log("SXRControllerManager6dof Instance (sInstance == null): " + (sInstance == null));
                }
                return sInstance;
            }
        }

        public void AddController(ControllerInterface6dof controller) {
            mControllers.Add(controller);
        }

        public SXRSixdofController GetController(ControllerId6dof id) {
            int count = 0;
            foreach (ControllerInterface6dof controller in mControllers) {
                if (DEBUG_PER_FRAME) {
                    Debug.Log("SXRControllerManager6dof::GetController() type = " + controller.controllerId());
                }
                if (id == controller.controllerId()) {// count will also match the enum
                    return (SXRSixdofController)controller;
                }
                count++;
            }

            Debug.Log("SXRControllerManager6dof::GetController() not found, id = " + id);
            return null;
        }

        void Awake() {
            Debug.Log("SXRControllerManager6dof::Awake (sInstance == null):" + (sInstance == null));

            // instantiate early enough for applications so that they can use this on their Start()
            mControllers = new List<ControllerInterface6dof>();

            // singleton check
            if (sInstance != null && sInstance != this) {
                Debug.LogFormat("SXRControllerManager6dof::Awake {0} sInstance is being destroyed because a singelton reference was already set!", this.name);
                Destroy(this);
            } else { // sInstance == null or this:  me the singelton. Get the reference to the component.
                Debug.LogFormat("SXRControllerManager6dof::Awake {0} sInstance is set", this.name);  
                sInstance = this.GetComponent<SXRControllerManager6dof>(); // this object can be assigned by Instance call too, depending on call order
            }

        }

        void Start() {
            Debug.Log("SXRControllerManager6dof Start.");
        }

        void Update() {
            foreach (ControllerInterface6dof controller in mControllers) {
                if (DEBUG_PER_FRAME) {
                    Debug.Log("SXRControllerManager6dof Update() type = " + controller.controllerId());
                }
            }
        }

        void OnDestroy() {
            if (sInstance != null) { //this) {
                Debug.Log("SXRControllerManager6dof OnDestroy. Was sInstance != null.");
                sInstance = null;
            }
        }

    }
}
