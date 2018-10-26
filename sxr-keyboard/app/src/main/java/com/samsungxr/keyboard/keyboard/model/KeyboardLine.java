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

package com.samsungxr.keyboard.keyboard.model;

import com.samsungxr.SXRContext;
import com.samsungxr.SXRNode;
import com.samsungxr.SXRSphereCollider;
import com.samsungxr.keyboard.util.Constants;
import com.samsungxr.keyboard.util.NodeNames;
import com.samsungxr.keyboard.util.Util;

public class KeyboardLine extends SXRNode {

    public float softLineWidth = 0;
    protected int LINE_MARGINT_TOP = 164;

    public KeyboardLine(SXRContext sxrContext) {
        super(sxrContext);
        setName(NodeNames.KEYBOARD_LINE);
    }

    private void beforeAddItem(float width) {
        softLineWidth += Util.convertPixelToVRFloatValue(width) / 2;
    }

    private void afterAddItem(float width) {
        softLineWidth += Util.convertPixelToVRFloatValue(width) / 2;
    }

    public void addItemKeyboard(KeyboardItemBase item) {

        beforeAddItem(item.getWidth());

        item.getTransform().setPositionX(softLineWidth);

        afterAddItem(item.getWidth());

        addChildObject(item);

        attachDefaultEyePointee(item);
    }

    private void attachDefaultEyePointee(SXRNode sceneObject) {
        sceneObject.attachComponent(new SXRSphereCollider(getSXRContext()));
    }

    public void alingCenter(int numbersLine) {

        float backSpaceAdjust = (getWidth() / 2);

        for (SXRNode item : getChildren()) {

            float newX = item.getTransform().getPositionX() - backSpaceAdjust;
            item.getTransform().setPositionX(newX);
        }

        getTransform().setPosition(0.0f,
                Util.convertPixelToVRFloatValue(-LINE_MARGINT_TOP) * numbersLine,
                Constants.CAMERA_DISTANCE);
    }

    public float getWidth() {
        return softLineWidth;
    }
}
