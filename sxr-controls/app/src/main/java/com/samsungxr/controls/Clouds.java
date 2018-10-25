/* Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.samsungxr.controls;

import android.content.res.Resources;
import android.content.res.TypedArray;

import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRMesh;
import com.samsungxr.SXRSceneObject;
import com.samsungxr.animation.SXRAnimation;
import com.samsungxr.animation.SXRRepeatMode;
import com.samsungxr.animation.SXRRotationByAxisWithPivotAnimation;
import com.samsungxr.controls.model.Cloud;

public class Clouds extends SXRSceneObject {

    public Cloud[] clouds;
    private final int NUMBER_OF_CLOUDS = 4;
    private final int FULL_ROTATION = 360;

    private final int CLOUD_ROTATION_DURATION = 1800;

    public Clouds(SXRContext gvrContext, float cloudDistance, int numberOfClouds) {
        super(gvrContext);

        SXRMesh mesh = gvrContext.getAssetLoader().loadMesh(
                new SXRAndroidResource(gvrContext, R.raw.cloud_mesh));

        clouds = new Cloud[numberOfClouds];
        Resources res = gvrContext.getContext().getResources();
        TypedArray cloudArray = res.obtainTypedArray(R.array.clouds);
        TypedArray cloudTypeValues;

        for (int i = 0; i < numberOfClouds; i++) {
            float angle = FULL_ROTATION / numberOfClouds;
            int currentCloudIndex = i % NUMBER_OF_CLOUDS;

            cloudTypeValues = res.obtainTypedArray(cloudArray.getResourceId(currentCloudIndex, 0));

            clouds[i] = new Cloud(gvrContext, mesh, -cloudDistance, cloudTypeValues, angle * i);

            gvrContext.getMainScene().addSceneObject(clouds[i]);

            rotateCloudsAroundCameraAnimation(gvrContext, clouds[i]);

        }

    }

    private void rotateCloudsAroundCameraAnimation(SXRContext gvrContext, Cloud cloud) {
        SXRAnimation anim = new SXRRotationByAxisWithPivotAnimation(
                cloud, CLOUD_ROTATION_DURATION, FULL_ROTATION, 0, 1, 0, 0, 0, 0);
        anim.setRepeatCount(-1);
        anim.setRepeatMode(SXRRepeatMode.REPEATED);
        anim.start(gvrContext.getAnimationEngine());
    }
}
