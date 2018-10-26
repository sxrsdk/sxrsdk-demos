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

package pw.ian.vrtransit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRScene;
import com.samsungxr.SXRNode;
import com.samsungxr.SXRScript;
import com.samsungxr.SXRTransform;
import com.samsungxr.animation.SXRAnimation;
import com.samsungxr.animation.SXRAnimationEngine;
import com.samsungxr.animation.SXRRepeatMode;
import com.samsungxr.animation.SXRRotationByAxisWithPivotAnimation;
import com.samsungxr.utility.Log;

public class SolarViewManager extends SXRScript {

    @SuppressWarnings("unused")
    private static final String TAG = Log.tag(SolarViewManager.class);

    private SXRAnimationEngine mAnimationEngine;

    private SXRNode asyncNode(SXRContext context,
            SXRAndroidResource meshResource, String textureName)
            throws IOException {
        return new SXRNode(context, meshResource,
                new SXRAndroidResource(context, textureName));
    }

    @Override
    public void onInit(SXRContext sxrContext) throws IOException {
        mAnimationEngine = sxrContext.getAnimationEngine();

        SXRScene mainScene = sxrContext.getNextMainScene(new Runnable() {

            @Override
            public void run() {
                for (SXRAnimation animation : mAnimations) {
                    animation.start(mAnimationEngine);
                }
                mAnimations = null;
            }
        });

        mainScene.setFrustumCulling(true);
        
        mainScene.getMainCameraRig().getLeftCamera()
                .setBackgroundColor(0.0f, 0.0f, 0.0f, 1.0f);
        mainScene.getMainCameraRig().getRightCamera()
                .setBackgroundColor(0.0f, 0.0f, 0.0f, 1.0f);

        mainScene.getMainCameraRig().getTransform()
                .setPosition(0.0f, 0.0f, 0.0f);

        SXRNode solarSystemObject = new SXRNode(sxrContext);
        mainScene.addNode(solarSystemObject);

        SXRNode sunRotationObject = new SXRNode(sxrContext);
        solarSystemObject.addChildObject(sunRotationObject);

        SXRAndroidResource meshResource = new SXRAndroidResource(sxrContext,
                "sphere.obj");

        SXRNode sunMeshObject = asyncNode(sxrContext,
                meshResource, "sunmap.astc");
        sunMeshObject.getTransform().setPosition(0.0f, 0.0f, 0.0f);
        sunMeshObject.getTransform().setScale(10.0f, 10.0f, 10.0f);
        sunRotationObject.addChildObject(sunMeshObject);

        SXRNode mercuryRevolutionObject = new SXRNode(sxrContext);
        mercuryRevolutionObject.getTransform().setPosition(14.0f, 0.0f, 0.0f);
        solarSystemObject.addChildObject(mercuryRevolutionObject);

        SXRNode mercuryRotationObject = new SXRNode(sxrContext);
        mercuryRevolutionObject.addChildObject(mercuryRotationObject);

        SXRNode mercuryMeshObject = asyncNode(sxrContext,
                meshResource, "mercurymap.jpg");
        mercuryMeshObject.getTransform().setScale(0.3f, 0.3f, 0.3f);
        mercuryRotationObject.addChildObject(mercuryMeshObject);

        SXRNode venusRevolutionObject = new SXRNode(sxrContext);
        venusRevolutionObject.getTransform().setPosition(17.0f, 0.0f, 0.0f);
        solarSystemObject.addChildObject(venusRevolutionObject);

        SXRNode venusRotationObject = new SXRNode(sxrContext);
        venusRevolutionObject.addChildObject(venusRotationObject);

        SXRNode venusMeshObject = asyncNode(sxrContext,
                meshResource, "venusmap.jpg");
        venusMeshObject.getTransform().setScale(0.8f, 0.8f, 0.8f);
        venusRotationObject.addChildObject(venusMeshObject);

        SXRNode earthRevolutionObject = new SXRNode(sxrContext);
        earthRevolutionObject.getTransform().setPosition(22.0f, 0.0f, 0.0f);
        solarSystemObject.addChildObject(earthRevolutionObject);

        SXRNode earthRotationObject = new SXRNode(sxrContext);
        earthRevolutionObject.addChildObject(earthRotationObject);

        SXRNode earthMeshObject = asyncNode(sxrContext,
                meshResource, "earthmap1k.jpg");
        earthMeshObject.getTransform().setScale(1.0f, 1.0f, 1.0f);
        earthRotationObject.addChildObject(earthMeshObject);

        SXRNode moonRevolutionObject = new SXRNode(sxrContext);
        moonRevolutionObject.getTransform().setPosition(4.0f, 0.0f, 0.0f);
        earthRevolutionObject.addChildObject(moonRevolutionObject);
        moonRevolutionObject.addChildObject(mainScene.getMainCameraRig());

        SXRNode marsRevolutionObject = new SXRNode(sxrContext);
        marsRevolutionObject.getTransform().setPosition(30.0f, 0.0f, 0.0f);
        solarSystemObject.addChildObject(marsRevolutionObject);

        SXRNode marsRotationObject = new SXRNode(sxrContext);
        marsRevolutionObject.addChildObject(marsRotationObject);

        SXRNode marsMeshObject = asyncNode(sxrContext,
                meshResource, "mars_1k_color.jpg");
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

        clockwise(mainScene.getMainCameraRig().getTransform(),
                60f);

        counterClockwise(marsRevolutionObject, 1200f);
        counterClockwise(marsRotationObject, 200f);
    }

    @Override
    public void onStep() {
    }

    private List<SXRAnimation> mAnimations = new ArrayList<SXRAnimation>();

    private void setup(SXRAnimation animation) {
        animation.setRepeatMode(SXRRepeatMode.REPEATED).setRepeatCount(-1);
        mAnimations.add(animation);
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
