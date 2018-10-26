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

package com.samsungxr.widgetViewer;

import android.view.MotionEvent;

import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRAssetLoader;
import com.samsungxr.SXRContext;
import com.samsungxr.SXREventListeners;
import com.samsungxr.SXRMain;
import com.samsungxr.SXRMaterial;
import com.samsungxr.SXRMesh;
import com.samsungxr.SXRPicker;
import com.samsungxr.SXRPointLight;
import com.samsungxr.SXRRenderData;
import com.samsungxr.SXRScene;
import com.samsungxr.SXRSceneObject;
import com.samsungxr.SXRShaderId;
import com.samsungxr.SXRSharedTexture;
import com.samsungxr.SXRSpotLight;
import com.samsungxr.SXRSwitch;
import com.samsungxr.SXRTexture;
import com.samsungxr.SXRWidgetViewer.R;
import com.samsungxr.io.SXRCursorController;
import com.samsungxr.io.SXRInputManager;
import com.samsungxr.scene_objects.SXRCubeSceneObject;
import com.samsungxr.utility.Log;
import com.samsungxr.widgetplugin.SXRWidgetPlugin;
import com.samsungxr.widgetplugin.SXRWidgetSceneObject;
import com.samsungxr.widgetplugin.SXRWidgetSceneObjectMeshInfo;

import java.io.IOException;

public class ViewerMain extends SXRMain {
    private static final String TAG = "ViewerMain";

    private SXRWidgetPlugin mPlugin = null;
    private SXRContext mSXRContext = null;
    private SXRScene mScene = null;

    private final float EYE_TO_OBJECT = 2.4f;
    public int ThumbnailSelected = 2;
    private SXRSceneObject mWidgetButtonObject;
    private SXRSceneObject mWdgetButtonObject2;
    public boolean mButtonPointed = false;
    public boolean mObjectPointed = true;

    private PickHandler mPickHandler = new PickHandler();

    public SXRSceneObject mObjectPos;
    public SXRSceneObject mObjectRot;
    public float mRotateX = 0.0f;
    public float mRotateY = 0.0f;
    public float mRotateZ = 0.0f;

    public float mLastRotateX = 0.0f;
    public float mLastRotateY = 0.0f;
    public float mLastRotateZ = 0.0f;

    public boolean mResetRotate = false;
    public int mTexColor = 1;
    public boolean mLookInside = false;
    public float mZoomLevel = -2.0f;

    ViewerMain(SXRWidgetPlugin plugin) {
        mPlugin = plugin;
    }
    SXRTexture mWidgetTexture = null;
    SXRMaterial mWidgetMaterial;
    SXRMaterial mWidgetMaterial2;

    private SXRSceneObject mLightNode;

    @Override
    public SplashMode getSplashMode() {
        return SplashMode.MANUAL;
    }

    public class PickHandler extends SXREventListeners.TouchEvents
    {
        public void onExit(SXRSceneObject sceneObj, SXRPicker.SXRPickedObject pickInfo) {
            if ((mWidgetButtonObject == sceneObj) ||
                (mWdgetButtonObject2 == sceneObj))
            {
                mButtonPointed = false;
            }
            mObjectPointed = true;
        }

        public void onEnter(SXRSceneObject sceneObj, SXRPicker.SXRPickedObject pickInfo) {
            mButtonPointed = false;
            if ((mWidgetButtonObject == sceneObj) ||
                (mWdgetButtonObject2 == sceneObj))
            {
                mObjectPointed = false;
                mButtonPointed = true;
            }
        }

        public void onInside(SXRSceneObject sceneObj, SXRPicker.SXRPickedObject pickInfo) {
            if (pickInfo.isTouched())
            {
                updateState();
            }
        }

        public void onMotionOutside(SXRPicker picker, MotionEvent event)
        {
            updateState();
        }
    }

