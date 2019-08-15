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

package com.samsungxr.controls.anim;

import android.content.res.Resources;

import com.samsungxr.SXRContext;
import com.samsungxr.SXRNode;
import com.samsungxr.animation.SXRAnimation;
import com.samsungxr.animation.SXROnFinish;
import com.samsungxr.animation.SXRPositionAnimation;
import com.samsungxr.controls.Main;
import com.samsungxr.controls.R;
import com.samsungxr.controls.focus.GamepadTouchImpl;
import com.samsungxr.controls.focus.TouchAndGestureImpl;
import com.samsungxr.controls.input.GamepadMap;

public class ActionWormAnimation extends SXRNode {

    private static final float CLEARBUTTON_POSITIONY = -0.3f;
    private AnimButtonPlay playButton;
    private AnimCleanButton cleanButton;
    private boolean playbuttonbIsHidden = true;
    private boolean cleanbuttonbIsHidden = true;

    private final float MINIMUM_Y_POSITION = 0.65f;
    private final float MAXIMUM_Y_POSITION = 1f;
    private final float MINIMUM_SCALE = 0.4f;
    private final float MAXIMUM_SCALE = 1.2f;

    private boolean animColorPlaying = false;
    private boolean animScalePlaying = false;

    public ActionWormAnimation(SXRContext sxrContext) {
        super(sxrContext);

        Resources res = sxrContext.getContext().getResources();
        String clearButtonText = res.getString(R.string.clear_button);

        playButton = new AnimButtonPlay(sxrContext);
        cleanButton = new AnimCleanButton(sxrContext, clearButtonText);

        playButton.getTransform().setPosition(0, 0, 0);
        playButton.getTransform().setRotationByAxis(16, 0, 1, 0);

        cleanButton.getTransform().setPosition(0, CLEARBUTTON_POSITIONY, 0);
        cleanButton.getTransform().setRotationByAxis(5f, 0, 1, 0);

        getTransform().setPositionY(MINIMUM_Y_POSITION);

        attachActionButtons();
    }

    private void attachActionButtons() {

        playButton.setTouchAndGesturelistener(new TouchAndGestureImpl() {

            @Override
            public void singleTap() {
                super.singleTap();

                ScaleWorm.playAnim();

                playAnimations();
            }
        });

        playButton.setGamepadTouchListener(new GamepadTouchImpl() {

            @Override
            public void down(Integer code) {
                super.down(code);

                if (code == GamepadMap.KEYCODE_BUTTON_A ||
                        code == GamepadMap.KEYCODE_BUTTON_B ||
                        code == GamepadMap.KEYCODE_BUTTON_X ||
                        code == GamepadMap.KEYCODE_BUTTON_Y) {

                    ScaleWorm.playAnim();

                    playAnimations();
                }
            }
        });

        cleanButton.setTouchAndGesturelistener(new TouchAndGestureImpl() {

            @Override
            public void singleTap() {
                super.singleTap();

                cleanControls();
            }
        });

        cleanButton.setGamepadTouchListener(new GamepadTouchImpl() {

            @Override
            public void down(Integer code) {
                super.down(code);

                if (code == GamepadMap.KEYCODE_BUTTON_A ||
                        code == GamepadMap.KEYCODE_BUTTON_B ||
                        code == GamepadMap.KEYCODE_BUTTON_X ||
                        code == GamepadMap.KEYCODE_BUTTON_Y) {

                    cleanControls();
                }
            }
        });
    }

    private void cleanControls() {

        removeChildObject(playButton);
        removeChildObject(cleanButton);

        playbuttonbIsHidden = true;
        cleanbuttonbIsHidden = true;

        animScalePlaying = false;
        animColorPlaying = false;
    }

    public void resetAnimationState() {

        removeChildObject(cleanButton);

        playbuttonbIsHidden = true;
        cleanbuttonbIsHidden = true;

        showPlayButton();
    }

    private void playAnimations() {

        animPlaying();

        if (ColorWorm.lastColor != null) {

            animColorPlaying = true;

            Main.worm.resetColor(ColorWorm.lastColor);
            Main.worm.changeColor(ColorWorm.currentColor);
        }

        if (ScaleWorm.scaleAnimIsEnable()) {

            animScalePlaying = true;

            float factor = ScaleWorm.getWorm().getHead().getTransform().getScaleX()
                    - ScaleWorm.getLastSize()[0];
            ScaleWorm.animPlaying = true;

            resetPositionParts();

            WormApplyTransformAnims.resetScaleWorm(ScaleWorm.getLastSize());
            WormApplyTransformAnims.scaleWorm(getSXRContext(), factor);
        }
    }

    private void animPlaying() {

        removeChildObject(playButton);
        removeChildObject(cleanButton);

        getSXRContext().getPeriodicEngine().runAfter(new Runnable() {

            @Override
            public void run() {

                addChildObject(playButton);
                addChildObject(cleanButton);
                ScaleWorm.animPlaying = false;

                getTransform().setPositionY(calculateNewYPosition());
            }

        }, getMajorDelayAnim());
    }

    private float calculateNewYPosition() {
        float currentScale = Main.worm.getHead().getTransform().getScaleY();
        float position = (((currentScale - MINIMUM_SCALE) / (MAXIMUM_SCALE - MINIMUM_SCALE)) * (MAXIMUM_Y_POSITION - MINIMUM_Y_POSITION))
                + MINIMUM_Y_POSITION;
        return position;
    }

    private float getMajorDelayAnim() {

        if (animColorPlaying && animScalePlaying) {

            float maxTime = AnimationsTime.getChangeColorTime() > AnimationsTime.getScaleTime()
                    ? AnimationsTime.getChangeColorTime() : AnimationsTime.getScaleTime();

            return maxTime;

        } else if (animColorPlaying && !animScalePlaying) {

            return AnimationsTime.getChangeColorTime();

        } else {

            return AnimationsTime.getScaleTime();
        }
    }

    private void resetPositionParts() {

        SXRPositionAnimation headAnimation = WormApplyTransformAnims.moveWormPartReset(
                Main.worm.getHead().getParent(), Main.worm.getMiddle());
        headAnimation.start(getSXRContext().getAnimationEngine()).setOnFinish(new SXROnFinish() {

            @Override
            public void finished(SXRAnimation arg0) {

                WormApplyTransformAnims.moveWormPartToClose(getSXRContext(), Main.worm
                        .getHead().getParent(), Main.worm.getMiddle());
            }
        });

        SXRPositionAnimation endAnimation = WormApplyTransformAnims.moveWormPartReset(
                Main.worm.getEnd(), Main.worm.getMiddle());
        endAnimation.start(getSXRContext().getAnimationEngine()).setOnFinish(new SXROnFinish() {

            @Override
            public void finished(SXRAnimation arg0) {
                WormApplyTransformAnims.moveWormPartToClose(getSXRContext(),
                        Main.worm.getEnd(), Main.worm.getMiddle());
            }
        });
    }

    public void showPlayButton() {

        if (playbuttonbIsHidden) {
            playbuttonbIsHidden = false;
            addChildObject(playButton);
        }
    }

    public void showCleanButton() {

        if (cleanbuttonbIsHidden) {

            cleanbuttonbIsHidden = false;
            addChildObject(cleanButton);
        }
    }
}
