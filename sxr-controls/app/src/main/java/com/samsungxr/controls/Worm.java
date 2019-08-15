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

package com.samsungxr.controls;

import android.view.MotionEvent;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import com.samsungxr.SXRCameraRig;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRNode;
import com.samsungxr.animation.SXRAnimation;
import com.samsungxr.animation.SXROnFinish;
import com.samsungxr.animation.SXRPositionAnimation;
import com.samsungxr.animation.SXRRotationByAxisAnimation;
import com.samsungxr.animation.SXRRotationByAxisWithPivotAnimation;
import com.samsungxr.animation.SXRScaleAnimation;
import com.samsungxr.controls.anim.ScaleWorm;
import com.samsungxr.controls.input.GamepadInput;
import com.samsungxr.controls.input.TouchPadInput;
import com.samsungxr.controls.model.Apple;
import com.samsungxr.controls.util.ColorControls;
import com.samsungxr.controls.util.ColorControls.Color;
import com.samsungxr.controls.util.Constants;
import com.samsungxr.controls.util.MathUtils;
import com.samsungxr.controls.util.RenderingOrder;
import com.samsungxr.controls.util.Util;
import com.samsungxr.io.SXRTouchPadGestureListener;

public class Worm extends SXRNode {

    private static final float SHADOW_END_OFFSET = 0.801f;
    private static final float SHADOW_MIDDLE_OFFSET = 0.8f;
    private static final float SHADOW_HEAD_OFFSET = 0.9f;
    // private static final float MINIMUM_DISTANCE_FACTOR = 0.5f;
    // Chain Data
    private final float CHAIN_DISTANCE_HEAD_MIDDLE = 0.575f;
    private final float CHAIN_DISTANCE_MIDDLE_END = 0.475f;

    private final float CHAIN_SPEED_HEAD_MIDDLE = 0.055f;
    private final float CHAIN_SPEED_MIDDLE_END = 0.065f;
    private final float WORM_INITIAL_Z = -3;
    private final float WORM_INITIAL_Y = -0.9f;

    private float DISTANCE_TO_EAT_APPLE = 0.50f;
    private WormBasePart head, middle, end;

    public SXRNode wormParent;
    private boolean isRotatingWorm = false;

    private SXRAnimation wormParentAnimation;

    private MovementDirection wormDirection = MovementDirection.Up;

    private Color color;

    private float[] scaleWorm = new float[] {
            0.4f, 0.4f, 0.4f
    };
    private WormShadow shadowHead;
    private WormShadow shadowMiddle;
    private WormShadow shadowEnd;

    public enum MovementDirection {
        Up, Right, Down, Left
    }

    public Worm(SXRContext sxrContext) {
        super(sxrContext);

        ColorControls sxrColor = new ColorControls(sxrContext.getContext());
        Color color = sxrColor.parseColor(R.color.color10);

        this.color = color;

        createWormParts(color);
    }

    public SXRNode getWormParentation() {
        return wormParent;
    }

    public void resetColor(Color color) {
        head.resetColor(color);
        middle.resetColor(color);
        end.resetColor(color);
    }

    private void createWormParts(Color color) {

        wormParent = new SXRNode(getSXRContext());
        addChildObject(wormParent);

        head = new WormBasePart(getSXRContext(), R.raw.sphere_head, R.drawable.worm_head_texture, color);
        middle = new WormBasePart(getSXRContext(), R.raw.sphere_body, R.drawable.worm_head_texture, color);
        end = new WormBasePart(getSXRContext(), R.raw.sphere_tail, R.drawable.worm_head_texture, color);

        wormParent.getTransform().setPosition(0, WORM_INITIAL_Y, WORM_INITIAL_Z);
        head.getTransform().setPosition(0, 0, 0);

        wormParent.addChildObject(head);

        addChildObject(middle);
        addChildObject(end);
    }

