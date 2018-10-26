/*
 * Copyright 2015 Samsung Electronics Co., LTD
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package com.samsung.accessibility.util;

import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRTexture;

import com.samsung.accessibility.R;

public class    AccessibilityTexture {

    private static AccessibilityTexture instance;
    private SXRTexture accessibilityIcon;
    private SXRTexture backIcon;
    private SXRTexture spaceTexture;
    private SXRTexture zoomOut;
    private SXRTexture zoomIn;
    private SXRTexture talkBackLess;
    private SXRTexture talkBackMore;
    private SXRTexture invertedColorsIcon;
    private SXRTexture emptyIcon;
    private SXRTexture speechIcon;
    private SXRContext sxrContext;

    private AccessibilityTexture(SXRContext sxrContext) {

        this.sxrContext = sxrContext;
        loadFiles();
    }

    public static AccessibilityTexture getInstance(SXRContext sxrContext) {
        if (instance == null)
            instance = new AccessibilityTexture(sxrContext);
        return instance;
    }

    private void loadFiles() {
        accessibilityIcon = sxrContext.getAssetLoader().loadTexture(new SXRAndroidResource(sxrContext, R.drawable.ico_accessibility));
        backIcon = sxrContext.getAssetLoader().loadTexture(new SXRAndroidResource(sxrContext, R.drawable.ico_back));
        spaceTexture = sxrContext.getAssetLoader().loadTexture(new SXRAndroidResource(sxrContext, R.drawable.circle_normal));
        talkBackMore = sxrContext.getAssetLoader().loadTexture(new SXRAndroidResource(sxrContext, R.drawable.ico_talkback_mais));
        talkBackLess = sxrContext.getAssetLoader().loadTexture(new SXRAndroidResource(sxrContext, R.drawable.ico_talkback_menos));
        zoomIn = sxrContext.getAssetLoader().loadTexture(new SXRAndroidResource(sxrContext, R.drawable.ico_zoom_mais));
        invertedColorsIcon = sxrContext.getAssetLoader().loadTexture(new SXRAndroidResource(sxrContext, R.drawable.ico_inverted));
        zoomOut = sxrContext.getAssetLoader().loadTexture(new SXRAndroidResource(sxrContext, R.drawable.ico_zoom_menos));
        speechIcon = sxrContext.getAssetLoader().loadTexture(new SXRAndroidResource(sxrContext, R.drawable.ico_speech));
        emptyIcon = sxrContext.getAssetLoader().loadTexture(new SXRAndroidResource(sxrContext, R.drawable.empty));
    }

    public SXRTexture getAccessibilityIcon() {
        return accessibilityIcon;
    }

    public SXRTexture getBackIcon() {
        return backIcon;
    }

    public SXRTexture getSpaceTexture() {
        return spaceTexture;
    }

    public SXRTexture getZoomOut() {
        return zoomOut;
    }

    public SXRTexture getZoomIn() {
        return zoomIn;
    }

    public SXRTexture getTalkBackLess() {
        return talkBackLess;
    }

    public SXRTexture getTalkBackMore() {
        return talkBackMore;
    }

    public SXRTexture getInvertedColorsIcon() {
        return invertedColorsIcon;
    }

    public SXRTexture getEmptyIcon() {
        return emptyIcon;
    }

    public void setEmptyIcon(SXRTexture emptyIcon) {
        this.emptyIcon = emptyIcon;
    }

    public SXRTexture getSpeechIcon() {
        return speechIcon;
    }

    public void setSpeechIcon(SXRTexture speechIcon) {
        this.speechIcon = speechIcon;
    }

}
