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

package com.samsungxr.keyboard.model;

import android.content.res.Resources;
import android.content.res.TypedArray;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRCameraRig;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRMaterial;
import com.samsungxr.SXRRenderData;
import com.samsungxr.SXRNode;
import com.samsungxr.SXRShaderId;
import com.samsungxr.SXRTexture;
import com.samsungxr.animation.SXRAnimation;
import com.samsungxr.animation.SXROnFinish;
import com.samsungxr.animation.SXRPositionAnimation;
import com.samsungxr.animation.SXRRepeatMode;
import com.samsungxr.animation.SXRScaleAnimation;
import com.samsungxr.keyboard.R;
import com.samsungxr.keyboard.interpolator.FloatEffectInterpolator;
import com.samsungxr.keyboard.interpolator.InterpolatorExpoEaseInOut;
import com.samsungxr.keyboard.interpolator.InterpolatorExpoEaseOut;
import com.samsungxr.keyboard.shader.SXRShaderAnimation;
import com.samsungxr.keyboard.shader.SphereShader;
import com.samsungxr.keyboard.util.Constants;
import com.samsungxr.keyboard.util.NodeNames;
import com.samsungxr.keyboard.util.Util;
import com.samsungxr.utility.Log;

public class SphereFlag extends SXRNode {

    private final float CURSOR_POSITION_OFFSET_Y = 2f;
    private String mCountryName;
    private int mTexture;
    private int mResultTexture;
    private String mQuestion;
    private String mAnswer;
    private Vector3D positionVector;
    public boolean isSpottingSphere = false;
    public boolean isUnsnappingSphere = false;
    public boolean isUnspottingSphere = false;
    public boolean isFloatingSphere = false;
    public int answerState = SphereStaticList.MOVEABLE;
    public SXRAnimation snapAnimation;
    private SXRAnimation spotAnimation;
    private SXRAnimation scaleParentAnimation;
    private SXRAnimation scaleThisAnimation;
    private SXRAnimation floatingAnimation;
    private SXRAnimation followCursorAnimation;

    private boolean moveTogetherDashboard = false;
    private SXRContext sxrContext;

    public SphereFlag(SXRContext sxrContext, TypedArray sphere) {
        super(sxrContext);
        setName(NodeNames.SPHERE_FLAG);

        this.sxrContext = sxrContext;

        initSphere(sphere);

        SXRMaterial material = getMaterial();
        
        SXRRenderData renderData = getRenderData(material);
        //renderData.setShaderTemplate(SphereShader.class);
        attachRenderData(renderData);
        
        updateMaterial();
    }

    private void initSphere(TypedArray sphere) {
        Resources res = sxrContext.getContext().getResources();

        mCountryName = res.getString(sphere.getResourceId(0, -1));
        mTexture = sphere.getResourceId(1, -1);
        mQuestion = res.getString(sphere.getResourceId(2, -1));
        mAnswer = res.getString(sphere.getResourceId(3, -1));
        mResultTexture = R.drawable.check;

        float posX = Util.applyRatioAt(sphere.getFloat(4, -1));
        float posY = Util.applyRatioAt(sphere.getFloat(5, -1));
        float posZ = Util.applyRatioAt(sphere.getFloat(6, -1));
        positionVector = new Vector3D(posX, posY, posZ);
    }

    public void updateMaterial() {

        float[] mat = this.getTransform().getModelMatrix();

        float[] light = new float[4];
        light[0] = 0;
        light[1] = 6;
        light[2] = 6;
        light[3] = 1.0f;

        float lX = mat[0] * light[0] + mat[1] * light[1] + mat[2] * light[2] + mat[3] * light[3];
        float lY = mat[4] * light[0] + mat[5] * light[1] + mat[6] * light[2] + mat[7] * light[3];
        float lZ = mat[8] * light[0] + mat[9] * light[1] + mat[10] * light[2] + mat[11] * light[3];

        float x = 0;
        float y = 0;
        float z = 0;

       
        this.getRenderData().getMaterial().setVec3(SphereShader.LIGHT_KEY,
                lX - this.getTransform().getPositionX(),
                lY - this.getTransform().getPositionY(),
                lZ - this.getTransform().getPositionZ());
        this.getRenderData().getMaterial().setVec3(SphereShader.EYE_KEY, x, y, z);

    }

