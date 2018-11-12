using UnityEngine;
using System.Collections;
using System.Collections.Generic;
using System.Runtime.InteropServices;

namespace Sxr {

    public class SensorManager {
        private const bool DEBUG_PER_FRAME = false;

        private static SensorManager sInstance;

        private List<SensorInterface> mSensors;

        SXRFrameRegulator fr;// = null;

        public struct SxrPose {
            public long time_stamp;
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
        }

        private SensorManager() {
            // Initialize all sensors
            mSensors = new List<SensorInterface>();
        }

        private void addSensor(SensorInterface sensor) {
        }

        public static SensorManager Instance {
            get {
                if (sInstance == null) {
                    sInstance = new SensorManager();
                }
                return sInstance;
            }
        }

        public void onConnected(SensorInterface sensor) {
        }

        public void onDisconnected(SensorInterface sensor) {
        }

        public void pause() {
            foreach (SensorInterface s in mSensors) {
                s.pause();
            }
        }

        public void resume() {
            foreach (SensorInterface s in mSensors) {
                s.resume();
            }
        }

        // Polls data from internal sensor
        public SensorManager.SxrPose getDrawPose() {
            if (fr == null) {
                SXRManager sxrm = SXRManager.Instance;
                fr = sxrm.getFrameRegulator();
            }

            // fetch sensor data from plugin native
            SensorManager.SxrPose pose = SXRSensorPluginIF.GetSensorDrawPose(fr.getBufferIdx());

            return pose;

        }

        public bool isValid(float value) {
            return value != double.NaN && double.PositiveInfinity != value && double.NegativeInfinity != value;
        }

        public static UnityEngine.Quaternion toCamera(UnityEngine.Quaternion raw) {
            UnityEngine.Quaternion conv = new UnityEngine.Quaternion();

            // Left-handed
            conv.w = -raw.w;
            conv.x = raw.x;
            conv.y = raw.y;
            conv.z = -raw.z;
            return conv;
        }

        public static UnityEngine.Vector3 toCameraPos(float x, float y, float z) {
            UnityEngine.Vector3 conv = new UnityEngine.Vector3();

            // Left-handed
            conv.x = x;
            conv.y = y;
            conv.z = -z;
            return conv;
        }

        public float w = 0.0f;
        public float x = 0.0f;
        public float y = 0.0f;
        public float z = 0.0f;

        public float gyroX = 0.0f;
        public float gyroY = 0.0f;
        public float gyroZ = 0.0f;

        public long time = 0;
    }
}