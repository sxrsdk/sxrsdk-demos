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

package com.samsungxr.controls.gamepad;

import android.content.res.TypedArray;

import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRMesh;
import com.samsungxr.SXRNode;
import com.samsungxr.SXRTexture;
import com.samsungxr.SXRTextureParameters;
import com.samsungxr.SXRTextureParameters.TextureFilterType;
import com.samsungxr.animation.SXRAnimation;
import com.samsungxr.animation.SXROnFinish;
import com.samsungxr.animation.SXROpacityAnimation;
import com.samsungxr.animation.SXRPositionAnimation;
import com.samsungxr.animation.SXRRepeatMode;
import com.samsungxr.animation.SXRRotationByAxisWithPivotAnimation;
import com.samsungxr.controls.R;
import com.samsungxr.controls.util.RenderingOrder;

public class GamepadButton extends SXRNode {

    private static final float DOWN_SIMPLE_BUTTON_TIME = 0.01f;
    private static final float DOWN_SIMPLE_BUTTON = 0.02f;
    private SXRTexture buttonTexture;
    private float pivotX;
    private float pivotY;
    private float pivotZ;
    private SXRNode buttonHover;
    private SXRNode buttonNormal;
    private SXROpacityAnimation animOpacity;
    private float evPositionX, evPositionY, evPositionZ, evRotationW;
    private SXRTexture eventTexture;
    private boolean isDown = false;

    public GamepadButton(SXRContext sxrContext, TypedArray array) {
        super(sxrContext);

        setName(array.getString(0));
        SXRTextureParameters parameters = new SXRTextureParameters(getSXRContext());
        parameters.setAnisotropicValue(16);
        parameters.setMinFilterType(TextureFilterType.GL_NEAREST_MIPMAP_NEAREST);
        parameters.setMagFilterType(TextureFilterType.GL_NEAREST_MIPMAP_NEAREST);
        buttonTexture = sxrContext.getAssetLoader().loadTexture(new SXRAndroidResource(
                sxrContext, R.drawable.gamepad_diffuse), parameters);

        eventTexture = sxrContext.getAssetLoader().loadTexture(new SXRAndroidResource(
                sxrContext, R.drawable.event_color));

        attachButton(array.getResourceId(1, -0));
        attachEvent(array.getResourceId(2, -0));

        pivotX = array.getFloat(3, -0);
        pivotY = array.getFloat(4, -0);
        pivotZ = array.getFloat(5, -0);

        array.recycle();
    }

    private void attachButton(int drawable) {

        SXRMesh buttonMesh = getSXRContext().getAssetLoader().loadMesh(new SXRAndroidResource(
                getSXRContext(), drawable));

        buttonNormal = new SXRNode(getSXRContext(), buttonMesh,
                buttonTexture);
        buttonNormal.getRenderData().setRenderingOrder(
                RenderingOrder.ORDER_RENDERING_GAMEPAD_BUTTONS);

        addChildObject(buttonNormal);
    }

    private void attachEvent(int drawable) {

        if (drawable == -0) {
            return;
        }

        SXRMesh dpadEventMesh = getSXRContext().getAssetLoader().loadMesh(new SXRAndroidResource(
                getSXRContext(), drawable));

        buttonHover = new SXRNode(getSXRContext(), dpadEventMesh, eventTexture);
        buttonHover.getRenderData().getMaterial().setOpacity(0);

        evPositionX = buttonHover.getTransform().getPositionX();
        evPositionY = buttonHover.getTransform().getPositionY();
        evPositionZ = buttonHover.getTransform().getPositionZ();
        evRotationW = buttonHover.getTransform().getRotationW();

        buttonHover.getRenderData().setRenderingOrder(
                RenderingOrder.ORDER_RENDERING_GAMEPAD_BUTTONS_EVENT);

        addChildObject(buttonHover);
    }

