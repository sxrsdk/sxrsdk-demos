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

package com.samsungxr.controls.menu.scale;

import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRTexture;
import com.samsungxr.animation.SXRPositionAnimation;
import com.samsungxr.controls.Main;
import com.samsungxr.controls.R;
import com.samsungxr.controls.Worm;
import com.samsungxr.controls.anim.AnimationsTime;
import com.samsungxr.controls.anim.ScaleWorm;
import com.samsungxr.controls.focus.ControlNode;
import com.samsungxr.controls.focus.GamepadTouchImpl;
import com.samsungxr.controls.focus.TouchAndGestureImpl;
import com.samsungxr.controls.input.GamepadMap;
import com.samsungxr.controls.menu.ItemSelectedListener;
import com.samsungxr.controls.menu.MenuWindow;
import com.samsungxr.controls.menu.RadioButtonNode;
import com.samsungxr.controls.menu.RadioGrupoNode;
import com.samsungxr.controls.menu.TouchableButton;

import java.util.ArrayList;

public class ScaleMenu extends MenuWindow {

    private static final float WORM_SCALE_FACTOR = 0.1f;
    private static final float ARROW_X_POSITION = 1.35f;
    private static final float ARROW_Y_POSITION = -0.7f;
    private static final float WORM_Y_POSITION = -1.0f;
    private final float WORM_HEAD_X_POSITION = -0.2f;
    private final float WORM_MIDDLE_X_POSITION = 0f;
    private final float WORM_END_X_POSITION = 0.16f;
    private Worm worm;
    private TouchableButton minusSign;
    private TouchableButton plusSign;

    private RadioGrupoNode radioGroup;

    public ScaleMenu(SXRContext sxrContext) {
        super(sxrContext);
        addWorm(sxrContext);

        addScaleSigns(sxrContext);

        attachRadioGroup();
    }

    private void addWorm(SXRContext sxrContext) {
        worm = new Worm(sxrContext);
        worm.getTransform().setPositionZ(2.7f);
        worm.getTransform().setPositionY(0.20f);

        worm.getHead().getParent().getTransform().setPositionY(WORM_Y_POSITION);
        worm.getMiddle().getTransform().setPositionY(WORM_Y_POSITION);
        worm.getEnd().getTransform().setPositionY(WORM_Y_POSITION);

        worm.getHead().getTransform().setPositionX(WORM_HEAD_X_POSITION);
        worm.getHead().getTransform().rotateByAxis(90, 0, 1, 0);

        worm.getMiddle().getTransform().setPositionX(WORM_MIDDLE_X_POSITION);
        worm.getMiddle().getTransform().rotateByAxis(90, 0, 1, 0);

        worm.getEnd().getTransform().setPositionX(WORM_END_X_POSITION);
        worm.getEnd().getTransform().rotateByAxis(90, 0, 1, 0);
        
        ScaleWorm.putWormPreviewReference(worm);
    }

    private void attachRadioGroup() {

        radioGroup = new RadioGrupoNode(getSXRContext(), new ItemSelectedListener() {

            @Override
            public void selected(ControlNode object) {

                RadioButtonNode button = (RadioButtonNode) object;

                AnimationsTime.setScaleTime(button.getSecond());
            }
            
        }, 0.2f, 0.5f, 1f);

        radioGroup.getTransform().setPosition(-.5f, -1.24f, 0f);

        addChildObject(radioGroup);
    }

    private void addScaleSigns(SXRContext sxrContext) {
        addMinusSign(sxrContext);
        addPlusSign(sxrContext);
    }

    private void addMinusSign(SXRContext sxrContext) {
        final ArrayList<SXRTexture> minusSignTextures = createTextureList(getSXRContext(),
                R.drawable.scale_less_idle, R.drawable.scale_less_hover,
                R.drawable.scale_less_pressed);
        minusSign = new TouchableButton(sxrContext, minusSignTextures);
        addMinusSignTouchListener(sxrContext);
        addMinusSignGamepadListener();
        minusSign.getTransform().setPositionX(-ARROW_X_POSITION);
        minusSign.getTransform().setPositionY(ARROW_Y_POSITION);
    }

    private void addMinusSignTouchListener(SXRContext sxrContext) {
        minusSign.setTouchAndGesturelistener(new TouchAndGestureImpl() {

            @Override
            public void pressed() {
                super.pressed();

                ScaleWorm.setLastSize();
                pressedMinus(-1);
            }

            @Override
            public void up() {
                super.up();
                minusSign.unPressButton();
            }
        });
    }

