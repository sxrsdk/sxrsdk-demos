package com.samsungxr.arpet;

import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;

import com.samsungxr.SXRRenderData;
import com.samsungxr.SXRScene;
import com.samsungxr.nodes.SXRViewNode;

import com.samsungxr.arpet.constant.PetConstants;
import com.samsungxr.arpet.mode.BasePetView;
import com.samsungxr.arpet.mode.OnClickExitScreen;

public class ExitApplicationScreen extends BasePetView implements View.OnClickListener {
    private SXRViewNode mExitApplicationScreen;
    private OnClickExitScreen mClickExitScreenListener;

    public ExitApplicationScreen(PetContext context) {
        super(context);

        onInit();
    }

    @Override
    protected void onShow(SXRScene mainScene) {
        mainScene.getMainCameraRig().addChildObject(this);
        setEnable(true);
    }

    @Override
    protected void onHide(SXRScene mainScene) {
        mainScene.getMainCameraRig().removeChildObject(this);
        setEnable(false);
    }

    public void setClickExitScreenListener(OnClickExitScreen Listener) {
        mClickExitScreenListener = Listener;
    }

    private void onInit() {
        mClickExitScreenListener = null;
        final DisplayMetrics metrics = new DisplayMetrics();
        mPetContext.getActivity().getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
        ViewGroup view = (ViewGroup) View.inflate(mPetContext.getActivity(), R.layout.screen_exit_application, null);
        view.setLayoutParams(new ViewGroup.LayoutParams(metrics.widthPixels, metrics.heightPixels));
        mExitApplicationScreen = new SXRViewNode(mPetContext.getSXRContext(), view);
        mExitApplicationScreen.setTextureBufferSize(PetConstants.TEXTURE_BUFFER_SIZE);
        mExitApplicationScreen.getRenderData().setRenderingOrder(SXRRenderData.SXRRenderingOrder.OVERLAY);
        mExitApplicationScreen.getTransform().setPosition(0f, 0f, -0.74f);
        addChildObject(mExitApplicationScreen);
        setEnable(false);
        view.findViewById(R.id.cancel_button_screen).setOnClickListener(this);
        view.findViewById(R.id.button_confirm).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (mClickExitScreenListener == null) {
            return;
        }

        if (view.getId() == R.id.cancel_button_screen) {
            mClickExitScreenListener.OnCancel();
        } else {
            mClickExitScreenListener.OnConfirm();
        }
    }
}
