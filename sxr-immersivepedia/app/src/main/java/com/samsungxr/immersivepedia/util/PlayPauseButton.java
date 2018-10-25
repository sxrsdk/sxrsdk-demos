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

package com.samsungxr.immersivepedia.util;

import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRTexture;
import com.samsungxr.immersivepedia.R;
import com.samsungxr.immersivepedia.focus.FocusableSceneObject;

public class PlayPauseButton extends FocusableSceneObject {

    public static final String PAUSE_HOVER = "inactive_pause";
    public static final String PAUSE_NORMAL = "normal_pause";

    public static final String PLAY_HOVER = "inactive_play";
    public static final String PLAY_NORMAL = "normal_play";

    private SXRTexture pauseHover;
    private SXRTexture pauseNormal;

    private SXRTexture playHover;
    private SXRTexture playNormal;
    private SXRContext gvrContext;

    public PlayPauseButton(SXRContext gvrContext, float f, float g, SXRTexture t) {
        super(gvrContext, f, g, t);
        this.gvrContext = gvrContext;
        loadTexture();
        setTextures();
        setName("playpause");
    }

    private void loadTexture() {
        pauseHover = gvrContext.getAssetLoader().loadTexture(new SXRAndroidResource(gvrContext, R.drawable.pause_hover));
        playHover = gvrContext.getAssetLoader().loadTexture(new SXRAndroidResource(gvrContext, R.drawable.play_hover));
        pauseNormal = gvrContext.getAssetLoader().loadTexture(new SXRAndroidResource(gvrContext, R.drawable.pause));
        playNormal = gvrContext.getAssetLoader().loadTexture(new SXRAndroidResource(gvrContext, R.drawable.play));
    }

    private void setTextures() {
        getRenderData().getMaterial().setTexture(PAUSE_NORMAL, pauseNormal);
        getRenderData().getMaterial().setTexture(PAUSE_HOVER, pauseHover);
        getRenderData().getMaterial().setTexture(PLAY_HOVER, playHover);
        getRenderData().getMaterial().setTexture(PLAY_NORMAL, playNormal);
    }

}
