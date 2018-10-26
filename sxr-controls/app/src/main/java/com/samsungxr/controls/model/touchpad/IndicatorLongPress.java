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
import com.samsungxr.SXRSceneObject;
import com.samsungxr.SXRTexture;
import com.samsungxr.animation.SXROpacityAnimation;
import com.samsungxr.animation.SXRScaleAnimation;
import com.samsungxr.controls.util.RenderingOrder;

public class IndicatorLongPress extends SXRSceneObject {

    private static final float TIME_ANIMATION = 0.1f;
    static final float SCALE_OBJECT = 0.3f;
    private SXROpacityAnimation opacityAnimationOn;
    private SXRScaleAnimation scaleAnimationBiger;
    private SXROpacityAnimation opacityAnimationOff;
    private SXRScaleAnimation scaleAnimationSmaller;

    public IndicatorLongPress(SXRContext sxrContext, float width, float height, SXRTexture texture) {
        super(sxrContext, width, height, texture);

        this.getTransform().setPositionZ(0.1f);
        this.getTransform().setScale(SCALE_OBJECT, SCALE_OBJECT, SCALE_OBJECT);
        this.getRenderData().setRenderingOrder(RenderingOrder.ORDER_RENDERING_GAMEPAD_BUTTONS_EVENT);
        this.getRenderData().getMaterial().setOpacity(0);
    }

    public void pressed() {

        opacityAnimationOn = new SXROpacityAnimation(this, TIME_ANIMATION, 1);

        scaleAnimationBiger = new SXRScaleAnimation(this, TIME_ANIMATION, SCALE_OBJECT + 0.1f);

        opacityAnimationOn.start(getSXRContext().getAnimationEngine());

        scaleAnimationBiger.start(getSXRContext().getAnimationEngine());

    }

    public void pressedRelese() {
        
        opacityAnimationOff = new SXROpacityAnimation(this, TIME_ANIMATION, 0);

        scaleAnimationSmaller = new SXRScaleAnimation(this, TIME_ANIMATION, SCALE_OBJECT - 0.1f);

        opacityAnimationOff.start(getSXRContext().getAnimationEngine());

        scaleAnimationSmaller.start(getSXRContext().getAnimationEngine());

    }

}
