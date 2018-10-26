package com.example.org.sxrfapplication;

import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRMain;
import com.samsungxr.SXRMesh;
import com.samsungxr.SXRNode;
import com.samsungxr.SXRTexture;
import com.samsungxr.animation.SXRAnimator;
import com.samsungxr.animation.SXRRepeatMode;
import com.samsungxr.nodes.SXRNode;

import java.util.concurrent.Future;

/**
 * The Main Scene of the App
 */
public class MainScene extends SXRMain {

    @Override
    public void onInit(SXRContext sxrContext) throws Throwable {

        //Load Mesh
        SXRMesh dinoMesh = sxrContext.getAssetLoader().loadMesh(
                new SXRAndroidResource(sxrContext, "trex_mesh.fbx")
        );

        //Load Texture
        SXRTexture dinoTexture = sxrContext.getAssetLoader().loadTexture(
                new SXRAndroidResource(sxrContext, "trex_tex_diffuse.png")
        );

        //Create Node
        SXRNode dinoObj = new SXRNode(sxrContext, dinoMesh, dinoTexture);

        dinoObj.getTransform().setPosition(0,-3,-8);
        dinoObj.getTransform().rotateByAxis(-90, 1f, 0f, 0f);
        sxrContext.getMainScene().addNode(dinoObj);

        //Load animated model
//        SXRNode character = sxrContext.getAssetLoader().loadModel("astro_boy.dae");
//        character.getTransform().setRotationByAxis(45.0f, 0.0f, 1.0f, 0.0f);
//        character.getTransform().setScale(3, 3, 3);
//        character.getTransform().setPosition(0.0f, -0.4f, -0.5f);
//        sxrContext.getMainScene().addNode(character);
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
