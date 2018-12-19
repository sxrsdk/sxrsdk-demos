/*
 * Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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
