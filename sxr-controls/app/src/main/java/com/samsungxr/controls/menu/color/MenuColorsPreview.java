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

package com.samsungxr.controls.menu.color;

import com.samsungxr.SXRContext;
import com.samsungxr.SXRMesh;
import com.samsungxr.SXRNode;
import com.samsungxr.SXRTexture;
import com.samsungxr.controls.Worm;
import com.samsungxr.controls.util.ColorControls.Color;

public class MenuColorsPreview extends SXRNode {

    private final float WORM_HEAD_X_POSITION = -0.15f;
    private final float WORM_MIDDLE_X_POSITION = 0f;
    private final float WORM_END_X_POSITION = 0.16f;
    private final float WORM_Y_POSITION = -.85f;

    private final float WORM_SCALE = 1.2f;

    private Worm worm;

    public MenuColorsPreview(SXRContext sxrContext, SXRMesh mesh, SXRTexture texture) {
        super(sxrContext, mesh, texture);

        worm = new Worm(sxrContext);
        
        worm.getHead().getParent().getTransform().setPositionY(WORM_Y_POSITION);
        worm.getMiddle().getTransform().setPositionY(WORM_Y_POSITION);
        worm.getEnd().getTransform().setPositionY(WORM_Y_POSITION);

        worm.getHead().getTransform().setPositionX(WORM_HEAD_X_POSITION);
        worm.getHead().getTransform().rotateByAxis(90, 0, 1, 0);

        worm.getMiddle().getTransform().setPositionX(WORM_MIDDLE_X_POSITION);
        worm.getMiddle().getTransform().rotateByAxis(90, 0, 1, 0);

        worm.getEnd().getTransform().setPositionX(WORM_END_X_POSITION);
        worm.getEnd().getTransform().rotateByAxis(90, 0, 1, 0);

        worm.getTransform().setScale(WORM_SCALE, WORM_SCALE, WORM_SCALE);

        worm.getTransform().setPosition(0.05f, 1.f, 3.7f);
        
        addChildObject(worm);
    }

    public void show() {
    }

    public void changeColorTo(Color color) {
        worm.changeColor(color);
    }

    public void hide() {
    }
}
