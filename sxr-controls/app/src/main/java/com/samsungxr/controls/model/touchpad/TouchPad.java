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

import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRSceneObject;
import com.samsungxr.controls.R;
import com.samsungxr.controls.input.TouchPadInput;
import com.samsungxr.controls.util.RenderingOrder;
import com.samsungxr.io.SXRTouchPadGestureListener;
import com.samsungxr.io.SXRTouchPadGestureListener.Action;

public class TouchPad extends SXRSceneObject {

    SXRSceneObject touchPad;
    IndicatorTap indicator;
    IndicatorLongPress indicatorLongPress;
    Arrow aroowUp;
    Arrow aroowDown;
    Arrow aroowLeft;
    Arrow aroowRight;

    static final float CORRETION_FACTOR_SCALE_INPUT_X = 0.005f;
    static final float CORRETION_FACTOR_SCALE_INPUT_Y = 0.005f;
    static final float CORRETION_FACTOR_CENTER_INPUT_X = 1300f;
    static final float CORRETION_FACTOR_CENTER_INPUT_Y = 700;
    static final float SCALE_OBJECT = 2f;

    public TouchPad(SXRContext sxrContext) {
        super(sxrContext);

        createTouchpad();

        createIndicator();

        createIndicatorLongPress();

        createArrows();

        this.addChildObject(aroowUp);
        this.addChildObject(aroowDown);
        this.addChildObject(aroowLeft);
        this.addChildObject(aroowRight);
        this.addChildObject(touchPad);
        this.addChildObject(indicator);
        this.addChildObject(indicatorLongPress);

    }

    private void createArrows() {

        aroowUp = new Arrow(getSXRContext(), 1f, 1f,
                getSXRContext().getAssetLoader().loadTexture(new SXRAndroidResource(getSXRContext(), R.drawable.swipe)), Arrow.UP);
        aroowDown = new Arrow(getSXRContext(), 1f, 1f,
                getSXRContext().getAssetLoader().loadTexture(new SXRAndroidResource(getSXRContext(), R.drawable.swipe)), Arrow.DOWN);
        aroowLeft = new Arrow(getSXRContext(), 1f, 1f,
                getSXRContext().getAssetLoader().loadTexture(new SXRAndroidResource(getSXRContext(), R.drawable.swipe)), Arrow.LEFT);
        aroowRight = new Arrow(getSXRContext(), 1f, 1f,
                getSXRContext().getAssetLoader().loadTexture(new SXRAndroidResource(getSXRContext(), R.drawable.swipe)), Arrow.RIGHT);

    }

    private void createIndicator() {
        indicator = new IndicatorTap(getSXRContext(), 1f, 1f,
                getSXRContext().getAssetLoader().loadTexture(new SXRAndroidResource(getSXRContext(), R.drawable.tap)));

    }

    private void createIndicatorLongPress() {
        indicatorLongPress = new IndicatorLongPress(getSXRContext(), 1f, 1f,
                getSXRContext().getAssetLoader().loadTexture(new SXRAndroidResource(getSXRContext(), R.drawable.longpress)));

    }

    private void createTouchpad() {
        touchPad = new SXRSceneObject(getSXRContext(),
                getSXRContext().getAssetLoader().loadMesh(new SXRAndroidResource(getSXRContext(), (R.raw.gear_vr))),
                getSXRContext().getAssetLoader().loadTexture(new SXRAndroidResource(getSXRContext(), R.drawable.gear_vr_texture)));
        touchPad.getTransform().setPositionZ(-4f);
        touchPad.getTransform().setPositionY(0.1f);
        touchPad.getTransform().setPositionX(-0.895f);
        touchPad.getTransform().rotateByAxis(-90, 0, 1, 0);
        touchPad.getTransform().setScale(SCALE_OBJECT, SCALE_OBJECT, SCALE_OBJECT);
        touchPad.getRenderData().setRenderingOrder(RenderingOrder.ORDER_RENDERING_GAMEPAD);
        
    }

    public void updateIndicator() {

        updateIndicatorTap();

        updateArrows();

        updateLongPress();

    }

    private void updateIndicatorTap() {
        if (TouchPadInput.getCurrent().buttonState.isSingleTap()) {

            indicator.pressed();

        }
        
    }



    private void updateLongPress() {

        if (TouchPadInput.getCurrent().buttonState.isLongPressed()) {

            indicatorLongPress.pressed();

        }
        
        if (TouchPadInput.getCurrent().buttonState.isUp()) {

            indicatorLongPress.pressedRelese();

        }

    }

    private void updateArrows() {

        switch (TouchPadInput.getCurrent().swipeDirection) {
            case SwipeUp:
                aroowUp.animateArrowOn();
                break;
            case SwipeBackward:
                aroowLeft.animateArrowOn();
                break;
            case SwipeDown:
                aroowDown.animateArrowOn();
                break;
            case SwipeForward:
                aroowRight.animateArrowOn();
                break;
            default:
                break;
        }

    }

}
