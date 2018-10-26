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

package com.samsungxr.arcore.simplesample;

import android.graphics.Color;

import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRMaterial;
import com.samsungxr.SXRMesh;
import com.samsungxr.SXRPicker;
import com.samsungxr.SXRRenderData;
import com.samsungxr.SXRSceneObject;
import com.samsungxr.io.SXRCursorController;
import com.samsungxr.io.SXRInputManager;

import java.util.EnumSet;


public class SampleHelper {
    private SXRSceneObject mCursor;
    private SXRCursorController mCursorController;

    private int hsvHUE = 0;

    public SXRSceneObject createQuadPlane(SXRContext sxrContext) {
        SXRMesh mesh = SXRMesh.createQuad(sxrContext,
                "float3 a_position", 1.0f, 1.0f);

        SXRMaterial mat = new SXRMaterial(sxrContext, SXRMaterial.SXRShaderType.Phong.ID);

        SXRSceneObject polygonObject = new SXRSceneObject(sxrContext, mesh, mat);

        hsvHUE += 35;
        float[] hsv = new float[3];
        hsv[0] = hsvHUE % 360;
        hsv[1] = 1f; hsv[2] = 1f;

        int c =  Color.HSVToColor(50, hsv);
        mat.setDiffuseColor(Color.red(c) / 255f,Color.green(c) / 255f,
                Color.blue(c) / 255f, 0.2f);

        polygonObject.getRenderData().setMaterial(mat);
        polygonObject.getRenderData().setAlphaBlend(true);
        polygonObject.getTransform().setRotationByAxis(-90, 1, 0, 0);

        return polygonObject;
    }

    public void initCursorController(SXRContext sxrContext, final SampleMain.TouchHandler handler) {
        final int cursorDepth = 100;
        sxrContext.getMainScene().getEventReceiver().addListener(handler);
        SXRInputManager inputManager = sxrContext.getInputManager();
        mCursor = new SXRSceneObject(sxrContext,
                sxrContext.createQuad(0.2f * cursorDepth,
                        0.2f * cursorDepth),
                sxrContext.getAssetLoader().loadTexture(new SXRAndroidResource(sxrContext,
                        R.raw.cursor)));
        mCursor.getRenderData().setDepthTest(false);
        mCursor.getRenderData().setRenderingOrder(SXRRenderData.SXRRenderingOrder.OVERLAY);
        final EnumSet<SXRPicker.EventOptions> eventOptions = EnumSet.of(
                SXRPicker.EventOptions.SEND_TOUCH_EVENTS,
                SXRPicker.EventOptions.SEND_TO_LISTENERS);
        inputManager.selectController(new SXRInputManager.ICursorControllerSelectListener()
        {
            public void onCursorControllerSelected(SXRCursorController newController, SXRCursorController oldController)
            {
                if (oldController != null)
                {
                    oldController.removePickEventListener(handler);
                }
                mCursorController = newController;
                newController.addPickEventListener(handler);
                newController.setCursor(mCursor);
                newController.setCursorDepth(-cursorDepth);
                newController.setCursorControl(SXRCursorController.CursorControl.CURSOR_CONSTANT_DEPTH);
                newController.getPicker().setEventOptions(eventOptions);
            }
        });
    }

    SXRCursorController getCursorController() {
        return this.mCursorController;
    }
}
