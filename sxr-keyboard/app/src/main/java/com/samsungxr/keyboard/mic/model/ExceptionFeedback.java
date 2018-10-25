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
package com.samsungxr.keyboard.mic.model;

import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRSceneObject;
import com.samsungxr.animation.SXRAnimation;
import com.samsungxr.animation.SXROpacityAnimation;
import com.samsungxr.animation.SXRScaleAnimation;
import com.samsungxr.keyboard.R;
import com.samsungxr.keyboard.interpolator.InterpolatorExpoEaseInOut;
import com.samsungxr.keyboard.model.AudioClip;
import com.samsungxr.keyboard.util.RenderingOrder;
import com.samsungxr.keyboard.util.SceneObjectNames;

public class ExceptionFeedback extends SXRSceneObject {

    private SXRSceneObject blurObject;
    private SXRSceneObject iconObject;
    private SXRSceneObject ringObject;
    private SXRAnimation blurScale, ringScale, blurOpacity, ringOpacity, iconOpacity;

    private static final float ANIMATION_TIME = 0.3f;

    public ExceptionFeedback(SXRContext context) {
        super(context);
        setName(SceneObjectNames.EXCEPTION_FEEDBACK);

        SXRAndroidResource mResourceGlow = new SXRAndroidResource(context,
                R.drawable.exception_glow);
        SXRAndroidResource mResourceIcon = new SXRAndroidResource(context,
                R.drawable.exception_icon);
        SXRAndroidResource mResourceRing = new SXRAndroidResource(context,
                R.drawable.exception_ring);

        blurObject = new SXRSceneObject(context, 2.8f, 2.8f, context.getAssetLoader().loadTexture(mResourceGlow));
        iconObject = new SXRSceneObject(context, 0.1f, 0.1f, context.getAssetLoader().loadTexture(mResourceIcon));
        ringObject = new SXRSceneObject(context, 1.4f, 1.4f, context.getAssetLoader().loadTexture(mResourceRing));

        this.addChildObject(blurObject);
        this.addChildObject(iconObject);
        this.addChildObject(ringObject);

        iconObject.getTransform().setPositionZ(-1.1f);
        ringObject.getTransform().setPositionZ(-1.2f);
        blurObject.getTransform().setPositionZ(-1.3f);

        blurObject.getTransform().setScale(1.2f, 1.2f, 1.2f);
        ringObject.getTransform().setScale(1.2f, 1.2f, 1.2f);

        blurObject.getRenderData().getMaterial().setOpacity(0);
        iconObject.getRenderData().getMaterial().setOpacity(0);
        ringObject.getRenderData().getMaterial().setOpacity(0);

        blurObject.getRenderData().setRenderingOrder(RenderingOrder.ORDER_RENDERING_EXCEPTION_BLUR);
        ringObject.getRenderData().setRenderingOrder(RenderingOrder.ORDER_RENDERING_EXCEPTION_RING);
        iconObject.getRenderData().setRenderingOrder(RenderingOrder.ORDER_RENDERING_EXCEPTION_ICON);

    }

    public void show() {

        AnimateScale(1);
        AnimateOpacity(1);
        getSXRContext().getPeriodicEngine().runAfter(new Runnable() {

            @Override
            public void run() {

                hide();

            }

        }, 2.0f);

        AudioClip.getInstance(getSXRContext().getContext()).playSound(AudioClip.getExceptionSoundID(), 1.0f, 1.0f);

    }

    private void hide() {

        AnimateScale(1.3f);
        AnimateOpacity(0);
    }

    private void AnimateScale(float scale) {

        blurScale = new SXRScaleAnimation(blurObject, ANIMATION_TIME, scale);
        blurScale.start(this.getSXRContext().getAnimationEngine());
        blurScale.setInterpolator(new InterpolatorExpoEaseInOut());
        ringScale = new SXRScaleAnimation(ringObject, ANIMATION_TIME, scale);
        ringScale.start(this.getSXRContext().getAnimationEngine());
        ringScale.setInterpolator(new InterpolatorExpoEaseInOut());
    }

    private void AnimateOpacity(float opacity) {

        blurOpacity = new SXROpacityAnimation(blurObject, ANIMATION_TIME, opacity);
        blurOpacity.start(this.getSXRContext().getAnimationEngine());
        blurOpacity.setInterpolator(new InterpolatorExpoEaseInOut());

        ringOpacity = new SXROpacityAnimation(ringObject, ANIMATION_TIME, opacity);
        ringOpacity.start(this.getSXRContext().getAnimationEngine());
        ringOpacity.setInterpolator(new InterpolatorExpoEaseInOut());

        iconOpacity = new SXROpacityAnimation(iconObject, ANIMATION_TIME, opacity);
        iconOpacity.start(this.getSXRContext().getAnimationEngine());
        iconOpacity.setInterpolator(new InterpolatorExpoEaseInOut());

    }

}
