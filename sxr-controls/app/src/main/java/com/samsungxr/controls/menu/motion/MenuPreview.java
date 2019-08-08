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

package com.samsungxr.controls.menu.motion;

import com.samsungxr.SXRContext;
import com.samsungxr.SXRMesh;
import com.samsungxr.SXRNode;
import com.samsungxr.SXRTexture;
import com.samsungxr.animation.SXRAnimation;
import com.samsungxr.animation.SXRInterpolator;
import com.samsungxr.animation.SXRPositionAnimation;
import com.samsungxr.animation.SXRRepeatMode;
import com.samsungxr.controls.anim.AnimationsTime;
import com.samsungxr.controls.model.Apple;
import com.samsungxr.controls.shaders.ColorSwapShader;

public class MenuPreview extends SXRNode {

    private final float APPLE_SCALE = 0.5f;
    private final float APPLE_INITIAL_POSITION_Y = 0.2f;
    private final float Y_ANIMATION_DELTA = -0.7f;

    private Apple apple;
    private SXRAnimation appleAnimation;
    private SXRInterpolator animationInterpolator = null;

    public MenuPreview(SXRContext sxrContext, SXRMesh mesh, SXRTexture texture) {
        super(sxrContext, mesh, texture);

        apple = new Apple(sxrContext);
        apple.getRenderData().getMaterial().setOpacity(0f);
        apple.getTransform().setPositionY(APPLE_INITIAL_POSITION_Y);
        apple.getTransform().setScale(APPLE_SCALE, APPLE_SCALE, APPLE_SCALE);
        addChildObject(apple);
    }

    public void show() {
        startAppleAnimation();
    }

    public void startAppleAnimation() {
        apple.getTransform().setPositionY(APPLE_INITIAL_POSITION_Y);
        apple.getRenderData().getMaterial().setOpacity(1f);

        appleAnimation = new SXRPositionAnimation(apple, AnimationsTime.getDropTime(), 0,
                Y_ANIMATION_DELTA, 0)
                .setInterpolator(animationInterpolator)
                .setRepeatMode(SXRRepeatMode.REPEATED)
                .setRepeatCount(-1)
                .start(getSXRContext()
                        .getAnimationEngine());
    }

    public void changeColorTo(float[] color) {

        apple.getRenderData().getMaterial()
                .setVec4(ColorSwapShader.COLOR, color[0], color[1], color[2], 1);

    }

    public void changeInterpolatorTo(SXRInterpolator interpolator) {
        animationInterpolator = interpolator;
        getSXRContext().getAnimationEngine().stop(appleAnimation);
        startAppleAnimation();
    }

    public void hide() {
        apple.getTransform().setPositionY(APPLE_INITIAL_POSITION_Y);
        apple.getRenderData().getMaterial().setOpacity(0f);
        getSXRContext().getAnimationEngine().stop(appleAnimation);
    }

    public void animationsTime(){
        getSXRContext().getAnimationEngine().stop(appleAnimation);
        startAppleAnimation();
    }
}