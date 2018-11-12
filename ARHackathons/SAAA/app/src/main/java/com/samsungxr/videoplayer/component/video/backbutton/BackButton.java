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

package com.samsungxr.videoplayer.component.video.backbutton;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.samsungxr.SXRContext;
import com.samsungxr.SXRNode;
import com.samsungxr.IViewEvents;
import com.samsungxr.nodes.SXRViewNode;
import com.samsungxr.videoplayer.R;
import com.samsungxr.videoplayer.component.FadeableObject;
import com.samsungxr.videoplayer.focus.FocusListener;
import com.samsungxr.videoplayer.focus.Focusable;

@SuppressLint("InflateParams")
public class BackButton extends FadeableObject implements Focusable, IViewEvents {

    private SXRViewNode mBackButtonObject;
    private ImageView mBackButton;
    private FocusListener mFocusListener = null;
    private View.OnClickListener mClickListener = null;

    public BackButton(final SXRContext sxrContext, int intViewId) {
        super(sxrContext);
        mBackButtonObject = new SXRViewNode(sxrContext, intViewId, this);
        setName(getClass().getSimpleName());
    }

    public void setFocusListener(@NonNull FocusListener listener) {
        mFocusListener = listener;
    }

    public void setOnClickListener(@NonNull final View.OnClickListener listener) {
        mClickListener = listener;

        if (mBackButton != null) {
            mBackButton.setOnClickListener(listener);
        }
    }

    @NonNull
    @Override
    protected SXRNode getFadeable() {
        return mBackButtonObject;
    }

    @Override
    public void onInitView(SXRViewNode sxrViewNode, View view) {
        mBackButton = view.findViewById(R.id.backButtonImage);
        mBackButton.setOnClickListener(mClickListener);
        mBackButton.setOnHoverListener(new View.OnHoverListener() {
            @Override
            public boolean onHover(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_HOVER_ENTER) {
                    mBackButtonObject.getRenderData().getMaterial().setOpacity(2.f);
                    if (mFocusListener != null) {
                        mFocusListener.onFocusGained(BackButton.this);
                    }
                } else if (motionEvent.getAction() == MotionEvent.ACTION_HOVER_EXIT) {
                    mBackButtonObject.getRenderData().getMaterial().setOpacity(.5f);
                    if (mFocusListener != null) {
                        mFocusListener.onFocusLost(BackButton.this);
                    }
                }
                return false;
            }
        });
    }

    @Override
    public void onStartRendering(SXRViewNode sxrViewNode, View view) {
        addChildObject(sxrViewNode);
    }

    public void performClick() {
        mBackButton.post(new Runnable() {
            @Override
            public void run() {
                mBackButton.performClick();
            }
        });
    }

    @Override
    public void gainFocus() {

    }

    @Override
    public void loseFocus() {
    }
}
