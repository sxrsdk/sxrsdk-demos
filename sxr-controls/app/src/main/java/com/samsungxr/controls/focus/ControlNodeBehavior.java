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

package com.samsungxr.controls.focus;

import com.samsungxr.SXRContext;
import com.samsungxr.SXRPicker;
import com.samsungxr.controls.cursor.ControlGazeController;
import com.samsungxr.controls.input.GamepadInput;
import com.samsungxr.controls.input.GamepadMap;
import com.samsungxr.controls.input.TouchPadInput;
import com.samsungxr.io.SXRTouchPadGestureListener;

import java.util.ArrayList;

public class ControlNodeBehavior {

    public static ArrayList<ControlNode> interactiveObjects = new ArrayList<ControlNode>();

    public static void process(SXRContext context) {

        SXRPicker.SXRPickedObject[] pickedObjects = SXRPicker.pickObjects(context.getMainScene(), 0,0,0,0,0,-1);
        ArrayList<ControlNode> needToDisableFocus = new ArrayList<ControlNode>();
        for (ControlNode obj : interactiveObjects) {
            obj.onStep();
            needToDisableFocus.add(obj);
        }

        if (pickedObjects.length == 0) {
            ControlGazeController.disableInteractiveCursor();
        } else {
            for (SXRPicker.SXRPickedObject po : pickedObjects) {
                for (ControlNode object : interactiveObjects) {
                    if (po.getHitObject().equals(object)) {
                        object.setFocus(true);
                        needToDisableFocus.remove(object);
                    }
                }
            }
        }

        for (ControlNode obj : needToDisableFocus) {
            obj.setFocus(false);
        }

        processTap(context);
    }

    private static void processTap(SXRContext context) {

        for (ControlNode object : interactiveObjects) {
            if (object.hasFocus()) {

                checkInput(object);
            }
        }
    }

    private static void checkInput(ControlNode object) {
        if (TouchPadInput.getCurrent().buttonState.isSingleTap()) {
            object.singleTap();

        }
        handleTouchPad(object);
        handleGamePad(object);
    }

    private static void handleGamePad(ControlNode object) {
        if (object.gamepadActionButtonslistener != null) {
            hadleGamepadPressed(object);
            hadleGamepadUp(object);
            hadleGamepadDown(object);
        }
    }

    private static void hadleGamepadDown(ControlNode object) {
        if (GamepadInput.getKeyDown(GamepadMap.KEYCODE_BUTTON_A)) {
            object.gamepadActionButtonslistener.down(GamepadMap.KEYCODE_BUTTON_A);
        }
        if (GamepadInput.getKeyDown(GamepadMap.KEYCODE_BUTTON_B)) {
            object.gamepadActionButtonslistener.down(GamepadMap.KEYCODE_BUTTON_B);
        }
        if (GamepadInput.getKeyDown(GamepadMap.KEYCODE_BUTTON_X)) {
            object.gamepadActionButtonslistener.down(GamepadMap.KEYCODE_BUTTON_X);
        }
        if (GamepadInput.getKeyDown(GamepadMap.KEYCODE_BUTTON_Y)) {
            object.gamepadActionButtonslistener.down(GamepadMap.KEYCODE_BUTTON_Y);
        }

    }

    private static void hadleGamepadUp(ControlNode object) {
        if (GamepadInput.getKeyUp(GamepadMap.KEYCODE_BUTTON_A)) {
            object.gamepadActionButtonslistener.up(GamepadMap.KEYCODE_BUTTON_A);
        }
        if (GamepadInput.getKeyUp(GamepadMap.KEYCODE_BUTTON_B)) {
            object.gamepadActionButtonslistener.up(GamepadMap.KEYCODE_BUTTON_B);
        }
        if (GamepadInput.getKeyUp(GamepadMap.KEYCODE_BUTTON_X)) {
            object.gamepadActionButtonslistener.up(GamepadMap.KEYCODE_BUTTON_X);
        }
        if (GamepadInput.getKeyUp(GamepadMap.KEYCODE_BUTTON_Y)) {
            object.gamepadActionButtonslistener.up(GamepadMap.KEYCODE_BUTTON_Y);
        }

    }

    private static void hadleGamepadPressed(ControlNode object) {

        if (GamepadInput.getKey(GamepadMap.KEYCODE_BUTTON_A)) {
            object.gamepadActionButtonslistener.pressed(GamepadMap.KEYCODE_BUTTON_A);
        }
        if (GamepadInput.getKey(GamepadMap.KEYCODE_BUTTON_B)) {
            object.gamepadActionButtonslistener.pressed(GamepadMap.KEYCODE_BUTTON_B);
        }
        if (GamepadInput.getKey(GamepadMap.KEYCODE_BUTTON_X)) {
            object.gamepadActionButtonslistener.pressed(GamepadMap.KEYCODE_BUTTON_X);
        }
        if (GamepadInput.getKey(GamepadMap.KEYCODE_BUTTON_Y)) {
            object.gamepadActionButtonslistener.pressed(GamepadMap.KEYCODE_BUTTON_Y);
        }

    }

    private static void handleTouchPad(ControlNode object) {
        if (object.touchAndGesturelistener != null) {

            if (TouchPadInput.getCurrent().buttonState.isLongPressed()) {
                object.touchAndGesturelistener.longPressed();
            }
            if (TouchPadInput.getCurrent().buttonState.isDown()) {
                object.touchAndGesturelistener.down();
            }
            if (TouchPadInput.getCurrent().buttonState.isUp()) {
                object.touchAndGesturelistener.up();
            }
            if (TouchPadInput.getCurrent().buttonState.isPressed()) {
                object.touchAndGesturelistener.pressed();
            }
            if (TouchPadInput.getCurrent().swipeDirection != SXRTouchPadGestureListener.Action.None) {
                object.touchAndGesturelistener.swipe(TouchPadInput.getCurrent().swipeDirection);
            }
        }

    }
}