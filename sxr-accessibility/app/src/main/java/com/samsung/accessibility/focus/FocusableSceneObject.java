/*
 * Copyright 2015 Samsung Electronics Co., LTD
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */

package com.samsung.accessibility.focus;

import com.samsungxr.SXRContext;
import com.samsungxr.SXRMesh;
import com.samsungxr.SXRTexture;
import com.samsungxr.accessibility.SXRAccessiblityObject;

public class FocusableSceneObject extends SXRAccessiblityObject {

    private boolean focus = false;
    private OnFocusListener onFocusListener = null;
    public String tag = null;
    public boolean showInteractiveCursor = true;
    private OnClickListener onClickListener;
    private int focusCount = 0;

    public FocusableSceneObject(SXRContext sxrContext) {
        super(sxrContext);
    }

    public FocusableSceneObject(SXRContext sxrContext, SXRMesh sxrMesh,
            SXRTexture sxrTexture) {
        super(sxrContext, sxrMesh, sxrTexture);
    }

    public FocusableSceneObject(SXRContext sxrContext, float width,
            float height, SXRTexture t) {
        super(sxrContext, width, height, t);
    }

    public void dispatchGainedFocus() {
        if (this.onFocusListener != null) {
            this.onFocusListener.gainedFocus(this);
        }
        if (showInteractiveCursor) {
            // GazeController.enableInteractiveCursor();
        }
    }

    public void dispatchLostFocus() {
        if (this.onFocusListener != null) {
            onFocusListener.lostFocus(this);
            focusCount = 0;
        }
        if (showInteractiveCursor) {
            // GazeController.disableInteractiveCursor();
        }
    }

    public void setFocus(boolean state) {
        if (state == true && focus == false && focusCount > 1) {
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
        if (this.onFocusListener != null) {
            if (focusCount > 1)
                this.onFocusListener.inFocus(this);
            if (focusCount <= 2)
                focusCount++;
        }
        if (showInteractiveCursor) {
            // GazeController.get().enableInteractiveCursor();
        }
    }

    public void dispatchInClick() {
        if (this.onClickListener != null) {
            this.onClickListener.onClick();
        }

    }

    public boolean hasFocus() {
        return focus;
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public void setOnFocusListener(OnFocusListener onFocusListener) {
        this.onFocusListener = onFocusListener;
    }

}
