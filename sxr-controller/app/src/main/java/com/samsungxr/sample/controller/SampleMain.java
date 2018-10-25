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
package com.samsungxr.sample.controller;

import android.view.MotionEvent;

import com.samsungxr.SXRActivity;
import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRAssetLoader;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRMain;
import com.samsungxr.SXRMaterial;
import com.samsungxr.SXRMaterial.SXRShaderType;
import com.samsungxr.SXRMesh;
import com.samsungxr.SXRMeshCollider;
import com.samsungxr.SXRPicker;
import com.samsungxr.SXRRenderData;
import com.samsungxr.SXRScene;
import com.samsungxr.SXRSceneObject;
import com.samsungxr.SXRSphereCollider;
import com.samsungxr.SXRTexture;
import com.samsungxr.ITouchEvents;
import com.samsungxr.io.SXRCursorController;
import com.samsungxr.io.SXRInputManager;
import com.samsungxr.scene_objects.SXRCubeSceneObject;
import com.samsungxr.scene_objects.SXRSphereSceneObject;
import com.samsungxr.utility.Log;

import java.io.IOException;
import java.util.EnumSet;

public class SampleMain extends SXRMain
{
    private static final String TAG = "SampleMain";

    private static final float UNPICKED_COLOR_R = 0.7f;
    private static final float UNPICKED_COLOR_G = 0.7f;
    private static final float UNPICKED_COLOR_B = 0.7f;
    private static final float UNPICKED_COLOR_A = 1.0f;

    private static final float PICKED_COLOR_R = 1.0f;
    private static final float PICKED_COLOR_G = 0.0f;
    private static final float PICKED_COLOR_B = 0.0f;
    private static final float PICKED_COLOR_A = 1.0f;

    private static final float CLICKED_COLOR_R = 0.5f;
    private static final float CLICKED_COLOR_G = 0.5f;
    private static final float CLICKED_COLOR_B = 1.0f;
    private static final float CLICKED_COLOR_A = 1.0f;

    private static final float SCALE = 200.0f;
    private static final float DEPTH = -7.0f;
    private static final float BOARD_OFFSET = 2.0f;
    private SXRScene mainScene;
    private SXRContext mSXRContext = null;
    private SXRActivity mActivity;
    private SXRSceneObject cursor;
    private SXRCursorController controller;

    SampleMain(SXRActivity activity)
    {
        mActivity = activity;
    }

