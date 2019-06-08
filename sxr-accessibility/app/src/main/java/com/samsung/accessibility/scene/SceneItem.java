/*
 * Copyright 2015 Samsung Electronics Co., LTD
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package com.samsung.accessibility.scene;

import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRMesh;
import com.samsungxr.SXRNode;
import com.samsungxr.SXRTexture;
import com.samsungxr.animation.SXRAnimation;
import com.samsungxr.animation.SXROnFinish;
import com.samsungxr.animation.SXRPositionAnimation;
import com.samsungxr.animation.SXRRotationByAxisAnimation;

import com.samsung.accessibility.R;
import com.samsung.accessibility.focus.FocusableNode;
import com.samsung.accessibility.focus.OnFocusListener;
import com.samsung.accessibility.interpolator.InterpolatorBackEaseIn;
import com.samsung.accessibility.interpolator.InterpolatorBackEaseOut;
import com.samsung.accessibility.interpolator.InterpolatorStrongEaseInOut;
import com.samsung.accessibility.util.Utils;

public class SceneItem extends FocusableNode {
    protected boolean isActive = false;
    private boolean isAnimating = false;
    private static final float duration = 0.35f;

    public SceneItem(SXRContext sxrContext, SXRMesh mesh, SXRTexture texture) {
        super(sxrContext, mesh, texture);

        final SXRNode onFocusNode = new SXRNode(sxrContext, sxrContext.getAssetLoader().loadMesh(new SXRAndroidResource(sxrContext,
                R.raw.edge_box_normal)), sxrContext.getAssetLoader().loadTexture(new SXRAndroidResource(sxrContext, R.drawable.edge_box)));
        onFocusNode.getTransform().setPositionZ(-.1f);
        onFocusNode.getRenderData().setRenderingOrder(getRenderData().getRenderingOrder() + 1);
        onFocusNode.getRenderData().setDepthTest(false);

        setOnFocusListener(new OnFocusListener() {

            @Override
            public void lostFocus(FocusableNode object) {
                removeChildObject(onFocusNode);
            }

            @Override
            public void inFocus(FocusableNode object) {
            }

            @Override
            public void gainedFocus(FocusableNode object) {
                addChildObject(onFocusNode);
            }
        });

    }

    public void animate() {
        float distance = (float) Utils.distance(this, getSXRContext().getMainScene().getMainCameraRig());
        final float[] initialPosition = new float[3];
        initialPosition[0] = getTransform().getPositionX();
        initialPosition[1] = getTransform().getPositionY();
        initialPosition[2] = getTransform().getPositionZ();
        final float[] newPosition = Utils.calculatePointBetweenTwoObjects(this, getSXRContext().getMainScene().getMainCameraRig(),
                distance + 2);

        if (!isAnimating) {
            isAnimating = true;
            if (isActive) {
                new SXRPositionAnimation(this, duration, newPosition[0] - initialPosition[0], newPosition[1] - initialPosition[1],
                        newPosition[2] - initialPosition[2]).start(getSXRContext().getAnimationEngine())
                        .setInterpolator(InterpolatorBackEaseOut.getInstance()).setOnFinish(new SXROnFinish() {

                            @Override
                            public void finished(SXRAnimation animation) {
                                new SXRRotationByAxisAnimation(SceneItem.this, duration * 3, 180, 0, 1, 0)
                                        .start(getSXRContext().getAnimationEngine())
                                        .setInterpolator(InterpolatorStrongEaseInOut.getInstance()).setOnFinish(new SXROnFinish() {

                                            @Override
                                            public void finished(SXRAnimation animation) {
                                                new SXRPositionAnimation(SceneItem.this, duration, initialPosition[0]
                                                        - newPosition[0],
                                                        initialPosition[1] - newPosition[1], initialPosition[2] - newPosition[2])
                                                        .start(getSXRContext().getAnimationEngine())
                                                        .setInterpolator(InterpolatorBackEaseIn.getInstance())
                                                        .setOnFinish(new SXROnFinish() {

                                                            @Override
                                                            public void finished(SXRAnimation animation) {
                                                                isAnimating = false;

                                                            }
                                                        });
                                            }
                                        });
                            }
                        });
            } else {
                new SXRPositionAnimation(this, duration, newPosition[0] - initialPosition[0], newPosition[1] - initialPosition[1],
                        newPosition[2] - initialPosition[2]).start(getSXRContext().getAnimationEngine())
                        .setInterpolator(InterpolatorBackEaseOut.getInstance()).setOnFinish(new SXROnFinish() {

                            @Override
                            public void finished(SXRAnimation animation) {
                                new SXRRotationByAxisAnimation(SceneItem.this, duration * 3, -180, 0, 1, 0)
                                        .start(getSXRContext().getAnimationEngine())
                                        .setInterpolator(InterpolatorStrongEaseInOut.getInstance()).setOnFinish(new SXROnFinish() {

                                            @Override
                                            public void finished(SXRAnimation animation) {
                                                new SXRPositionAnimation(SceneItem.this, duration, initialPosition[0]
                                                        - newPosition[0],
                                                        initialPosition[1] - newPosition[1], initialPosition[2] - newPosition[2])
                                                        .start(getSXRContext().getAnimationEngine())
                                                        .setInterpolator(InterpolatorBackEaseIn.getInstance())
                                                        .setOnFinish(new SXROnFinish() {

                                                            @Override
                                                            public void finished(SXRAnimation animation) {
                                                                isAnimating = false;

                                                            }
                                                        });
                                            }
                                        });
                            }
                        });
            }
            isActive = !isActive;
        }
    }

    public boolean isActive() {
        return isActive;
    }

}
