package com.example.org.sxrfapplication;

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
    public void onInit(SXRContext sxrContext) throws Throwable {

        //Load animated model
        SXRSceneObject character = sxrContext.getAssetLoader().loadModel("astro_boy.dae");
        character.getTransform().setRotationByAxis(45.0f, 0.0f, 1.0f, 0.0f);
        character.getTransform().setScale(6, 6, 6);
        character.getTransform().setPosition(0.0f, -0.5f, -1f);
        sxrContext.getMainScene().addSceneObject(character);

        SXRAnimator animator = (SXRAnimator)character.getComponent(SXRAnimator.getComponentType());
        animator.setRepeatCount(-1);
        animator.setRepeatMode(SXRRepeatMode.REPEATED);
        animator.start();
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
