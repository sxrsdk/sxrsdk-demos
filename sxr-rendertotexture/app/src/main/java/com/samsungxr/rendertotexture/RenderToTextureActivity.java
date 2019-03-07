/* Copyright 2017 Samsung Electronics Co., LTD
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
package com.samsungxr.rendertotexture;

import android.os.Bundle;

import com.samsungxr.SXRActivity;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRMain;
import com.samsungxr.SXRPerspectiveCamera;
import com.samsungxr.SXRShader;
import com.samsungxr.SXRRenderTarget;
import com.samsungxr.SXRRenderTexture;
import com.samsungxr.SXRScene;
import com.samsungxr.SXRNode;
import com.samsungxr.SXRSpotLight;
import com.samsungxr.SXRTransform;
import com.samsungxr.animation.SXRAnimation;
import com.samsungxr.animation.SXRAnimator;
import com.samsungxr.animation.SXRRepeatMode;
import com.samsungxr.nodes.SXRCubeNode;
import com.samsungxr.utility.Log;

import java.io.IOException;
import java.util.List;

public final class RenderToTextureActivity extends SXRActivity {
    @Override
    protected void onCreate(final Bundle bundle) {
        super.onCreate(bundle);
        setMain(new RenderToTextureMain());
    }

    final class RenderToTextureMain extends SXRMain {
        @Override
        public void onInit(final SXRContext sxrContext) {
            if (!SXRShader.isVulkanInstance()) {
                addLight();
            }
            final SXRNode cube = addCube();
            final SXRScene scene = createRenderToTextureScene();

            sxrContext.runOnGlThread(new Runnable()
            {
                @Override
                public void run()
                {
                    mRenderTexture = new SXRRenderTexture(sxrContext, 512, 512);
                    SXRRenderTarget renderTarget = new SXRRenderTarget(mRenderTexture, scene);

                    scene.getMainCameraRig().getOwnerObject().attachComponent(renderTarget);
                    //to prevent rendering untextured cube for few frames at the start
                    cube.getRenderData().getMaterial().setMainTexture(mRenderTexture);
                    closeSplashScreen();
                    renderTarget.setEnable(true);
                }
            });
        }

        private SXRScene createRenderToTextureScene() {
            final SXRScene newScene = new SXRScene(getSXRContext());

            final SXRPerspectiveCamera centerCamera = newScene.getMainCameraRig().getCenterCamera();
            centerCamera.setBackgroundColor(0.7f, 0.4f, 0, 1);

            try {
                final SXRNode model = getSXRContext().getAssetLoader().loadModel("astro_boy.dae", newScene);

                model.getTransform()
                        .setRotationByAxis(45.0f, 0.0f, 1.0f, 0.0f)
                        .setScale(2f, 2f, 2f)
                        .setPosition(0.0f, -0.15f, -0.3f);

                SXRAnimator animations = (SXRAnimator) model.getComponent(SXRAnimator.getComponentType());
                if (animations != null) {
                    animations.setRepeatMode(SXRRepeatMode.REPEATED);
                    animations.setRepeatCount(-1);
                    animations.start(getSXRContext().getAnimationEngine());
                }
            } catch (final IOException e) {
                Log.e(TAG, "Failed to load a model: %s", e);
                getSXRContext().getActivity().finish();
            }

            return newScene;
        }

        private SXRNode addCube() {
            final SXRNode cube = new SXRCubeNode(getSXRContext(), true);
            mCubeTransform = cube.getTransform();
            mCubeTransform.setPosition(0, 0, -4f).setScale(2, 2, 2);

            getSXRContext().getMainScene().addNode(cube);
            return cube;
        }

        private void addLight() {
            final SXRContext context = getSXRContext();

            final SXRSpotLight light = new SXRSpotLight(context);
            light.setAmbientIntensity(0.8f, 0.8f, 0.8f, 1);
            light.setDiffuseIntensity(0.8f, 0.8f, 0.8f, 1);
            light.setSpecularIntensity(0.8f, 0.8f, 0.8f, 1);
            light.setInnerConeAngle(8);
            light.setOuterConeAngle(24);

            final SXRNode lightNode = new SXRNode(context);
            lightNode.attachLight(light);
            lightNode.getTransform().setPosition(0, 1, 3);

            context.getMainScene().addNode(lightNode);
        }

        @Override
        public void onStep() {
            mCubeTransform.rotateByAxis(0.25f, 0, 0, 1);
            mCubeTransform.rotateByAxis(0.5f, 1, 0, 0);
            mCubeTransform.rotateByAxis(0.25f, 0, 1, 0);
        }

        @Override
        public SplashMode getSplashMode() {
            return SplashMode.MANUAL;
        }

        private volatile SXRRenderTexture mRenderTexture;
        private SXRTransform mCubeTransform;
        private static final String TAG = "RenderToTextureMain";
    }

}
