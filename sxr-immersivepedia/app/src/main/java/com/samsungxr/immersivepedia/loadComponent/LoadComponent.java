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

package com.samsungxr.immersivepedia.loadComponent;

import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRDrawFrameListener;
import com.samsungxr.SXRMaterial;
import com.samsungxr.SXRSceneObject;
import com.samsungxr.SXRShaderId;
import com.samsungxr.SXRTexture;
import com.samsungxr.immersivepedia.R;
import com.samsungxr.immersivepedia.focus.FocusListener;
import com.samsungxr.immersivepedia.focus.FocusableSceneObject;
import com.samsungxr.immersivepedia.shader.CutoutShader;
import com.samsungxr.immersivepedia.util.RenderingOrderApplication;

public class LoadComponent extends SXRSceneObject implements FocusListener {

    private static final int CUTOUT_VALUE = 1;
    private static final float LOADING_SPEED = 0.01f;
    private SXRTexture circleAlphaTexture;
    private SXRTexture circleTexture;
    private SXRTexture plusTexture;

    private SXRSceneObject circleAlpha;
    private SXRSceneObject plus;
    private FocusableSceneObject circle;
    private SXRContext sxrContext;

    private float valueFloatTexture;

    private SXRDrawFrameListener drawFrameListener;
    private LoadComponentListener componentListener;
    private boolean isLoading = false;

    public LoadComponent(SXRContext sxrContext, LoadComponentListener componentListener) {
        super(sxrContext);
        this.componentListener = componentListener;
        this.sxrContext = sxrContext;
        this.sxrContext.runOnGlThread(new Runnable() {

            @Override
            public void run() {

                loadTexture();
                createLoadComponent();
            }
        });
    }

    private void createLoadComponent() {
        circleAlpha = new SXRSceneObject(sxrContext, sxrContext.createQuad(.5f, .5f),
                circleAlphaTexture);
        plus = new SXRSceneObject(sxrContext, sxrContext.createQuad(.5f, .5f), plusTexture);
        circle = new FocusableSceneObject(sxrContext, sxrContext.createQuad(.5f, .5f),
                circleTexture);

        plus.getRenderData().getMaterial().setMainTexture(plusTexture);
        plus.getRenderData().setRenderingOrder(RenderingOrderApplication.LOADING_COMPONENT);

        circle.getRenderData().getMaterial().setMainTexture(circleTexture);
        circle.getRenderData().setRenderingOrder(RenderingOrderApplication.LOADING_COMPONENT);
        circle.focusListener = this;

        circleAlpha.getRenderData().setMaterial(new SXRMaterial(sxrContext, new SXRShaderId(CutoutShader.class)));
        circleAlpha.getRenderData().getMaterial()
                .setTexture(CutoutShader.TEXTURE_KEY, circleAlphaTexture);
        circleAlpha.getRenderData().getMaterial()
                .setFloat(CutoutShader.CUTOUT, valueFloatTexture);
        circleAlpha.getRenderData().setRenderingOrder(RenderingOrderApplication.LOADING_COMPONENT);
        circleAlpha.getRenderData().getMaterial().setMainTexture(circleAlphaTexture);
        circle.setName("circle");
        addChildObject(circleAlpha);
        addChildObject(plus);
        addChildObject(circle);
    }

    private void loadTexture() {
        circleAlphaTexture = sxrContext.getAssetLoader().loadTexture(new SXRAndroidResource(sxrContext,
                R.drawable.loading_two__colors));
        circleTexture = sxrContext.getAssetLoader().loadTexture(new SXRAndroidResource(sxrContext,
                R.drawable.loading));
        plusTexture = sxrContext.getAssetLoader().loadTexture(new SXRAndroidResource(sxrContext, R.drawable.plus));
    }

    public void setFloatTexture() {

        drawFrameListener = new SXRDrawFrameListener() {

            @Override
            public void onDrawFrame(float frameTime) {
                isLoading = true;
                valueFloatTexture += LOADING_SPEED;

                if (valueFloatTexture <= CUTOUT_VALUE) {
                    circleAlpha.getRenderData().getMaterial()
                            .setFloat(CutoutShader.CUTOUT, valueFloatTexture);

                } else {
                    startSoundLoadComponent();
                }

            }
        };

        sxrContext.registerDrawFrameListener(drawFrameListener);

    }

    private void startSoundLoadComponent() {
        finishLoadComponent();
    }

    public void finishLoadComponent() {
        sxrContext.unregisterDrawFrameListener(drawFrameListener);
        isLoading = false;
        sxrContext.getMainScene().removeSceneObject(this);

        componentListener.onFinishLoadComponent();
    }

    public void disableListener() {
        sxrContext.unregisterDrawFrameListener(drawFrameListener);
    }
    @Override
    public void gainedFocus(FocusableSceneObject object) {
    }

    @Override
    public void lostFocus(FocusableSceneObject object) {
        finishLoadComponent();
    }

    @Override
    public void inFocus(FocusableSceneObject object) {
    }

    public boolean isLoading() {
        return isLoading;
    }

}
