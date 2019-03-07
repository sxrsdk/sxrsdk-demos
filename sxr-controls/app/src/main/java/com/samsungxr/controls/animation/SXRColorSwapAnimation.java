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

package com.samsungxr.controls.animation;

import com.samsungxr.SXRHybridObject;
import com.samsungxr.SXRMaterial;
import com.samsungxr.SXRNode;
import com.samsungxr.animation.SXRMaterialAnimation;
import com.samsungxr.controls.shaders.ColorSwapShader;
import com.samsungxr.utility.Colors;

/** Animate the overlay color. */
public class SXRColorSwapAnimation extends SXRMaterialAnimation {

    private final float mStartR, mStartG, mStartB;
    private final float mDeltaR, mDeltaG, mDeltaB;

    /**
     * Animate the {@linkplain SXRMaterial#setColor(float, float, float) overlay
     * color.}
     * 
     * @param target {@link SXRMaterial} to animate
     * @param duration The animation duration, in seconds.
     * @param rgb A float[3] array, containing rgb values in GL format, from 0
     *            to 1
     */
    public SXRColorSwapAnimation(SXRMaterial target, float duration, float[] rgb) {
        super(target, duration);

        float[] rgbStart = mMaterial.getVec4(ColorSwapShader.COLOR);
        mStartR = rgbStart[0];
        mStartG = rgbStart[1];
        mStartB = rgbStart[2];

        mDeltaR = rgb[0] - mStartR;
        mDeltaG = rgb[1] - mStartG;
        mDeltaB = rgb[2] - mStartB;
    }

    /**
     * Animate the {@linkplain SXRMaterial#setColor(float, float, float) overlay
     * color.}
     * 
     * @param target {@link SXRMaterial} to animate
     * @param duration The animation duration, in seconds.
     * @param color An Android {@link Color} value
     */
    public SXRColorSwapAnimation(SXRMaterial target, float duration, int color) {
        this(target, duration, Colors.toColors(color));
    }

    /**
     * Animate the {@linkplain SXRMaterial#setColor(float, float, float) overlay
     * color.}
     * 
     * @param target {@link SXRNode} containing a {@link SXRMaterial} to
     *            animate
     * @param duration The animation duration, in seconds.
     * @param rgb A float[3] array, containing rgb values in GL format, from 0
     *            to 1
     */
    public SXRColorSwapAnimation(SXRNode target, float duration, float[] rgb) {
        this(getMaterial(target), duration, rgb);
    }

    /**
     * Animate the {@linkplain SXRMaterial#setColor(float, float, float) overlay
     * color.}
     * 
     * @param target {@link SXRNode} containing a {@link SXRMaterial} to
     *            animate
     * @param duration The animation duration, in seconds.
     * @param color An Android {@link Color} value
     */
    public SXRColorSwapAnimation(SXRNode target, float duration, int color) {
        this(target, duration, Colors.toColors(color));
    }

    public void animate(float timeInSec) {

        mMaterial.setVec4(ColorSwapShader.COLOR, mStartR + (timeInSec/getDuration()) * mDeltaR, mStartG + (timeInSec/getDuration())
                * mDeltaG, mStartB + (timeInSec/getDuration()) * mDeltaB,
                1);

    }
}
