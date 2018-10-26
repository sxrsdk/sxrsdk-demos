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

package com.samsungxr.video;

import com.samsungxr.SXRActivity;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRMain;
import com.samsungxr.SXRPicker;
import com.samsungxr.SXRScene;
import com.samsungxr.SXRSceneObject;
import com.samsungxr.video.focus.FocusableController;
import com.samsungxr.video.movie.MovieManager;
import com.samsungxr.video.movie.MovieTheater;
import com.samsungxr.video.overlay.OverlayUI;

public class VideoMain extends SXRMain {

    private SXRContext mSXRContext = null;
    private SXRActivity mActivity = null;

    private MovieManager mMovieManager = null;
    private OverlayUI mOverlayUI = null;

    private boolean mIsOverlayVisible = false;

    private SXRPicker mPicker = null;
    private FocusableController focusableController = null;

    VideoMain(SXRActivity activity) {
        mActivity = activity;
    }

    @Override
    public void onInit(SXRContext sxrContext) {
        mSXRContext = sxrContext;

        SXRScene mainScene = sxrContext.getMainScene();

        // focusableController
        focusableController = new FocusableController();

        // movie manager
        mMovieManager = new MovieManager(mSXRContext);
        // add all theaters to main scene
        SXRSceneObject theaters[] = mMovieManager.getAllMovieTheater();
        for (SXRSceneObject theater : theaters) {
            mainScene.addSceneObject(theater);
            // hide all except active one
            if (theater != mMovieManager.getCurrentMovieTheater()) {
                ((MovieTheater)theater).hideCinemaTheater();
            }
        }

        // video control
        mOverlayUI = new OverlayUI(sxrContext, mMovieManager, mainScene);
        mainScene.addSceneObject(mOverlayUI);
        mOverlayUI.hide();

        mPicker = new SXRPicker(sxrContext, mainScene);
        mainScene.getEventReceiver().addListener(focusableController);
    }

    @Override
    public void onStep() {
        mMovieManager.getCurrentMovieTheater().setShaderValues();
        if (mSXRContext != null) {
            if (mIsOverlayVisible) {
                mOverlayUI.updateOverlayUI(mSXRContext);
            }
        }
    }

    public void onTap() {
        if (mSXRContext != null && mOverlayUI != null) {
            if (mIsOverlayVisible) {
                // if overlay is visible, check anything pointed
                if (focusableController.processClick(mSXRContext) || mOverlayUI.isOverlayPointed(mSXRContext)) {
                } else {
                    mOverlayUI.hide();
                    mIsOverlayVisible = false;
                }
            } else {
                // overlay is not visible, make it visible
                mOverlayUI.show();
                mIsOverlayVisible = true;
            }
        }
    }

    public void onTouch() {
        if (mSXRContext != null && mOverlayUI != null) {
            mOverlayUI.processTouch(mSXRContext);
        }
    }

    public void onPause() {
        if (mSXRContext != null && mOverlayUI != null) {
            mOverlayUI.pauseVideo();
        }
    }
}
