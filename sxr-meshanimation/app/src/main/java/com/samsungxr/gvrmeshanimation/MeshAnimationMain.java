package com.samsungxr.gvrmeshanimation;

import java.io.IOException;

import com.samsungxr.SXRActivity;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRScene;
import com.samsungxr.SXRMain;
import com.samsungxr.animation.SXRAnimation;
import com.samsungxr.animation.SXRAnimationEngine;
import com.samsungxr.animation.SXRAnimator;
import com.samsungxr.animation.SXRRepeatMode;
import com.samsungxr.SXRSceneObject;

import android.util.Log;

public class MeshAnimationMain extends SXRMain {

    private SXRContext mSXRContext;
    private SXRSceneObject mCharacter;

    private final String mModelPath = "TRex_NoGround.fbx";

    private SXRActivity mActivity;

    private static final String TAG = "MeshAnimationSample";

    private SXRAnimationEngine mAnimationEngine;
    SXRAnimator mAssimpAnimation = null;


    public MeshAnimationMain(SXRActivity activity) {
        mActivity = activity;
    }

    @Override
    public void onInit(SXRContext gvrContext) {
        mSXRContext = gvrContext;
        mAnimationEngine = gvrContext.getAnimationEngine();

        SXRScene mainScene = gvrContext.getMainScene();


        try {
            mCharacter = gvrContext.getAssetLoader().loadModel(mModelPath, mainScene);
            mCharacter.getTransform().setPosition(0.0f, -10.0f, -10.0f);
            mCharacter.getTransform().setRotationByAxis(40.0f, 1.0f, 0.0f, 0.0f);
            mCharacter.getTransform().setScale(1.5f, 1.5f, 1.5f);

            mAssimpAnimation = (SXRAnimator) mCharacter.getComponent(SXRAnimator.getComponentType());
            mAssimpAnimation.setRepeatMode(SXRRepeatMode.REPEATED);
            mAssimpAnimation.setRepeatCount(-1);
        } catch (IOException e) {
            e.printStackTrace();
            mActivity.finish();
            mActivity = null;
            Log.e(TAG, "One or more assets could not be loaded.");
        }
        mAssimpAnimation.start();
    }

    @Override
    public void onStep() {
    }
}