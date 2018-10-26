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

package com.samsungxr.controls.menu;

import com.samsungxr.SXRContext;
import com.samsungxr.SXRMaterial;
import com.samsungxr.SXRMesh;
import com.samsungxr.SXRMeshCollider;
import com.samsungxr.SXRRenderData;
import com.samsungxr.SXRTexture;
import com.samsungxr.controls.focus.ControlNode;
import com.samsungxr.controls.util.RenderingOrder;

import java.util.ArrayList;

public class TouchableButton extends ControlNode {

    private final int IDLE = 0;
    private final int HOVER = 1;
    private final int PRESSED = 2;

    private ArrayList<SXRTexture> textures;

    public TouchableButton(SXRContext sxrContext, ArrayList<SXRTexture> textures) {
        super(sxrContext);

        this.textures = textures;
        SXRMesh sMesh = getSXRContext().createQuad(0.4f, 0.4f);

        attachRenderData(new SXRRenderData(sxrContext));
        getRenderData().setMaterial(new SXRMaterial(sxrContext));
        getRenderData().setMesh(sMesh);

        getRenderData().getMaterial().setMainTexture(textures.get(IDLE));

        getRenderData().setRenderingOrder(RenderingOrder.MENU_FRAME_TEXT);

        attachComponent(new SXRMeshCollider(sxrContext, false));

    }

    @Override
    protected void gainedFocus() {
        getRenderData().getMaterial().setMainTexture(textures.get(HOVER));
    }

    @Override
    protected void lostFocus() {
        getRenderData().getMaterial().setMainTexture(textures.get(IDLE));
    }

    public void pressButton() {
        getRenderData().getMaterial().setMainTexture(textures.get(PRESSED));
    }

    public void unPressButton() {
        getRenderData().getMaterial().setMainTexture(textures.get(HOVER));
    }

}