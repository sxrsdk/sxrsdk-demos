using UnityEngine;
using System.Collections;

namespace Sxr {

    public class SXRConfiguration {

        private const bool DEBUG_PER_FRAME = false;

        private static SXRConfiguration mInstance;

        public static SXRConfiguration Instance {
            get {
                if (mInstance == null) {
                    mInstance = new SXRConfiguration();
                }
                return mInstance;
            }
        }

        private SXRConfiguration() {
            Debug.Log("SXRConfiguration constructed.");
        }

        /// <summary>
        /// Sets Anti Aliasing level for the app. This will override the default settings.
        /// </summary>
        public void SetAntiAliasing(int level) {
            //TODO:
        }

        /// <summary>
        /// Set chromatic aberration correction. The default is flag on.
        /// </summary>
        public void SetChromaticAberration(bool on) {
            SXRConfigurationIF.SetRunMode(SXRConfigurationIF.ChromaticAberration, (on) ? SXRConfigurationIF.ParamOn : SXRConfigurationIF.ParamOff);
        }

        /// <summary>
        /// Set frame rate regulating for SXR, called Frame Regulator or FR. The default is flag on.
        /// On FR mode, drawing and rendering happens asynchronously. Also, prediction is
        /// used to minimize motion-to-photon time. 
        /// This API here is to allow Unity app developers to change FR mode of the app, 
        /// depending on the objects binding to camera, so that his/her app can best fit in.
        /// </summary>
        public void SetFRMode(bool on) {
            SXRConfigurationIF.SetRunMode(SXRConfigurationIF.FRMode, (on) ? SXRConfigurationIF.ParamOn : SXRConfigurationIF.ParamOff);
        }

        /// <summary>
        /// Enable/Disable external camera (i.e. UVC camera) feature.
        /// </summary>
        public void SetExternalCameraMode(bool on) {
            SXRConfigurationIF.SetRunMode(SXRConfigurationIF.ExternalCamera, (on) ? SXRConfigurationIF.ParamOn : SXRConfigurationIF.ParamOff);
        }
    }
}
