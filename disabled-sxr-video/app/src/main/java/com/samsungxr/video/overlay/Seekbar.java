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

import android.annotation.SuppressLint;

import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRRenderData.SXRRenderMaskBit;
import com.samsungxr.SXRRenderData.SXRRenderingOrder;
import com.samsungxr.SXRSceneObject;

import java.io.FileNotFoundException;

public class Seekbar extends SXRSceneObject {
    private static final float WIDTH = 8.0f;
    private static final float HEIGHT = 0.4f;
    private static final float DEPTH = 8.0f;
    private static final float Y = -0.2f;
    private SXRSceneObject mPlayedSide = null;
    private SXRSceneObject mLeftSide = null;
    private SXRSceneObject mPointer = null;
    private SXRSceneObject mGlow = null;
    private SXRSceneObject mCurrentTime = null;
    private SXRSceneObject mDuration = null;

    public Seekbar(SXRContext gvrContext) throws FileNotFoundException {
        super(gvrContext);
        getTransform().setPosition(-0.1f, Y, -DEPTH);

        mPlayedSide = new SXRSceneObject(gvrContext, gvrContext.createQuad(
                1.0f, 0.1f), gvrContext.getAssetLoader().loadTexture(new SXRAndroidResource("seekbar/dark-gray.png")));
        mPlayedSide.getRenderData().setRenderingOrder(
                SXRRenderingOrder.TRANSPARENT + 2);
        mPlayedSide.getRenderData().setOffset(true);
        mPlayedSide.getRenderData().setOffsetFactor(-2.0f);
        mPlayedSide.getRenderData().setOffsetUnits(-2.0f);

        mLeftSide = new SXRSceneObject(gvrContext, gvrContext.createQuad(1.0f,
                0.1f), gvrContext.getAssetLoader().loadTexture(new SXRAndroidResource("seekbar/light-gray.png")));
        mLeftSide.getRenderData().setRenderingOrder(
                SXRRenderingOrder.TRANSPARENT + 2);
        mLeftSide.getRenderData().setOffset(true);
        mLeftSide.getRenderData().setOffsetFactor(-2.0f);
        mLeftSide.getRenderData().setOffsetUnits(-2.0f);

        mPointer = new SXRSceneObject(gvrContext, gvrContext.createQuad(0.08f,
                0.3f), gvrContext.getAssetLoader().loadTexture(new SXRAndroidResource("seekbar/dark-gray-circle.png")));
        mPointer.getRenderData().setRenderingOrder(
                SXRRenderingOrder.TRANSPARENT + 3);
        mPointer.getRenderData().setOffset(true);
        mPointer.getRenderData().setOffsetFactor(-3.0f);
        mPointer.getRenderData().setOffsetUnits(-3.0f);

        mGlow = new SXRSceneObject(gvrContext,
                gvrContext.createQuad(8.8f, 0.5f),
                gvrContext.getAssetLoader().loadTexture(new SXRAndroidResource("seekbar/seekbar-glow.png")));
        mGlow.getRenderData().setRenderingOrder(
                SXRRenderingOrder.TRANSPARENT + 1);
        mGlow.getRenderData().setOffset(true);
        mGlow.getRenderData().setOffsetFactor(-1.0f);
        mGlow.getRenderData().setOffsetUnits(-1.0f);

        mCurrentTime = new SXRSceneObject(gvrContext, gvrContext.createQuad(
                2.4f, 0.3f), TextFactory.create(gvrContext, "1111"));
        mCurrentTime.getTransform().setPosition(-3.2f, -0.3f, 0.0f);
        mCurrentTime.getRenderData().setRenderingOrder(
                SXRRenderingOrder.TRANSPARENT + 2);
        mCurrentTime.getRenderData().setOffset(true);
        mCurrentTime.getRenderData().setOffsetFactor(-2.0f);
        mCurrentTime.getRenderData().setOffsetUnits(-2.0f);

        mDuration = new SXRSceneObject(gvrContext, gvrContext.createQuad(2.4f,
                0.3f), TextFactory.create(gvrContext, "2222"));
        mDuration.getTransform().setPosition(3.2f, -0.3f, 0.0f);
        mDuration.getRenderData().setRenderingOrder(
                SXRRenderingOrder.TRANSPARENT + 2);
        mDuration.getRenderData().setOffset(true);
        mDuration.getRenderData().setOffsetFactor(-2.0f);
        mDuration.getRenderData().setOffsetUnits(-2.0f);

        addChildObject(mPlayedSide);
        addChildObject(mLeftSide);
        addChildObject(mPointer);
        addChildObject(mGlow);
        addChildObject(mCurrentTime);
        addChildObject(mDuration);
        // glow is hidden at first
        mGlow.getRenderData().setRenderMask(0);
    }

    public Float getRatio(float[] lookAt) {
        float x = lookAt[0];
        float y = lookAt[1];
        float z = lookAt[2];

        x *= -DEPTH / z;
        y *= -DEPTH / z;

        if (x > -WIDTH * 0.5f && x < WIDTH * 0.5f && y > Y - HEIGHT * 0.5f
                && y < Y + HEIGHT * 0.5f) {
            return x / WIDTH + 0.5f;
        } else {
            return null;
        }
    }

    @SuppressLint("DefaultLocale")
    public void setTime(SXRContext gvrContext, int current, int duration) {
        float ratio = (float) current / (float) duration;
        float left = -WIDTH * 0.5f;
        float center = ratio * WIDTH + left;
        float right = WIDTH * 0.5f;
        mPlayedSide.getTransform().setPositionX((left + center) * 0.5f);
        mPlayedSide.getTransform().setScaleX(center - left);
        mLeftSide.getTransform().setPositionX((center + right) * 0.5f);
        mLeftSide.getTransform().setScaleX(right - center);
        mPointer.getTransform().setPositionX(center);

        /*
         * ms to s
         */
        current /= 1000;
        duration /= 1000;

        String currentText = String.format("%02d:%02d:%02d", current / 3600,
                (current % 3600) / 60, current % 60);
        String durationText = String.format("%02d:%02d:%02d", duration / 3600,
                (duration % 3600) / 60, duration % 60);
        mCurrentTime.getRenderData().getMaterial()
                .setMainTexture(TextFactory.create(gvrContext, currentText));
        mDuration.getRenderData().getMaterial()
                .setMainTexture(TextFactory.create(gvrContext, durationText));
    }

    public void glow() {
        mGlow.getRenderData().setRenderMask(
                SXRRenderMaskBit.Left | SXRRenderMaskBit.Right);
    }

    public void unglow() {
        mGlow.getRenderData().setRenderMask(0);
    }

    public void setRenderMask(int renderMask) {
        mPlayedSide.getRenderData().setRenderMask(renderMask);
        mLeftSide.getRenderData().setRenderMask(renderMask);
        mPointer.getRenderData().setRenderMask(renderMask);
        mCurrentTime.getRenderData().setRenderMask(renderMask);
        mDuration.getRenderData().setRenderMask(renderMask);
        if (renderMask == 0) {
            mGlow.getRenderData().setRenderMask(renderMask);
        }
    }
}
