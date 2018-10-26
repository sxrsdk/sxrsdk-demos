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
import com.samsungxr.animation.SXROnFinish;
import com.samsungxr.animation.SXROpacityAnimation;
import com.samsungxr.animation.SXRRepeatMode;
import com.samsungxr.animation.SXRRotationByAxisAnimation;
import com.samsungxr.keyboard.R;
import com.samsungxr.keyboard.interpolator.InterpolatorExpoEaseIn;
import com.samsungxr.keyboard.interpolator.InterpolatorExpoEaseOut;
import com.samsungxr.keyboard.mic.model.MicItem;
import com.samsungxr.keyboard.util.RenderingOrder;
import com.samsungxr.keyboard.util.SceneObjectNames;

public class MicGroupProgress extends SXRSceneObject {
    MicItem mProgress;
    boolean isVisibleByOpacity;
    SXRAnimation mOpacityAnimation;
    SXRAnimation mRotationIn;
    SXRAnimation mRotationWork;
    SXRAnimation mRotationOut;
    public static float TIME_ANIMATION_ROTATION_IN = 1f;
    public static float TIME_ANIMATION_ROTATION_WORK = 0.5f;
    public static float TIME_ANIMATION_ROTATION_OUT = 3f;
    public static float ROTATION = -360;

    public MicGroupProgress(SXRContext sxrContext) {
        super(sxrContext);
        setName(SceneObjectNames.MIC_GROUP_PROGRESS);
        mProgress = new MicItem(sxrContext, R.drawable.mic_loading);
        mProgress.getRenderData().getMaterial().setOpacity(0);
        mProgress.getRenderData().setRenderingOrder(RenderingOrder.ORDER_RENDERING_PROGRESS);
        this.addChildObject(mProgress);
    }

    public void show() {
        workIn();

    }

    private void workIn() {
        stop(mRotationOut);
        stop(mRotationWork);
        animateOpacity(1, TIME_ANIMATION_ROTATION_IN);
        animateRotationIn();
    }

    public void hide() {
        workOut();

    }

    private void workOut() {
        stop(mRotationIn);
        stop(mRotationWork);
        animateOpacity(0, TIME_ANIMATION_ROTATION_OUT);
        animateRotationOut();
    }

    private void work() {

        stop(mRotationIn);

        animateRotationWork();
    }

    private void animateRotationIn() {

        mRotationIn = new SXRRotationByAxisAnimation(mProgress, TIME_ANIMATION_ROTATION_IN,
                ROTATION,
                0, 0, 1);
        mRotationIn.setInterpolator(new InterpolatorExpoEaseIn());
        mRotationIn.setOnFinish(new SXROnFinish() {

            @Override
            public void finished(SXRAnimation arg0) {
                work();

            }
        });
        mRotationIn.start(this.getSXRContext().getAnimationEngine());

    }

    private void animateRotationOut() {

        mRotationOut = new SXRRotationByAxisAnimation(mProgress, TIME_ANIMATION_ROTATION_OUT,
                ROTATION * 3,
                0, 0, 1);
        mRotationOut.setInterpolator(new InterpolatorExpoEaseOut());
        mRotationOut.start(this.getSXRContext().getAnimationEngine());

    }

    private void animateOpacity(int alpha, float time) {

        stop(mOpacityAnimation);
        mOpacityAnimation = new SXROpacityAnimation(mProgress, time, alpha);
        mOpacityAnimation.start(this.getSXRContext().getAnimationEngine());

    }

    private void animateRotationWork() {

        mRotationWork = new SXRRotationByAxisAnimation(mProgress, TIME_ANIMATION_ROTATION_WORK,
                ROTATION, 0, 0, 1);
        mRotationWork.setRepeatMode(SXRRepeatMode.REPEATED);
        mRotationWork.setRepeatCount(-1);
        mRotationWork.start(this.getSXRContext().getAnimationEngine());

    }

    private void stop(SXRAnimation animation) {
        if (animation != null) {
            this.getSXRContext().getAnimationEngine().stop(animation);

        }

    }

}
