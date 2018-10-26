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

package com.samsungxr.controls.menu;

import com.samsungxr.SXRContext;
import com.samsungxr.SXRSceneObject;
import com.samsungxr.controls.focus.GamepadTouchImpl;
import com.samsungxr.controls.focus.TouchAndGestureImpl;
import com.samsungxr.controls.input.GamepadMap;

public class RadioGrupoSceneObject extends SXRSceneObject {

    private static final float SPACE_BETWEEN_BUTTONS = 0.5f;
    private RadioButtonSceneObject button1;
    private RadioButtonSceneObject button2;
    private RadioButtonSceneObject button3;
    private RadioButtonSceneObject buttonSelected;

    private ItemSelectedListener mItemSelectedListener;

    public RadioGrupoSceneObject(SXRContext sxrContext, ItemSelectedListener mItemSelectedListener,
                                 float time1, float time2, float time3) {
        super(sxrContext);

        this.mItemSelectedListener = mItemSelectedListener;

        button1 = new RadioButtonSceneObject(sxrContext, time1 >= 1 ? (int) time1 + "s" : time1
                + "s", time1);
        button2 = new RadioButtonSceneObject(sxrContext, time2 >= 1 ? (int) time2 + "s" : time2
                + "s", time2);
        button3 = new RadioButtonSceneObject(sxrContext, time3 >= 1 ? (int) time3 + "s" : time3
                + "s", time3);

        buttonSelected = button1;
        buttonSelected.select();

        handlerEvents(button1);
        handlerEvents(button2);
        handlerEvents(button3);
    }

    public void hide() {
        removeChildObject(button1);
        removeChildObject(button2);
        removeChildObject(button3);
    }

    public void show() {

        if (!MenuFrame.isOpen) {

            getSXRContext().getPeriodicEngine().runAfter(new Runnable() {

                @Override
                public void run() {
                    setInitialPosition();
                }

            }, 2.5f);

        } else {
            setInitialPosition();
        }
    }

    private void handlerEvents(final RadioButtonSceneObject button) {

        button.setTouchAndGesturelistener(new TouchAndGestureImpl() {

            @Override
            public void singleTap() {
                super.singleTap();
                handleUISelection(button);

                if (mItemSelectedListener != null) {
                    mItemSelectedListener.selected(button);
                }
            }
        });

        button.setGamepadTouchListener(new GamepadTouchImpl() {

            @Override
            public void down(Integer code) {
                super.down(code);

                if (code == GamepadMap.KEYCODE_BUTTON_A ||
                        code == GamepadMap.KEYCODE_BUTTON_B ||
                        code == GamepadMap.KEYCODE_BUTTON_X ||
                        code == GamepadMap.KEYCODE_BUTTON_Y) {

                    handleUISelection(button);
                    button.select();

                    if (mItemSelectedListener != null) {
                        mItemSelectedListener.selected(button);
                    }

                }
            }
        });
    }

    public void setInitialPosition() {

        Position initPosition = new Position(0f, 0f, 0f);

        setNewPosition(button1, initPosition);

        initPosition.x += SPACE_BETWEEN_BUTTONS;
        setNewPosition(button2, initPosition);

        initPosition.x += SPACE_BETWEEN_BUTTONS;
        setNewPosition(button3, initPosition);

        addChildObject(button1);
        addChildObject(button2);
        addChildObject(button3);
    }

    private void setNewPosition(RadioButtonSceneObject button, Position position) {
        button.getTransform().setPosition(position.x, position.y, position.z);
    }

    private void handleUISelection(RadioButtonSceneObject object) {

        if (buttonSelected != null) {

            if (buttonSelected != object) {
                buttonSelected.unselect();
            }
        }

        buttonSelected = object;
    }

    private static class Position {

        public float x = 0f, y = 0f, z = 0f;

        public Position(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }
}