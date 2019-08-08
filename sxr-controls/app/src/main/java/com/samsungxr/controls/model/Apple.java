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

package com.samsungxr.controls.model;

import android.content.res.Resources;
import android.content.res.TypedArray;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRMaterial;
import com.samsungxr.SXRMesh;
import com.samsungxr.SXRRenderData;
import com.samsungxr.SXRNode;
import com.samsungxr.SXRShaderId;
import com.samsungxr.SXRTexture;
import com.samsungxr.animation.SXRAnimation;
import com.samsungxr.animation.SXRInterpolator;
import com.samsungxr.animation.SXROpacityAnimation;
import com.samsungxr.animation.SXRPositionAnimation;
import com.samsungxr.animation.SXRScaleAnimation;
import com.samsungxr.controls.Main;
import com.samsungxr.controls.R;
import com.samsungxr.controls.WormShadow;
import com.samsungxr.controls.anim.AnimationsTime;
import com.samsungxr.controls.interpolators.Bounce;
import com.samsungxr.controls.interpolators.CircularIn;
import com.samsungxr.controls.interpolators.CircularOut;
import com.samsungxr.controls.interpolators.ExpoIn;
import com.samsungxr.controls.interpolators.ExpoOut;
import com.samsungxr.controls.interpolators.QuadIn;
import com.samsungxr.controls.interpolators.QuadOut;
import com.samsungxr.controls.shaders.ColorSwapShader;
import com.samsungxr.controls.util.Constants;
import com.samsungxr.controls.util.RenderingOrder;
import com.samsungxr.controls.util.Util;

import java.util.ArrayList;

public class Apple extends SXRNode {

    // public final float ANIMATION_DURATION =
    // AnimationsTime.getDropTime();//2.5f;
    public final float OPACITY_ANIMATION_DURATION = 2;
    public final float Y_ANIMATION_DELTA = -5;
    private final float APPLE_SCALE = 0.75f;
    private final static float MAX_APPLES_DISTANCE = 1.5f;
    private final static float CAMERA_DIRECTION_THREASHOLD = 0.75f;
    public static ArrayList<Apple> appleList = new ArrayList<Apple>();
    public Star star;
    private WormShadow shadow;

    public static int currentMotion = 0;

    public enum Motion {
        Linear, Bouncing, CircularIn, CircularOut, ExpoIn, ExpoOut, QuadIn, QuadOut
    };

    public static Motion motion = Motion.Linear;

    public Apple(SXRContext sxrContext) {
        super(sxrContext);
        this.getTransform().setScale(APPLE_SCALE, APPLE_SCALE, APPLE_SCALE);
        setAppleRenderData(sxrContext);
        setAppleShaderParameters(sxrContext);
        star = new Star(sxrContext);
        shadow = new WormShadow(sxrContext, 0.27f, 0.27f, RenderingOrder.APPLE_SHADOW);
        shadow.getTransform().setScale(2, 2, 2);
        sxrContext.getMainScene().addNode(star);
    }

    public void setAppleRenderData(SXRContext sxrContext) {
        SXRMesh mesh = sxrContext.getAssetLoader().loadMesh(new SXRAndroidResource(sxrContext,
                R.raw.apple));

        SXRMaterial material = new SXRMaterial(sxrContext, new SXRShaderId(ColorSwapShader.class));
        SXRRenderData renderData = new SXRRenderData(sxrContext);
        renderData.setMesh(mesh);
        renderData.setMaterial(material);
        this.attachRenderData(renderData);

        getRenderData().setRenderingOrder(RenderingOrder.APPLE);
    }

    public void setAppleShaderParameters(SXRContext sxrContext) {
        SXRTexture grayScaleTexture = sxrContext.getAssetLoader().loadTexture(new SXRAndroidResource(sxrContext,
                R.drawable.apple_diffuse));
        SXRTexture detailsTexture = sxrContext.getAssetLoader().loadTexture(new SXRAndroidResource(sxrContext,
                R.drawable.apple_details));

        this.getRenderData().getMaterial().setOpacity(0);
        this.getRenderData().getMaterial()
                .setTexture(ColorSwapShader.TEXTURE_GRAYSCALE, grayScaleTexture);
        this.getRenderData().getMaterial()
                .setTexture(ColorSwapShader.TEXTURE_DETAILS, detailsTexture);
        updateAppleColor();
    }

    public static void addApple(Apple apple) {
        appleList.add(apple);
    }

    public static float[] getColor(SXRContext sxrContext) {

        Resources res = sxrContext.getContext().getResources();
        TypedArray colorArray = res.obtainTypedArray(R.array.colors);
        TypedArray colorTypeValues;
        float[] appleColor = new float[3];
        colorTypeValues = res.obtainTypedArray(colorArray.getResourceId(Apple.currentMotion, 0));

        appleColor[0] = colorTypeValues.getFloat(0, 0);
        appleColor[1] = colorTypeValues.getFloat(1, 0);
        appleColor[2] = colorTypeValues.getFloat(2, 0);

        return Util.normalizeColor(appleColor);

    }

    public static SXRInterpolator defineInterpolator(Motion motion) {

        SXRInterpolator interpolator = null;
        switch (motion) {

            case Bouncing:
                interpolator = new Bounce();

                break;
            case CircularIn:
                interpolator = new CircularIn();

                break;
            case CircularOut:
                interpolator = new CircularOut();

                break;
            case ExpoIn:
                interpolator = new ExpoIn();

                break;
            case ExpoOut:
                interpolator = new ExpoOut();

                break;
            case QuadIn:
                interpolator = new QuadIn();

                break;
            case QuadOut:
                interpolator = new QuadOut();

                break;
            default:
                interpolator = null;

                break;
        }
        currentMotion = motion.ordinal();
        return interpolator;
    }

