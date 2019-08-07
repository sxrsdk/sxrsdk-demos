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
package com.samsungxr.io.cursorsimple;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRBitmapImage;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRMain;
import com.samsungxr.SXRMaterial;
import com.samsungxr.SXRMesh;
import com.samsungxr.shaders.SXRPhongShader;
import com.samsungxr.SXRPicker;
import com.samsungxr.SXRRenderData.SXRRenderingOrder;
import com.samsungxr.SXRScene;
import com.samsungxr.SXRNode;
import com.samsungxr.SXRShaderId;
import com.samsungxr.SXRTexture;
import com.samsungxr.SXRTransform;
import com.samsungxr.io.cursor3d.Cursor;
import com.samsungxr.io.cursor3d.CursorManager;
import com.samsungxr.io.cursor3d.MovableBehavior;
import com.samsungxr.io.cursor3d.SelectableBehavior;
import com.samsungxr.io.cursor3d.SelectableBehavior.ObjectState;
import com.samsungxr.io.cursor3d.SelectableBehavior.StateChangedListener;
import com.samsungxr.nodes.SXRCubeNode;
import com.samsungxr.utility.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This sample can be used with a Laser Cursor as well as an Object Cursor. By default the Object
 * Cursor is enabled. To switch to a Laser Cursor simply rename the "laser_cursor_settings.xml"
 * in the assets directory to "settings.xml"
 */
public class CursorMain extends SXRMain {
    private static final String TAG = CursorMain.class.getSimpleName();
    private static final String ASTRONAUT_MODEL = "Astronaut.fbx";
    private static final String ROCKET_MODEL = "Rocket.fbx";
    private SXRScene mainScene;
    private CursorManager cursorManager;
    private SXRNode rocket;
    private SXRNode astronaut;

    @Override
    public void onInit(SXRContext sxrContext) {
        mainScene = sxrContext.getMainScene();
        mainScene.getMainCameraRig().getLeftCamera().setBackgroundColor(Color.BLACK);
        mainScene.getMainCameraRig().getRightCamera().setBackgroundColor(Color.BLACK);

        cursorManager = new CursorManager(sxrContext, mainScene);
        SXRNode astronautModel, rocketModel;

        float[] position = new float[]{5.0f, 0.0f, -20.0f};
        try {
            astronautModel = sxrContext.getAssetLoader().loadModel(ASTRONAUT_MODEL);
            rocketModel = sxrContext.getAssetLoader().loadModel(ROCKET_MODEL);
        } catch (IOException e) {
            Log.e(TAG, "Could not load the assets:", e);
            return;
        }

        astronaut = astronautModel.getChildByIndex(0);
        astronaut.getTransform().setPosition(position[0], position[1], position[2]);
        SelectableBehavior selectableBehavior = new SelectableBehavior(cursorManager, true);
        astronaut.attachComponent(selectableBehavior);
        astronautModel.removeChildObject(astronaut);
        mainScene.addNode(astronaut);

        position[0] = -5.0f;
        MovableBehavior movableRocketBehavior = new MovableBehavior(cursorManager, new ObjectState[]{
                ObjectState.DEFAULT, ObjectState.BEHIND, ObjectState.COLLIDING, ObjectState.CLICKED});
        rocket = rocketModel.getChildByIndex(0);
        rocket.getTransform().setPosition(position[0], position[1], position[2]);
        rocket.attachComponent(movableRocketBehavior);
        rocketModel.removeChildObject(rocket);
        mainScene.addNode(rocket);

        position[0] = 2.0f;
        position[1] = 2.0f;
        SXRCubeNode cubeNode = new SXRCubeNode(sxrContext, true, sxrContext
                .getAssetLoader().loadTexture(new SXRAndroidResource(sxrContext,R.mipmap.ic_launcher)));
        cubeNode.getTransform().setPosition(position[0], position[1], position[2]);
        MovableBehavior movableCubeBehavior = new MovableBehavior(cursorManager);
        cubeNode.attachComponent(movableCubeBehavior);
        mainScene.addNode(cubeNode);

        movableCubeBehavior.setStateChangedListener(new StateChangedListener() {
            @Override
            public void onStateChanged(SelectableBehavior selectableBehavior, ObjectState prev,
                                       ObjectState current, Cursor cursor, SXRPicker.SXRPickedObject hit) {
                if (current == ObjectState.CLICKED) {
                    SXRTransform transform = astronaut.getTransform();
                    transform.setPositionZ(transform.getPositionZ() - 1);
                }
            }
        });

        addCustomMovableCube(sxrContext);
    }

