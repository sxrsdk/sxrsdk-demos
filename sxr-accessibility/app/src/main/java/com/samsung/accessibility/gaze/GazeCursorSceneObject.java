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
import com.samsungxr.SXRSceneObject;

import com.samsung.accessibility.R;

public class GazeCursorSceneObject extends SXRSceneObject {

    private static final float NEAR_CLIPPING_OFFSET = 0.00001f;
    private static final float DEFAULT_NEAR_CLIPPING_DISTANCE = 0.1f;
    private static final float NORMAL_CURSOR_SIZE = 0.5f;
    private static final int CURSOR_RENDER_ORDER = 100000;

    private SXRSceneObject rightCursor;
    private SXRSceneObject leftCursor;
    private static GazeCursorSceneObject sInstance;

    public static GazeCursorSceneObject getInstance(SXRContext gvrContext) {
        if (sInstance == null) {
            sInstance = new GazeCursorSceneObject(gvrContext);
        }
        return sInstance;
    }

    private GazeCursorSceneObject(SXRContext gvrContext) {
        super(gvrContext);

        float xRightCursor = gvrContext.getMainScene().getMainCameraRig().getRightCamera().getTransform().getPositionX();
        float xLeftCursor = gvrContext.getMainScene().getMainCameraRig().getLeftCamera().getTransform().getPositionX();
        float zRightCursor, zLeftCursor;
        try {
            zRightCursor = -(((SXRPerspectiveCamera) gvrContext.getMainScene()
                    .getMainCameraRig().getRightCamera()).getNearClippingDistance() +
                    NEAR_CLIPPING_OFFSET);
            zLeftCursor = -(((SXRPerspectiveCamera) gvrContext.getMainScene()
                    .getMainCameraRig().getLeftCamera()).getNearClippingDistance() +
                    NEAR_CLIPPING_OFFSET);
        } catch (ClassCastException e) {
            // cameras cannot be cast to SXRPerspectiveCamera, use the default clipping distances
            // instead.
            zRightCursor = -(DEFAULT_NEAR_CLIPPING_DISTANCE + NEAR_CLIPPING_OFFSET);
            zLeftCursor = -(DEFAULT_NEAR_CLIPPING_DISTANCE + NEAR_CLIPPING_OFFSET);
        }

        rightCursor = new SXRSceneObject(gvrContext);
        rightCursor.attachRenderData(createRenderData(gvrContext));
        rightCursor.getRenderData().setRenderMask(SXRRenderMaskBit.Right);
        rightCursor.getTransform().setPosition(xRightCursor, 0, zRightCursor);
        addChildObject(rightCursor);

        leftCursor = new SXRSceneObject(gvrContext);
        leftCursor.attachRenderData(createRenderData(gvrContext));
        leftCursor.getRenderData().setRenderMask(SXRRenderMaskBit.Left);
        leftCursor.getTransform().setPosition(xLeftCursor, 0, zLeftCursor);
        addChildObject(leftCursor);
    }

    private SXRRenderData createRenderData(SXRContext gvrContext) {
        SXRMaterial material = new SXRMaterial(gvrContext);
        SXRMesh mesh = gvrContext.createQuad(NORMAL_CURSOR_SIZE, NORMAL_CURSOR_SIZE);
        material.setMainTexture(gvrContext.getAssetLoader().loadTexture(new SXRAndroidResource(gvrContext, R.drawable.head_tracker)));
        SXRRenderData renderData = new SXRRenderData(gvrContext);
        renderData.setMaterial(material);
        renderData.setMesh(mesh);
        renderData.setDepthTest(false);
        renderData.setRenderingOrder(CURSOR_RENDER_ORDER);

        return renderData;
    }

}