    public void enableShadow() {

        float factor = 3f;
        shadowHead = new WormShadow(getSXRContext(), 0.27f * factor, 0.27f * factor, RenderingOrder.WORM_SHADOW_HEADER);
        shadowMiddle = new WormShadow(getSXRContext(), 0.2f * factor, 0.2f * factor, RenderingOrder.WORM_SHADOW_MIDDLE);
        shadowEnd = new WormShadow(getSXRContext(), 0.18f * factor, 0.18f * factor, RenderingOrder.WORM_SHADOW_END);

        head.addChildObject(shadowHead);
        middle.addChildObject(shadowMiddle);
        end.addChildObject(shadowEnd);

        startShadowsPosition();
    }

    private void startShadowsPosition() {

        shadowHead.getTransform().setPositionY(shadowHead.getParent().getParent().getTransform().getPositionY() + SHADOW_HEAD_OFFSET);
        shadowMiddle.getTransform().setPositionY(shadowMiddle.getParent().getTransform().getPositionY() + SHADOW_MIDDLE_OFFSET);
        shadowEnd.getTransform().setPositionY(shadowEnd.getParent().getTransform().getPositionY() + SHADOW_END_OFFSET);
    }

    public void changeColor(Color color) {

        this.color = color;

        float[] colorArray = new float[3];
        colorArray[0] = color.getRed();
        colorArray[1] = color.getGreen();
        colorArray[2] = color.getBlue();

        head.animChangeColor(color);
        middle.animChangeColor(color);
        end.animChangeColor(color);
    }

    public Color getColor() {
        return color;
    }

    public float[] getScaleFactor() {

        this.scaleWorm[0] = getHead().getTransform().getScaleX();
        this.scaleWorm[1] = getMiddle().getTransform().getScaleX();
        this.scaleWorm[2] = getEnd().getTransform().getScaleX();

        return scaleWorm;
    }

    public void chainMove(SXRContext sxrContext) {

        if (!ScaleWorm.animPlaying) {

            if (MathUtils.distance(wormParent, middle) > CHAIN_DISTANCE_HEAD_MIDDLE
                    * middle.getTransform().getScaleX()) {

                float chainSpeed = CHAIN_SPEED_HEAD_MIDDLE
                        * (float) Util.distance(wormParent.getTransform(), getSXRContext()
                        .getMainScene().getMainCameraRig().getTransform());

                middle.getTransform().setRotationByAxis(
                        MathUtils.getYRotationAngle(middle, wormParent), 0, 1, 0);
                end.getTransform().setRotationByAxis(MathUtils.getYRotationAngle(end, middle), 0, 1, 0);

                float newX = middle.getTransform().getPositionX()
                        + (wormParent.getTransform().getPositionX() -
                        middle.getTransform().getPositionX()) * chainSpeed;

                float newY = middle.getTransform().getPositionY()
                        + (wormParent.getTransform().getPositionY() -
                        middle.getTransform().getPositionY()) * chainSpeed;

                float newZ = middle.getTransform().getPositionZ()
                        + (wormParent.getTransform().getPositionZ() -
                        middle.getTransform().getPositionZ()) * chainSpeed;

                middle.getTransform().setPosition(newX, newY, newZ);
            }

            if (MathUtils.distance(middle, end) > CHAIN_DISTANCE_MIDDLE_END
                    * end.getTransform().getScaleX()) {

                float chainSpeed = CHAIN_SPEED_MIDDLE_END
                        * (float) Util.distance(wormParent.getTransform(), getSXRContext()
                        .getMainScene().getMainCameraRig().getTransform());

                middle.getTransform().setRotationByAxis(
                        MathUtils.getYRotationAngle(middle, wormParent), 0, 1, 0);
                end.getTransform().setRotationByAxis(MathUtils.getYRotationAngle(end, middle), 0, 1, 0);

                float newX = end.getTransform().getPositionX() + (middle.getTransform().getPositionX() -
                        end.getTransform().getPositionX()) * chainSpeed;

                float newY = end.getTransform().getPositionY() + (middle.getTransform().getPositionY() -
                        end.getTransform().getPositionY()) * chainSpeed;

                float newZ = end.getTransform().getPositionZ() + (middle.getTransform().getPositionZ() -
                        end.getTransform().getPositionZ()) * chainSpeed;

                end.getTransform().setPosition(newX, newY, newZ);
            }
        }
    }

