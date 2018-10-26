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

package com.samsungxr.immersivepedia.scene;

import android.graphics.Color;
import android.view.Gravity;

import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRMaterial;
import com.samsungxr.SXRMesh;
import com.samsungxr.SXRMeshCollider;
import com.samsungxr.SXRRenderData;
import com.samsungxr.SXRSceneObject;
import com.samsungxr.SXRShaderId;
import com.samsungxr.SXRTexture;
import com.samsungxr.immersivepedia.R;
import com.samsungxr.immersivepedia.focus.FocusListener;
import com.samsungxr.immersivepedia.focus.FocusableSceneObject;
import com.samsungxr.immersivepedia.shader.MenuImageShader;
import com.samsungxr.immersivepedia.util.AudioClip;
import com.samsungxr.immersivepedia.util.RenderingOrderApplication;
import com.samsungxr.scene_objects.SXRTextViewSceneObject;

public class MenuItem extends FocusableSceneObject {


    private static final float difGroupTextY = -0f;
    private static final float difTextToBackground = -0.03f;

    private final float SUB_TEXT_Y = -1.08f + (difTextToBackground+difGroupTextY);
    private final float MAIN_TEXT_Y = -0.86f + (difTextToBackground+difGroupTextY);
    private final float FRONT_IMAGE_Z_OFFSET = 2.2f;
    private final float FRONT_IMAGE_Y = -.18f;
    private final float FRONT_IMAGE_X = +0.0f;
    private final float TEXT_BACKGROUND_Z = FRONT_IMAGE_Z_OFFSET + 0.1f;
    private final float TEXT_BACKGROUND_Y = -1f+(difGroupTextY) ;
    private final float TEXT_HEIGHT = 0.41f;
    private final float TEXT_WIDTH = 1.31f;

    private final float WIDTH = 4.6f;
    private final float HEIGHT = 2.8f;
    private final float CHILD_HEIGHT = 6f;
    private final float CHILD_WIDTH = 8f;
    private final int IDLE_STATE = 0;
    private final int HOVER_STATE = 1;

    private final float SCALE_BIGER_OFFSET = .05f;

    private SXRSceneObject frontObj;
    private SXRSceneObject backgroundObj;
    private SXRTextViewSceneObject mainText;
    private SXRSceneObject textBackground;
    private SXRTextViewSceneObject subText;
    float scale ;

    public MenuItem(SXRContext sxrContext, int frontIdleRes, int frontHoverRes, int backgroundIdleRes, int backgroundHoverRes) {
        super(sxrContext);

        attachRenderData(new SXRRenderData(sxrContext));
        getRenderData().setMaterial(new SXRMaterial(sxrContext));
        getRenderData().setMesh(sxrContext.createQuad(WIDTH, HEIGHT));
        SXRTexture texture = sxrContext.getAssetLoader().loadTexture(new SXRAndroidResource(sxrContext, R.drawable.empty_clickable));
        getRenderData().getMaterial().setMainTexture(texture);

        frontObj = createSceneObject(frontIdleRes, frontHoverRes);
        frontObj.getTransform().setPositionZ(FRONT_IMAGE_Z_OFFSET);
        frontObj.getTransform().setPositionY(FRONT_IMAGE_Y);
        frontObj.getTransform().setPositionX(FRONT_IMAGE_X);
        frontObj.getRenderData().setRenderingOrder(RenderingOrderApplication.MAIN_IMAGE);
        scale = (MenuScene.DISTANCE_TO_CAMERA - FRONT_IMAGE_Z_OFFSET) / MenuScene.DISTANCE_TO_CAMERA;
        frontObj.getTransform().setScale(scale, scale, scale);
        backgroundObj = createSceneObject(backgroundIdleRes, backgroundHoverRes);
        backgroundObj.getRenderData().setRenderingOrder(RenderingOrderApplication.BACKGROUND_IMAGE);

        SXRMeshCollider collider = new SXRMeshCollider(sxrContext, false);

        attachComponent(collider);
        createFocusListener();
    }

