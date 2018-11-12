/*
 * Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.samsungxr.videoplayer.component;

import android.support.annotation.NonNull;
import android.view.Gravity;

import com.samsungxr.SXRContext;
import com.samsungxr.SXRRenderData;
import com.samsungxr.SXRNode;
import com.samsungxr.nodes.SXRTextViewNode;

public class LabelCursor extends FadeableObject {
    SXRTextViewNode mTextView;

    public LabelCursor(SXRContext context, float width, float height, String text) {
        super(context);
        mTextView = new SXRTextViewNode(context, width, height, text);
        mTextView.getRenderData().setDepthTest(false);
        mTextView.getRenderData().setRenderingOrder(SXRRenderData.SXRRenderingOrder.OVERLAY);
        mTextView.setGravity(Gravity.CENTER);
        mTextView.setTextSize(7f);
        mTextView.setTypeface(context, "font_roboto_medium");
        addChildObject(mTextView);
    }

    @NonNull
    @Override
    protected SXRNode getFadeable() {
        return mTextView;
    }
}
