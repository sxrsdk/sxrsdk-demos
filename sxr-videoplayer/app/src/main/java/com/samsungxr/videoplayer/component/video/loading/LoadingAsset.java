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

package com.samsungxr.videoplayer.component.video.loading;

import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.samsungxr.SXRContext;
import com.samsungxr.SXRSceneObject;
import com.samsungxr.IViewEvents;
import com.samsungxr.animation.SXRAnimation;
import com.samsungxr.animation.SXRRotationByAxisWithPivotAnimation;
import com.samsungxr.scene_objects.SXRViewSceneObject;
import com.samsungxr.videoplayer.R;

public class LoadingAsset extends SXRSceneObject implements IViewEvents {
    private static final String TAG = LoadingAsset.class.getSimpleName();
    private SXRViewSceneObject mLoadingObject;
    LinearLayout mLoading;
    SXRAnimation mAnimation;

    public LoadingAsset(SXRContext gvrContext) {
        super(gvrContext);
        mLoadingObject = new SXRViewSceneObject(gvrContext, R.layout.layout_loading, this);
    }

    @Override
    public void onInitView(SXRViewSceneObject gvrViewSceneObject, View view) {
        mLoading = view.findViewById(R.id.loading);
    }

    @Override
    public void onStartRendering(SXRViewSceneObject gvrViewSceneObject, View view) {
        addChildObject(gvrViewSceneObject);
        mAnimation = new SXRRotationByAxisWithPivotAnimation(this, 2, -360f, 0.0f,
                0.0f, 1.0f, 0.0f, 0.0f, 0.0f).start(getSXRContext().getAnimationEngine());
        mAnimation.setRepeatMode(1);
        mAnimation.setRepeatCount(-1);
        Log.d(TAG, "Animation Loading ");
    }
}
