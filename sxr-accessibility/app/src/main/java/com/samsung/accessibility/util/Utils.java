/*
 * Copyright 2015 Samsung Electronics Co., LTD
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package com.samsung.accessibility.util;

import com.samsungxr.SXRCameraRig;
import com.samsungxr.SXRSceneObject;

public class Utils {

    public static double distance(SXRSceneObject object, SXRCameraRig sxrCameraRig) {
        return Math.sqrt(Math.pow(object.getTransform().getPositionX()
                - sxrCameraRig.getTransform().getPositionX(), 2)
                +
                Math.pow(object.getTransform().getPositionY()
                        - sxrCameraRig.getTransform().getPositionY(), 2)
                +
                Math.pow(object.getTransform().getPositionZ()
                        - sxrCameraRig.getTransform().getPositionZ(), 2));

    }

    public static float[] calculatePointBetweenTwoObjects(SXRSceneObject object,
            SXRCameraRig sxrCameraRig, float desiredDistance) {
        float[] point = new float[3];
        float ratio = desiredDistance / (float) distance(object, sxrCameraRig);
        point[0] = (1 - ratio) * sxrCameraRig.getTransform().getPositionX() + (ratio)
                * object.getTransform().getPositionX();
        point[1] = (1 - ratio) * sxrCameraRig.getTransform().getPositionY() + (ratio)
                * object.getTransform().getPositionY();
        point[2] = (1 - ratio) * sxrCameraRig.getTransform().getPositionZ() + (ratio)
                * object.getTransform().getPositionZ();

        return point;
    }
}
