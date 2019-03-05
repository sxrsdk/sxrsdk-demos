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

package com.samsungxr.multilight;

import java.io.IOException;

import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRMaterial;
import com.samsungxr.SXRRenderData;
import com.samsungxr.SXRScene;
import com.samsungxr.SXRNode;
import com.samsungxr.SXRMain;
import com.samsungxr.SXRSpotLight;
import com.samsungxr.SXRTexture;
import com.samsungxr.SXRTransform;
import com.samsungxr.animation.SXRAnimator;

import org.joml.Quaternionf;

import android.util.Log;
import android.view.MotionEvent;

public class MultiLightMain extends SXRMain {

    private static final float LIGHT_Z = 100.0f;
    private static final float LIGHT_ROTATE_RADIUS = 100.0f;
    private SXRContext mSXRContext;
    private SXRNode rotateObject;
    private SXRNode backdrop;
    private SXRScene mScene;

    @Override
    public void onInit(SXRContext sxrContext) {
        mSXRContext = sxrContext;
        mScene = mSXRContext.getMainScene();
        float zdist = 2.0f;

        SXRNode root = new SXRNode(sxrContext);
        SXRNode character = createCharacter(sxrContext);

        SXRNode light1 = createLight(sxrContext, 1, 0, 0, 0.8f);
        SXRNode light2 = createLight(sxrContext, 0, 1, 0, -0.8f);
        
        backdrop = createBackdrop(sxrContext);
        root.setName("root");
        root.getTransform().setPosition(0, 0, -zdist);
        mScene.addNode(root);
        root.addChildObject(backdrop);
        root.addChildObject(light1);
        root.addChildObject(light2);
        root.addChildObject(character);
        rotateObject = light1;
    }

    private double theta = 0;

    @Override
    public void onStep() {
        FPSCounter.tick();
        theta += 0.005;
        if (theta >= Math.PI / 4)
            theta = -Math.PI / 4;
        if (rotateObject != null) {
            Quaternionf q = new Quaternionf();
            q.rotateAxis((float) theta, 0.0f, 1.0f, 0.0f);
            SXRTransform trans = rotateObject.getTransform();
            trans.setRotation(q.w, q.x, q.y, q.z);
        }
    }

    private boolean lightEnabled = false;

    public void onTouchEvent(MotionEvent event) {
        if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
            SXRRenderData rdata = backdrop.getRenderData();
            if (lightEnabled)
                rdata.disableLight();
            else
                rdata.enableLight();
            lightEnabled = !lightEnabled;
        }
    }
    
    /*
     * Load in a model of a little guy
     */
    private SXRNode createCharacter(SXRContext context)
    {
         try
         {
            SXRNode model = context.getAssetLoader().loadModel("astro_boy.dae");

            model.getTransform().setScale(10, 10, 10);
            model.getTransform().setPositionY(-1);
            return model;
         }
         catch (IOException e)
         {
            Log.e("multilight", "Failed to load a model: %s", e);
            return null;
         }
    }
    
    /*
     * Creates a  spot light in front of the character
     * pointing straight at it.
     */
    private SXRNode createLight(SXRContext context, float r, float g, float b, float y)
    {
        SXRNode lightNode = new SXRNode(context);
        SXRSpotLight light = new SXRSpotLight(context);
        Quaternionf q = new Quaternionf();
        
        lightNode.attachLight(light);         
        lightNode.getTransform().setPosition(0, y, 3);
        light.setAmbientIntensity(0.3f * r, 0.3f * g, 0.3f * b, 1);
        light.setDiffuseIntensity(r, g, b, 1);
        light.setSpecularIntensity(r, g, b, 1);
        light.setInnerConeAngle(8);
        light.setOuterConeAngle(12);
        return lightNode;
    }
    
    /*
     * Create a backdrop with the GearVRF logo and enable
     * multiple lighting support by choosing the SXRPhongShader template.
     * The multiple light shader uses the name "diffuseTexture" instead
     * of the name "main_texture".
     */
    private SXRNode createBackdrop(SXRContext context)
    {
        SXRTexture tex = context.getAssetLoader().loadTexture(new SXRAndroidResource(mSXRContext, R.drawable.samsung_xr_512x128));
        SXRNode backdrop = new SXRNode(context, 10.0f, 4.0f, tex);
        SXRRenderData rdata = backdrop.getRenderData();
        SXRMaterial material = new SXRMaterial(context, SXRMaterial.SXRShaderType.Phong.ID);
        
        material.setVec4("diffuse_color", 0.8f, 0.8f, 0.8f, 1.0f);
        material.setVec4("ambient_color", 0.3f, 0.3f, 0.3f, 1.0f);
        material.setVec4("specular_color", 1.0f, 1.0f, 1.0f, 1.0f);
        material.setVec4("emissive_color", 0.0f, 0.0f, 0.0f, 0.0f);
        material.setFloat("specular_exponent", 10.0f);
        material.setTexture("diffuseTexture", tex);
        backdrop.setName("Backdrop");
        backdrop.getTransform().setPositionZ(-2.0f);
        rdata.setMaterial(material);
    	return backdrop;
    }

}
