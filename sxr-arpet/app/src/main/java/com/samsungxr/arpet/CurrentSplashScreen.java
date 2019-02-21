package com.samsungxr.arpet;

import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRNode;
import com.samsungxr.SXRPerspectiveCamera;
import com.samsungxr.SXRScene;
import com.samsungxr.SXRTexture;
import com.samsungxr.animation.SXRAnimation;
import com.samsungxr.animation.SXROnFinish;
import com.samsungxr.animation.SXROpacityAnimation;

public class CurrentSplashScreen {
    private SXRNode mSplashScreen;
    private SXRContext mContext;

    public CurrentSplashScreen(SXRContext context) {
        mContext = context;

        onInit();
    }

    private void onInit() {
        final SXRPerspectiveCamera cam = mContext.getMainScene().getMainCameraRig().getCenterCamera();
        final float aspect = cam.getAspectRatio();
        final float near = cam.getNearClippingDistance();
        final double fov = Math.toRadians(cam.getFovY());
        final float z = 1.0f;
        final float h = (float)(z * Math.tan(fov * 0.5f));
        final float w = aspect * h;

        SXRTexture tex = mContext.getAssetLoader().loadTexture(new SXRAndroidResource(mContext, R.drawable.splash_view));
        mSplashScreen = new SXRNode(mContext, 2 * w, 2 * h);
        mSplashScreen.getRenderData().getMaterial().setMainTexture(tex);
        mSplashScreen.getTransform().setPosition(0.0f, 0.0f, -z);
    }

    protected void onShow() {
        mContext.getMainScene().getMainCameraRig().addChildObject(mSplashScreen);
    }

    protected void onHide(final SXRScene mainScene) {
        SXROpacityAnimation mAnimation;
        mAnimation = new SXROpacityAnimation(mSplashScreen, .8f, 0);
        mAnimation.setOnFinish(new SXROnFinish() {
                                   @Override
                                   public void finished(SXRAnimation sxrAnimation) {
                                       mContext. getMainScene().getMainCameraRig().removeChildObject(mSplashScreen);
                                       mContext.setMainScene(mainScene);
                                   }
                               });
        mAnimation.start(mContext.getAnimationEngine());
    }

}
