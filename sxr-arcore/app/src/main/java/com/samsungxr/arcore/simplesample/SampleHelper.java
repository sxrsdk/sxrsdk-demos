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

import android.opengl.GLES30;

import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRMaterial;
import com.samsungxr.SXRPicker;
import com.samsungxr.SXRRenderData;
import com.samsungxr.SXRNode;
import com.samsungxr.ITouchEvents;
import com.samsungxr.io.SXRCursorController;
import com.samsungxr.io.SXRGazeCursorController;
import com.samsungxr.io.SXRInputManager;
import org.joml.Vector4f;

import java.util.EnumSet;


public class SampleHelper {
    private SXRNode mCursor;
    private SXRCursorController mCursorController;

    private Vector4f[] mColors;
    private int mPlaneIndex = 0;

    SampleHelper()
    {
        mColors = new Vector4f[]
        {
            new Vector4f(1, 0, 0, 0.3f),
            new Vector4f(0, 1, 0, 0.3f),
            new Vector4f(0, 0, 1, 0.3f),
            new Vector4f(1, 0, 1, 0.3f),
            new Vector4f(0, 1, 1, 0.3f),
            new Vector4f(1, 1, 0, 0.3f),
            new Vector4f(1, 1, 1, 0.3f),

            new Vector4f(1, 0, 0.5f, 0.3f),
            new Vector4f(0, 0.5f, 0, 0.3f),
            new Vector4f(0, 0, 0.5f, 0.3f),
            new Vector4f(1, 0, 0.5f, 0.3f),
            new Vector4f(0, 1, 0.5f, 0.3f),
            new Vector4f( 1, 0.5f, 0,0.3f),
            new Vector4f( 1, 0.5f, 1,0.3f),

            new Vector4f(0.5f, 0, 1, 0.3f),
            new Vector4f(0.5f, 0, 1, 0.3f),
            new Vector4f(0, 0.5f, 1, 0.3f),
            new Vector4f( 0.5f, 1, 0,0.3f),
            new Vector4f( 0.5f, 1, 1,0.3f),
            new Vector4f( 1, 1, 0.5f, 0.3f),
            new Vector4f( 1, 0.5f, 0.5f, 0.3f),
            new Vector4f( 0.5f, 0.5f, 1, 0.3f),
            new Vector4f( 0.5f, 1, 0.5f, 0.3f),
       };
    }

    public SXRNode createPlaneNode(SXRContext sxrContext)
    {
        Vector4f color = mColors[mPlaneIndex % mColors.length];
        SXRMaterial mat = new SXRMaterial(sxrContext, SXRMaterial.SXRShaderType.Phong.ID);
        mat.setDiffuseColor(color.x, color.y, color.x, color.w);

        SXRRenderData renderData = new SXRRenderData(sxrContext);
        renderData.disableLight();
        renderData.setAlphaBlend(true);
        renderData.setRenderingOrder(SXRRenderData.SXRRenderingOrder.TRANSPARENT);
        renderData.setDrawMode(GLES30.GL_TRIANGLE_FAN);
        renderData.setMaterial(mat);

        SXRNode plane = new SXRNode(sxrContext);
        plane.attachComponent(renderData);
        plane.setName("Plane" + mPlaneIndex);
        mPlaneIndex++;
        return plane;
    }

    public void initCursorController(SXRContext SXRContext, final ITouchEvents handler, final float displayDepth)
    {
        final float cursorDepth = 100.0f;
        SXRContext.getMainScene().getEventReceiver().addListener(handler);
        SXRInputManager inputManager = SXRContext.getInputManager();
        mCursor = new SXRNode(SXRContext,
                SXRContext.createQuad(0.2f * cursorDepth,
                        0.2f * cursorDepth),
                SXRContext.getAssetLoader().loadTexture(new SXRAndroidResource(SXRContext,
                        R.raw.cursor)));
        mCursor.getRenderData().setDepthTest(false);
        mCursor.getRenderData().disableLight();
        mCursor.getRenderData().setRenderingOrder(SXRRenderData.SXRRenderingOrder.OVERLAY);
        final EnumSet<SXRPicker.EventOptions> eventOptions = EnumSet.of(
                SXRPicker.EventOptions.SEND_TOUCH_EVENTS,
                SXRPicker.EventOptions.SEND_TO_HIT_OBJECT,
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
                if (newController instanceof SXRGazeCursorController)
                {
                    ((SXRGazeCursorController) newController).setTouchScreenDepth(displayDepth);
                }
                newController.setCursor(mCursor);
                newController.getPicker().setPickClosest(false);
                newController.setCursorDepth(cursorDepth);
                newController.setCursorControl(SXRCursorController.CursorControl.CURSOR_CONSTANT_DEPTH);
                newController.getPicker().setEventOptions(eventOptions);
            }
        });
    }

    SXRCursorController getCursorController() {
        return this.mCursorController;
    }
}
