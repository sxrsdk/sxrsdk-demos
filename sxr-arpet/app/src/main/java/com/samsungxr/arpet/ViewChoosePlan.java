package com.samsungxr.arpet;

import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRNode;
import com.samsungxr.SXRPerspectiveCamera;
import com.samsungxr.SXRScene;
import com.samsungxr.SXRTexture;
import com.samsungxr.animation.SXROpacityAnimation;
import com.samsungxr.arpet.mode.BasePetView;

public class ViewChoosePlan extends BasePetView {
    private SXRNode mViewChoosePlan;

    public ViewChoosePlan(PetContext petContext) {
        super(petContext);

        onInit();
    }

    @Override
    protected void onShow(SXRScene mainScene) {
        SXROpacityAnimation mAnimation;
        mAnimation = new SXROpacityAnimation(mViewChoosePlan, .8f, 1);
        mAnimation.setOnFinish(sxrAnimation -> {
            mPetContext.getMainScene().getMainCameraRig().addChildObject(mViewChoosePlan);
        });
        mAnimation.start(mPetContext.getSXRContext().getAnimationEngine());
        setEnable(true);
    }

    @Override
    protected void onHide(SXRScene mainScene) {
        SXROpacityAnimation mAnimation;
        mAnimation = new SXROpacityAnimation(mViewChoosePlan, .8f, 0);
        mAnimation.setOnFinish(sxrAnimation -> {
            mPetContext.getMainScene().getMainCameraRig().removeChildObject(mViewChoosePlan);
        });
        mAnimation.start(mPetContext.getSXRContext().getAnimationEngine());
        setEnable(false);
    }


    private void onInit() {
        final SXRPerspectiveCamera cam = mPetContext.getMainScene().getMainCameraRig().getCenterCamera();
        final float aspect = cam.getAspectRatio();
        final double fov = Math.toRadians(cam.getFovY());
        final float z = 1.0f;
        final float h = (float) (z * Math.tan(fov * 0.5f));
        final float w = aspect * h;


        SXRTexture tex = mPetContext.getSXRContext().getAssetLoader().loadTexture(new SXRAndroidResource(mPetContext.getSXRContext(), R.drawable.view_tap_plane));
        mViewChoosePlan = new SXRNode(mPetContext.getSXRContext(), 0.8f * w, 1 * h);
        mViewChoosePlan.getRenderData().getMaterial().setMainTexture(tex);
        mViewChoosePlan.getTransform().setPosition(0.0f, -0.12f, -z);
    }
}
