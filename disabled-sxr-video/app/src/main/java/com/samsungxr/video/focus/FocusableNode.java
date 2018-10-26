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

package com.samsungxr.video.focus;

import com.samsungxr.SXRContext;
import com.samsungxr.SXRMesh;
import com.samsungxr.SXRNode;
import com.samsungxr.SXRTexture;

public class FocusableNode extends SXRNode {

    private FocusListener focusListener = null;
    private OnClickListener onClickListener = null;
    private boolean focus = false;

    public FocusableNode(SXRContext context) {
        super(context);
    }

    public FocusableNode(SXRContext context, SXRMesh mesh, SXRTexture texture) {
        super(context, mesh, texture);
    }

    public FocusableNode(SXRContext context, float width, float height, SXRTexture texture) {
        super(context, width, height, texture);
    }

    public void setFocusListener(FocusListener focusListener) {
        this.focusListener = focusListener;
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public void dispatchGainedFocus() {
        if (this.focusListener != null) {
            this.focusListener.gainedFocus(this);
        }
    }

    public void dispatchLostFocus() {
        if (this.focusListener != null) {
            focusListener.lostFocus(this);
        }
    }

    public void dispatchInClick() {
        if (this.onClickListener != null) {
            this.onClickListener.onClick();
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
        }
    }
}
