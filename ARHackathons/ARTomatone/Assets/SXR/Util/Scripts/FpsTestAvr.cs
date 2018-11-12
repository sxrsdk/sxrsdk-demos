using UnityEngine;
using System.Collections;
using UnityEngine.UI;

namespace Sxr {

    public class FpsTestAvr : MonoBehaviour {
        private const bool DEBUG_PER_FRAME = false;
        Text text;

        public float updateInterval = 0.5F;
        private double lastInterval;
        private int frames = 0;
        private float fps;
        private float timeLast;

        // Use this for initialization
        void Start() {
            text = GetComponent<Text>();

            lastInterval = Time.realtimeSinceStartup;
            frames = 0;
        }

        // Update is called once per frame
        void Update() {
            ++frames;
            float timeNow = Time.realtimeSinceStartup;// The real time in seconds since the game started (Read Only).
            if (DEBUG_PER_FRAME) {
                Debug.Log("comp Time.deltaTime = " + Time.deltaTime // deltaTime(): The time in seconds it took to complete the last frame (Read Only).
                    + " interval (timeNow - timeLast) = " + (timeNow - timeLast) // actual time including any blocking from 
                    );
                timeLast = timeNow;
            }

            if (timeNow > lastInterval + updateInterval) {

                fps = (float)(frames / (timeNow - lastInterval));
                if (DEBUG_PER_FRAME) {
                    Debug.Log("frames = " + frames + " (timeNow - lastInterval) = " + (timeNow - lastInterval) + " fps = " + fps);
                }
                frames = 0;
                lastInterval = timeNow;

                if (text != null) {
                    text.text = "" + (int)fps;
                }
                string strText = "" + (int)fps;

                Debug.Log("HMD: " + ((SXRManager.Instance.getFrameRegulator().getTransform() == null) ? false : (SXRManager.Instance.getFrameRegulator().getTransform().rotation.x != 0.0F))//HMD data is coming
                    + ", using UnityPlugin FPS is: " + strText);
            }

        }
    }
}