    @Override
    public void onStep() {
    }

    void close() {
        if (cursorManager != null) {
            cursorManager.close();
        }

        //_VENDOR_TODO_ close the devices here
        //device.close();
        //device1.close();
        //device2.close();

    }

    private void addCustomMovableCube(SXRContext sxrContext) {
        SXRNode root = new SXRNode(sxrContext);
        SXRMaterial red = new SXRMaterial(sxrContext, new SXRShaderId(SXRPhongShader.class));
        SXRMaterial blue = new SXRMaterial(sxrContext, new SXRShaderId(SXRPhongShader.class));
        SXRMaterial green = new SXRMaterial(sxrContext, new SXRShaderId(SXRPhongShader.class));
        SXRMaterial alphaRed = new SXRMaterial(sxrContext, new SXRShaderId(SXRPhongShader.class));
        red.setDiffuseColor(1, 0, 0, 1);
        blue.setDiffuseColor(0, 0, 1, 1);
        green.setDiffuseColor(0, 1, 0, 1);
        alphaRed.setDiffuseColor(1, 0, 0, 0.5f);

        SXRCubeNode cubeDefault = new SXRCubeNode(sxrContext, true, red);
        root.addChildObject(cubeDefault);

        SXRMesh cubeMesh = cubeDefault.getRenderData().getMesh();

        SXRNode cubeColliding = new SXRNode(sxrContext, cubeMesh);
        cubeColliding.getRenderData().setMaterial(blue);
        root.addChildObject(cubeColliding);

        SXRNode cubeClicked = new SXRNode(sxrContext, cubeMesh);
        cubeClicked.getRenderData().setMaterial(green);
        root.addChildObject(cubeClicked);

        SXRNode cubeBehind = new SXRNode(sxrContext, cubeMesh);
        cubeBehind.getRenderData().setMaterial(alphaRed);
        cubeBehind.getRenderData().getMaterial().setOpacity(0.5f);
        cubeBehind.getRenderData().setRenderingOrder(SXRRenderingOrder.TRANSPARENT);
        root.addChildObject(cubeBehind);

        MovableBehavior movableBehavior = new MovableBehavior(cursorManager, new ObjectState[] {
                ObjectState.DEFAULT, ObjectState.COLLIDING, ObjectState.CLICKED, ObjectState.BEHIND
        });
        float[] position = new float[]{-2, 2, -10};
        root.getTransform().setPosition(position[0], position[1], position[2]);
        root.attachComponent(movableBehavior);
        mainScene.addNode(root);

        movableBehavior.setStateChangedListener(new StateChangedListener() {
            @Override
            public void onStateChanged(SelectableBehavior selectableBehavior, ObjectState prev,
                                       ObjectState current, Cursor cursor, SXRPicker.SXRPickedObject hit) {
                if(current == ObjectState.CLICKED) {
                    SXRTransform transform = astronaut.getTransform();
                    transform.setPositionZ(transform.getPositionZ() + 1);
                }
            }
        });
    }

    @Override
    public SXRTexture getSplashTexture(SXRContext sxrContext) {
        Bitmap bitmap = BitmapFactory.decodeResource(
                sxrContext.getContext().getResources(),
                R.mipmap.ic_launcher);
        // return the correct splash screen bitmap
        SXRTexture tex = new SXRTexture(sxrContext);
        tex.setImage(new SXRBitmapImage(sxrContext, bitmap));
        return tex;
    }
}
