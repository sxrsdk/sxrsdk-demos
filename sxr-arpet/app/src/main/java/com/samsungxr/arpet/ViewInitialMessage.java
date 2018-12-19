package com.samsungxr.arpet;

import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;

import com.samsungxr.SXRRenderData;
import com.samsungxr.SXRScene;
import com.samsungxr.animation.SXROpacityAnimation;
import com.samsungxr.arpet.constant.PetConstants;
import com.samsungxr.arpet.mode.BasePetView;
import com.samsungxr.nodes.SXRViewNode;

public class ViewInitialMessage extends BasePetView {
    private SXRViewNode mViewInitialMessage;

    public ViewInitialMessage(PetContext context) {
        super(context);

        onInit();
    }

    @Override
    protected void onShow(SXRScene mainScene) {
        SXROpacityAnimation mAnimation;
        mAnimation = new SXROpacityAnimation(mViewInitialMessage, .8f, 1);
        mAnimation.setOnFinish(sxrAnimation -> {
            mainScene.getMainCameraRig().addChildObject(mViewInitialMessage);
        });
        mAnimation.start(mPetContext.getSXRContext().getAnimationEngine());
        setEnable(true);
    }

    @Override
    protected void onHide(SXRScene mainScene) {
        SXROpacityAnimation mAnimation;
        mAnimation = new SXROpacityAnimation(mViewInitialMessage, .8f, 0);
        mAnimation.setOnFinish(sxrAnimation -> {
            mainScene.getMainCameraRig().removeChildObject(mViewInitialMessage);
        });
        mAnimation.start(mPetContext.getSXRContext().getAnimationEngine());
        setEnable(false);
    }


    private void onInit() {
        final DisplayMetrics metrics = new DisplayMetrics();
        mPetContext.getActivity().getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
        ViewGroup view = (ViewGroup) View.inflate(mPetContext.getActivity(), R.layout.view_initial_message  ,null);
        view.setLayoutParams(new ViewGroup.LayoutParams(metrics.widthPixels, metrics.heightPixels));
        mViewInitialMessage = new SXRViewNode(mPetContext.getSXRContext(), view);
        mViewInitialMessage.setTextureBufferSize(PetConstants.TEXTURE_BUFFER_SIZE);
        mViewInitialMessage.getRenderData().setRenderingOrder(SXRRenderData.SXRRenderingOrder.OVERLAY);
        mViewInitialMessage.getTransform().setPosition(0f, 0f, -0.74f);
    }

}
