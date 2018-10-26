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
import com.samsungxr.SXRNode;
import com.samsungxr.SXRMain;
import com.samsungxr.SXRRenderData;
import com.samsungxr.SXRRenderData.SXRRenderingOrder;
import com.samsungxr.SXRShader;
import com.samsungxr.SXRTexture;
import com.samsungxr.nodes.SXRSphereNode;
import android.view.MotionEvent;

public class BalloonMain extends SXRMain {
    private SXRScene mScene = null;

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
        SXRNode headTracker = new SXRNode(context,
                context.createQuad(0.1f, 0.1f),
                context.getAssetLoader().loadTexture(new SXRAndroidResource(context, R.drawable.headtrackingpointer)));
        headTracker.getTransform().setPosition(0.0f, 0.0f, -1.0f);
        headTracker.getRenderData().setDepthTest(false);
        headTracker.getRenderData().setRenderingOrder(100000);
        mScene.getMainCameraRig().addChildObject(headTracker);
        /*
         * Add the environment and a single balloon
         */
        SXRNode balloon = makeBalloon(context);
        mScene.addNode(balloon);
        SXRNode environment = makeEnvironment(context);
        mScene.addNode(environment);
    }
    

    SXRNode makeBalloon(SXRContext context)
    {
        SXRNode sphere = new SXRSphereNode(context, true);
        SXRRenderData rdata = sphere.getRenderData();
        SXRMaterial mtl = new SXRMaterial(context, SXRMaterial.SXRShaderType.Phong.ID);
        mtl.setDiffuseColor(1.0f, 0.0f, 1.0f, 0.5f);
        sphere.setName("balloon");
        rdata.setAlphaBlend(true);
        rdata.setMaterial(mtl);
        rdata.setRenderingOrder(SXRRenderingOrder.TRANSPARENT);
        sphere.getTransform().setPositionZ(-3.0f);
        return sphere;
    }

    SXRNode makeEnvironment(SXRContext context)
    {
        SXRTexture tex = context.getAssetLoader().loadCubemapTexture(new SXRAndroidResource(context, R.raw.lycksele3));
        SXRMaterial material = new SXRMaterial(context, SXRMaterial.SXRShaderType.Cubemap.ID);
        material.setMainTexture(tex);
        SXRSphereNode environment = new SXRSphereNode(context, 18, 36, false, material, 4, 4);
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
    }

}
