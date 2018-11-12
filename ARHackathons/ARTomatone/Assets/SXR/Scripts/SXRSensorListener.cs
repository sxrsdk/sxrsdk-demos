using UnityEngine;
using System.Collections;

namespace Sxr {

    public interface SensorListener {
        void onConnected(SensorInterface sensor);
        void onDisconnected(SensorInterface sensor);
        void onNewData(SensorInterface sensor, float w, float x, float y, float z, float gyroX, float gyroY, float gyroZ, long time);
    };
}
