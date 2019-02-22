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

package com.samsungxr.keyboard.shader;

import com.samsungxr.SXRHybridObject;
import com.samsungxr.SXRMaterial;
import com.samsungxr.SXRNode;
import com.samsungxr.animation.SXRMaterialAnimation;

/** Animate the opacity. */
public class SXRShaderAnimation extends SXRMaterialAnimation {

    private final float mInitialValue;
    private final float mDeltaValue;
    private final String mKey;

    /**
     * Animate the {@link SXRMaterial#setFloat(key,float) blur} property.
     * 
     * @param target {@link SXRMaterial} to animate.
     * @param duration The animation duration, in seconds.
     * @param opacity A value from 0 to 1
     */
    public SXRShaderAnimation(SXRMaterial target, String key, float duration, float finalValue) {
        super(target, duration);
        mKey = key;
        mInitialValue = mMaterial.getFloat(mKey);
        mDeltaValue = finalValue - mInitialValue;

    }

    /**
     * Animate the {@link SXRMaterial#setFloat(key,float) blur} property.
     * 
     * @param target {@link SXRNode} containing a {@link SXRMaterial} to
     *            animate.
     * @param duration The animation duration, in seconds.
     * @param opacity A value from 0 to 1
     */
    public SXRShaderAnimation(SXRNode target, String key, float duration,
            float finalValue) {
        this(getMaterial(target), key, duration, finalValue);
    }

    public void animate(float timeInSec) {
        mMaterial.setFloat(mKey, mInitialValue + mDeltaValue * (timeInSec/getDuration()));
    }
}
