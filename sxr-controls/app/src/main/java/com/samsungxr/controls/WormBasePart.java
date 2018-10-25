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

import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRMesh;
import com.samsungxr.SXRSceneObject;
import com.samsungxr.SXRShaderId;
import com.samsungxr.SXRTexture;
import com.samsungxr.controls.anim.AnimationsTime;
import com.samsungxr.controls.animation.SXRColorSwapAnimation;
import com.samsungxr.controls.shaders.ColorSwapShader;
import com.samsungxr.controls.util.ColorControls.Color;
import com.samsungxr.controls.util.RenderingOrder;

public class WormBasePart extends SXRSceneObject {

    private Color color;
    private final float WORM_INITIAL_Z = -3;
    private final float WORM_INITIAL_Y = -0.83f;
    private SXRSceneObject segment;

    public WormBasePart(SXRContext gvrContext, int meshResId, int textureResId, Color color) {
        super(gvrContext);
        this.color = color;

        getTransform().setPosition(0, WORM_INITIAL_Y, WORM_INITIAL_Z);
        getTransform().setScale(0.4f, 0.4f, 0.4f);

        build(meshResId, textureResId);
    }

    private void build(int meshResId, int textureResId) {

        SXRContext gvrContext = getSXRContext();

        SXRMesh mesh = gvrContext.getAssetLoader().loadMesh(
                new SXRAndroidResource(gvrContext, meshResId));

        SXRTexture texture = gvrContext.getAssetLoader().loadTexture(
                new SXRAndroidResource(gvrContext, textureResId));

        segment = new SXRSceneObject(gvrContext, mesh, texture, new SXRShaderId(ColorSwapShader.class));

        segment.getRenderData().setRenderingOrder(RenderingOrder.WORM);

        applyShader(gvrContext, segment, color);
        addChildObject(segment);
    }

    private void applyShader(SXRContext gvrContext, SXRSceneObject wormPiece, Color color) {

        SXRTexture texture = gvrContext.getAssetLoader().loadTexture(new SXRAndroidResource(gvrContext,
                R.drawable.wormy_diffuse_light));

        wormPiece.getRenderData().getMaterial()
                .setTexture(ColorSwapShader.TEXTURE_GRAYSCALE, texture);

        texture = gvrContext.getAssetLoader().loadTexture(new SXRAndroidResource(gvrContext,
                R.drawable.wormy_diffuse_2));

        wormPiece.getRenderData().getMaterial()
                .setTexture(ColorSwapShader.TEXTURE_DETAILS, texture);
        wormPiece
                .getRenderData()
                .getMaterial()
                .setVec4(ColorSwapShader.COLOR, color.getRed(), color.getGreen(), color.getBlue(),
                        1);
    }

    public void animChangeColor(Color color) {

        float[] colorArray = new float[3];
        colorArray[0] = color.getRed();
        colorArray[1] = color.getGreen();
        colorArray[2] = color.getBlue();

        new SXRColorSwapAnimation(segment, AnimationsTime.getChangeColorTime(), colorArray)
                .start(getSXRContext().getAnimationEngine());
    }

    public void resetColor(Color color) {

        segment.getRenderData().getMaterial().setVec4(
                ColorSwapShader.COLOR, color.getRed(), color.getGreen(), color.getBlue(), 1);
    }
}
