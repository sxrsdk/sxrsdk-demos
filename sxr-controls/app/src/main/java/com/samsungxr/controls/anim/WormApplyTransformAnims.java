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

package com.samsungxr.controls.anim;

import com.samsungxr.SXRContext;
import com.samsungxr.SXRNode;
import com.samsungxr.animation.SXRPositionAnimation;
import com.samsungxr.animation.SXRScaleAnimation;
import com.samsungxr.controls.Main;
import com.samsungxr.controls.util.Util;

public class WormApplyTransformAnims {

    public static void moveWorm(SXRContext sxrContext, float scaleFactor) {
        moveWormPart(sxrContext, Main.worm.getHead(), scaleFactor);
        moveWormPart(sxrContext, Main.worm.getEnd(), scaleFactor);
    }

    public static void resetScaleWorm(float[] scaleFactor) {

        Main.worm.getHead().getTransform()
                .setScale(scaleFactor[0], scaleFactor[0], scaleFactor[0]);
        Main.worm.getMiddle().getTransform()
                .setScale(scaleFactor[1], scaleFactor[1], scaleFactor[1]);
        Main.worm.getEnd().getTransform()
                .setScale(scaleFactor[2], scaleFactor[2], scaleFactor[2]);
    }

    public static void scaleWorm(SXRContext sxrContext, float scaleFactor) {

        scaleWormPart(sxrContext, Main.worm.getHead(), scaleFactor);
        scaleWormPart(sxrContext, Main.worm.getMiddle(), scaleFactor);
        scaleWormPart(sxrContext, Main.worm.getEnd(), scaleFactor);
    }

    public static void moveWormPart(SXRContext sxrContext, SXRNode part, float scaleFactor) {

        float currentScale = part.getTransform().getScaleX();
        float ratio = (currentScale + scaleFactor) / currentScale;
        float currentPartPositionX = part.getTransform().getPositionX();
        float newPartPositionX = ratio * currentPartPositionX;

        new SXRPositionAnimation(part, AnimationsTime.getScaleTime(),
                newPartPositionX
                        - currentPartPositionX, 0, 0).start(sxrContext
                .getAnimationEngine());
    }

    public static void moveWormPartToClose(SXRContext sxrContext, SXRNode moveablePart,
            SXRNode basePart) {

        float scaleRatio = ScaleWorm.getWorm().getHead().getTransform().getScaleX()
                / ScaleWorm.getLastSize()[0];

        float distance = (float) Util.distance(basePart, moveablePart) * scaleRatio;
        float[] newPosition = Util
                .calculatePointBetweenTwoObjects(basePart, moveablePart, distance);

        float newX = newPosition[0] - moveablePart.getTransform().getPositionX();
        float newZ = newPosition[2] - moveablePart.getTransform().getPositionZ();

        new SXRPositionAnimation(moveablePart, AnimationsTime.getScaleTime(), newX, 0, newZ)
                .start(sxrContext.getAnimationEngine());
    }

    private static void scaleWormPart(SXRContext sxrContext, SXRNode part, float scaleFactor) {

        new SXRScaleAnimation(part, AnimationsTime.getScaleTime(), part.getTransform().getScaleX()
                + scaleFactor)
                .start(sxrContext.getAnimationEngine());
    }

    public static SXRPositionAnimation moveWormPartReset(SXRNode moveablePart,
            SXRNode basePart) {

        float scaleRatio = ScaleWorm.getLastSize()[0] / basePart.getTransform().getScaleX();

        float distance = (float) Util.distance(basePart, moveablePart) * scaleRatio;
        float[] newPosition = Util
                .calculatePointBetweenTwoObjects(basePart, moveablePart, distance);

        float newX = newPosition[0] - moveablePart.getTransform().getPositionX();
        float newZ = newPosition[2] - moveablePart.getTransform().getPositionZ();

        return new SXRPositionAnimation(moveablePart, 0f, newX, 0, newZ);
    }
}
