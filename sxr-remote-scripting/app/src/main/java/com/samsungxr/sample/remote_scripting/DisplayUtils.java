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

package com.samsungxr.sample.remote_scripting;

import com.samsungxr.SXRContext;

import com.samsungxr.SXRCameraRig;
import com.samsungxr.SXRMaterial;
import com.samsungxr.SXRShaderData;
import com.samsungxr.SXRShaderId;

import java.lang.Runnable;

public class DisplayUtils {
    SXRMaterial postEffect;
    SXRContext gvrContext;

    public DisplayUtils(SXRContext context) {
        gvrContext = context;
    }

    public void addGammaCorrection() {
        // add a custom post effect for dynamically adjusting gamma
        SXRShaderId gammaShader = new SXRShaderId(GammaShader.class);
        postEffect = new SXRMaterial(gvrContext, gammaShader);

        postEffect.setFloat("u_gamma", 2.2f);
        SXRCameraRig rig = gvrContext.getMainScene().getMainCameraRig();
        rig.getLeftCamera().addPostEffect(postEffect);
        rig.getRightCamera().addPostEffect(postEffect);
    }

    public void setGamma(float gammaLevel) {
        final float gamma = gammaLevel;
        gvrContext.runOnGlThread(new Runnable() {
                @Override
                public void run() {
                    if(postEffect == null) {
                        addGammaCorrection();
                    }
                    postEffect.setFloat("u_gamma", gamma);
                }
            });
    }
}

