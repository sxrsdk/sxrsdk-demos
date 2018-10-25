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

import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRMaterial;
import com.samsungxr.SXRMesh;
import com.samsungxr.SXRMeshCollider;
import com.samsungxr.SXRRenderData;
import com.samsungxr.SXRShaderId;
import com.samsungxr.SXRTexture;
import com.samsungxr.animation.SXROpacityAnimation;
import com.samsungxr.animation.SXRRepeatMode;
import com.samsungxr.controls.R;
import com.samsungxr.controls.focus.ControlSceneObject;
import com.samsungxr.controls.shaders.ButtonShader;
import com.samsungxr.controls.util.RenderingOrder;

public class MenuCloseButton extends ControlSceneObject {

    private final int IDLE_STATE = 0;
    private final int HOVER_STATE = 1;
    private final int SELECTED_STATE = 2;

    private SXROpacityAnimation opacityShow;
    private SXROpacityAnimation opacityHide;

    public MenuCloseButton(SXRContext gvrContext) {
        super(gvrContext);

        SXRMesh sMesh = getSXRContext().createQuad(0.4f, 0.4f);

        attachRenderData(new SXRRenderData(gvrContext));
        getRenderData().setMaterial(new SXRMaterial(gvrContext, new SXRShaderId(ButtonShader.class)));
        getRenderData().setMesh(sMesh);
        createTextures(gvrContext);

        getRenderData().getMaterial().setFloat(ButtonShader.TEXTURE_SWITCH, IDLE_STATE);
        getRenderData().setRenderingOrder(RenderingOrder.MENU_FRAME_TEXT + 1);

        attachComponent(new SXRMeshCollider(gvrContext, false));
    }

    private void createTextures(SXRContext gvrContext) {

        SXRTexture empty = gvrContext.getAssetLoader().loadTexture(new SXRAndroidResource(gvrContext, R.raw.empty));
        SXRTexture idle = gvrContext.getAssetLoader().loadTexture(new SXRAndroidResource(gvrContext, R.drawable.bt_close));
        SXRTexture hover = gvrContext.getAssetLoader().loadTexture(new SXRAndroidResource(gvrContext, R.drawable.bt_close_hover));
        SXRTexture selected = gvrContext.getAssetLoader().loadTexture(new SXRAndroidResource(gvrContext, R.drawable.bt_close_pressed));

        getRenderData().getMaterial().setTexture(ButtonShader.STATE1_BACKGROUND_TEXTURE, empty);
        getRenderData().getMaterial().setTexture(ButtonShader.STATE1_TEXT_TEXTURE, idle);

        getRenderData().getMaterial().setTexture(ButtonShader.STATE2_BACKGROUND_TEXTURE, empty);
        getRenderData().getMaterial().setTexture(ButtonShader.STATE2_TEXT_TEXTURE, hover);

        getRenderData().getMaterial().setTexture(ButtonShader.STATE3_BACKGROUND_TEXTURE, empty);
        getRenderData().getMaterial().setTexture(ButtonShader.STATE3_TEXT_TEXTURE, selected);
    }

    @Override
    protected void gainedFocus() {
        getRenderData().getMaterial().setFloat(ButtonShader.TEXTURE_SWITCH, HOVER_STATE);
    }

    @Override
    protected void lostFocus() {
        getRenderData().getMaterial().setFloat(ButtonShader.TEXTURE_SWITCH, IDLE_STATE);
    }

    @Override
    protected void singleTap() {
        super.singleTap();

        getRenderData().getMaterial().setFloat(ButtonShader.TEXTURE_SWITCH, SELECTED_STATE);

    }

    public void unselect() {
        getRenderData().getMaterial().setFloat(ButtonShader.TEXTURE_SWITCH, IDLE_STATE);
    }

    private void stop(){

        if(opacityShow != null){
            getSXRContext().getAnimationEngine().stop(opacityShow);
        }

        if(opacityHide != null){
            getSXRContext().getAnimationEngine().stop(opacityHide);
        }
    }

    public void show(){

        stop();

        opacityShow = new SXROpacityAnimation(this, 1f, 1);
        opacityShow.setRepeatMode(SXRRepeatMode.ONCE);
        opacityShow.start(getSXRContext().getAnimationEngine());
    }

    public void hide(){

        stop();

        opacityHide = new SXROpacityAnimation(this, 0.3f, 0);
        opacityHide.setRepeatMode(SXRRepeatMode.ONCE);
        opacityHide.start(getSXRContext().getAnimationEngine());
    }
}