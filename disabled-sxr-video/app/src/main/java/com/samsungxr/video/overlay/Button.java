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

package com.samsungxr.video.overlay;

import com.samsungxr.SXRContext;
import com.samsungxr.SXRMesh;
import com.samsungxr.SXRRenderData;
import com.samsungxr.SXRTexture;
import com.samsungxr.video.focus.FocusListener;
import com.samsungxr.video.focus.FocusableSceneObject;

public class Button extends FocusableSceneObject {

    public Button(SXRContext sxrContext, SXRMesh mesh, SXRTexture active, SXRTexture inactive) {
        super(sxrContext, mesh, inactive);
        this.getRenderData().getMaterial().setTexture("active_texture", active);
        this.getRenderData().getMaterial().setTexture("inactive_texture", inactive);
        this.getRenderData().setRenderingOrder(SXRRenderData.SXRRenderingOrder.TRANSPARENT + 1);
        this.getRenderData().setOffset(true);
        this.getRenderData().setOffsetFactor(-1.0f);
        this.getRenderData().setOffsetUnits(-1.0f);
        this.attachEyePointeeHolder();

        super.setFocusListener(new FocusListener() {
            @Override
            public void gainedFocus(FocusableSceneObject object) {
                getRenderData().getMaterial().setMainTexture(
                        getRenderData().getMaterial().getTexture("active_texture"));
            }

            @Override
            public void lostFocus(FocusableSceneObject object) {
                getRenderData().getMaterial().setMainTexture(
                        getRenderData().getMaterial().getTexture("inactive_texture"));
            }
        });
    }

    public void setPosition(float x, float y, float z) {
        getTransform().setPosition(x, y, z);
    }

    public void show() {
        getRenderData().setRenderMask(
                SXRRenderData.SXRRenderMaskBit.Left | SXRRenderData.SXRRenderMaskBit.Right);
        getEyePointeeHolder().setEnable(true);
    }

    public void hide() {
        getRenderData().setRenderMask(0);
        getEyePointeeHolder().setEnable(false);
    }
}