    private void createFocusListener() {
        focusListener = new FocusListener() {

            @Override
            public void lostFocus(FocusableSceneObject object) {
                frontObj.getRenderData().getMaterial().setFloat(MenuImageShader.TEXTURE_SWITCH, IDLE_STATE);
                backgroundObj.getRenderData().getMaterial().setFloat(MenuImageShader.TEXTURE_SWITCH, IDLE_STATE);
                hideText();
                scaleSmaller();
            }

            private void scaleBiger() {
                frontObj.getTransform().setScale(scale+SCALE_BIGER_OFFSET, scale+SCALE_BIGER_OFFSET, scale+SCALE_BIGER_OFFSET);
            }

            @Override
            public void inFocus(FocusableSceneObject object) {
            }

            @Override
            public void gainedFocus(FocusableSceneObject object) {
                AudioClip.getInstance(getSXRContext().getContext()).playSound(AudioClip.getUIMenuHoverSoundID(), 1.0f, 1.0f);
                frontObj.getRenderData().getMaterial().setFloat(MenuImageShader.TEXTURE_SWITCH, HOVER_STATE);
                backgroundObj.getRenderData().getMaterial().setFloat(MenuImageShader.TEXTURE_SWITCH, HOVER_STATE);
                showText();
                scaleBiger();
            }

            private void scaleSmaller() {
                frontObj.getTransform().setScale(scale, scale, scale);
            }
        };
    }

    protected void hideText() {
        textBackground.getRenderData().getMaterial().setOpacity(0);
        mainText.getRenderData().getMaterial().setOpacity(0);
        subText.getRenderData().getMaterial().setOpacity(0);

    }

    protected void showText() {
        textBackground.getRenderData().getMaterial().setOpacity(1);
        mainText.getRenderData().getMaterial().setOpacity(1);
        subText.getRenderData().getMaterial().setOpacity(1);
    }

    public SXRSceneObject createSceneObject(int idleImageRes, int hoverImageRes) {
        SXRSceneObject obj = new SXRSceneObject(getSXRContext());

        SXRTexture idle = getSXRContext().getAssetLoader().loadTexture(new SXRAndroidResource(getSXRContext(), idleImageRes));
        SXRTexture hover = getSXRContext().getAssetLoader().loadTexture(new SXRAndroidResource(getSXRContext(), hoverImageRes));

        obj.attachRenderData(new SXRRenderData(getSXRContext()));
        obj.getRenderData().setMaterial(new SXRMaterial(getSXRContext(), new SXRShaderId(MenuImageShader.class)));
        obj.getRenderData().setMesh(getSXRContext().createQuad(CHILD_WIDTH, CHILD_HEIGHT));
        obj.getRenderData().getMaterial().setTexture(MenuImageShader.STATE1_TEXTURE, idle);
        obj.getRenderData().getMaterial().setTexture(MenuImageShader.STATE2_TEXTURE, hover);
        obj.getRenderData().getMaterial().setFloat(MenuImageShader.TEXTURE_SWITCH, IDLE_STATE);

        addChildObject(obj);

        return obj;
    }

    public void setTexts(String text, String subText) {
        createTextBackground();
        createMainText(text);
        createSubText(subText);
    }

    private void createTextBackground() {
        SXRMesh mesh = getSXRContext().createQuad(TEXT_WIDTH, TEXT_HEIGHT);
        textBackground = new SXRSceneObject(getSXRContext(), mesh, getSXRContext().getAssetLoader().loadTexture(
                new SXRAndroidResource(getSXRContext(), R.drawable.text_bg)));

        textBackground.getTransform().setPosition(0, TEXT_BACKGROUND_Y, TEXT_BACKGROUND_Z);
        textBackground.getRenderData().setRenderingOrder(RenderingOrderApplication.IMAGE_TEXT_BACKGROUND);
        textBackground.getRenderData().getMaterial().setOpacity(0);

        addChildObject(textBackground);
    }

    private void createMainText(String text) {
        mainText = new SXRTextViewSceneObject(getSXRContext(), TEXT_WIDTH, TEXT_HEIGHT / 2, text);

        mainText.setTextColor(Color.WHITE);
        mainText.setTextSize(5);
        mainText.setGravity(Gravity.CENTER);

        mainText.getTransform().setPosition(0, MAIN_TEXT_Y, TEXT_BACKGROUND_Z + 0.01f);
        mainText.getRenderData().setRenderingOrder(RenderingOrderApplication.IMAGE_TEXT);
        mainText.getRenderData().getMaterial().setOpacity(0);

        addChildObject(mainText);
    }

    public void createSubText(String text) {
        subText = new SXRTextViewSceneObject(getSXRContext(), TEXT_WIDTH, TEXT_HEIGHT / 2, text);

        subText.setTextColor(Color.WHITE);
        subText.setTextSize(3);
        subText.setGravity(Gravity.CENTER);

        subText.getTransform().setPosition(0, SUB_TEXT_Y, TEXT_BACKGROUND_Z + 0.01f);
        subText.getRenderData().setRenderingOrder(RenderingOrderApplication.IMAGE_TEXT);
        subText.getRenderData().getMaterial().setOpacity(0);

        addChildObject(subText);
    }

}