    public void rotateWorm(MovementDirection movementDirection) {
        if (!isRotatingWorm) {
            isRotatingWorm = true;
            float angle = getRotatingAngle(movementDirection);
            new SXRRotationByAxisAnimation(head, .1f, angle, 0, 1, 0).start(
                    getSXRContext().getAnimationEngine()).setOnFinish(
                    new SXROnFinish() {

                        @Override
                        public void finished(SXRAnimation arg0) {
                            isRotatingWorm = false;
                        }
                    });
        }
    }

    private int getRotatingAngle(MovementDirection movementDirection) {
        int movementDiference = movementDirection.ordinal() - wormDirection.ordinal();
        wormDirection = movementDirection;
        if (movementDiference == 1 || movementDiference == -3) {
            return -90;
        } else if (movementDiference == 2 || movementDiference == -2) {
            return 180;
        } else if (movementDiference == 3 || movementDiference == -1) {
            return 90;
        } else {
            return 0;
        }
    }

    public void moveAlongCameraVector(float duration, float movement) {
        if (wormParentAnimation != null) {
            getSXRContext().getAnimationEngine().stop(wormParentAnimation);
        }

        SXRCameraRig cameraObject = getSXRContext().getMainScene().getMainCameraRig();

        float distance = (float) Util.distance(wormParent.getTransform(),
                cameraObject.getTransform())
                + movement;
        float[] newPosition = Util.calculatePointBetweenTwoObjects(cameraObject.getTransform(),
                wormParent.getTransform(), distance);

        if (movement < 0
                && MathUtils.distance(cameraObject.getTransform(), newPosition) < Constants.MIN_WORM_MOVE_DISTANCE)
            return;
        if (movement > 0
                && MathUtils.distance(cameraObject.getTransform(),
                wormParent.getTransform()) > Constants.MAX_WORM_MOVE_DISTANCE)
            return;

        wormParentAnimation = new SXRPositionAnimation(wormParent.getTransform(),
                duration, newPosition[0] - wormParent.getTransform().getPositionX(),
                0,
                newPosition[2] - wormParent.getTransform().getPositionZ())
                .start(getSXRContext().getAnimationEngine());
    }

    public void rotateAroundCamera(float duration, float degree) {
        if (wormParentAnimation != null) {
            getSXRContext().getAnimationEngine().stop(wormParentAnimation);
        }

        wormParentAnimation = new SXRRotationByAxisWithPivotAnimation(
                wormParent.getTransform(), duration, degree, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f)
                .start(getSXRContext().getAnimationEngine());

    }

    public void interactWithDPad() {

        if (!ScaleWorm.animPlaying) {

            if (GamepadInput.getCenteredAxis(MotionEvent.AXIS_HAT_X) >= 1
                    || GamepadInput.getCenteredAxis(MotionEvent.AXIS_X) >= 1
                    || GamepadInput.getCenteredAxis(MotionEvent.AXIS_RX) >= 1) {

                rotateAroundCamera(.1f, -5f);
                rotateWorm(MovementDirection.Right);
            }
            if (GamepadInput.getCenteredAxis(MotionEvent.AXIS_HAT_X) <= -1
                    || GamepadInput.getCenteredAxis(MotionEvent.AXIS_X) <= -1
                    || GamepadInput.getCenteredAxis(MotionEvent.AXIS_RX) <= -1) {

                rotateAroundCamera(.1f, 5f);
                rotateWorm(MovementDirection.Left);
            }
            if (GamepadInput.getCenteredAxis(MotionEvent.AXIS_HAT_Y) >= 1
                    || GamepadInput.getCenteredAxis(MotionEvent.AXIS_Y) >= 1
                    || GamepadInput.getCenteredAxis(MotionEvent.AXIS_RY) >= 1) {

                moveAlongCameraVector(.1f, -.225f);
                rotateWorm(MovementDirection.Down);
            }
            if (GamepadInput.getCenteredAxis(MotionEvent.AXIS_HAT_Y) <= -1
                    || GamepadInput.getCenteredAxis(MotionEvent.AXIS_Y) <= -1
                    || GamepadInput.getCenteredAxis(MotionEvent.AXIS_RY) <= -1) {

                moveAlongCameraVector(.1f, .225f);
                rotateWorm(MovementDirection.Up);
            }
        }
    }

