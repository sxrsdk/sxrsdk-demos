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

package com.samsungxr.controls.model.touchpad;

import com.samsungxr.SXRContext;
import com.samsungxr.SXRNode;
import com.samsungxr.SXRTexture;
import com.samsungxr.animation.SXRAnimation;
import com.samsungxr.animation.SXROnFinish;
import com.samsungxr.animation.SXROpacityAnimation;
import com.samsungxr.animation.SXRScaleAnimation;
import com.samsungxr.controls.util.RenderingOrder;

public class Arrow extends SXRNode {

    public static final float UP = 270;
    public static final float DOWN = 90;
    public static final float LEFT = 0;
    public static final float RIGHT = 180;

    private static final float TIME_TO_OFF = 0.6f;
    static final float SCALE_ARROW = 0.4f;
    private SXROpacityAnimation opacityAnimationOn;
    private SXROpacityAnimation opacityAnimationOff;
    private boolean finishAnimationOn = true;
    private boolean lockOff;
    private long lastSwip;
    SXRScaleAnimation scaleAnimation;

    public Arrow(SXRContext sxrContext, float width, float height, SXRTexture texture, float angle) {
        super(sxrContext, width, height, texture);
        createAroow(angle);

    }

    private void createAroow(float angle) {

        this.getTransform().setScale(SCALE_ARROW, SCALE_ARROW, SCALE_ARROW);
        this.getTransform().setPosition(-0.35f, 0f, 0.2f);
        this.getTransform().rotateByAxisWithPivot(angle, 0, 0, 1, 0, 0, 0);
        this.getRenderData().getMaterial().setOpacity(0);
        this.getRenderData().setRenderingOrder(RenderingOrder.ORDER_RENDERING_TOUCHPAD_AROOWS);

    }

    public void animateArrowOn() {

        if (finishAnimationOn && !lockOff) {

            if (opacityAnimationOn != null) {

                getSXRContext().getAnimationEngine().stop(opacityAnimationOn);

            } else {
                opacityAnimationOn = new SXROpacityAnimation(this, 1f, 1);

            }

            executeAnimationOn();
        } else {
            lockOff = true;

            lastSwip = System.currentTimeMillis();

            getSXRContext().getPeriodicEngine().runAfter(getRunnableOff(), TIME_TO_OFF +0.1f);

        }

    }

    private Runnable getRunnableOff() {
        return new Runnable() {

            @Override
            public void run() {

                tryOff();

            }
        };

    }

    private void tryOff() {
        if (System.currentTimeMillis() - lastSwip > (TIME_TO_OFF * 1000)) {

            animateArrowOff();

            lockOff = false;
        }

    }

    private void animateArrowOff() {

        if (opacityAnimationOff != null) {

            getSXRContext().getAnimationEngine().stop(opacityAnimationOff);

        } else {
            opacityAnimationOff = new SXROpacityAnimation(this, TIME_TO_OFF, 0);

        }

        opacityAnimationOff.start(getSXRContext().getAnimationEngine());

    }

    private void executeAnimationOn() {

        finishAnimationOn = false;

        opacityAnimationOn.setOnFinish(getSXROnFinish());

        opacityAnimationOn.start(getSXRContext().getAnimationEngine());

    }

    private SXROnFinish getSXROnFinish() {

        return new SXROnFinish() {

            @Override
            public void finished(SXRAnimation arg0) {
                finishAnimationOn = true;
                if (!lockOff)
                    animateArrowOff();
            }
        };

    }

}
