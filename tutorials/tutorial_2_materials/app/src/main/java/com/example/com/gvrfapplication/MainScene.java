package com.example.org.gvrfapplication;

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
    public void onInit(SXRContext gvrContext) throws Throwable {

        //Create Sphere
        mSphere = new SXRSphereSceneObject(gvrContext);
        mSphere.getTransform().setPosition(1, 0, -3);
        gvrContext.getMainScene().addSceneObject(mSphere);

        //Create Cube
        mCube = new SXRCubeSceneObject(gvrContext);
        mCube.getTransform().setPosition(-1, 0, -3);
        gvrContext.getMainScene().addSceneObject(mCube);

        /*******************
         * Assign solid color to Sphere
         ********************/
        SXRMaterial flatMaterial;
        flatMaterial = new SXRMaterial(gvrContext);
        flatMaterial.setColor(1.0f, 1.0f, 1.0f);
        mSphere.getRenderData().setMaterial(flatMaterial);


        /********************
         * Assign textured material to Cube
         *********************/
        //Load texture
        SXRTexture texture = gvrContext.getAssetLoader().loadTexture(new SXRAndroidResource(gvrContext, R.raw.crate_wood));

        SXRMaterial textureMaterial;
        textureMaterial = new SXRMaterial(gvrContext);
        textureMaterial.setMainTexture(texture);
        mCube.getRenderData().setMaterial(textureMaterial);


        /**************************
         * Create Light
         **************************/
        SXRPointLight pointLight;
        pointLight = new SXRPointLight(gvrContext);
        pointLight.setDiffuseIntensity(0.9f, 0.7f, 0.7f, 1.0f);

        SXRSceneObject lightNode = new SXRSceneObject(gvrContext);
        lightNode.getTransform().setPosition(0,0,0);
        lightNode.attachLight(pointLight);

        gvrContext.getMainScene().addSceneObject(lightNode);
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
