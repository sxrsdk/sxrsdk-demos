package com.example.org.sxrfapplication;

import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRMain;
import com.samsungxr.SXRPhongShader;
import com.samsungxr.SXRNode;
import com.samsungxr.SXRTexture;
import com.samsungxr.nodes.SXRCubeNode;

/**
 * The Main Scene of the App
 */
public class MainScene extends SXRMain {

    SXRNode mCube;

    @Override
    public void onInit(SXRContext sxrContext) throws Throwable {

        //Create a cube
        mCube = new SXRCubeNode(sxrContext);

        //Set position of the cube at (0, -2, -3)
        mCube.getTransform().setPosition(0, -2, -3);

        //Add cube to the scene
        sxrContext.getMainScene().addNode(mCube);
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