    private SXRInputManager.ICursorControllerSelectListener mControllerSelector = new SXRInputManager.ICursorControllerSelectListener()
    {
        public void onCursorControllerSelected(SXRCursorController newController, SXRCursorController oldController)
        {
            if (oldController != null)
            {
                oldController.removePickEventListener(mPlugin.getTouchHandler());
                oldController.removePickEventListener(mPickHandler);
            }
            newController.addPickEventListener(mPickHandler);
            newController.addPickEventListener(mPlugin.getTouchHandler());
        }
    };

    @Override
    public void onInit(SXRContext sxrContext) {
        mSXRContext = sxrContext;
        mScene = mSXRContext.getMainScene();
        mScene.setBackgroundColor(1.0f, 1.0f, 1.0f, 1.0f);

        sxrContext.getInputManager().selectController(mControllerSelector);
        mScene.addSceneObject(addEnvironment());

        mLightNode = createLight(mSXRContext, 1, 1, 1, 2.8f);
        mScene.addSceneObject(mLightNode);

        try
        {
            mScene.addSceneObject(makeObjects(sxrContext));
            makeWidgetButtons();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            Log.e(TAG, "Assets were not loaded. Stopping application!");
            sxrContext.getActivity().finish();
        }
        sxrContext.runOnGlThread(new Runnable() {
            @Override
            public void run() {
                updateState();
                closeSplashScreen();
            }
        });
    }


    private SXRTexture mEnvTex;
    private SXRSceneObject addEnvironment()
    {
        mEnvTex = mSXRContext.getAssetLoader().loadCubemapTexture(new SXRAndroidResource(mSXRContext, R.raw.envmap));
        SXRMaterial mtl = new SXRMaterial(mSXRContext, SXRMaterial.SXRShaderType.Cubemap.ID);
        mtl.setMainTexture(mEnvTex);
        SXRSceneObject env = new SXRCubeSceneObject(mSXRContext, false, mtl);
        env.getRenderData().disableLight();
        env.getTransform().setScale(100, 100, 100);

        return env;
    }

    public void selectObject()
    {
        SXRSwitch selector = (SXRSwitch) mObjectRot.getComponent(SXRSwitch.getComponentType());
        selector.setSwitchIndex(ThumbnailSelected);
    }

    private void updateState()
    {
        if (mRotateY - mLastRotateY != 0.0f)
        {
            mObjectRot.getTransform().setRotationByAxis(mRotateY, 0.0f, 1.0f, 0.0f);
            mLastRotateZ = mRotateZ;
            mLastRotateX = mRotateX;
            mLastRotateY = mRotateY;
        }
        else if (mResetRotate)
        {
            mLastRotateZ = mRotateZ = 0.0f;
            mLastRotateX = mRotateX = 0.0f;
            mLastRotateY = mRotateY = 0.0f;
            mResetRotate = false;
            mObjectRot.getTransform().setRotation(1.0f, 0.0f, 0.0f, 0.0f);
        }
        if ((ThumbnailSelected == 3) && mLookInside)
        {
            mObjectPos.getTransform().setPositionZ(0);
        }
        else
        {
            mObjectPos.getTransform().setPositionZ(-EYE_TO_OBJECT + mZoomLevel);
        }

        updateTexColor();
    }

    void updateTexColor()
    {
        switch (mTexColor)
        {
            case 1:
                setLightColor(mLightNode, 1,1,1);
                break;
            case 2:
                setLightColor(mLightNode, 1,0,0);
                break;
            case 3:
                setLightColor(mLightNode, 0,0,1);
                break;
            case 4:
                setLightColor(mLightNode, 0,1,0);
                break;
        }
    }


    public void onButtonDown() {
        mSXRContext.getMainScene().getMainCameraRig().resetYaw();
    }

    public void onSingleTap(MotionEvent e)
    {
        if (ThumbnailSelected == 3 && mLookInside)
            mLookInside = false;
    }

