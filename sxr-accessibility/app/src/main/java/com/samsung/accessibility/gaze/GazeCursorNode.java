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
package com.samsung.accessibility.gaze;

import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRMaterial;
import com.samsungxr.SXRMesh;
import com.samsungxr.SXRPerspectiveCamera;
import com.samsungxr.SXRRenderData;
import com.samsungxr.SXRRenderData.SXRRenderMaskBit;
import com.samsungxr.SXRNode;

import com.samsung.accessibility.R;

public class GazeCursorNode extends SXRNode {

    private static final float NEAR_CLIPPING_OFFSET = 0.00001f;
    private static final float DEFAULT_NEAR_CLIPPING_DISTANCE = 0.1f;
    private static final float NORMAL_CURSOR_SIZE = 0.5f;
    private static final int CURSOR_RENDER_ORDER = 100000;

    private SXRNode rightCursor;
    private SXRNode leftCursor;
    private static GazeCursorNode sInstance;

    public static GazeCursorNode getInstance(SXRContext sxrContext) {
        if (sInstance == null) {
            sInstance = new GazeCursorNode(sxrContext);
        }
        return sInstance;
    }

    private GazeCursorNode(SXRContext sxrContext) {
        super(sxrContext);

        float xRightCursor = sxrContext.getMainScene().getMainCameraRig().getRightCamera().getTransform().getPositionX();
        float xLeftCursor = sxrContext.getMainScene().getMainCameraRig().getLeftCamera().getTransform().getPositionX();
        float zRightCursor, zLeftCursor;
        try {
            zRightCursor = -(((SXRPerspectiveCamera) sxrContext.getMainScene()
                    .getMainCameraRig().getRightCamera()).getNearClippingDistance() +
                    NEAR_CLIPPING_OFFSET);
            zLeftCursor = -(((SXRPerspectiveCamera) sxrContext.getMainScene()
                    .getMainCameraRig().getLeftCamera()).getNearClippingDistance() +
                    NEAR_CLIPPING_OFFSET);
        } catch (ClassCastException e) {
            // cameras cannot be cast to SXRPerspectiveCamera, use the default clipping distances
            // instead.
            zRightCursor = -(DEFAULT_NEAR_CLIPPING_DISTANCE + NEAR_CLIPPING_OFFSET);
            zLeftCursor = -(DEFAULT_NEAR_CLIPPING_DISTANCE + NEAR_CLIPPING_OFFSET);
        }

        rightCursor = new SXRNode(sxrContext);
        rightCursor.attachRenderData(createRenderData(sxrContext));
        rightCursor.getRenderData().setRenderMask(SXRRenderMaskBit.Right);
        rightCursor.getTransform().setPosition(xRightCursor, 0, zRightCursor);
        addChildObject(rightCursor);

        leftCursor = new SXRNode(sxrContext);
        leftCursor.attachRenderData(createRenderData(sxrContext));
        leftCursor.getRenderData().setRenderMask(SXRRenderMaskBit.Left);
        leftCursor.getTransform().setPosition(xLeftCursor, 0, zLeftCursor);
        addChildObject(leftCursor);
    }

    private SXRRenderData createRenderData(SXRContext sxrContext) {
        SXRMaterial material = new SXRMaterial(sxrContext);
        SXRMesh mesh = sxrContext.createQuad(NORMAL_CURSOR_SIZE, NORMAL_CURSOR_SIZE);
        material.setMainTexture(sxrContext.getAssetLoader().loadTexture(new SXRAndroidResource(sxrContext, R.drawable.head_tracker)));
        SXRRenderData renderData = new SXRRenderData(sxrContext);
        renderData.setMaterial(material);
        renderData.setMesh(mesh);
        renderData.setDepthTest(false);
        renderData.setRenderingOrder(CURSOR_RENDER_ORDER);

        return renderData;
    }

}
