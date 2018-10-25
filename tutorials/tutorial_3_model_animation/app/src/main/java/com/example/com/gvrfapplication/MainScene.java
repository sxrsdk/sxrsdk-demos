package com.example.org.gvrfapplication;

import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRMain;
import com.samsungxr.SXRMesh;
import com.samsungxr.SXRSceneObject;
import com.samsungxr.SXRTexture;
import com.samsungxr.animation.SXRAnimator;
import com.samsungxr.animation.SXRRepeatMode;
import com.samsungxr.scene_objects.SXRSceneObject;

import java.util.concurrent.Future;

/**
 * The Main Scene of the App
 */
public class MainScene extends SXRMain {

    @Override
    public void onInit(SXRContext gvrContext) throws Throwable {

        //Load Mesh
        SXRMesh dinoMesh = gvrContext.getAssetLoader().loadMesh(
                new SXRAndroidResource(gvrContext, "trex_mesh.fbx")
        );

        //Load Texture
        SXRTexture dinoTexture = gvrContext.getAssetLoader().loadTexture(
                new SXRAndroidResource(gvrContext, "trex_tex_diffuse.png")
        );

        //Create SceneObject
        SXRSceneObject dinoObj = new SXRSceneObject(gvrContext, dinoMesh, dinoTexture);

        dinoObj.getTransform().setPosition(0,-3,-8);
        dinoObj.getTransform().rotateByAxis(-90, 1f, 0f, 0f);
        gvrContext.getMainScene().addSceneObject(dinoObj);

        //Load animated model
//        SXRSceneObject character = gvrContext.getAssetLoader().loadModel("astro_boy.dae");
//        character.getTransform().setRotationByAxis(45.0f, 0.0f, 1.0f, 0.0f);
//        character.getTransform().setScale(3, 3, 3);
//        character.getTransform().setPosition(0.0f, -0.4f, -0.5f);
//        gvrContext.getMainScene().addSceneObject(character);
//
//        final SXRAnimator animator = (SXRAnimator)character.getComponent(SXRAnimator.getComponentType());
//        animator.setRepeatCount(-1);
//        animator.setRepeatMode(SXRRepeatMode.REPEATED);
//        animator.start();
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
