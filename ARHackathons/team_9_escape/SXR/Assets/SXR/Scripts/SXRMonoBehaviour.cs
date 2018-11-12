using UnityEngine;
using System.Collections;

namespace Sxr {

    public abstract class SXRMonoBehaviour<T> : MonoBehaviour
    where T : MonoBehaviour {
        #region members
        private static T instance;
        private static bool lockCamera = false;
        public static bool LockCamera { get { return lockCamera; } set { lockCamera = value; } }
        #endregion members

        #region properties
        public static T Instance {
            get {
                if (instance == null) {
                    var inScene = GameObject.FindObjectOfType<T>();

                    if (inScene != null) {
                        instance = inScene;
                    }
                }

                return instance;
            }
        }
        #endregion properties

        #region constructors
        protected virtual void Awake() {
            if (instance != null && instance != this) {
                Debug.LogFormat("{0} is being destroyed because a singelton reference was already set!", this.name);
                Destroy(this);
                return;
            }

            //If we got here then we are the singelton. Get the reference to the component.
            instance = this.GetComponent<T>();
        }
        #endregion construcors


        #region methods
        protected virtual void Start() {
        }
        protected virtual void OnDestroy() {
            if (instance == this) {
                Debug.LogFormat("{0} is being destroyed !", gameObject.FullPath());
                instance = null;
            }
        }

        protected virtual void OnApplicationQuit() {
            if (instance != this) {
                return;
            }
        }
        #endregion methods
    }
}