    public void moveToPosition(float x, float y, float z) {

        if (x != 0 || y != 0) {
            buttonHover.getRenderData().getMaterial().setOpacity(1);
        } else {
            buttonHover.getRenderData().getMaterial().setOpacity(0);
        }

        buttonHover.getTransform().setPosition(x * 0.14f, y * -0.14f, evPositionZ);
        buttonNormal.getTransform().setPosition(x * 0.02f, y * -0.02f, evPositionZ);
    }

    public void showButtonPressed(float angle) {

        buttonHover.getRenderData().getMaterial().setOpacity(0);

        buttonHover.getTransform().setPosition(evPositionX, evPositionY, evPositionZ);
        buttonHover.getTransform().setRotation(evRotationW, evPositionX, evPositionY, evPositionZ);

        SXRRotationByAxisWithPivotAnimation dpadRotation = new SXRRotationByAxisWithPivotAnimation(
                buttonHover, 0.001f, angle, 0, 0, 1, pivotX, pivotY, pivotZ);
        dpadRotation.setRepeatMode(SXRRepeatMode.ONCE);
        dpadRotation.setRepeatCount(1);
        dpadRotation.start(this.getSXRContext().getAnimationEngine());

        animOpacity = new SXROpacityAnimation(buttonHover, 2, 1);
        animOpacity.setRepeatMode(SXRRepeatMode.ONCE);
        animOpacity.setRepeatCount(1);
        animOpacity.setOnFinish(new SXROnFinish() {
            @Override
            public void finished(SXRAnimation sxrAnimation) {
                buttonHover.getRenderData().getMaterial().setOpacity(0);
            }
        });

        animOpacity.start(getSXRContext().getAnimationEngine());
    }

    public void actionPressedLR(boolean pressed) {

        if (pressed) {

            buttonNormal.getRenderData().getMaterial().setOpacity(0f);
            buttonHover.getRenderData().getMaterial().setOpacity(1f);

        } else {

            buttonNormal.getRenderData().getMaterial().setOpacity(1f);
            buttonHover.getRenderData().getMaterial().setOpacity(0.f);
        }
    }

    public void handlerButtonStates(boolean pressed) {

        if (pressed) {

            buttonHover.getRenderData().getMaterial().setOpacity(0.5f);

            if (!isDown) {

                SXRPositionAnimation eventDown = new SXRPositionAnimation(this,
                        DOWN_SIMPLE_BUTTON_TIME,
                        this.getTransform().getPositionX(),
                        this.getTransform().getPositionY(),
                        this.getTransform().getPositionZ() - DOWN_SIMPLE_BUTTON);

                eventDown.setRepeatMode(SXRRepeatMode.ONCE);
                eventDown.setRepeatCount(1);
                eventDown.start(this.getSXRContext().getAnimationEngine());

                isDown = true;
            }

        } else {

            buttonHover.getRenderData().getMaterial().setOpacity(0.f);

            if (isDown) {

                SXRPositionAnimation evDown = new SXRPositionAnimation(this,
                        DOWN_SIMPLE_BUTTON_TIME,
                        this.getTransform().getPositionX(),
                        this.getTransform().getPositionY(),
                        evPositionZ - this.getTransform().getPositionZ());

                evDown.setRepeatMode(SXRRepeatMode.ONCE);
                evDown.setRepeatCount(1);
                evDown.start(this.getSXRContext().getAnimationEngine());

                isDown = false;
            }
        }
    }

    public void showEvent() {

        if (animOpacity != null) {
            this.getSXRContext().getAnimationEngine().stop(animOpacity);
        }

        animOpacity = new SXROpacityAnimation(buttonHover, 0.6f, 1);
        animOpacity.setRepeatMode(SXRRepeatMode.ONCE);
        animOpacity.setRepeatCount(1);
        animOpacity.setOnFinish(new SXROnFinish() {
            @Override
            public void finished(SXRAnimation sxrAnimation) {
                buttonHover.getRenderData().getMaterial().setOpacity(0);
            }
        });

        animOpacity.start(getSXRContext().getAnimationEngine());
    }
}
