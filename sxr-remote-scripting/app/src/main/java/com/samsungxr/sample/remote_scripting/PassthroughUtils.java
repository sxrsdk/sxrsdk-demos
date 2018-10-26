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

package com.samsungxr.sample.remote_scripting;

import java.lang.Runnable;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRScene;
import com.samsungxr.SXRRenderData.SXRRenderingOrder;
import com.samsungxr.nodes.SXRCameraNode;
import android.hardware.Camera;
import android.os.Handler;

public class PassthroughUtils {
    private SXRContext sxrContext;
    private SXRCameraNode cameraObject;
    private GearVRScripting activity;
    private Camera camera;
    private boolean previewStarted = false;
    private Handler handler;

    public PassthroughUtils(SXRContext context, GearVRScripting activity) {
        sxrContext = context;
        this.activity = activity;
        camera = activity.getCamera();
        handler = activity.getHandler();
    }

    public void show() {
        activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    camera.startPreview();
                    previewStarted = true;
                }
            });

        handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    sxrContext.runOnGlThread(createCameraObject);
                }
            }, 100);
    }

    private float PASSTHROUGH_ASPECT = 16.0f / 9.0f;
    private float PASSTHROUGH_HEIGHT = 24.36f;
    private float PASSTHROUGH_WIDTH = PASSTHROUGH_HEIGHT * PASSTHROUGH_ASPECT;
    private float PASSTHROUGH_Z = -45.0f;

    private Runnable createCameraObject = new Runnable() {
        @Override
        public void run() {
            if(cameraObject == null) {
                cameraObject = new SXRCameraNode(sxrContext, PASSTHROUGH_WIDTH, PASSTHROUGH_HEIGHT, camera);
                cameraObject.setUpCameraForVrMode(1); // set up 60 fps camera preview.
                cameraObject.getTransform().setPosition(0.0f, 0.0f, PASSTHROUGH_Z);
                cameraObject.getRenderData().setRenderingOrder(SXRRenderingOrder.BACKGROUND);
                cameraObject.setName("passthrough");
            }
            sxrContext.getMainScene().getMainCameraRig().addChildObject(cameraObject);
        }
    };

    public void hide() {
        activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    camera.stopPreview();
                    previewStarted = false;
                }
            });

        sxrContext.runOnGlThread(new Runnable() {
                @Override
                public void run() {
                    if(cameraObject != null) {
                        sxrContext.getMainScene().removeNode(cameraObject);
                    }
                }
            });
    }

}
