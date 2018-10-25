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

import android.graphics.Color;
import android.graphics.Paint;

import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRBitmapImage;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRMaterial;
import com.samsungxr.SXRMesh;
import com.samsungxr.SXRRenderData;
import com.samsungxr.SXRSceneObject;
import com.samsungxr.SXRShaderId;
import com.samsungxr.SXRTexture;
import com.samsungxr.keyboard.model.KeyboardCharItem;
import com.samsungxr.keyboard.shader.TransparentButtonShaderThreeStates;
import com.samsungxr.keyboard.util.SXRTextBitmapFactory;
import com.samsungxr.keyboard.util.RenderingOrder;
import com.samsungxr.keyboard.util.SceneObjectNames;
import com.samsungxr.keyboard.util.Util;

public abstract class KeyboardItemBase extends SXRSceneObject {

    protected KeyboardItemStyle styleItem;
    protected KeyboardCharItem keyboardCharItem;

    public KeyboardItemBase(SXRContext gvrContext, KeyboardItemStyle styleItem) {
        super(gvrContext);
        setName(SceneObjectNames.KEYBOARD_ITEM);

        this.styleItem = styleItem;
    }

    public abstract void setNormalMaterial();

    public abstract void setHoverMaterial();

    public abstract void switchMaterialState(int state);

    public float getWidth() {
        return styleItem.getSizeQuadWidth() + styleItem.getSpace();
    }

    public void createTextures() {

        SXRRenderData renderData = new SXRRenderData(getSXRContext());

        SXRMesh mesh = new SXRMesh(getSXRContext(), "float3 a_position float2 a_texcoord");
        mesh.createQuad(
                Util.convertPixelToVRFloatValue(styleItem.getSizeQuadWidth()),
                Util.convertPixelToVRFloatValue(styleItem.getSizeQuadHeight()));

        SXRMaterial mat = new SXRMaterial(getSXRContext(), new SXRShaderId(TransparentButtonShaderThreeStates.class));//dif.getShaderId());
        renderData.setMesh(mesh);
        renderData.setMaterial(mat);

        attachRenderData(renderData);

        getRenderData().setRenderingOrder(RenderingOrder.KEYBOARD);

        getRenderData().getMaterial().setFloat(TransparentButtonShaderThreeStates.OPACITY,
                styleItem.getOpacityTarget());
    }

    public void configureTextures() {

        getRenderData().getMaterial().setTexture(
                TransparentButtonShaderThreeStates.TEXTURE_KEY,
                getSXRContext().getAssetLoader().loadTexture(
                        new SXRAndroidResource(getSXRContext(), styleItem.getTexture())));

        getRenderData().getMaterial().setTexture(
                TransparentButtonShaderThreeStates.TEXTURE_HOVER_KEY,
                getSXRContext().getAssetLoader().loadTexture(
                        new SXRAndroidResource(getSXRContext(), styleItem.getTextureHover())));

        getRenderData().getMaterial().setFloat(TransparentButtonShaderThreeStates.TEXTURE_SWITCH,
                0.0f);
    }

    protected void setTextureFromResource(String shaderKey, int resource) {

        getRenderData().getMaterial().setTexture(shaderKey,
                getSXRContext().getAssetLoader().loadTexture(new SXRAndroidResource(getSXRContext(), resource)));
    }

    public void setNomalTexture(String character, String ShaderKey) {

        SXRBitmapImage bitmapNormal = new SXRBitmapImage(getSXRContext(),
                SXRTextBitmapFactory.create(
                        styleItem.getCharacterBackgroundWidth(),
                        styleItem.getCharacterBackgroundHeight(), character, styleItem
                                .getFontSize(), Paint.Align.CENTER,
                        styleItem.getColorText(), Color.argb(0, 0, 0, 0), getSXRContext()
                                .getContext().getApplicationContext()));

        SXRTexture texture = new SXRTexture(getSXRContext());
        texture.setImage(bitmapNormal);
        getRenderData().getMaterial().setTexture(ShaderKey, texture);
    }

    public void setHoverTexture(String character, String ShaderKey) {

        SXRBitmapImage bitmapHover = new SXRBitmapImage(getSXRContext(),
                SXRTextBitmapFactory.create(
                        styleItem.getCharacterBackgroundWidth(),
                        styleItem.getCharacterBackgroundHeight(), character,
                        styleItem.getFontSize(), Paint.Align.CENTER,
                        styleItem.getHoverTextColor(), styleItem.getColorBackgroundTextHover(),
                        getSXRContext().getContext().getApplicationContext()));

        SXRTexture texture = new SXRTexture(getSXRContext());
        texture.setImage(bitmapHover);
        getRenderData().getMaterial().setTexture(ShaderKey, texture);
    }

    public KeyboardCharItem getKeyboardCharItem() {
        return keyboardCharItem;
    }
}
