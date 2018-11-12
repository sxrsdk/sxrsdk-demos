using UnityEngine;
using System.Runtime.InteropServices;

namespace Sxr {

    public static class SXRSensorPluginIF {

        [StructLayout(LayoutKind.Sequential)]
        struct SxrPoseRaw {
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

        // -----------------------------------------------------------------------
        private const string dllSxr = "sxr-plugin";

        [DllImport(dllSxr)]
        private static extern SxrPoseRaw PureGetSensorDrawPose(int bufferIdx);

        private const bool DEBUG_PER_FRAME = false;

        public static SensorManager.SxrPose GetSensorDrawPose(int bufferIdx) {
            SxrPoseRaw poseRaw = PureGetSensorDrawPose(bufferIdx);
            SensorManager.SxrPose pose = new SensorManager.SxrPose();

            if (DEBUG_PER_FRAME) {
                Debug.LogFormat("GetSensorDrawPose time_stamp:{0} ", poseRaw.time_stamp);
                Debug.LogFormat("GetSensorDrawPose quat:{0} {1} {2} {3} ",
                   poseRaw.x,
                   poseRaw.y,
                   poseRaw.z,
                   poseRaw.w);
                Debug.LogFormat("GetSensorDrawPose gyro: {0} {1} {2}",
                    poseRaw.gyro_x, poseRaw.gyro_y, poseRaw.gyro_z);
            }

            pose.time_stamp = poseRaw.time_stamp;
            pose.x = poseRaw.x;
            pose.y = poseRaw.y;
            pose.z = poseRaw.z;
            pose.w = poseRaw.w;

            pose.gyro_x = poseRaw.gyro_x;
            pose.gyro_y = poseRaw.gyro_y;
            pose.gyro_z = poseRaw.gyro_z;

            pose.p_x = poseRaw.p_x;
            pose.p_y = poseRaw.p_y;
            pose.p_z = poseRaw.p_z;

            return pose;
        }
    }
}