    private SXRSceneObject makeObjects(SXRContext ctx) throws IOException
    {
        mObjectPos = new SXRSceneObject(ctx);
        mObjectPos.getTransform().setPositionZ(-EYE_TO_OBJECT);
        mObjectRot = new SXRSceneObject(ctx);
        SXRSwitch selector = new SXRSwitch(ctx);
        mObjectRot.attachComponent(selector);
        mObjectPos.addChildObject(mObjectRot);

        addModeltoScene("/Suzanne/glTF/Suzanne.gltf", 1,1,1, false);
        addModeltoScene("/WaterBottle/glTF-pbrSpecularGlossiness/WaterBottle.gltf", 8,8,8, true);
        addModeltoScene("/BoomBox/glTF-pbrSpecularGlossiness/BoomBox.gltf", 70,70,70, true);
        addModeltoScene("/SciFiHelmet/glTF/SciFiHelmet.gltf", 1,1,1, true);
        addModeltoScene("/Corset/glTF/Corset.gltf", 50,50,50, true);

        return mObjectPos;
    }

    private void addModeltoScene(String filePath, float scaleX, float scaleY, float scaleZ, boolean hasSpecularEnv) throws IOException {

        SXRSceneObject.BoundingVolume bv;
        SXRAssetLoader loader = mSXRContext.getAssetLoader();
        SXRSceneObject root = loader.loadModel(filePath);
        if(hasSpecularEnv)
            setEnvironmentTex(root, mEnvTex);
        root.getTransform().setScale(scaleX,scaleY,scaleZ);
        bv = root.getBoundingVolume();
        root.getTransform().setPosition(-bv.center.x, -bv.center.y, -bv.center.z);
        mObjectRot.addChildObject(root);
    }


    private void setEnvironmentTex( SXRSceneObject obj, SXRTexture tex)
    {
        if(obj.getRenderData() != null)
            if(obj.getRenderData().getMaterial()!= null)
                obj.getRenderData().getMaterial().setTexture("specularEnvTexture", tex);

        for (SXRSceneObject child: obj.getChildren())
            setEnvironmentTex(child, tex);
    }

    private SXRSceneObject createLight(SXRContext context, float r, float g, float b, float y)
    {
        SXRSceneObject lightNode = new SXRSceneObject(context);
        SXRSpotLight light = new SXRSpotLight(context);

        lightNode.attachLight(light);
        lightNode.getTransform().setPosition(0, 0.5f, 0);
        light.setAmbientIntensity(0.7f * r, 0.7f * g, 0.7f * b, 1);
        light.setDiffuseIntensity(r , g , b , 1);
        light.setSpecularIntensity(r, g, b, 1);
        light.setInnerConeAngle(20);
        light.setOuterConeAngle(30);
        return lightNode;
    }

    private void setLightColor( SXRSceneObject lightNode, float r, float g, float b)
    {
        SXRPointLight light = (SXRPointLight)lightNode.getLight();
        light.setAmbientIntensity(0.4f * r, 0.4f * g, 0.4f * b, 1);
        light.setDiffuseIntensity(r * 0.5f, g * 0.5f, b * 0.5f, 1);
        light.setSpecularIntensity(r, g, b, 1);
    }


