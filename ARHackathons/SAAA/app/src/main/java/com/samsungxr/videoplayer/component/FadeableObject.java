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

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;

import com.samsungxr.SXRContext;
import com.samsungxr.SXRMesh;
import com.samsungxr.SXRNode;
import com.samsungxr.SXRTexture;
import com.samsungxr.animation.SXRAnimation;
import com.samsungxr.animation.SXROnFinish;
import com.samsungxr.animation.SXROpacityAnimation;
import com.samsungxr.nodes.SXRViewNode;

public abstract class FadeableObject extends SXRNode {

    private static final float FADE_DURATION = .2F;

    public FadeableObject(SXRContext sxrContext) {
        super(sxrContext);
    }

    public FadeableObject(SXRContext mContext, SXRMesh quad, SXRTexture sxrTexture) {
        super(mContext, quad, sxrTexture);
    }

    @NonNull
    protected abstract SXRNode getFadeable();

    public final void fadeIn() {
        fadeIn(null);
    }

    public final void fadeOut() {
        fadeOut(null);
    }

    @CallSuper
    public final void fadeIn(final OnFadeFinish callback) {
        if (getFadeable() instanceof SXRViewNode) {
            getSXRContext().getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    doFadeIn(callback);
                }
            });
        } else {
            doFadeIn(callback);
        }
    }

    @CallSuper
    public final void fadeOut(final OnFadeFinish callback) {
        if (getFadeable() instanceof SXRViewNode) {
            getSXRContext().getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    doFadeOut(callback);
                }
            });
        } else {
            doFadeOut(callback);
        }
    }

    private void doFadeIn(final OnFadeFinish callback) {
        SXROpacityAnimation animation = new SXROpacityAnimation(
                getFadeable(), FADE_DURATION, 1);
        animation.setOnFinish(new SXROnFinish() {
            @Override
            public void finished(SXRAnimation sxrAnimation) {
                if (callback != null) {
                    callback.onFadeFinished();
                }
            }
        });
        animation.start(getSXRContext().getAnimationEngine());
    }

    private void doFadeOut(final OnFadeFinish callback) {
        SXROpacityAnimation animation = new SXROpacityAnimation(
                getFadeable(), FADE_DURATION, 0);
        animation.setOnFinish(new SXROnFinish() {
            @Override
            public void finished(SXRAnimation sxrAnimation) {
                if (callback != null) {
                    callback.onFadeFinished();
                }
            }
        });
        animation.start(getSXRContext().getAnimationEngine());
    }

    public void show() {
        show(null);
    }

    public void show(final OnFadeFinish fadeInCallback) {
        if (!isEnabled()) {
            setEnable(true);
            fadeIn(new OnFadeFinish() {
                @Override
                public void onFadeFinished() {
                    if (fadeInCallback != null) {
                        fadeInCallback.onFadeFinished();
                    }
                }
            });
        }
    }

    public void hide() {
        hide(null);
    }

    public void hide(final OnFadeFinish fadeOutCallback) {
        if (isEnabled()) {
            fadeOut(new OnFadeFinish() {
                @Override
                public void onFadeFinished() {
                    setEnable(false);
                    if (fadeOutCallback != null) {
                        fadeOutCallback.onFadeFinished();
                    }
                }
            });
        }
    }
}
