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

package com.samsungxr.aravatar;

import android.graphics.Color;
import android.util.Log;

import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRDirectLight;
import com.samsungxr.SXRLight;
import com.samsungxr.SXRMaterial;
import com.samsungxr.SXRMesh;
import com.samsungxr.SXRPicker;
import com.samsungxr.SXRRenderData;
import com.samsungxr.SXRNode;
import com.samsungxr.ITouchEvents;
import com.samsungxr.animation.SXRAnimation;
import com.samsungxr.animation.SXRAnimator;
import com.samsungxr.animation.SXRAvatar;
import com.samsungxr.animation.SXRRepeatMode;
import com.samsungxr.io.SXRCursorController;
import com.samsungxr.io.SXRGazeCursorController;
import com.samsungxr.io.SXRInputManager;
import org.joml.Vector4f;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;


public class SceneUtils
{
    private Vector4f[] mColors;
    private int mPlaneIndex = 0;

    SceneUtils()
    {
        mColors = new Vector4f[]
        {
            new Vector4f(1, 0, 0, 0.2f),
            new Vector4f(0, 1, 0, 0.2f),
            new Vector4f(0, 0, 1, 0.2f),
            new Vector4f(1, 0, 1, 0.2f),
            new Vector4f(0, 1, 1, 0.2f),
            new Vector4f(1, 1, 0, 0.2f),
            new Vector4f(1, 1, 1, 0.2f),

            new Vector4f(1, 0, 0.5f, 0.2f),
            new Vector4f(0, 0.5f, 0, 0.2f),
            new Vector4f(0, 0, 0.5f, 0.2f),
            new Vector4f(1, 0, 0.5f, 0.2f),
            new Vector4f(0, 1, 0.5f, 0.2f),
            new Vector4f( 1, 0.5f, 0,0.2f),
            new Vector4f( 1, 0.5f, 1,0.2f),

            new Vector4f(0.5f, 0, 1, 0.2f),
            new Vector4f(0.5f, 0, 1, 0.2f),
            new Vector4f(0, 0.5f, 1, 0.2f),
            new Vector4f( 0.5f, 1, 0,0.2f),
            new Vector4f( 0.5f, 1, 1,0.2f),
            new Vector4f( 1, 1, 0.5f, 0.2f),
            new Vector4f( 1, 0.5f, 0.5f, 0.2f),
            new Vector4f( 0.5f, 0.5f, 1, 0.2f),
            new Vector4f( 0.5f, 1, 0.5f, 0.2f),
       };
    }

    public SXRNode createPlane(SXRContext SXRContext)
    {
        SXRNode plane = new SXRNode(SXRContext);
        SXRMesh mesh = SXRMesh.createQuad(SXRContext,
                "float3 a_position", 1.0f, 1.0f);
        SXRMaterial mat = new SXRMaterial(SXRContext, SXRMaterial.SXRShaderType.Phong.ID);
        SXRNode polygonObject = new SXRNode(SXRContext, mesh, mat);
        Vector4f color = mColors[mPlaneIndex % mColors.length];

        plane.setName("Plane" + mPlaneIndex);
        polygonObject.setName("PlaneGeometry" + mPlaneIndex);
        mPlaneIndex++;
        mat.setDiffuseColor(color.x, color.y, color.x, color.w);
        polygonObject.getRenderData().setAlphaBlend(true);
        polygonObject.getRenderData().setRenderingOrder(SXRRenderData.SXRRenderingOrder.OVERLAY);
        polygonObject.getRenderData().disableLight();
        polygonObject.getTransform().setRotationByAxis(-90, 1, 0, 0);
        plane.addChildObject(polygonObject);
        return plane;
    }

    public SXRDirectLight makeSceneLight(SXRContext ctx)
    {
        SXRNode lightOwner = new SXRNode(ctx);
        SXRDirectLight light = new SXRDirectLight(ctx);

        lightOwner.setName("SceneLight");
        light.setAmbientIntensity(0.2f, 0.2f, 0.2f, 1);
        light.setDiffuseIntensity(0.2f, 0.2f, 0.2f, 1);
        light.setSpecularIntensity(0.2f, 0.2f, 0.2f, 1);
        lightOwner.attachComponent(light);
        return light;
    }

    public void initCursorController(SXRContext SXRContext, final ITouchEvents handler, final float screenDepth)
    {
        final int cursorDepth = 10;
        SXRInputManager inputManager = SXRContext.getInputManager();
        final EnumSet<SXRPicker.EventOptions> eventOptions = EnumSet.of(
                SXRPicker.EventOptions.SEND_TOUCH_EVENTS,
                SXRPicker.EventOptions.SEND_TO_LISTENERS,
                SXRPicker.EventOptions.SEND_TO_HIT_OBJECT);
        inputManager.selectController(new SXRInputManager.ICursorControllerSelectListener()
        {
            public void onCursorControllerSelected(SXRCursorController newController, SXRCursorController oldController)
            {
                if (oldController != null)
                {
                    oldController.removePickEventListener(handler);
                }
                newController.addPickEventListener(handler);
                newController.setCursorDepth(cursorDepth);
                newController.setCursorControl(SXRCursorController.CursorControl.PROJECT_CURSOR_ON_SURFACE);
                newController.getPicker().setEventOptions(eventOptions);
                if (newController instanceof SXRGazeCursorController)
                {
                    ((SXRGazeCursorController) newController).setTouchScreenDepth(screenDepth);
                    newController.setCursor(null);
                }
            }
        });
    }

}
