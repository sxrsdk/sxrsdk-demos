using UnityEngine;
using UnityEngine.VR;
using System.Runtime.InteropServices;
using System;
using System.Text;

namespace Sxr {

    /// <summary>
    /// An interface class to the sxr plugin lower modules, and this will do the roles
    /// including setting calls to the plugin lower and params queries from for upper Unity.
    /// This class can be changed anytime without backward compatibility, so the app developers are
    /// recommended not to call members in this class directly, but to use SXRConfiguration class.
    /// </summary>
    static class SXRConfigurationIF {

        private const string dllSxr = "sxr-plugin";

        [DllImport(dllSxr, CallingConvention = CallingConvention.Cdecl)]
        private static extern void PureSetRunMode(string mode, string param1);

        [DllImport(dllSxr)]
        private static extern void PureSetGridParameters(int mdistortion_grid_size, float mk0, float mk1, float mk2, float mk3, float mscale, float mfreeParam1, float mfreeparam2);

        [DllImport(dllSxr, CallingConvention = CallingConvention.Cdecl)]
        private static extern int PureInitializePluginRender(string xmlFile);

        [DllImport(dllSxr, CallingConvention = CallingConvention.Cdecl)]
        private static extern int PureStart();

        [DllImport(dllSxr, CallingConvention = CallingConvention.Cdecl)]
        private static extern int PureStop();

        [DllImport(dllSxr, CallingConvention = CallingConvention.Cdecl)]
        private static extern int PureBeforeDraw(int bufferIdx);

        [DllImport(dllSxr, CallingConvention = CallingConvention.Cdecl)]
        private static extern int PureStartExtCamera();

        [DllImport(dllSxr, CallingConvention = CallingConvention.Cdecl)]
        private static extern int PureStopExtCamera();

        [DllImport(dllSxr, CallingConvention = CallingConvention.Cdecl)]
        private static extern int PureGetExtCameraTextureID();

        [DllImport(dllSxr, CallingConvention = CallingConvention.Cdecl)]
        private static extern int PureQueryBufferCountStatus(long bufferCount);

        [DllImport(dllSxr, CallingConvention = CallingConvention.Cdecl)]
        private static extern int PureQueryPluginResumed();

        [DllImport(dllSxr, CallingConvention = CallingConvention.Cdecl)]
        private static extern void PureGetVersion(StringBuilder version, int len);

        [DllImport(dllSxr, CallingConvention = CallingConvention.Cdecl)]
        private static extern float PureGetFovY();

        [DllImport(dllSxr, CallingConvention = CallingConvention.Cdecl)]
        private static extern float PureGetEyeHeight();

        [DllImport(dllSxr, CallingConvention = CallingConvention.Cdecl)]
        private static extern float PureGetInterpupillaryDistance();

        [DllImport(dllSxr, CallingConvention = CallingConvention.Cdecl)]
        private static extern float PureGetHeadModelDepth();

        [DllImport(dllSxr, CallingConvention = CallingConvention.Cdecl)]
        private static extern float PureGetHeadModelHeight();

        [DllImport(dllSxr, CallingConvention = CallingConvention.Cdecl)]
        private static extern float PureGetAspectRatio();

        [DllImport(dllSxr, CallingConvention = CallingConvention.Cdecl)]
        private static extern int PureGetResolutionWidth();

        [DllImport(dllSxr, CallingConvention = CallingConvention.Cdecl)]
        private static extern int PureGetResolutionHeight();

        // -----------------------------------------------------------------------
        private const bool DEBUG_PER_FRAME = false;

        public const string ParamOn = "on";

        public const string ParamOff = "off";

        public const string FRMode = "frameRegulator";

        public const string ChromaticAberration = "chromatic";

        public const string Distortion = "distortion";

        public const string MonoMode = "monoMode";

        public const string MonoFullScreen = "monoFullScreen";

        public const string ExternalCamera = "externalCamera";

        // -----------------------------------------------------------------------
        public static void SetRunMode(string mode, string param1) {
            PureSetRunMode(mode, param1);
        }

        public static bool InitializePluginRender(string xmlFile) {
            return PureInitializePluginRender(xmlFile) > 0;
        }

        public static bool Start() {
            int value = PureStart();
            //Debug.Log("PureStart value=" + value);
            return value > 0;
        }

        public static bool Stop() {
            int value = PureStop();
            //Debug.Log("PureStop() value=" + value);
            return value > 0;
        }

        public static bool startExtCamera() {
            int value = PureStartExtCamera();
            //Debug.Log("PureStartExtCamera value=" + value);
            return value > 0;
        }

        public static bool stopExtCamera() {
            int value = PureStopExtCamera();
            //Debug.Log("PureStopExtCamera value=" + value);
            return value > 0;
        }

        public static int GetExtCameraNativeTextureID() {
            int value = PureGetExtCameraTextureID();
            //Debug.Log("GetExtCameraNativeTextureID value=" + value);
            return value;
        }

        public static bool BeforeDraw(int buffIdx) {
            int value = PureBeforeDraw(buffIdx);
            //Debug.Log("BeforeDraw() value=" + value);
            return value > 0;
        }

        public static bool QueryBufferCountStatus(long bufCnt) {
            int value = PureQueryBufferCountStatus(bufCnt);
            //Debug.Log("QueryBufferCountStatus value=" + value);
            return value > 0;
        }

        public static bool QueryPluginResumed() {
            int value = PureQueryPluginResumed();
            //Debug.Log("QueryPluginResumed value=" + value);
            return value > 0;
        }

        public static string GetVersion() {
            StringBuilder version = new StringBuilder(64);//max len
            PureGetVersion(version, version.Capacity);

            string value = version.ToString();
            //Debug.Log("GetVersion() value=" + value);

            return value;
        }

        public static float GetFovY() {
            float value = PureGetFovY();
            //Debug.Log("PureGetFovY value=" + value);
            return value;
        }

        public static float GetEyeHeight() {
            float value = PureGetEyeHeight();
            //Debug.Log("GetEyeHeight value=" + value);
            return value;
        }

        public static float GetInterpupillaryDistance() {
            float value = PureGetInterpupillaryDistance();
            //Debug.Log("GetGetInterpupillaryDistance value=" + value);
            return value;
        }

        public static float GetHeadModelDepth() {
            float value = PureGetHeadModelDepth();
            //Debug.Log("GetGetHeadModelDepth value=" + value);
            return value;
        }

        public static float GetHeadModelHeight() {
            float value = PureGetHeadModelHeight();
            //Debug.Log("GetGetHeadModelHeight value=" + value);
            return value;
        }

        public static float GetAspectRatio() {
            float value = PureGetAspectRatio();
            //Debug.Log("GetGetAspectRatio value=" + value);
            return value;
        }

        public static int GetResolutionWidth() {
            int value = PureGetResolutionWidth();
            Debug.Log("GetResolutionWidth value=" + value);
            return value;
        }

        public static int GetResolutionHeight() {
            int value = PureGetResolutionHeight();
            Debug.Log("GetResolutionHeight value=" + value);
            return value;
        }
    }
}
