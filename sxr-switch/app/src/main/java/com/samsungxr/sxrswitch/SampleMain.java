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

package com.samsungxr.sxrswitch;

import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRDirectLight;
import com.samsungxr.SXRMaterial;
import com.samsungxr.SXRScene;
import com.samsungxr.SXRNode;
import com.samsungxr.SXRMain;
import com.samsungxr.SXRShader;
import com.samsungxr.SXRSwitch;
import com.samsungxr.SXRTexture;
import com.samsungxr.nodes.SXRConeNode;
import com.samsungxr.nodes.SXRCubeNode;
import com.samsungxr.nodes.SXRCylinderNode;
import com.samsungxr.nodes.SXRSphereNode;

import android.util.Log;

public class SampleMain extends SXRMain {

    private SXRContext mSXRContext = null;
    private SXRScene scene = null;
    private SXRSwitch mSwitchNode;
    private Integer mSelectedIndex = 0;
    private int mMaxIndex = 0;
    private int counter = 0;
    
    @Override
    public void onInit(SXRContext sxrContext)
    {
        mSXRContext = sxrContext;

        scene = mSXRContext.getMainScene();

        /*
         * Add a head tracking pointer to the scene
         */
        SXRTexture texture = sxrContext.getAssetLoader().loadTexture(
                new SXRAndroidResource(mSXRContext, R.drawable.headtrackingpointer));
        SXRNode headTracker = new SXRNode(sxrContext, 0.1f, 0.1f, texture);
        headTracker.getTransform().setPosition(0.0f, 0.0f, -1.0f);
        headTracker.getRenderData().setDepthTest(false);
        headTracker.getRenderData().setRenderingOrder(100000);
        scene.getMainCameraRig().addChildObject(headTracker);
        /*
         * Add a light to the scene that looks down the negative Z axis
         */
        if (!SXRShader.isVulkanInstance())
        {
            SXRNode lightObj = new SXRNode(sxrContext);
            SXRDirectLight light = new SXRDirectLight(sxrContext);

            lightObj.getTransform().setPositionZ(2.0f);
            lightObj.attachComponent(light);
            scene.addNode(lightObj);
        }
        /*
         * Add a root node with four geometric shapes as children
         */
        SXRMaterial red = new SXRMaterial(sxrContext, SXRMaterial.SXRShaderType.Phong.ID);
        SXRMaterial blue = new SXRMaterial(sxrContext, SXRMaterial.SXRShaderType.Phong.ID);
        SXRNode root = new SXRNode(sxrContext);
        SXRCubeNode cube = new SXRCubeNode(sxrContext, true, red);
        SXRSphereNode sphere = new SXRSphereNode(sxrContext, true, blue);
        SXRCylinderNode cylinder = new SXRCylinderNode(sxrContext, true, red);
        SXRConeNode cone = new SXRConeNode(sxrContext, true, blue);
        
        mMaxIndex = 3;
        red.setDiffuseColor(1,  0,  0, 1);
        blue.setDiffuseColor(0, 0,  1, 1);
        cube.setName("cube");
        sphere.setName("sphere");
        cylinder.setName("cylinder");
        cone.setName("cone");
        root.addChildObject(cube);
        root.addChildObject(sphere);
        root.addChildObject(cylinder);
        root.addChildObject(cone);
        root.getTransform().setPositionZ(-5.0f);
        mSwitchNode = new SXRSwitch(sxrContext);
        root.attachComponent(mSwitchNode);
        scene.addNode(root);
    }


    public void onStep() {
        counter++;
        if(counter > 120) {
            mSelectedIndex++;
            if (mSelectedIndex > 3) {
                mSelectedIndex = 0;
            }
            Log.d("ASD","Set Switch Index:" + mSelectedIndex);
            mSwitchNode.setSwitchIndex(mSelectedIndex);
            counter = 0;
        }
    }

}
