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

package com.samsungxr.immersivepedia.focus;

import com.samsungxr.SXRContext;
import com.samsungxr.SXRMesh;
import com.samsungxr.SXRNode;
import com.samsungxr.SXRTexture;
import com.samsungxr.immersivepedia.GazeController;
import com.samsungxr.immersivepedia.input.TouchPadInput;
import com.samsungxr.io.SXRTouchPadGestureListener.Action;

public class FocusableNode extends SXRNode {

    private boolean focus = false;
    public FocusListener focusListener = null;
    public String tag = null;
    public boolean showInteractiveCursor = true;
    private OnClickListener onClickListener;
    private OnGestureListener onGestureListener;
    public float[] hitLocation;

    public FocusableNode(SXRContext sxrContext) {
        super(sxrContext);
    }

    public FocusableNode(SXRContext sxrContext, SXRMesh sxrMesh, SXRTexture sxrTexture) {
        super(sxrContext, sxrMesh, sxrTexture);
    }

    public FocusableNode(SXRContext sxrContext, float width, float height, SXRTexture t) {
        super(sxrContext, width, height, t);
    }

    public void dispatchGainedFocus() {
        if (this.focusListener != null) {
            this.focusListener.gainedFocus(this);
        }
        if (showInteractiveCursor) {
            GazeController.get().enableInteractiveCursor();
        }
    }

    public void dispatchLostFocus() {
        if (this.focusListener != null) {
            focusListener.lostFocus(this);
        }
        if (showInteractiveCursor) {
            GazeController.get().disableInteractiveCursor();
        }
    }

    public void setFocus(boolean state) {
        if (state == true && focus == false) {
            focus = true;
            this.dispatchGainedFocus();
            return;
        }

        if (state == false && focus == true) {
            focus = false;
            this.dispatchLostFocus();
            return;
        }
    }

    public void dispatchInFocus() {
        if (this.focusListener != null) {
            this.focusListener.inFocus(this);
        }
        if (showInteractiveCursor) {
            GazeController.get().enableInteractiveCursor();
        }
    }

    public void dispatchInClick() {
        if (this.onClickListener != null)
            this.onClickListener.onClick();
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public void setOnGestureListener(OnGestureListener onGestureListener) {
        this.onGestureListener = onGestureListener;
    }

    public boolean isFocus() {
        return focus;
    }

    public void dispatchInGesture(Action swipeDirection) {
        if (this.onGestureListener != null) {

            if (TouchPadInput.getCurrent().swipeDirection == Action.None)
                onGestureListener.onSwipeIgnore();
            else if (TouchPadInput.getCurrent().swipeDirection == Action.SwipeForward)
                onGestureListener.onSwipeForward();
            else if (TouchPadInput.getCurrent().swipeDirection == Action.SwipeBackward)
                onGestureListener.onSwipeBack();
            else if (TouchPadInput.getCurrent().swipeDirection == Action.SwipeUp)
                onGestureListener.onSwipeUp();
            else if (TouchPadInput.getCurrent().swipeDirection == Action.SwipeDown)
                onGestureListener.onSwipeDown();
        }
    }
}