    public void updateAppleColor() {

        float[] color = getColor(this.getSXRContext());
        this.getRenderData().getMaterial()
                .setVec4(ColorSwapShader.COLOR, color[0], color[1], color[2], 1);

    }

    public void playAnimation(SXRContext sxrContext) {

        SXRAnimation anim = new SXRPositionAnimation(this, AnimationsTime.getDropTime(), 0,
                -Constants.APPLE_INICIAL_YPOS - 1, 0);
        anim.setInterpolator(defineInterpolator(motion));
        anim.start(sxrContext.getAnimationEngine());
        playShadowAnimation();
        playOpacityAnimation(sxrContext);
    }

    private void playShadowAnimation() {
        new SXRScaleAnimation(shadow, AnimationsTime.getDropTime(), 2f).setInterpolator(defineInterpolator(motion)).start(
                getSXRContext().getAnimationEngine());
    }

    public void playOpacityAnimation(SXRContext sxrContext) {

        SXRAnimation anim = new SXROpacityAnimation(this, OPACITY_ANIMATION_DURATION, 1);
        anim.start(sxrContext.getAnimationEngine());

    }

    public void resetPosition(SXRContext sxrContext) {

        updateAppleColor();
        star.playMoveAnimation(sxrContext, this);
        setApplePositionInsideFrustum(sxrContext);
        this.getTransform().setPositionY(Constants.APPLE_INICIAL_YPOS);
        playAnimation(sxrContext);

    }

    public boolean checkValidPosition(Vector3D pos) {

        Vector3D wormPos = new
                Vector3D(Main.worm.wormParent.getTransform().getPositionX(),
                Main.worm.wormParent
                        .getTransform().getPositionY(), Main.worm.wormParent
                .getTransform()
                .getPositionZ());
        if (Vector3D.distance(pos, wormPos) < MAX_APPLES_DISTANCE)
            return false;
        for (Apple a : appleList) {

            if (a == this)
                continue;
            Vector3D iteratedApple = new
                    Vector3D(a.getTransform().getPositionX(), 0, a.getTransform()
                    .getPositionZ());
            float distance = (float) Vector3D.distance(pos, iteratedApple);

            if (distance < MAX_APPLES_DISTANCE) {

                return false;
            }

        }
        return true;
    }

    public void setAppleRandomPosition(
            SXRContext context) {

        float angle = (float) Math.random() * 360;
        float distance = (float) (Math.random()
                * (Constants.MAX_APPLE_DISTANCE - (Constants.MAX_APPLE_DISTANCE - Constants.MIN_APPLE_DISTANCE))
                + Constants.MIN_APPLE_DISTANCE);
        this.getTransform().setPositionZ(distance);
        this.getTransform().rotateByAxisWithPivot(angle, 0, 1, 0, 0, 0, 0);

        Vector3D instanceApple = new Vector3D(this.getTransform().getPositionX(), this
                .getTransform().getPositionY(), this.getTransform().getPositionZ());
        if (!checkValidPosition(instanceApple)) {
            setAppleRandomPosition(context);

        }
        else {
            if (!appleList.contains(this)) {
                addApple(this);
                shadow.getTransform().setPosition((float) instanceApple.getX(), -0.9999f, (float) instanceApple.getZ());
                getSXRContext().getMainScene().addNode(shadow);
            }
        }
    }

    public void setApplePositionInsideFrustum(
            SXRContext context) {
        float angle = (float) Math.random() * 80 - 40;
        float distance = (float) (Math.random()
                * (Constants.MAX_WORM_MOVE_DISTANCE - (Constants.MAX_WORM_MOVE_DISTANCE - Constants.MIN_WORM_MOVE_DISTANCE))
                + Constants.MIN_WORM_MOVE_DISTANCE);

        Vector3D instanceApple = setNewApplePosition(context, angle, distance);
        if (instanceApple == null)
            return;
        if (!checkValidPosition(instanceApple)) {

            setApplePositionInsideFrustum(context);

        }

    }

    private Vector3D setNewApplePosition(SXRContext context, float angle, float distance) {
        float[] vecDistance = context.getMainScene().getMainCameraRig().getLookAt();

        if (vecDistance[1] > CAMERA_DIRECTION_THREASHOLD
                || vecDistance[1] < -CAMERA_DIRECTION_THREASHOLD) {
            setAppleRandomPosition(context);
            return null;
        }

        vecDistance[0] *= distance;
        vecDistance[2] *= distance;
        Vector3D instanceApple = new Vector3D(vecDistance[0], 0, vecDistance[2]);
        instanceApple.normalize();
        this.getTransform().setPositionX((float) instanceApple.getX());
        this.getTransform().setPositionZ((float) instanceApple.getZ());
        this.getTransform().rotateByAxisWithPivot(angle, 0, 1, 0, 0, 0, 0);
        instanceApple = new Vector3D(this.getTransform().getPositionX(), this
                .getTransform().getPositionY(), this.getTransform().getPositionZ());
        shadow.getTransform().setPosition((float) instanceApple.getX(), -0.9999f, (float) instanceApple.getZ());
        shadow.getTransform().setScale(1, 1, 1);
        return instanceApple;
    }
}
