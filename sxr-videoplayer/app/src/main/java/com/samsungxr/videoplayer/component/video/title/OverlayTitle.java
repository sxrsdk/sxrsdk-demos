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

package com.samsungxr.videoplayer.component.video.title;

import android.support.annotation.NonNull;
import android.view.View;

import com.samsungxr.SXRContext;
import com.samsungxr.SXRSceneObject;
import com.samsungxr.IViewEvents;
import com.samsungxr.scene_objects.SXRViewSceneObject;
import com.samsungxr.videoplayer.R;
import com.samsungxr.videoplayer.component.FadeableObject;
import com.samsungxr.videoplayer.focus.Focusable;

public class OverlayTitle extends FadeableObject implements Focusable, IViewEvents {

    private SXRViewSceneObject mTitleObject;

    public OverlayTitle(SXRContext sxrContext) {
        super(sxrContext);
        mTitleObject = new SXRViewSceneObject(sxrContext, R.layout.layout_title_image, this);
    }

    @NonNull
    @Override
    protected SXRSceneObject getFadeable() {
        return mTitleObject;
    }

    @Override
    public void onInitView(SXRViewSceneObject sxrViewSceneObject, View view) {
    }

    @Override
    public void onStartRendering(SXRViewSceneObject sxrViewSceneObject, View view) {
        addChildObject(sxrViewSceneObject);
    }

    @Override
    public void gainFocus() {
        mTitleObject.getRenderData().getMaterial().setOpacity(2.f);
    }

    @Override
    public void loseFocus() {
        mTitleObject.getRenderData().getMaterial().setOpacity(1f);
    }
}
