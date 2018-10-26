package com.example.org.sxrfapplication;

import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRMain;
import com.samsungxr.SXRMaterial;
import com.samsungxr.SXRPhongShader;
import com.samsungxr.SXRPointLight;
import com.samsungxr.SXRNode;
import com.samsungxr.SXRTexture;
import com.samsungxr.nodes.SXRCubeNode;
import com.samsungxr.nodes.SXRSphereNode;

/**
 * The Main Scene of the App
 */
public class MainScene extends SXRMain {

    SXRCubeNode mCube;
    SXRSphereNode mSphere;


    @Override
    public void onInit(SXRContext sxrContext) throws Throwable {

        //Create Sphere
        mSphere = new SXRSphereNode(sxrContext);
        mSphere.getTransform().setPosition(1, 0, -3);
        sxrContext.getMainScene().addNode(mSphere);

        //Create Cube
        mCube = new SXRCubeNode(sxrContext);
        mCube.getTransform().setPosition(-1, 0, -3);
        sxrContext.getMainScene().addNode(mCube);

        /*******************
         * Assign solid color to Sphere
         ********************/
        SXRMaterial flatMaterial;
        flatMaterial = new SXRMaterial(sxrContext);
        flatMaterial.setColor(1.0f, 1.0f, 1.0f);
        mSphere.getRenderData().setMaterial(flatMaterial);


        /********************
         * Assign textured material to Cube
         *********************/
        //Load texture
        SXRTexture texture = sxrContext.getAssetLoader().loadTexture(new SXRAndroidResource(sxrContext, R.raw.crate_wood));

        SXRMaterial textureMaterial;
        textureMaterial = new SXRMaterial(sxrContext);
        textureMaterial.setMainTexture(texture);
        mCube.getRenderData().setMaterial(textureMaterial);


        /**************************
         * Create Light
         **************************/
        SXRPointLight pointLight;
        pointLight = new SXRPointLight(sxrContext);
        pointLight.setDiffuseIntensity(0.9f, 0.7f, 0.7f, 1.0f);

        SXRNode lightNode = new SXRNode(sxrContext);
        lightNode.getTransform().setPosition(0,0,0);
        lightNode.attachLight(pointLight);

        sxrContext.getMainScene().addNode(lightNode);
    }

    @Override
    public SplashMode getSplashMode() {
        return SplashMode.NONE;
    }

    @Override
    public void onStep() {
        mCube.getTransform().rotateByAxis(1, 0, 1, 0);
    }
}
