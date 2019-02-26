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

package com.samsungxr.solarsystem;

import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRCamera;
import com.samsungxr.SXRCameraRig;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRMain;
import com.samsungxr.SXRPerspectiveCamera;
import com.samsungxr.SXRRenderData;
import com.samsungxr.SXRScene;
import com.samsungxr.SXRNode;
import com.samsungxr.SXRTransform;
import com.samsungxr.animation.SXRAnimation;
import com.samsungxr.animation.SXRAnimationEngine;
import com.samsungxr.animation.SXRAnimator;
import com.samsungxr.animation.SXRRepeatMode;
import com.samsungxr.animation.SXRRotationByAxisWithPivotAnimation;
import com.samsungxr.utility.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SolarMain extends SXRMain {

    @SuppressWarnings("unused")
    private static final String TAG = Log.tag(SolarMain.class);

    private SXRAnimator mAnimator;
    private SXRScene mMainScene;

    private SXRNode asyncNode(SXRContext context,
            String textureName) throws IOException {
        return new SXRNode(context, //
                new SXRAndroidResource(context, "sphere.obj"), //
                new SXRAndroidResource(context, textureName));
    }

    @Override
    public void onInit(final SXRContext sxrContext) throws IOException {
        final SXRCameraRig newRig = SXRCameraRig.makeInstance(sxrContext);
        final SXRCamera leftCamera = new SXRPerspectiveCamera(sxrContext);
        leftCamera.setRenderMask(SXRRenderData.SXRRenderMaskBit.Left);
        final SXRCamera rightCamera = new SXRPerspectiveCamera(sxrContext);
        rightCamera.setRenderMask(SXRRenderData.SXRRenderMaskBit.Right);
        final SXRPerspectiveCamera centerCamera = new SXRPerspectiveCamera(sxrContext);
        centerCamera.setRenderMask(SXRRenderData.SXRRenderMaskBit.Left | SXRRenderData.SXRRenderMaskBit.Right);
        newRig.attachLeftCamera(leftCamera);
        newRig.attachRightCamera(rightCamera);
        newRig.attachCenterCamera(centerCamera);
        mAnimator = new SXRAnimator(sxrContext);
        mMainScene = sxrContext.getMainScene();

        SXRNode solarSystemObject = new SXRNode(sxrContext);
        solarSystemObject.attachComponent(mAnimator);
        mMainScene.addNode(solarSystemObject);

        SXRNode sunRotationObject = new SXRNode(sxrContext);
        solarSystemObject.addChildObject(sunRotationObject);

        SXRNode sunMeshObject = asyncNode(sxrContext, "sunmap.astc");
        sunMeshObject.getTransform().setScale(10.0f, 10.0f, 10.0f);
        sunRotationObject.addChildObject(sunMeshObject);

        SXRNode mercuryRevolutionObject = new SXRNode(sxrContext);
        mercuryRevolutionObject.getTransform().setPosition(14.0f, 0.0f, 0.0f);
        solarSystemObject.addChildObject(mercuryRevolutionObject);

        SXRNode mercuryRotationObject = new SXRNode(sxrContext);
        mercuryRevolutionObject.addChildObject(mercuryRotationObject);

        SXRNode mercuryMeshObject = asyncNode(sxrContext, "mercurymap.jpg");
        mercuryMeshObject.getTransform().setScale(0.3f, 0.3f, 0.3f);
        mercuryRotationObject.addChildObject(mercuryMeshObject);

        SXRNode venusRevolutionObject = new SXRNode(sxrContext);
        venusRevolutionObject.getTransform().setPosition(17.0f, 0.0f, 0.0f);
        solarSystemObject.addChildObject(venusRevolutionObject);

        SXRNode venusRotationObject = new SXRNode(sxrContext);
        venusRevolutionObject.addChildObject(venusRotationObject);

        SXRNode venusMeshObject = asyncNode(sxrContext, "venusmap.jpg");
        venusMeshObject.getTransform().setScale(0.8f, 0.8f, 0.8f);
        venusRotationObject.addChildObject(venusMeshObject);

        SXRNode earthRevolutionObject = new SXRNode(sxrContext);
        earthRevolutionObject.getTransform().setPosition(22.0f, 0.0f, 0.0f);
        solarSystemObject.addChildObject(earthRevolutionObject);

        SXRNode earthRotationObject = new SXRNode(sxrContext);
        earthRevolutionObject.addChildObject(earthRotationObject);

        SXRNode moonRevolutionObject = new SXRNode(sxrContext);
        moonRevolutionObject.getTransform().setPosition(4.0f, 0.0f, 0.0f);
        earthRevolutionObject.addChildObject(moonRevolutionObject);
        moonRevolutionObject.addChildObject(newRig.getOwnerObject());

        SXRNode earthMeshObject = asyncNode(sxrContext, "earthmap1k.jpg");
        earthMeshObject.getTransform().setScale(1.0f, 1.0f, 1.0f);
        earthRotationObject.addChildObject(earthMeshObject);

        SXRNode marsRevolutionObject = new SXRNode(sxrContext);
        marsRevolutionObject.getTransform().setPosition(30.0f, 0.0f, 0.0f);
        solarSystemObject.addChildObject(marsRevolutionObject);

        SXRNode marsRotationObject = new SXRNode(sxrContext);
        marsRevolutionObject.addChildObject(marsRotationObject);

        SXRNode marsMeshObject = asyncNode(sxrContext, "mars_1k_color.jpg");
        marsMeshObject.getTransform().setScale(0.6f, 0.6f, 0.6f);
        marsRotationObject.addChildObject(marsMeshObject);

        counterClockwise(sunRotationObject, 50f);

        counterClockwise(mercuryRevolutionObject, 150f);
        counterClockwise(mercuryRotationObject, 100f);

        counterClockwise(venusRevolutionObject, 400f);
        clockwise(venusRotationObject, 400f);

        counterClockwise(earthRevolutionObject, 600f);
        counterClockwise(earthRotationObject, 1.5f);

        counterClockwise(moonRevolutionObject, 60f);

        clockwise(newRig.getTransform(), 60f);

        counterClockwise(marsRevolutionObject, 1200f);
        counterClockwise(marsRotationObject, 200f);
        mAnimator.start(getSXRContext().getAnimationEngine());
        mMainScene.setMainCameraRig(newRig);
    }

    @Override
    public void onStep() {
    }

    void onTap() {
        if (null != mMainScene) {
            // toggle whether stats are displayed.
            boolean statsEnabled = mMainScene.getStatsEnabled();
            mMainScene.setStatsEnabled(!statsEnabled);
        }
    }

    private void setup(SXRAnimation animation) {
        animation.setRepeatMode(SXRRepeatMode.REPEATED).setRepeatCount(-1);
        mAnimator.addAnimation(animation);
    }

    private void counterClockwise(SXRNode object, float duration) {
        setup(new SXRRotationByAxisWithPivotAnimation( //
                object, duration, 360.0f, //
                0.0f, 1.0f, 0.0f, //
                0.0f, 0.0f, 0.0f));
    }

    private void clockwise(SXRNode object, float duration) {
        setup(new SXRRotationByAxisWithPivotAnimation( //
                object, duration, -360.0f, //
                0.0f, 1.0f, 0.0f, //
                0.0f, 0.0f, 0.0f));
    }

    private void clockwise(SXRTransform transform, float duration) {
        setup(new SXRRotationByAxisWithPivotAnimation( //
                transform, duration, -360.0f, //
                0.0f, 1.0f, 0.0f, //
                0.0f, 0.0f, 0.0f));
    }
}