    public void checkWormEatingApple(SXRContext sxrContext) {

        Vector3D wormPosition = new Vector3D(wormParent.getTransform().getPositionX(), head
                .getParent()
                .getTransform().getPositionY(), wormParent.getTransform().getPositionZ());

        for (Apple a : Apple.appleList) {
            Vector3D applePosition = new Vector3D(a.getTransform().getPositionX(), a.getTransform()
                    .getPositionY(), a.getTransform().getPositionZ());

            if (Vector3D.distance(applePosition, wormPosition) < DISTANCE_TO_EAT_APPLE) {

                a.resetPosition(sxrContext);
            }
        }
    }

    public void animateWormByTouchPad() {

        if (!ScaleWorm.animPlaying) {

            SXRTouchPadGestureListener.Action swipeDirection = TouchPadInput.getCurrent().swipeDirection;

            float duration = 0.6f;
            float movement = 0.75f;
            float degree = 22.5f;

            if (swipeDirection.equals(SXRTouchPadGestureListener.Action.SwipeUp))
            {
                moveAlongCameraVector(duration, movement);
                rotateWorm(MovementDirection.Up);
            }
            else if (swipeDirection.equals(SXRTouchPadGestureListener.Action.SwipeDown))
            {
                moveAlongCameraVector(duration, -movement);
                rotateWorm(MovementDirection.Down);

            }
            else if (swipeDirection.equals(SXRTouchPadGestureListener.Action.SwipeForward))
            {
                rotateAroundCamera(duration, -degree);
                rotateWorm(MovementDirection.Right);
            }
            else if (swipeDirection.equals(SXRTouchPadGestureListener.Action.SwipeBackward))
            {
                rotateAroundCamera(duration, degree);
                rotateWorm(MovementDirection.Left);
            }
        }
    }

    public SXRNode getHead() {
        return head;
    }

    public SXRNode getMiddle() {
        return middle;
    }

    public SXRNode getEnd() {
        return end;
    }

    public void moveWorm(float scaleFactor) {
        moveWormPart(getHead(), scaleFactor);
        moveWormPart(getEnd(), scaleFactor);
    }

    public void resetScaleWorm(float[] scaleFactor) {

        getHead().getTransform().setScale(scaleFactor[0], scaleFactor[0], scaleFactor[0]);
        getMiddle().getTransform().setScale(scaleFactor[1], scaleFactor[1], scaleFactor[1]);
        getEnd().getTransform().setScale(scaleFactor[2], scaleFactor[2], scaleFactor[2]);
    }

    public void scaleWorm(float scaleFactor) {

        scaleWormPart(getHead(), scaleFactor);
        scaleWormPart(getMiddle(), scaleFactor);
        scaleWormPart(getEnd(), scaleFactor);
    }

    private void moveWormPart(SXRNode part, float scaleFactor) {

        float currentScale = part.getTransform().getScaleX();
        float ratio = (currentScale + scaleFactor) / currentScale;
        float currentPartPositionX = part.getTransform().getPositionX();
        float newPartPositionX = ratio * currentPartPositionX;

        new SXRPositionAnimation(part, 0.1f,
                newPartPositionX
                        - currentPartPositionX, 0, 0).start(getSXRContext()
                .getAnimationEngine());
    }

    private void scaleWormPart(SXRNode part, float scaleFactor) {

        new SXRScaleAnimation(part, 0.1f, part.getTransform().getScaleX()
                + scaleFactor)
                .setOnFinish(new SXROnFinish() {

                    @Override
                    public void finished(SXRAnimation arg0) {
                        ScaleWorm.animPlaying = false;
                    }

                }).start(getSXRContext().getAnimationEngine());
    }
}