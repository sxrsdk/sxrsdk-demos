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
package com.samsungxr.gvreyepicking;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.samsungxr.*;
import com.samsungxr.SXRMaterial.SXRShaderType;
import com.samsungxr.utility.Log;

public class SampleMain extends SXRMain {
    public class PickHandler implements IPickEvents
    {
        public void onEnter(SXRSceneObject sceneObj, SXRPicker.SXRPickedObject pickInfo)
        {
            sceneObj.getRenderData().getMaterial().setVec4("u_color", PICKED_COLOR_R, PICKED_COLOR_G, PICKED_COLOR_B, PICKED_COLOR_A);
        }
        public void onExit(SXRSceneObject sceneObj)
        {
            sceneObj.getRenderData().getMaterial().setVec4("u_color", UNPICKED_COLOR_R, UNPICKED_COLOR_G, UNPICKED_COLOR_B, UNPICKED_COLOR_A);
        }
        public void onNoPick(SXRPicker picker) { }
        public void onPick(SXRPicker picker) { }
        public void onInside(SXRSceneObject sceneObj, SXRPicker.SXRPickedObject pickInfo) { }
    }

    private static final String TAG = "SampleMain";

    private static final float UNPICKED_COLOR_R = 0.7f;
    private static final float UNPICKED_COLOR_G = 0.7f;
    private static final float UNPICKED_COLOR_B = 0.7f;
    private static final float UNPICKED_COLOR_A = 1.0f;
    private static final float PICKED_COLOR_R = 1.0f;
    private static final float PICKED_COLOR_G = 0.0f;
    private static final float PICKED_COLOR_B = 0.0f;
    private static final float PICKED_COLOR_A = 1.0f;

    private SXRContext mSXRContext = null;
    private List<SXRSceneObject> mObjects = new ArrayList<SXRSceneObject>();
    private IPickEvents mPickHandler = new PickHandler();
    private SXRPicker mPicker;

    private SXRActivity mActivity;

    SampleMain(SXRActivity activity) {
        mActivity = activity;
    }

    @Override
    public void onInit(SXRContext gvrContext) {
        mSXRContext = gvrContext;

        SXRScene mainScene = mSXRContext.getMainScene();

        mainScene.getMainCameraRig().getLeftCamera()
                .setBackgroundColor(1.0f, 1.0f, 1.0f, 1.0f);
        mainScene.getMainCameraRig().getRightCamera()
                .setBackgroundColor(1.0f, 1.0f, 1.0f, 1.0f);
        mainScene.getEventReceiver().addListener(mPickHandler);
        mPicker = new SXRPicker(gvrContext, mainScene);

        /*
         * Adding Boards
         */
        SXRSceneObject object = getColorBoard(1.0f, 1.0f);
        object.getTransform().setPosition(0.0f, 3.0f, -5.0f);
        attachMeshCollider(object);
        mainScene.addSceneObject(object);
        mObjects.add(object);

        object = getColorBoard(1.0f, 1.0f);
        object.getTransform().setPosition(0.0f, -3.0f, -5.0f);
        attachMeshCollider(object);
        mainScene.addSceneObject(object);
        mObjects.add(object);

        object = getColorBoard(1.0f, 1.0f);
        object.getTransform().setPosition(-3.0f, 0.0f, -5.0f);
        attachMeshCollider(object);
        mainScene.addSceneObject(object);
        mObjects.add(object);

        object = getColorBoard(1.0f, 1.0f);
        object.getTransform().setPosition(3.0f, 0.0f, -5.0f);
        attachMeshCollider(object);
        mainScene.addSceneObject(object);
        mObjects.add(object);

        object = getColorBoard(1.0f, 1.0f);
        object.getTransform().setPosition(3.0f, 3.0f, -5.0f);
        attachMeshCollider(object);
        mainScene.addSceneObject(object);
        mObjects.add(object);

        object = getColorBoard(1.0f, 1.0f);
        object.getTransform().setPosition(3.0f, -3.0f, -5.0f);
        attachMeshCollider(object);
        mainScene.addSceneObject(object);
        mObjects.add(object);

        object = getColorBoard(1.0f, 1.0f);
        object.getTransform().setPosition(-3.0f, 3.0f, -5.0f);
        attachSphereCollider(object);
        mainScene.addSceneObject(object);
        mObjects.add(object);

        object = getColorBoard(1.0f, 1.0f);
        object.getTransform().setPosition(-3.0f, -3.0f, -5.0f);
        attachSphereCollider(object);
        mainScene.addSceneObject(object);
        mObjects.add(object);

        /*
         * Adding bunnies.
         */

        SXRMesh mesh = null;
        try {
            mesh = mSXRContext.getAssetLoader().loadMesh(new SXRAndroidResource(mSXRContext,
                    "bunny.obj"));
        } catch (IOException e) {
            e.printStackTrace();
            mesh = null;
        }
        if (mesh == null) {
            mActivity.finish();
            Log.e(TAG, "Mesh was not loaded. Stopping application!");
        }
        // activity was stored in order to stop the application if the mesh is
        // not loaded. Since we don't need anymore, we set it to null to reduce
        // chance of memory leak.
        mActivity = null;

        // These 2 are testing by the whole mesh.
        object = getColorMesh(1.0f, mesh);
        object.getTransform().setPosition(0.0f, 0.0f, -2.0f);
        attachMeshCollider(object);
        mainScene.addSceneObject(object);
        mObjects.add(object);

        object = getColorMesh(1.0f, mesh);
        object.getTransform().setPosition(3.0f, 3.0f, -2.0f);
        attachMeshCollider(object);
        mainScene.addSceneObject(object);
        object.getRenderData().setCullFace(SXRRenderPass.SXRCullFaceEnum.None);
        mObjects.add(object);

        // These 2 are testing by the bounding box of the mesh.
        object = getColorMesh(2.0f, mesh);
        object.getTransform().setPosition(-5.0f, 0.0f, -2.0f);
        attachBoundsCollider(object);
        mainScene.addSceneObject(object);
        mObjects.add(object);

        object = getColorMesh(1.0f, mesh);
        object.getTransform().setPosition(0.0f, -5.0f, -2.0f);
        attachBoundsCollider(object);
        mainScene.addSceneObject(object);
        mObjects.add(object);
    }