    private SXRMaterial getMaterial() {
        SXRMaterial material = new SXRMaterial(sxrContext, new SXRShaderId(SphereShader.class));
        material.setTexture(SphereShader.TEXTURE_KEY,
                sxrContext.getAssetLoader().loadTexture(new SXRAndroidResource(sxrContext, mTexture)));
        material.setFloat("blur", 0);
        material.setFloat(SphereShader.ANIM_TEXTURE, 0.0f);
        material.setTexture(SphereShader.SECUNDARY_TEXTURE_KEY,
                sxrContext.getAssetLoader().loadTexture(new SXRAndroidResource(sxrContext, mResultTexture)));
        material.setVec3(SphereShader.TRANSITION_COLOR, 1, 1, 1);
        material.setVec3(SphereShader.EYE_KEY, 0, 0, 0);

        // Light config
        SXRTexture hdriTexture = sxrContext.getAssetLoader().loadTexture(new SXRAndroidResource(sxrContext,
                R.drawable.hdri_reflex));
        material.setTexture(SphereShader.HDRI_TEXTURE_KEY, hdriTexture);

        return material;
    }

    private SXRRenderData getRenderData(SXRMaterial material) {
        SXRRenderData renderData = new SXRRenderData(sxrContext);
        renderData.setMesh(sxrContext.getAssetLoader().loadMesh(new SXRAndroidResource(sxrContext,
                R.raw.sphere_uv_flag)));
        renderData.setMaterial(material);
        renderData.setRenderingOrder(100);
        renderData.setAlphaBlend(true);
        return renderData;
    }

    public void animateFloating() {
        if (!isFloatingSphere) {
            isFloatingSphere = true;
            float intensity = 1;
            float randomValue = 0.7f + ((float) Math.random() * 1.0f);

            floatingAnimation = new SXRPositionAnimation
                    (getParent(), intensity * 3 * randomValue, 0, getParent().getTransform()
                            .getPositionY() - intensity * 2 * randomValue, 0);

            floatingAnimation.setInterpolator(new FloatEffectInterpolator());
            floatingAnimation.setRepeatMode(SXRRepeatMode.PINGPONG);
            floatingAnimation.setRepeatCount(-1);
            floatingAnimation.start(sxrContext.getAnimationEngine());
        }
    }

    public void stopFloatingSphere() {
        sxrContext.getAnimationEngine().stop(floatingAnimation);
        isFloatingSphere = false;
    }

    public void spotSphere() {
        if (!isSpottingSphere) {
            isSpottingSphere = true;

            stopAnimationsToSpot();

            spotAnimation = createSpotAnimation();
            spotAnimation.start(sxrContext.getAnimationEngine()).setOnFinish(new SXROnFinish() {
                @Override
                public void finished(SXRAnimation arg0) {
                    isSpottingSphere = false;
                }
            });
        }
    }

    private void stopAnimationsToSpot() {
        if (spotAnimation != null) {
            sxrContext.getAnimationEngine().stop(spotAnimation);
            sxrContext.getAnimationEngine().stop(scaleParentAnimation);
            sxrContext.getAnimationEngine().stop(scaleThisAnimation);
            isUnspottingSphere = false;
        }
    }

