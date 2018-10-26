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

package com.samsungxr.balloons;

import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRDirectLight;
import com.samsungxr.SXRMaterial;
import com.samsungxr.SXRScene;
import com.samsungxr.SXRSceneObject;
import com.samsungxr.SXRMain;
import com.samsungxr.SXRRenderData;
import com.samsungxr.SXRRenderData.SXRRenderingOrder;
import com.samsungxr.SXRShader;
import com.samsungxr.SXRSphereCollider;
import com.samsungxr.SXRTexture;
import com.samsungxr.scene_objects.SXRSphereSceneObject;
import android.view.MotionEvent;
import com.samsungxr.SXRPicker;
import com.samsungxr.IPickEvents;
import com.samsungxr.SXRPicker.SXRPickedObject;

public class BalloonMain extends SXRMain {

    public class PickHandler implements IPickEvents
    {
        public SXRSceneObject   PickedObject = null;

        public void onEnter(SXRSceneObject sceneObj, SXRPicker.SXRPickedObject pickInfo) { }
        public void onExit(SXRSceneObject sceneObj) { }
        public void onNoPick(SXRPicker picker)
        {
            if (PickedObject != null)
            {
                PickedObject.getRenderData().getMaterial().setDiffuseColor(1, 0, 0, 0.5f);
            }
            PickedObject = null;
        }
        public void onPick(SXRPicker picker)
        {
            SXRPickedObject picked = picker.getPicked()[0];
            PickedObject = picked.hitObject;
            PickedObject.getRenderData().getMaterial().setDiffuseColor(1, 0, 1, 0.5f);
        }

        public void onInside(SXRSceneObject sceneObj, SXRPicker.SXRPickedObject pickInfo) { }
    }

    private SXRScene mScene = null;
    private PickHandler mPickHandler;
    private SXRPicker mPicker;

    @Override
    public void onInit(SXRContext context) {
        /*
         * Set the background color
         */
        mScene = context.getMainScene();
        mScene.getMainCameraRig().getLeftCamera().setBackgroundColor(1.0f, 1.0f, 1.0f, 1.0f);
        mScene.getMainCameraRig().getRightCamera().setBackgroundColor(1.0f, 1.0f, 1.0f, 1.0f);

        /*
         * Set up a head-tracking pointer
         */
        SXRSceneObject headTracker = new SXRSceneObject(context,
                context.createQuad(0.1f, 0.1f),
                context.getAssetLoader().loadTexture(new SXRAndroidResource(context, R.drawable.headtrackingpointer)));
        headTracker.getTransform().setPosition(0.0f, 0.0f, -1.0f);
        headTracker.getRenderData().setDepthTest(false);
        headTracker.getRenderData().setRenderingOrder(100000);
        mScene.getMainCameraRig().addChildObject(headTracker);
        /*
         * Add the environment and a single balloon
         */
        SXRSceneObject balloon = makeBalloon(context);
        mScene.addSceneObject(balloon);
        SXRSceneObject environment = makeEnvironment(context);
        mScene.addSceneObject(environment);
        /*
         * Respond to picking events
         */
        mPickHandler = new PickHandler();
        mScene.getEventReceiver().addListener(mPickHandler);
        mPicker = new SXRPicker(context, mScene);
    }
    

    SXRSceneObject makeBalloon(SXRContext context)
    {
        SXRSceneObject sphere = new SXRSphereSceneObject(context, true);
        SXRRenderData rdata = sphere.getRenderData();
        SXRMaterial mtl = new SXRMaterial(context, SXRMaterial.SXRShaderType.Phong.ID);
        SXRSphereCollider collider = new SXRSphereCollider(context);

        collider.setRadius(1.0f);
        sphere.attachComponent(collider);
        mtl.setDiffuseColor(1.0f, 0.0f, 1.0f, 0.5f);
        sphere.setName("balloon");
        rdata.setAlphaBlend(true);
        rdata.setMaterial(mtl);
        rdata.setRenderingOrder(SXRRenderingOrder.TRANSPARENT);
        sphere.getTransform().setPositionZ(-3.0f);
        return sphere;
    }

    SXRSceneObject makeEnvironment(SXRContext context)
    {
        SXRTexture tex = context.getAssetLoader().loadCubemapTexture(new SXRAndroidResource(context, R.raw.lycksele3));
        SXRMaterial material = new SXRMaterial(context, SXRMaterial.SXRShaderType.Cubemap.ID);
        material.setMainTexture(tex);
        SXRSphereSceneObject environment = new SXRSphereSceneObject(context, 18, 36, false, material, 4, 4);
        environment.getTransform().setScale(20.0f, 20.0f, 20.0f);

        if (!SXRShader.isVulkanInstance())
        {
            SXRDirectLight sunLight = new SXRDirectLight(context);
            sunLight.setAmbientIntensity(0.4f, 0.4f, 0.4f, 1.0f);
            sunLight.setDiffuseIntensity(0.6f, 0.6f, 0.6f, 1.0f);
            environment.attachComponent(sunLight);
        }
        return environment;
    }

    @Override
    public void onStep() {
        FPSCounter.tick();
    }

    public void onTouchEvent(MotionEvent event)
    {
        switch (event.getAction() & MotionEvent.ACTION_MASK)
        {
            case MotionEvent.ACTION_DOWN:
                if (mPickHandler.PickedObject != null)
                {
                    mPickHandler.PickedObject.getRenderData().getMaterial().setDiffuseColor(0, 0, 1, 1);
                }
                break;

            default:
                break;
        }
    }

}
