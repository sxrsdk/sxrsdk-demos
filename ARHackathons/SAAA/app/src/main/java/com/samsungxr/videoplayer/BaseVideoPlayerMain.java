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

package com.samsungxr.videoplayer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.samsungxr.SXRBitmapImage;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRMain;
import com.samsungxr.SXRMesh;
import com.samsungxr.SXRTexture;


public abstract class BaseVideoPlayerMain extends SXRMain {

    private static final float IMAGE_WIDTH = 1;
    private static final float IMAGE_HEIGHT = IMAGE_WIDTH * .0992f;
    private static final float SPLASH_FADE_DURATION = 1;
    private static final float SPLASH_DURATION = 3;

    @Override
    public float getSplashFadeTime() {
        return SPLASH_FADE_DURATION;
    }

    @Override
    public float getSplashDisplayTime() {
        return SPLASH_DURATION;
    }

    @Override
    public SXRMesh getSplashMesh(SXRContext sxrContext) {
        return sxrContext.createQuad(IMAGE_WIDTH, IMAGE_HEIGHT);
    }

    @Override
    public SXRTexture getSplashTexture(SXRContext sxrContext) {
        Bitmap bitmap = BitmapFactory.decodeResource(
                sxrContext.getContext().getResources(),
                R.drawable.ic_app_name_highlight);
        SXRTexture splashScreen = new SXRTexture(sxrContext);
        splashScreen.setImage(new SXRBitmapImage(sxrContext, bitmap));
        return splashScreen;
    }
}