    @Override
    public void onInit(SXRContext gvrContext)
    {
        mSXRContext = gvrContext;
        mainScene = mSXRContext.getMainScene();
        mainScene.getEventReceiver().addListener(mPickHandler);
        SXRInputManager inputManager = mSXRContext.getInputManager();
        cursor = new SXRSceneObject(mSXRContext, mSXRContext.createQuad(1f, 1f),
                                    mSXRContext.getAssetLoader().loadTexture(
                                    new SXRAndroidResource(mSXRContext, R.raw.cursor)));
        cursor.getRenderData().setDepthTest(false);
        cursor.getRenderData().setRenderingOrder(SXRRenderData.SXRRenderingOrder.OVERLAY);
        final EnumSet<SXRPicker.EventOptions> eventOptions = EnumSet.of(
                SXRPicker.EventOptions.SEND_TOUCH_EVENTS,
                SXRPicker.EventOptions.SEND_TO_LISTENERS);
        inputManager.selectController(new SXRInputManager.ICursorControllerSelectListener()
        {
            public void onCursorControllerSelected(SXRCursorController newController, SXRCursorController oldController)
            {
                if (oldController != null)
                {
                    oldController.removePickEventListener(mPickHandler);
                }
                controller = newController;
                newController.addPickEventListener(mPickHandler);
                newController.setCursor(cursor);
                newController.setCursorDepth(DEPTH);
                newController.setCursorControl(SXRCursorController.CursorControl.PROJECT_CURSOR_ON_SURFACE);
                newController.getPicker().setEventOptions(eventOptions);
            }
        });

        /*
         * Adding Boards
         */
        SXRSceneObject object = getColorBoard();
        object.getTransform().setPosition(0.0f, BOARD_OFFSET, DEPTH);
        attachMeshCollider(object);
        object.setName("MeshBoard1");
        mainScene.addSceneObject(object);

        object = getColorBoard();
        object.getTransform().setPosition(0.0f, -BOARD_OFFSET, DEPTH);
        attachMeshCollider(object);
        object.setName("MeshBoard2");
        mainScene.addSceneObject(object);

        object = getColorBoard();
        object.getTransform().setPosition(-BOARD_OFFSET, 0.0f, DEPTH);
        attachMeshCollider(object);
        object.setName("MeshBoard3");
        mainScene.addSceneObject(object);

        object = getColorBoard();
        object.getTransform().setPosition(BOARD_OFFSET, 0.0f, DEPTH);
        attachMeshCollider(object);
        object.setName("MeshBoard4");
        mainScene.addSceneObject(object);

        object = getColorBoard();
        object.getTransform().setPosition(BOARD_OFFSET, BOARD_OFFSET, DEPTH);
        object.setName("MeshBoard5");
        attachMeshCollider(object);
        mainScene.addSceneObject(object);

        object = getColorBoard();
        object.getTransform().setPosition(BOARD_OFFSET, -BOARD_OFFSET, DEPTH);
        attachMeshCollider(object);
        object.setName("MeshBoard6");
        mainScene.addSceneObject(object);

        object = getColorBoard();
        object.getTransform().setPosition(-BOARD_OFFSET, BOARD_OFFSET, DEPTH);
        attachSphereCollider(object);
        object.setName("SphereBoard1");
        mainScene.addSceneObject(object);

        object = getColorBoard();
        object.getTransform().setPosition(-BOARD_OFFSET, -BOARD_OFFSET, DEPTH);
        object.setName("SphereBoard2");
        attachSphereCollider(object);
        mainScene.addSceneObject(object);

        SXRMesh mesh = null;
        try
        {
            mesh = mSXRContext.getAssetLoader().loadMesh(
                    new SXRAndroidResource(mSXRContext, "bunny.obj"));
        }
        catch (IOException e)
        {
            e.printStackTrace();
            mesh = null;
        }
        if (mesh == null)
        {
            mActivity.finish();
            Log.e(TAG, "Mesh was not loaded. Stopping application!");
        }
        // activity was stored in order to stop the application if the mesh is
        // not loaded. Since we don't need anymore, we set it to null to reduce
        // chance of memory leak.
        mActivity = null;

        object = getColorMesh(0.75f, mesh);
        object.getTransform().setPosition(0.0f, 0.0f, DEPTH);
        object.setName("BoundsBunny1");
        attachBoundsCollider(object);
        mainScene.addSceneObject(object);

        object = getColorMesh(0.75f, mesh);
        object.getTransform().setPosition(4.0f, 0.0f, DEPTH);
        attachBoundsCollider(object);
        object.setName("BoundsBunny2");
        mainScene.addSceneObject(object);

        object = getColorMesh(0.75f, mesh);
        object.getTransform().setPosition(-4.0f, 0.0f, DEPTH);
        attachMeshCollider(object);
        object.setName("MeshBunny3");
        mainScene.addSceneObject(object);

        object = getColorMesh(0.75f, mesh);
        object.getTransform().setPosition(0.0f, -4.0f, DEPTH);
        attachMeshCollider(object);
        object.setName("MeshBunny4");
        mainScene.addSceneObject(object);

        SXRAssetLoader assetLoader = gvrContext.getAssetLoader();
        SXRTexture texture = assetLoader.loadTexture(
                new SXRAndroidResource(gvrContext, R.drawable.skybox_gridroom));
        SXRMaterial material = new SXRMaterial(gvrContext);
        SXRSphereSceneObject skyBox = new SXRSphereSceneObject(gvrContext, false, material);
        skyBox.getTransform().setScale(SCALE, SCALE, SCALE);
        skyBox.getRenderData().getMaterial().setMainTexture(texture);
        mainScene.addSceneObject(skyBox);
    }