    private SXRAnimation createSpotAnimation() {
        SXRCameraRig cameraObject = sxrContext.getMainScene().getMainCameraRig();
        float distance = (float) Math.max(
                0.7 * Util.distance(getInitialPositionVector(), cameraObject.getTransform()),
                Constants.MINIMUM_DISTANCE_FROM_CAMERA);
        float[] newPosition = Util.calculatePointBetweenTwoObjects(cameraObject.getTransform(),
                getParent(),
                distance);
        float scaleFactor = Util.getHitAreaScaleFactor(distance);

        scaleParentAnimation = new SXRScaleAnimation(getParent(), 1.2f, scaleFactor)
                .start(sxrContext
                        .getAnimationEngine());
        scaleThisAnimation = new SXRScaleAnimation(this, 1.2f, 1 / scaleFactor).start(sxrContext
                .getAnimationEngine());

        return new SXRPositionAnimation(getParent(), 1.2f, newPosition[0]
                - getParent().getTransform().getPositionX(),
                newPosition[1] - getParent().getTransform().getPositionY(),
                newPosition[2] - getParent().getTransform().getPositionZ())
                .setInterpolator(new InterpolatorExpoEaseOut());
    }

    public void unspotSphere() {
        if (!isUnspottingSphere) {
            isUnspottingSphere = true;
            SXRCameraRig cameraObject = sxrContext.getMainScene().getMainCameraRig();
            float scaleFactor = Util.getHitAreaScaleFactor((float) Util.distance(
                    getInitialPositionVector(), cameraObject.getTransform()));

            stopAnimationsToUnspot();

            spotAnimation = createUnspotAnimation(scaleFactor);
            spotAnimation.start(sxrContext.getAnimationEngine()).setOnFinish(new SXROnFinish() {
                @Override
                public void finished(SXRAnimation arg0) {
                    isUnspottingSphere = false;
                }
            });
        }
    }

    private SXRAnimation createUnspotAnimation(float scaleFactor) {
        scaleParentAnimation = new SXRScaleAnimation(getParent(), 1.2f, scaleFactor)
                .start(sxrContext
                        .getAnimationEngine());
        scaleThisAnimation = new SXRScaleAnimation(this, 1.2f, 1 / scaleFactor).start(sxrContext
                .getAnimationEngine());

        return new SXRPositionAnimation(getParent(), 1.2f, (float) getInitialPositionVector()
                .getX() - getParent().getTransform().getPositionX(),
                (float) getInitialPositionVector().getY()
                        - getParent().getTransform().getPositionY(),
                (float) getInitialPositionVector().getZ()
                        - getParent().getTransform().getPositionZ())
                .setInterpolator(new InterpolatorExpoEaseOut());
    }

    private void stopAnimationsToUnspot() {
        if (spotAnimation != null) {
            sxrContext.getAnimationEngine().stop(spotAnimation);
            sxrContext.getAnimationEngine().stop(scaleParentAnimation);
            sxrContext.getAnimationEngine().stop(scaleThisAnimation);
            isSpottingSphere = false;
        }
    }

    public void snapSphere(float[] hit) {
        if (isUnsnappingSphere) {
            sxrContext.getAnimationEngine().stop(snapAnimation);
            isUnsnappingSphere = false;
        }

        if(hit != null)
        snapAnimation = new SXRPositionAnimation(this, 1.2f, hit[0]
                - getTransform().getPositionX(),
                hit[1] - getTransform().getPositionY(), 0f).start(sxrContext.getAnimationEngine());

    }

    public void unsnapSphere(float duration) {
        if (!isUnsnappingSphere) {
            if (snapAnimation != null) {
                sxrContext.getAnimationEngine().stop(snapAnimation);
            }
            isUnsnappingSphere = true;
            snapAnimation = new SXRPositionAnimation(this, duration, -getTransform()
                    .getPositionX(),
                    -getTransform().getPositionY(), 0f).start(sxrContext.getAnimationEngine())
                    .setInterpolator(new InterpolatorExpoEaseInOut())
                    .setOnFinish(new SXROnFinish() {
                        @Override
                        public void finished(SXRAnimation arg0) {
                            isUnsnappingSphere = false;
                            if (answerState == SphereStaticList.MOVEABLE) {
                                animateFloating();
                            }
                        }
                    });
        }
    }

    public void giveAnswer(String answer) {
        moveTogetherDashboard = false;
        checkAnswer(answer);
        changeTexture();
        resetSphereAfterTime(3f);
    }