    private void makeWidgetButtons() throws IOException
    {
        SXRMesh widgetbutton2_mesh = mSXRContext.getAssetLoader().loadMesh(new SXRAndroidResource(mSXRContext, "button2.obj"));

        mWidgetTexture = new SXRSharedTexture(mSXRContext, mPlugin.getTextureId());

        SXRWidgetSceneObjectMeshInfo info = new SXRWidgetSceneObjectMeshInfo(
                -2.5f, 1.0f, -1.5f, -1.0f, new int[]{0, 0}, new int[]{1280, 1440});

        SXRWidgetSceneObjectMeshInfo info2 =new SXRWidgetSceneObjectMeshInfo(
                1.5f,1.0f,2.5f,-1.0f,new int[] { 1281, 0 },new int[] { 2560, 1440 });

        mWidgetButtonObject = new SXRWidgetSceneObject(mSXRContext,
                                                       mPlugin.getTextureId(), info, mPlugin.getWidth(),
                                                       mPlugin.getHeight());
        mWdgetButtonObject2 = new SXRWidgetSceneObject(mSXRContext,
                                                       mPlugin.getTextureId(), info2, mPlugin.getWidth(),
                                                       mPlugin.getHeight());
        SXRRenderData ldata = new SXRRenderData(mSXRContext);
        SXRRenderData ldata2 = new SXRRenderData(mSXRContext);

        mWidgetMaterial2 = new SXRMaterial(mSXRContext, new SXRShaderId(PhongShader3.class));

        ldata2.setMesh(widgetbutton2_mesh);
        ldata2.setMaterial(mWidgetMaterial2);
        float[] light = new float[4];
        light[0] = 6.0f;
        light[1] = 10.0f;
        light[2] = 10.0f;
        light[3] = 1.0f;

        float[] eye = new float[4];
        eye[0] = 0.0f;
        eye[1] = 0.0f;
        eye[2] = 3.0f * EYE_TO_OBJECT;
        eye[3] = 1.0f;

        float[] matO = mObjectRot.getTransform().getModelMatrix();

        mWidgetMaterial = new SXRMaterial(mSXRContext, new SXRShaderId(PhongShader3.class));//new SXRMaterial(sxrContext, SXRShaderType.UnlitFBO.ID);
        ldata.setMaterial(mWidgetMaterial);
        mWidgetMaterial.setMainTexture(mWidgetTexture);
        mWidgetMaterial.setVec4(PhongShader3.MAT1_KEY, matO[0], matO[4], matO[8], matO[12]);
        mWidgetMaterial.setVec4(PhongShader3.MAT2_KEY, matO[1], matO[5], matO[9], matO[13]);
        mWidgetMaterial.setVec4(PhongShader3.MAT3_KEY, matO[2], matO[6], matO[10], matO[14]);
        mWidgetMaterial.setVec4(PhongShader3.MAT4_KEY, matO[3], matO[7], matO[11], matO[15]);
        mWidgetMaterial.setVec3(PhongShader3.LIGHT_KEY, light[0], light[1], light[2]);
        mWidgetMaterial.setVec3(PhongShader3.EYE_KEY, eye[0], eye[1], eye[2]);

        mWidgetMaterial2 = new SXRMaterial(mSXRContext, new SXRShaderId(PhongShader3.class));
        mWidgetMaterial2.setMainTexture(mWidgetTexture);
        mWidgetMaterial2.setVec4(PhongShader3.MAT1_KEY, matO[0], matO[4], matO[8], matO[12]);
        mWidgetMaterial2.setVec4(PhongShader3.MAT2_KEY, matO[1], matO[5], matO[9], matO[13]);
        mWidgetMaterial2.setVec4(PhongShader3.MAT3_KEY, matO[2], matO[6], matO[10], matO[14]);
        mWidgetMaterial2.setVec4(PhongShader3.MAT4_KEY, matO[3], matO[7], matO[11], matO[15]);
        mWidgetMaterial2.setVec3(PhongShader3.LIGHT_KEY, light[0], light[1], light[2]);
        mWidgetMaterial2.setVec3(PhongShader3.EYE_KEY, eye[0], eye[1], eye[2]);

        mWidgetButtonObject.getTransform().setPosition(0, 0, -EYE_TO_OBJECT - 1.5f);
        mWidgetButtonObject.getTransform().rotateByAxis(40.0f, 0.0f, 1.0f, 0.0f);
        mWidgetButtonObject.getRenderData().setRenderingOrder(SXRRenderData.SXRRenderingOrder.TRANSPARENT);

        mWdgetButtonObject2.getTransform().setPosition(0, 0, -EYE_TO_OBJECT - 1.5f);
        mWdgetButtonObject2.getTransform().rotateByAxis(-40.0f, 0.0f, 1.0f, 0.0f);
        mWdgetButtonObject2.getRenderData().setRenderingOrder(SXRRenderData.SXRRenderingOrder.TRANSPARENT);
        mScene.addSceneObject(mWidgetButtonObject);

        //@todo currently nothing shown in the second pane; the demo needs rework to actually take
        //advantage of a second panel
        mWdgetButtonObject2.setEnable(false);
        mScene.addSceneObject(mWdgetButtonObject2);
    }

}
