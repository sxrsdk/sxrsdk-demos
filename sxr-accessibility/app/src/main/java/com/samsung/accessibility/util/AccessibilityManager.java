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

import java.util.ArrayList;
import java.util.List;

import com.samsungxr.SXRContext;
import com.samsungxr.accessibility.SXRAccessibilityInvertedColors;
import com.samsungxr.accessibility.SXRAccessibilityTalkBack;
import com.samsungxr.accessibility.SXRAccessibilityZoom;

public class AccessibilityManager {

    private List<SXRAccessibilityTalkBack> mTalkBacks;

    private SXRAccessibilityInvertedColors mInvertedColors;
    private SXRAccessibilityZoom mZoom;

    public AccessibilityManager(SXRContext sxrContext) {
        mTalkBacks = new ArrayList<SXRAccessibilityTalkBack>();
        mInvertedColors = new SXRAccessibilityInvertedColors(sxrContext);
        mZoom = new SXRAccessibilityZoom();
    }

    public List<SXRAccessibilityTalkBack> getTalkBack() {
        return mTalkBacks;
    }

    public SXRAccessibilityInvertedColors getInvertedColors() {
        return mInvertedColors;
    }

    public SXRAccessibilityZoom getZoom() {
        return mZoom;
    }

}
