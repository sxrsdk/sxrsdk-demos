using UnityEngine;
using System.Collections;

namespace Sxr {

    public interface SensorInterface {
        void setListener(SensorListener listener);
        void start();
        void stop();
        UnityEngine.Quaternion poll();

        void pause();
        void resume();
    }
}