    private void checkAnswer(final String answer) {
        getSXRContext().runOnGlThread(new Runnable() {
            @Override
            public void run() {
                if (getAnswer().equalsIgnoreCase(answer)) {

                    AudioClip.getInstance(getSXRContext().getContext()).playSound(
                            AudioClip.getSucessSoundID(), 1.0f, 1.0f);
                    getRenderData().getMaterial().setVec3(SphereShader.TRANSITION_COLOR, 0.2f,
                            0.675f, 0.443f);
                    getRenderData().getMaterial().setTexture(
                            SphereShader.SECUNDARY_TEXTURE_KEY,
                            getSXRContext().getAssetLoader().loadTexture(
                                    new SXRAndroidResource(getSXRContext(), R.drawable.check)));
                } else {

                    AudioClip.getInstance(getSXRContext().getContext()).playSound(
                            AudioClip.getWrongSoundID(), 1.0f, 1.0f);
                    getRenderData().getMaterial().setVec3(SphereShader.TRANSITION_COLOR, 1, 0, 0);
                    getRenderData().getMaterial().setTexture(
                            SphereShader.SECUNDARY_TEXTURE_KEY,
                            getSXRContext().getAssetLoader().loadTexture(
                                    new SXRAndroidResource(getSXRContext(), R.drawable.error)));
                }
            }
        });
    }

    public void moveToCursor() {
        if (followCursorAnimation != null) {
            getSXRContext().getAnimationEngine().stop(followCursorAnimation);
        }
        SXRCameraRig cameraObject = getSXRContext().getMainScene().getMainCameraRig();

        float desiredDistance = (float) Math.max(
                0.7 * Util.distance(getParent(), cameraObject.getTransform()),
                Constants.MINIMUM_DISTANCE_FROM_CAMERA);
        float[] lookAt = getSXRContext().getMainScene().getMainCameraRig().getLookAt();
        Vector3D lookAtVector = new Vector3D(lookAt[0], lookAt[1], lookAt[2]);

        final float desiredX = (float) lookAtVector.getX() * desiredDistance;
        final float desiredY = (float) lookAtVector.getY() * desiredDistance
                + CURSOR_POSITION_OFFSET_Y;
        final float desiredZ = (float) lookAtVector.getZ() * desiredDistance;

        float x = desiredX - getParent().getTransform().getPositionX();
        float y = desiredY - getParent().getTransform().getPositionY();
        float z = desiredZ - getParent().getTransform().getPositionZ();

        followCursorAnimation = new SXRPositionAnimation(getParent(), 0.8f, x, y, z)
                .setInterpolator(new InterpolatorExpoEaseOut()).start(
                        getSXRContext().getAnimationEngine());
    }

    private void changeTexture() {
        new SXRShaderAnimation(this, SphereShader.ANIM_TEXTURE, 0.6f, 1).setInterpolator(
                new InterpolatorExpoEaseOut()).start(getSXRContext().getAnimationEngine());
    }

    private void resetSphereAfterTime(float delay) {
        getSXRContext().getPeriodicEngine().runAfter(new Runnable() {
            @Override
            public void run() {
                answerState = SphereStaticList.RESTORING;
                restoreSpherePosition(1.2f);
                restoreTexture();
            }
        }, delay);
    }

    public void restoreSpherePosition(float duration) {
        if (followCursorAnimation != null) {
            getSXRContext().getAnimationEngine().stop(followCursorAnimation);
        }

        float x = (float) getInitialPositionVector().getX()
                - getParent().getTransform().getPositionX();
        float y = (float) getInitialPositionVector().getY()
                - getParent().getTransform().getPositionY();
        float z = (float) getInitialPositionVector().getZ()
                - getParent().getTransform().getPositionZ();

        followCursorAnimation = new SXRPositionAnimation(getParent(), duration, x, y, z)
                .setInterpolator(new InterpolatorExpoEaseInOut())
                .start(getSXRContext().getAnimationEngine()).setOnFinish(new SXROnFinish() {
                    @Override
                    public void finished(SXRAnimation arg0) {
                        answerState = SphereStaticList.MOVEABLE;
                        animateFloating();
                    }
                });
    }

