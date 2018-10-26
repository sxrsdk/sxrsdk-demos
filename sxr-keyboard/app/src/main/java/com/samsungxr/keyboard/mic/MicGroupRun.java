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
import com.samsungxr.animation.SXRRepeatMode;
import com.samsungxr.animation.SXRScaleAnimation;
import com.samsungxr.keyboard.R;
import com.samsungxr.keyboard.interpolator.InterpolatorExpoEaseOut;
import com.samsungxr.keyboard.mic.model.MicItem;
import com.samsungxr.keyboard.util.RenderingOrder;
import com.samsungxr.keyboard.util.SceneObjectNames;

public class MicGroupRun extends SXRSceneObject {

    private static final float ANIMATION_TIME = 2f;
    private static final float DELAY_TIME = 3 / 5f;

    MicItem[] mMicRunItens;
    SXRAnimation[] mOpacityAnimation;
    SXRScaleAnimation[] mScaleAnimation;

    private static final int NUMBER_OF_WAVES = 3;

    public MicGroupRun(SXRContext sxrContext) {
        super(sxrContext);
        setName(SceneObjectNames.MIC_GROUP_RUN);

        mMicRunItens = new MicItem[NUMBER_OF_WAVES];
        mOpacityAnimation = new SXRAnimation[NUMBER_OF_WAVES];
        mScaleAnimation = new SXRScaleAnimation[NUMBER_OF_WAVES];

        for (int i = 0; i < NUMBER_OF_WAVES; i++) {

            mMicRunItens[i] = new MicItem(sxrContext, R.drawable.mic_active);
            mMicRunItens[i].getRenderData().getMaterial().setOpacity(0);
            mMicRunItens[i].getRenderData().setRenderingOrder(
                    RenderingOrder.ORDER_RENDERING_RUN - i);
            mMicRunItens[i].getTransform().setScale(0, 0, 0);
            mMicRunItens[i].getTransform().setPosition(0, 0, -(0.001f * i));

            this.addChildObject(mMicRunItens[i]);
        }
    }

    public void show() {

        animateScale();
        animateOpacityOn();
    }

    public void hide() {

        for (int i = 0; i < NUMBER_OF_WAVES; i++) {
            if (mOpacityAnimation[i] != null) {
                mOpacityAnimation[i].setRepeatMode(SXRRepeatMode.ONCE);
            }
            if (mScaleAnimation[i] != null) {
                mScaleAnimation[i].setRepeatMode(SXRRepeatMode.ONCE);
            }
        }

        animateOpacityOff();
    }

    private void animateOpacityOff() {

        for (int i = 0; i < NUMBER_OF_WAVES; i++) {

            mMicRunItens[i].getTransform().setScale(0, 0, 0);
            mMicRunItens[i].getRenderData().getMaterial().setOpacity(0);

            final int iterator = i;

            getSXRContext().getPeriodicEngine().runAfter(new Runnable() {

                @Override
                public void run() {

                    mOpacityAnimation[iterator] = new SXROpacityAnimation(mMicRunItens[iterator],
                            ANIMATION_TIME, 0);
                    mOpacityAnimation[iterator].setInterpolator(new InterpolatorExpoEaseOut());
                    mOpacityAnimation[iterator].setRepeatMode(SXRRepeatMode.ONCE);
                    mOpacityAnimation[iterator].setRepeatCount(1);
                    mOpacityAnimation[iterator].start(getSXRContext().getAnimationEngine());
                }

            }, DELAY_TIME * i);
        }
    }

    private void animateOpacityOn() {

        for (int i = 0; i < NUMBER_OF_WAVES; i++) {

            mMicRunItens[i].getTransform().setScale(0, 0, 0);
            mMicRunItens[i].getRenderData().getMaterial().setOpacity(0);

            final int iterator = i;

            getSXRContext().getPeriodicEngine().runAfter(new Runnable() {

                @Override
                public void run() {

                    mOpacityAnimation[iterator] = new SXROpacityAnimation(mMicRunItens[iterator],
                            ANIMATION_TIME, 1);
                    mOpacityAnimation[iterator].setInterpolator(new InterpolatorExpoEaseOut());
                    mOpacityAnimation[iterator].setRepeatMode(SXRRepeatMode.REPEATED);
                    mOpacityAnimation[iterator].setRepeatCount(-1);
                    mOpacityAnimation[iterator].start(getSXRContext().getAnimationEngine());
                }

            }, DELAY_TIME * i);
        }
    }

    private void animateScale() {

        for (int i = 0; i < NUMBER_OF_WAVES; i++) {

            final int iterator = i;

            getSXRContext().getPeriodicEngine().runAfter(new Runnable() {

                @Override
                public void run() {

                    mScaleAnimation[iterator] = new SXRScaleAnimation(mMicRunItens[iterator],
                            ANIMATION_TIME, 1);
                    mScaleAnimation[iterator].setRepeatMode(SXRRepeatMode.REPEATED);
                    mScaleAnimation[iterator].setRepeatCount(-1);
                    mScaleAnimation[iterator].setInterpolator(new InterpolatorExpoEaseOut());
                    mScaleAnimation[iterator].start(getSXRContext().getAnimationEngine());
                }

            }, DELAY_TIME * i);
        }
    }
}