    private ITouchEvents mPickHandler = new ITouchEvents()
    {
        private SXRSceneObject movingObject;

        public void onEnter(SXRSceneObject sceneObj, SXRPicker.SXRPickedObject pickInfo)
        {
            sceneObj.getRenderData().getMaterial().setVec4("u_color", PICKED_COLOR_R,
                                                           PICKED_COLOR_G, PICKED_COLOR_B,
                                                           PICKED_COLOR_A);
        }

        public void onTouchStart(SXRSceneObject sceneObj, SXRPicker.SXRPickedObject pickInfo)
        {
            if (movingObject == null)
            {
                sceneObj.getRenderData().getMaterial().setVec4("u_color", CLICKED_COLOR_R,
                                                               CLICKED_COLOR_G, CLICKED_COLOR_B,
                                                               CLICKED_COLOR_A);
                if (controller.startDrag(sceneObj))
                {
                    movingObject = sceneObj;
                }
            }
        }

        public void onTouchEnd(SXRSceneObject sceneObj, SXRPicker.SXRPickedObject pickInfo)
        {
            sceneObj.getRenderData().getMaterial().setVec4("u_color", PICKED_COLOR_R,
                                                           PICKED_COLOR_G, PICKED_COLOR_B,
                                                           PICKED_COLOR_A);
            if (sceneObj == movingObject)
            {
                controller.stopDrag();
                movingObject = null;
            }
         }

        public void onExit(SXRSceneObject sceneObj, SXRPicker.SXRPickedObject pickInfo)
        {
            sceneObj.getRenderData().getMaterial().setVec4("u_color", UNPICKED_COLOR_R,
                                                           UNPICKED_COLOR_G, UNPICKED_COLOR_B,
                                                           UNPICKED_COLOR_A);
            if (sceneObj == movingObject)
            {
                controller.stopDrag();
                movingObject = null;
            }
        }
        public void onInside(SXRSceneObject sceneObj, SXRPicker.SXRPickedObject pickInfo) { }

        public void onMotionOutside(SXRPicker p, MotionEvent e) { }
    };

    private SXRSceneObject getColorBoard()
    {
        SXRMaterial material = new SXRMaterial(mSXRContext, SXRShaderType.Color.ID);
        material.setVec4("u_color", UNPICKED_COLOR_R,
                         UNPICKED_COLOR_G, UNPICKED_COLOR_B, UNPICKED_COLOR_A);
        SXRCubeSceneObject board = new SXRCubeSceneObject(mSXRContext);
        board.getRenderData().setMaterial(material);
        board.getRenderData().setRenderingOrder(SXRRenderData.SXRRenderingOrder.GEOMETRY);
        return board;
    }

    private SXRSceneObject getColorMesh(float scale, SXRMesh mesh)
    {
        SXRMaterial material = new SXRMaterial(mSXRContext, SXRShaderType.Color.ID);
        material.setVec4("u_color", UNPICKED_COLOR_R,
                         UNPICKED_COLOR_G, UNPICKED_COLOR_B, UNPICKED_COLOR_A);

        SXRSceneObject meshObject = new SXRSceneObject(mSXRContext, mesh);
        meshObject.getTransform().setScale(scale, scale, scale);
        meshObject.getRenderData().setMaterial(material);
        meshObject.getRenderData().setRenderingOrder(SXRRenderData.SXRRenderingOrder.GEOMETRY);
        return meshObject;
    }

    private void attachMeshCollider(SXRSceneObject sceneObject)
    {
        sceneObject.attachComponent(new SXRMeshCollider(mSXRContext, false));
    }

    private void attachSphereCollider(SXRSceneObject sceneObject)
    {
        sceneObject.attachComponent(new SXRSphereCollider(mSXRContext));
    }

    private void attachBoundsCollider(SXRSceneObject sceneObject)
    {
        sceneObject.attachComponent(new SXRMeshCollider(mSXRContext, true));
    }
}