    public void restoreTexture() {
        new SXRShaderAnimation(this, SphereShader.ANIM_TEXTURE, 0.8f, 0).setInterpolator(
                new InterpolatorExpoEaseInOut()).start(getSXRContext().getAnimationEngine());
    }

    public void tapSphere() {
        final SphereFlag sphereFlag = this;

        if (spotAnimation != null) {
            getSXRContext().getAnimationEngine().stop(spotAnimation);
            getSXRContext().getAnimationEngine().stop(scaleParentAnimation);
            getSXRContext().getAnimationEngine().stop(scaleThisAnimation);
        }
        getSXRContext().getPeriodicEngine().runAfter(new Runnable() {

            @Override
            public void run() {
                float duration = 0.71f;
                unsnapSphere(duration);
                SXRCameraRig cameraObject = getSXRContext().getMainScene().getMainCameraRig()
                ;
                float distance = Constants.SPHERE_SELECTION_DISTANCE;
                float[] newPosition = Util.calculatePointBetweenTwoObjects(
                        cameraObject.getTransform(),
                        getInitialPositionVector(), distance);
                float scaleFactor = Util.getHitAreaScaleFactor(distance);

                scaleParentAnimation = new SXRScaleAnimation(getParent(), duration, scaleFactor).start(getSXRContext()
                        .getAnimationEngine());
                scaleThisAnimation = new SXRScaleAnimation(sphereFlag, duration, 1 / scaleFactor).start(getSXRContext()
                        .getAnimationEngine());

                new SXRPositionAnimation(getParent(), duration, newPosition[0]
                        - getParent().getTransform().getPositionX(),
                        newPosition[1] - getParent().getTransform().getPositionY(),
                        newPosition[2] - getParent().getTransform().getPositionZ())
                        .setInterpolator(new InterpolatorExpoEaseInOut())
                        .start(getSXRContext().getAnimationEngine()).setOnFinish(new SXROnFinish() {

                            @Override
                            public void finished(SXRAnimation arg0) {
                                SphereFlag.this.moveTogetherDashboard = true;
                            }
                        });
            }
        }, 0.5f);
    }

    public void unselectSphere() {
        answerState = SphereStaticList.RESTORING;
        stopFloatingSphere();
        final SphereFlag sphereFlag = this;
        float duration = 1.5f;
        new SXRShaderAnimation(sphereFlag, SphereShader.BLUR_INTENSITY, duration, 1)
                .start(getSXRContext().getAnimationEngine());

        SXRCameraRig cameraObject = getSXRContext().getMainScene().getMainCameraRig();
        float distance = (float) (Constants.NEAREST_NON_SELECTED_SPHERE - Constants.NEAREST_SPHERE + Util
                .distance(getInitialPositionVector(),
                        cameraObject.getTransform()));
        float[] newPosition = Util.calculatePointBetweenTwoObjects(cameraObject.getTransform(),
                getParent(),
                distance);
        float scaleFactor = Util.getHitAreaScaleFactor(distance);

        scaleParentAnimation = new SXRScaleAnimation(getParent(), duration, scaleFactor)
                .start(getSXRContext()
                        .getAnimationEngine());
        scaleThisAnimation = new SXRScaleAnimation(sphereFlag, duration, 1 / scaleFactor)
                .start(getSXRContext()
                        .getAnimationEngine());

        new SXRPositionAnimation(getParent(), duration, newPosition[0]
                - getParent().getTransform().getPositionX(),
                newPosition[1] - getParent().getTransform().getPositionY(),
                newPosition[2] - getParent().getTransform().getPositionZ())
                .setInterpolator(new InterpolatorExpoEaseInOut()).start(
                        getSXRContext().getAnimationEngine());
    }

    public String getQuestion() {
        return mQuestion;
    }

    public String getAnswer() {
        return mAnswer;
    }

    public Vector3D getInitialPositionVector() {
        return positionVector;
    }

    public boolean canMoveTogetherDashboard() {
        return moveTogetherDashboard;
    }

}
