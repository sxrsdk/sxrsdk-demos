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

package com.samsungxr.modelviewer2;

import android.opengl.GLES20;
import android.util.Log;
import android.view.MotionEvent;

import com.samsungxr.ITouchEvents;
import com.samsungxr.SXRActivity;
import com.samsungxr.SXRContext;
import com.samsungxr.SXREventListeners;
import com.samsungxr.SXRMain;
import com.samsungxr.SXRPicker;
import com.samsungxr.SXRRenderData;
import com.samsungxr.SXRScene;
import com.samsungxr.SXRSceneObject;
import com.samsungxr.io.SXRCursorController;
import com.samsungxr.io.SXRInputManager;
import com.samsungxr.io.SXRTouchPadGestureListener;
import com.samsungxr.widgetplugin.SXRWidgetPlugin;
import com.samsungxr.widgetplugin.SXRWidgetSceneObject;
import com.samsungxr.widgetplugin.SXRWidgetSceneObjectMeshInfo;

import org.joml.Vector3f;

import java.util.ArrayList;


public class ModelViewer2Manager extends SXRMain {

    private static final String TAG = "SXRModelViewer2";

    private SXRContext mSXRContext;
    private Controller controller;
    public boolean controllerReadyFlag = false;
    private SXRScene scene;
    private boolean mIsSingleTapped = false;

    SXRWidgetPlugin mPlugin;
    SXRWidgetSceneObject mWidget;
    float widgetModelMatrix[];
    SXRActivity activity;

    private Vector3f defaultCenterPosition = new Vector3f(0, 0, 0);

    private static final int TAP_INTERVAL = 300;
    private long mLatestTap = 0;

    private SXRInputManager.ICursorControllerSelectListener controllerSelector = new SXRInputManager.ICursorControllerSelectListener()
    {
        public void onCursorControllerSelected(SXRCursorController newController, SXRCursorController oldController)
        {
            if (oldController != null)
            {
                oldController.removePickEventListener(mPlugin.getTouchHandler());
                oldController.removePickEventListener(mPickHandler);
            }
            newController.addPickEventListener(mPlugin.getTouchHandler());
            newController.addPickEventListener(mPickHandler);
            newController.setCursorDepth(2.0f);
            newController.setCursorControl(SXRCursorController.CursorControl.PROJECT_CURSOR_ON_SURFACE);
        }
    };

    private SXRSceneObject mPicked = null;

    void setPicked(SXRSceneObject obj)
    {
        mPicked = obj;
    }

    SXRSceneObject getPicked() { return mPicked; }
    private ITouchEvents mPickHandler = new SXREventListeners.TouchEvents()
    {
        public void onExit(SXRSceneObject sceneObject, SXRPicker.SXRPickedObject pickInfo)
        {
           setPicked(null);
        }

        public void onTouchStart(SXRSceneObject sceneObject, SXRPicker.SXRPickedObject pickInfo)
        {
            setPicked(sceneObject);
        }

        public void onTouchEnd(SXRSceneObject sceneObject, SXRPicker.SXRPickedObject pickInfo)
        {
            setPicked(null);
        }
    };

    public ModelViewer2Manager(SXRActivity activity, SXRWidgetPlugin mPlugin) {
        this.mPlugin = mPlugin;
        this.activity = activity;

    }

    void addWidgetToTheRoom() {
        SXRWidgetSceneObjectMeshInfo info =
                new SXRWidgetSceneObjectMeshInfo(-4.5f, 1.0f, -1.5f, -1.0f, new int[]{0, 0}, new int[]{mPlugin.getWidth(), mPlugin.getHeight()});
        mWidget = new SXRWidgetSceneObject(mSXRContext,
                mPlugin.getTextureId(), info, mPlugin.getWidth(),
                mPlugin.getHeight());


        Log.d(TAG, Float.toString(mPlugin.getHeight()) + "   " + Float.toString(mPlugin.getHeight()));

        mWidget.getTransform().setPosition(-1.5f, 0, -5.5f);
        mWidget.getTransform().rotateByAxis(60.0f, 0.0f, 1.0f, 0.0f);
        mWidget.getRenderData().setRenderingOrder(SXRRenderData.SXRRenderingOrder.TRANSPARENT);
        mWidget.getRenderData().setDepthTest(false);

        widgetModelMatrix = mWidget.getTransform().getModelMatrix();

        scene.getMainCameraRig().addChildObject(mWidget);
        float temp[] = mWidget.getTransform().getModelMatrix();
        scene.getMainCameraRig().removeChildObject(mWidget);
        mWidget.getTransform().setModelMatrix(temp);

        scene.addSceneObject(mWidget);
        controller.enableDisableLightOnModel(mWidget, false);
    }