    private void addMinusSignGamepadListener() {
        minusSign.setGamepadTouchListener(new GamepadTouchImpl() {

            @Override
            public void pressed(Integer code) {
                super.pressed(code);

                ScaleWorm.setLastSize();

                pressedMinus(code);
            }

            @Override
            public void up(Integer code) {
                super.up(code);
                if (isActionButton(code)) {
                    minusSign.unPressButton();
                }
            }
        });
    }

    private void addPlusSign(SXRContext sxrContext) {
        final ArrayList<SXRTexture> plusSignTextures = createTextureList(getSXRContext(),
                R.drawable.scale_more_idle, R.drawable.scale_more_hover,
                R.drawable.scale_more_pressed);
        plusSign = new TouchableButton(sxrContext, plusSignTextures);

        addPlusSignTouchListener();
        addPlusSignGamepadListener();
        plusSign.getTransform().setPositionX(ARROW_X_POSITION);
        plusSign.getTransform().setPositionY(ARROW_Y_POSITION);
    }

    private void addPlusSignTouchListener() {
        plusSign.setTouchAndGesturelistener(new TouchAndGestureImpl() {

            @Override
            public void pressed() {
                super.pressed();

                ScaleWorm.setLastSize();
                pressedPlus(-1);
            }

            @Override
            public void up() {
                super.up();

                plusSign.unPressButton();
            }
        });
    }

    private void addPlusSignGamepadListener() {
        plusSign.setGamepadTouchListener(new GamepadTouchImpl() {

            @Override
            public void pressed(Integer code) {
                super.pressed(code);

                ScaleWorm.setLastSize();

                pressedPlus(code);
            }

            @Override
            public void up(Integer code) {
                super.up(code);

                if (isActionButton(code)) {
                    plusSign.unPressButton();
                }
            }
        });
    }

    private ArrayList<SXRTexture> createTextureList(SXRContext sxrContext, int res1, int res2,
            int res3) {
        ArrayList<SXRTexture> textureList = new ArrayList<SXRTexture>();
        textureList.add(sxrContext.getAssetLoader().loadTexture(new SXRAndroidResource(getSXRContext(), res1)));
        textureList.add(sxrContext.getAssetLoader().loadTexture(new SXRAndroidResource(getSXRContext(), res2)));
        textureList.add(sxrContext.getAssetLoader().loadTexture(new SXRAndroidResource(getSXRContext(), res3)));
        return textureList;
    }

    @Override
    protected void show() {
        
        radioGroup.show();
        
        removeChildObject(worm);
        addChildObject(worm);

        removeChildObject(minusSign);
        addChildObject(minusSign);

        removeChildObject(plusSign);
        addChildObject(plusSign);
    }

    @Override
    protected void hide() {
        
        radioGroup.hide();
        
        removeChildObject(worm);
        removeChildObject(minusSign);
        removeChildObject(plusSign);
    }

    private boolean scaleWorms(float scaleFactor) {
        boolean bool = true;
        float scale = worm.getHead().getTransform().getScaleX() + scaleFactor;
        if (scale < .4f) {
            scaleFactor = .4f - worm.getHead().getTransform().getScaleX();
            bool = false;
        } else if (scale > 1.2f) {
            scaleFactor = 1.2f - worm.getHead().getTransform().getScaleX();
            bool = false;
        }

        worm.moveWorm(scaleFactor);
        worm.scaleWorm(scaleFactor);
        
        Main.animationColor.showPlayButton();
        return bool;
    }

    private boolean canScaleLess() {
        return worm.getHead().getTransform().getScaleX() > .405f;
    }

    private boolean canScaleMore() {
        return worm.getHead().getTransform().getScaleX() < 1.195f;
    }

    private boolean isActionButton(Integer code) {
        return code == GamepadMap.KEYCODE_BUTTON_A || code == GamepadMap.KEYCODE_BUTTON_B
                || code == GamepadMap.KEYCODE_BUTTON_X
                || code == GamepadMap.KEYCODE_BUTTON_Y;
    }

    private void pressedMinus(Integer code) {

        minusSign.pressButton();

        if (code != -1) {

            if (isActionButton(code)) {
                less();
            }

        } else {
            less();
        }
    }

    private void pressedPlus(Integer code) {

        plusSign.pressButton();

        if (code != -1) {

            if (isActionButton(code)) {
                plus();
            }

        } else {
            plus();
        }
    }

    private void less() {

        if (canScaleLess()) {
            if(scaleWorms(-WORM_SCALE_FACTOR))
                new SXRPositionAnimation(worm, .1f, -0.015f, 0.015f, 0).start(getSXRContext()
                    .getAnimationEngine());
        }
    }

    private void plus() {
        if (canScaleMore()) {
            if(scaleWorms(WORM_SCALE_FACTOR))
                new SXRPositionAnimation(worm, .1f, 0.015f, -0.015f, 0).start(getSXRContext()
                    .getAnimationEngine());
        }
    }
}
