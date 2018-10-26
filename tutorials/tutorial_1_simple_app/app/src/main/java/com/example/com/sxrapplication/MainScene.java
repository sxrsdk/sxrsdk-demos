package com.example.org.sxrfapplication;

import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRMain;
import com.samsungxr.SXRPhongShader;
import com.samsungxr.SXRSceneObject;
import com.samsungxr.SXRTexture;
import com.samsungxr.scene_objects.SXRCubeSceneObject;

/**
 * The Main Scene of the App
 */
public class MainScene extends SXRMain {

    SXRSceneObject mCube;

    @Override
    public void onInit(SXRContext sxrContext) throws Throwable {

        //Create a cube
        mCube = new SXRCubeSceneObject(sxrContext);

        //Set position of the cube at (0, -2, -3)
        mCube.getTransform().setPosition(0, -2, -3);

        //Add cube to the scene
        sxrContext.getMainScene().addSceneObject(mCube);
    }

    @Override
    public SplashMode getSplashMode() {
        return SplashMode.NONE;
    }

    @Override
    public void onStep() {
        //Rotate the cube along the Y axis
        mCube.getTransform().rotateByAxis(1, 0, 1, 0);
    }
}
