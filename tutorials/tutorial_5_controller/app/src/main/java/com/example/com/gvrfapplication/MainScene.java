package com.example.org.gvrfapplication;

import android.util.Log;

import com.samsungxr.FutureWrapper;
import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRCursorController;
import com.samsungxr.SXRMain;
import com.samsungxr.SXRMesh;
import com.samsungxr.SXRScene;
import com.samsungxr.SXRSceneObject;
import com.samsungxr.SXRTexture;
import com.samsungxr.io.CursorControllerListener;
import com.samsungxr.io.SXRControllerType;
import com.samsungxr.io.SXRInputManager;

import java.util.ArrayList;

/**
 * The Main Scene of the App
 */
public class MainScene extends SXRMain {

    private SXRContext mContext;
    private SXRScene mMainScene;
    private static final float DEPTH = -1.5f;

    //Listener for add/removal of a controller
    private CursorControllerListener listener = new CursorControllerListener() {

        private SXRSceneObject cursor;
        private ArrayList<SXRCursorController> controllerList = new ArrayList<SXRCursorController>();

        @Override
        public void onCursorControllerAdded(SXRCursorController controller) {

            //Gaze Controller
            if (controller.getControllerType() == SXRControllerType.GAZE) {

                //Add controller cursor
                cursor = new SXRSceneObject(mContext,
                        mContext.createQuad(0.1f, 0.1f),
                        mContext.getAssetLoader().loadTexture(new SXRAndroidResource(mContext, R.raw.cursor))
                );
                cursor.getTransform().setPosition(0.0f, 0.0f, DEPTH);
                mMainScene.getMainCameraRig().addChildObject(cursor);
                cursor.getRenderData().setDepthTest(false);
                cursor.getRenderData().setRenderingOrder(100000);

                //Set controller position
                controller.setPosition(0.0f, 0.0f, DEPTH);
                controller.setNearDepth(DEPTH);
                controller.setFarDepth(DEPTH);
            } else {
                // disable all other types
                controller.setEnable(false);
            }

            controllerList.add(controller);
        }

        @Override
        public void onCursorControllerRemoved(SXRCursorController controller) {
            if (controller.getControllerType() == SXRControllerType.GAZE) {
                if (cursor != null) {
                    mMainScene.getMainCameraRig().removeChildObject(cursor);
                }
                controller.setEnable(false);
            }
        }
    };

    @Override
    public void onInit(SXRContext gvrContext) throws Throwable {

        mContext = gvrContext;
        mMainScene = gvrContext.getMainScene();

        //Load texture
        SXRTexture texture = gvrContext.getAssetLoader().loadTexture(new SXRAndroidResource(gvrContext, R.drawable.__default_splash_screen__));

        //Create a rectangle with the texture we just loaded
        SXRSceneObject quad = new SXRSceneObject(gvrContext, 4, 2, texture);
        quad.getTransform().setPosition(0, 0, -3);

        //Add rectangle to the scene
        gvrContext.getMainScene().addSceneObject(quad);

        //Listen controller events
        SXRInputManager input = gvrContext.getInputManager();
        input.addCursorControllerListener(listener);

        Log.i("GUI", "Add Controller Listener");
        for (SXRCursorController cursor : input.getCursorControllers()) {
            listener.onCursorControllerAdded(cursor);
        }
    }

    @Override
    public SplashMode getSplashMode() {
        return SplashMode.NONE;
    }

    @Override
    public void onStep() {
        //Add update logic here
    }
}

