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

package com.samsungxr.performance;

import android.util.Log;

import com.samsungxr.SXRActivity;
import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRCamera;
import com.samsungxr.SXRCameraRig;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRImportSettings;
import com.samsungxr.SXRScene;
import com.samsungxr.SXRSceneObject;
import com.samsungxr.SXRTexture;
import com.samsungxr.ZipLoader;
import com.samsungxr.SXRMain;

import com.samsungxr.animation.SXRAnimationEngine;
import com.samsungxr.animation.SXRRepeatMode;
import com.samsungxr.animation.SXRRotationByAxisWithPivotAnimation;

import java.io.IOException;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Future;

import static com.samsungxr.SXRImportSettings.NO_LIGHTING;

public class TestMain extends SXRMain {
    private static final String TAG = "TestMain";
    private static final int numberOfBunnies = 20;
    private static final String TEXTURE_FILE_NAME = "textures.zip";

    private SXRScene mMainScene = null;

    SXRAnimationEngine mAnimationEngine;

    private SXRActivity mActivity;

    TestMain(SXRActivity activity) {
        mActivity = activity;
    }

    @Override
    public void onInit(SXRContext sxrContext) {

        mAnimationEngine = sxrContext.getAnimationEngine();

        mMainScene = sxrContext.getMainScene();

        SXRCameraRig mainCameraRig = mMainScene.getMainCameraRig();

        SXRCamera leftCamera = mainCameraRig.getLeftCamera();
        SXRCamera rightCamera = mainCameraRig.getRightCamera();

        leftCamera.setBackgroundColorR(0.2f);
        leftCamera.setBackgroundColorG(0.2f);
        leftCamera.setBackgroundColorB(0.2f);
        leftCamera.setBackgroundColorA(1.0f);

        rightCamera.setBackgroundColorR(0.2f);
        rightCamera.setBackgroundColorG(0.2f);
        rightCamera.setBackgroundColorB(0.2f);
        rightCamera.setBackgroundColorA(1.0f);
        mainCameraRig.getTransform().setPosition(0.0f, 0.0f, 0.0f);

        try {
            List<SXRTexture> textures;
            textures = ZipLoader.load(sxrContext, TEXTURE_FILE_NAME, new ZipLoader
                    .ZipEntryProcessor<SXRTexture>() {

                @Override
                public SXRTexture getItem(SXRContext context, SXRAndroidResource resource) {
                    return context.getAssetLoader().loadTexture(resource);
                }
            });

            int numTextures = textures.size();
            for (int i = 0; i < numberOfBunnies; ++i) {

                SXRSceneObject bunny;
                EnumSet<SXRImportSettings> settings = SXRImportSettings.getRecommendedSettingsWith(EnumSet.of(NO_LIGHTING));
                // we assume that the mesh and the textures are valid
                bunny = new SXRSceneObject(sxrContext,
                        sxrContext.getAssetLoader().loadMesh(new SXRAndroidResource(sxrContext,
                                "bunny.obj"), settings), textures.get(i % numTextures));

                Random random = new Random();

                bunny.getTransform().setPosition(0.0f, 0.0f,
                        random.nextFloat() * 3.0f + 2.0f);
                bunny.getTransform().rotateByAxisWithPivot(
                        random.nextFloat() * 360.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                        0.0f);
                bunny.getTransform().rotateByAxisWithPivot(
                        random.nextFloat() * 360.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f,
                        0.0f);
                bunny.getTransform().rotateByAxisWithPivot(
                        random.nextFloat() * 360.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f,
                        0.0f);
                bunny.getTransform().translate(0.0f, 0.0f, -10.0f);
                mainCameraRig.addChildObject(bunny);

                float x = random.nextFloat() - 0.5f;
                float y = random.nextFloat() - 0.5f;
                float z = random.nextFloat() - 0.5f;
                float length = (float) Math.sqrt(x * x + y * y + z * z);
                x /= length;
                y /= length;
                z /= length;

                new SXRRotationByAxisWithPivotAnimation(bunny, //
                        5.0f + random.nextFloat() * 25.0f, //
                        360.0f, //
                        x, y, z, //
                        0.0f, 0.0f, -10.0f) //
                        .setRepeatMode(SXRRepeatMode.REPEATED).setRepeatCount(-1) //
                        .start(mAnimationEngine);
            }
        } catch (IOException e) {
            e.printStackTrace();
            mActivity.finish();
            Log.e(TAG,
                    "Mesh or texture were not loaded. Stopping application!");
        }
    }

    @Override
    public void onStep() {
    }

}
