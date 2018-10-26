package com.example.org.sxrfapplication;

import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRMain;
import com.samsungxr.SXRMaterial;
import com.samsungxr.SXRPhongShader;
import com.samsungxr.SXRPointLight;
import com.samsungxr.SXRSceneObject;
import com.samsungxr.SXRTexture;
import com.samsungxr.scene_objects.SXRCubeSceneObject;
import com.samsungxr.scene_objects.SXRSphereSceneObject;

/**
 * The Main Scene of the App
 */
public class MainScene extends SXRMain {

    SXRCubeSceneObject mCube;
    SXRSphereSceneObject mSphere;


    @Override
    public void onInit(SXRContext sxrContext) throws Throwable {

        //Create Sphere
        mSphere = new SXRSphereSceneObject(sxrContext);
        mSphere.getTransform().setPosition(1, 0, -3);
        sxrContext.getMainScene().addSceneObject(mSphere);

        //Create Cube
        mCube = new SXRCubeSceneObject(sxrContext);
        mCube.getTransform().setPosition(-1, 0, -3);
        sxrContext.getMainScene().addSceneObject(mCube);

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

        SXRSceneObject lightNode = new SXRSceneObject(sxrContext);
        lightNode.getTransform().setPosition(0,0,0);
        lightNode.attachLight(pointLight);

        sxrContext.getMainScene().addSceneObject(lightNode);
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
