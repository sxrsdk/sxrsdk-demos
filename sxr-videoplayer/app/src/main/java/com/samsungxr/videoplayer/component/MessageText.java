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
import android.view.View;
import android.widget.TextView;

import com.samsungxr.SXRContext;
import com.samsungxr.SXRSceneObject;
import com.samsungxr.IViewEvents;
import com.samsungxr.scene_objects.SXRViewSceneObject;
import com.samsungxr.videoplayer.R;
import com.samsungxr.videoplayer.component.gallery.OnMessageListener;

public class MessageText extends FadeableObject implements IViewEvents {

    private SXRViewSceneObject mMessageTextObject;
    private final boolean mHasBackground;
    private final String mText;
    private OnMessageListener mMessageListener;

    public MessageText(SXRContext gvrContext, String text) {
        this(gvrContext, false, text, null);
    }

    public MessageText(SXRContext gvrContext, boolean hasBackground, String text, OnMessageListener listener) {
        super(gvrContext);
        mHasBackground = hasBackground;
        mText = text;
        mMessageTextObject = new SXRViewSceneObject(gvrContext, R.layout.message_text, this);
        mMessageListener = listener;
    }

    public void notifyTimesUp() {
        if (mMessageListener != null) {
            mMessageListener.onTimesUp();
        }
    }

    @Override
    public void onInitView(SXRViewSceneObject gvrViewSceneObject, View view) {
        TextView textView = view.findViewById(R.id.message_text);
        if (mHasBackground) {
            int[] state = {android.R.attr.state_enabled};
            textView.getBackground().setState(state);
        } else {
            int[] state = {};
            textView.getBackground().setState(state);
        }
        textView.setText(mText);
    }

    @Override
    public void onStartRendering(SXRViewSceneObject gvrViewSceneObject, View view) {
        addChildObject(gvrViewSceneObject);
    }

    @NonNull
    @Override
    protected SXRSceneObject getFadeable() {
        return mMessageTextObject;
    }
}
