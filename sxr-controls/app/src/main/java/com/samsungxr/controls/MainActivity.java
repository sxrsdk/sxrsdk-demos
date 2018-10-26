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

package com.samsungxr.controls;

import android.os.Bundle;
import android.view.GestureDetector;
import android.view.InputDevice;
import android.view.MotionEvent;

import com.samsungxr.SXRActivity;
import com.samsungxr.controls.input.GamepadInput;
import com.samsungxr.controls.input.TouchPadInput;
import com.samsungxr.io.SXRTouchPadGestureListener;

public class MainActivity extends SXRActivity
{
    private GestureDetector mDetector = null;
    private Main main;
    private static final int TAP_INTERVAL = 300;
    private long mLatestTap = 0;
    private SwipeListener swipeListener = new SwipeListener();

    class SwipeListener extends SXRTouchPadGestureListener
    {
        @Override
        public boolean onSingleTapUp(MotionEvent e)
        {
            if (System.currentTimeMillis() > mLatestTap + TAP_INTERVAL)
            {
                mLatestTap = System.currentTimeMillis();
                TouchPadInput.onSingleTap();
            }

            return false;
        }

        @Override
        public void onLongPress(MotionEvent e)
        {
            TouchPadInput.onLongPress();
        }

        @Override
        public boolean onSwipe(MotionEvent e, Action action, float velocityX, float velocityY)
        {
            TouchPadInput.onSwipe(action);
            return false;
        }

        public void onSwiping(MotionEvent e, MotionEvent e2, float vx, float vy, SXRTouchPadGestureListener.Action action) { }

        public void onSwipeOppositeLastDirection() { }
    };

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        main = new Main();
        setMain(main, "sxr.xml");
        mDetector = new GestureDetector(getBaseContext(), swipeListener);
    }

    @Override
    public boolean dispatchKeyEvent(android.view.KeyEvent event) {
        boolean handled = false;

        GamepadInput.input(event);

        if (handled) {
            return true;
        } else {
            return super.dispatchKeyEvent(event);
        }
    }

    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent event) {

        boolean handled = false;

        // Check that the event came from a game controller
        if ((event.getSource() & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK
                && event.getAction() == MotionEvent.ACTION_MOVE) {

            // Process all historical movement samples in the batch
            final int historySize = event.getHistorySize();

            GamepadInput.input(event);

            // Process the movements starting from the
            // earliest historical position in the batch
            for (int i = 0; i < historySize; i++) {
                // Process the event at historical position i
                GamepadInput.processJoystickInput(event, i);
            }

            // Process the current movement sample in the batch (position -1)
            handled = GamepadInput.processJoystickInput(event, -1);

            if (handled) {
                return true;
            } else {
                return super.dispatchGenericMotionEvent(event);
            }
        }
        return handled;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mDetector == null) {
            return false;
        }
        TouchPadInput.input(event);
        mDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }


}
