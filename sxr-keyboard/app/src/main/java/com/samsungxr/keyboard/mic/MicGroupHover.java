/* Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.samsungxr.keyboard.mic;

import com.samsungxr.SXRContext;
import com.samsungxr.SXRSceneObject;
import com.samsungxr.animation.SXRAnimation;
import com.samsungxr.animation.SXROpacityAnimation;
import com.samsungxr.animation.SXRScaleAnimation;
import com.samsungxr.keyboard.R;
import com.samsungxr.keyboard.mic.model.MicItem;
import com.samsungxr.keyboard.util.RenderingOrder;
import com.samsungxr.keyboard.util.SceneObjectNames;

public class MicGroupHover extends SXRSceneObject {

    private boolean isVisibleByOpacity = false;
    private SXRAnimation mOpacityAnimation;
    private SXRAnimation mScaleAnimation;
    int mOff = 0;
    int mOn = 1;

    MicItem mHover;

    public MicGroupHover(SXRContext sxrContext) {
        super(sxrContext);
        setName(SceneObjectNames.MIC_GROUP_HOVER);
        mHover = new MicItem(sxrContext, R.drawable.mic_hover);
        mHover.getRenderData().setRenderingOrder(RenderingOrder.ORDER_RENDERING_HOVER);
        mHover.getRenderData().getMaterial().setOpacity(0);
        mHover.getTransform().setScale(0.94f, 0.94f, 0.94f);
        this.addChildObject(mHover);
    }

    public void animateOpacityOff() {

        if (isVisibleByOpacity) {
            isVisibleByOpacity = false;
            stop(mOpacityAnimation);
            mOpacityAnimation = new SXROpacityAnimation(mHover, 1, mOff);
            mOpacityAnimation.start(this.getSXRContext().getAnimationEngine());

        }
    }

    public void animateOpacityOn() {

        if (!isVisibleByOpacity) {
            isVisibleByOpacity = true;
            stop(mOpacityAnimation);
            mOpacityAnimation = new SXROpacityAnimation(mHover, 1, mOn);
            mOpacityAnimation.start(this.getSXRContext().getAnimationEngine());

        }
    }

    // public void animateOpacity(int i) {
    //
    // if ((!isVisibleByOpacity && i == 1) || (isVisibleByOpacity && i == 0)) {
    // isVisibleByOpacity = i == 1 ? true : false;
    // stop(mOpacityAnimation);
    // mOpacityAnimation = new SXROpacityAnimation(mHover, 1, i);
    // mOpacityAnimation.start(this.getSXRContext().getAnimationEngine());
    //
    // }
    // }

    private void stop(SXRAnimation opacityAnimation) {

        if (opacityAnimation != null) {
            this.getSXRContext().getAnimationEngine().stop(opacityAnimation);
        }
    }

    private void animateScale(float scale) {
        mScaleAnimation = new SXRScaleAnimation(mHover, 1, scale);
        mScaleAnimation.start(this.getSXRContext().getAnimationEngine());
    }

    public void show() {
        animateOpacityOn();
        scale();
    }

    public void hide() {
        animateOpacityOff();
        scale();
    }

    private void scale() {
        float scale = !isVisibleByOpacity ? 0.94f : 1f;

        animateScale(scale);
    }
}