    @Override
    public void onStep() {
    }

    private SXRSceneObject getColorBoard(float width, float height) {
        SXRMaterial material = new SXRMaterial(mSXRContext, SXRMaterial.SXRShaderType.Color.ID);
        material.setVec4("u_color", UNPICKED_COLOR_R,
                UNPICKED_COLOR_G, UNPICKED_COLOR_B, UNPICKED_COLOR_A);
        SXRSceneObject board = new SXRSceneObject(mSXRContext, width, height);
        board.getRenderData().setMaterial(material);
        // material.setVec4("u_color", UNPICKED_COLOR_R,
        //        UNPICKED_COLOR_G, UNPICKED_COLOR_B, UNPICKED_COLOR_A);
        return board;
    }

    private SXRSceneObject getColorMesh(float scale, SXRMesh mesh) {
        SXRMaterial material = new SXRMaterial(mSXRContext, SXRMaterial.SXRShaderType.Color.ID);
        material.setVec4("u_color", UNPICKED_COLOR_R,
                UNPICKED_COLOR_G, UNPICKED_COLOR_B, UNPICKED_COLOR_A);

        SXRSceneObject meshObject = null;
        meshObject = new SXRSceneObject(mSXRContext, mesh);
        meshObject.getTransform().setScale(scale, scale, scale);
        meshObject.getRenderData().setMaterial(material);
        // material.setVec4("u_color", UNPICKED_COLOR_R,
        //          UNPICKED_COLOR_G, UNPICKED_COLOR_B, UNPICKED_COLOR_A);
        return meshObject;
    }

    private void attachMeshCollider(SXRSceneObject sceneObject) {
        sceneObject.attachComponent(new SXRMeshCollider(mSXRContext, false));
    }

    private void attachSphereCollider(SXRSceneObject sceneObject) {
        sceneObject.attachComponent(new SXRSphereCollider(mSXRContext));
    }

    private void attachBoundsCollider(SXRSceneObject sceneObject) {
        sceneObject.attachComponent(new SXRMeshCollider(mSXRContext, true));
    }
}