    @Override
    public void onInit(final SXRContext sxrContext) {

        mSXRContext = sxrContext;
        scene = sxrContext.getMainScene();

        sxrContext.getInputManager().selectController( controllerSelector);
        Log.d(TAG, "Controller initialization done");

        controller = new Controller(activity, mSXRContext);
        controller.setDefaultCenterPosition(defaultCenterPosition);
        controller.initializeController();
        controllerReadyFlag = true;

        controller.displayCountInRoom(scene);
        addWidgetToTheRoom();
        controller.displayNavigators(scene);
        controller.setCameraPositionByNavigator(null, scene, scene, mWidget, widgetModelMatrix);
        controller.addLight(scene);


        // Add First SkyBox
        addSkyBox(0);
    }

    ArrayList<String> getListOfCustomShaders() {
        return controller.getListOfCustomShaders();
    }

    void setSelectedCustomShader(int index) {
        controller.applyCustomShader(index, scene);
    }


    void addSkyBox(int index) {
        controller.addSkyBox(index, scene);
    }


    ArrayList<String> getSkyBoxList() {
        return controller.getSkyBoxList();
    }

    public int getCountOfAnimations() {
        return controller.getCountOfAnimations();
    }

    public void setSelectedAnimation(int index) {
        if (controllerReadyFlag)
            controller.setSelectedAnimation(index);
    }

    ArrayList<String> getModelsList() {
        return controller.getModelsList();
    }

    public void setSelectedModel(int index) {
        if (controllerReadyFlag)
            controller.setModelWithIndex(index, scene);
    }

    public boolean isModelPresent() {
        return controller.currentModelFlag;
    }

    public void turnOnOffLight(boolean flag) {
        controller.turnOnOffLight(flag);
    }

    public void lookInside(boolean flag){
        // To ignore this touch of Selecting look inside
        mIsSingleTapped = false;
        controller.lookInside(scene, flag);
    }

    public ArrayList<String> getAmbient() {
        return controller.getAmbient();
    }

    public ArrayList<String> getDiffuse() {
        return controller.getDiffuse();
    }

    public ArrayList<String> getSpecular() {
        return controller.getSpecular();
    }

    public void setAmbient(int index, boolean lightOnOff) {
        if (lightOnOff)
            controller.setAmbient(index);
    }

    public void setDiffuse(int index, boolean lightOnOff) {
        if (lightOnOff)
            controller.setDiffuse(index);
    }

    public void setSpecular(int index, boolean lightOnOff) {
        if (lightOnOff)
            controller.setSpecular(index);
    }

    @Override
    public void onStep() {

        boolean isSingleTapped = mIsSingleTapped;
        mIsSingleTapped = false;
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        if (isSingleTapped)
        controller.checkLookInside(scene);

        if (isSingleTapped) {
            SXRSceneObject picked = getPicked();
            if (picked != null)
            {
                controller.setCameraPositionByNavigator(picked.getCollider(), scene, scene, mWidget, widgetModelMatrix);
            }
        }
    }

    public void onSingleTap(MotionEvent e) {
        Log.d(TAG, "On Single Touch Received");
        if (System.currentTimeMillis() > mLatestTap + TAP_INTERVAL) {
            mLatestTap = System.currentTimeMillis();
            mIsSingleTapped = true;
        }
    }

    public void onSwipe(MotionEvent e, SXRTouchPadGestureListener.Action action, float velocityX, float velocityY) {
    }

    public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2, float arg3) {
        Log.i(TAG, "Angle mover called");
        SXRSceneObject picked = getPicked();
        if (picked != null)
        {
            controller.onScrollOverModel(picked.getCollider(), arg2);
        }
        return false;
    }

    public void zoomCurrentModel(float zoomBy) {
        controller.onZoomOverModel(zoomBy);
    }
}
