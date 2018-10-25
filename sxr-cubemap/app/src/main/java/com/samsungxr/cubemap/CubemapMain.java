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

package com.samsungxr.cubemap;

import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRAssetLoader;
import com.samsungxr.SXRCameraRig;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRMain;
import com.samsungxr.SXRMaterial;
import com.samsungxr.SXRMesh;
import com.samsungxr.SXRScene;
import com.samsungxr.SXRSceneObject;
import com.samsungxr.SXRTexture;
import com.samsungxr.scene_objects.SXRCubeSceneObject;
import com.samsungxr.scene_objects.SXRCylinderSceneObject;
import com.samsungxr.scene_objects.SXRSphereSceneObject;

import java.io.File;
import java.util.ArrayList;

public final class CubemapMain extends SXRMain {

    private static final float CUBE_WIDTH = 20.0f;
    private static final float SCALE_FACTOR = 2.0f;
    private static final int MAX_ENVIRONMENTS = 6;
    private static final String TAG = "CubemapMain";

    private SXRContext mSXRContext = null;

    // Type of object for the environment
    // 0: surrounding sphere using SXRSphereSceneObject
    // 1: surrounding cube using SXRCubeSceneObject and 1 SXRCubemapImage
    //    (method A)
    // 2: surrounding cube using SXRCubeSceneObject and compressed ETC2 textures
    //    (method B, best performance)
    // 3: surrounding cube using SXRCubeSceneObject and 6 SXRTexture's
    //    (method C)
    // 4: surrounding cylinder using SXRCylinderSceneObject
    // 5: surrounding cube using six SXRSceneOjbects (quads)
    private int mEnvironmentType = 2;

    // Type of object for the reflective object
    // 0: reflective sphere using SXRSphereSceneObject
    // 1: reflective sphere using OBJ model
    private static final int mReflectiveType = 0;
    private SXRTexture mCubemapTexture;
    private SXRMaterial mCubemapMaterial;
    private SXRMaterial mCompressedCubemapMaterial;
    private ArrayList<SXRTexture> mTextureList;
    private ArrayList<File> mSdcardResources;

    @Override
    public SplashMode getSplashMode() {
        return SplashMode.NONE;
    }
    @Override
    public void onInit(SXRContext gvrContext) {
        mSXRContext = gvrContext;

        SXRScene scene = mSXRContext.getMainScene();
        SXRAssetLoader loader = mSXRContext.getAssetLoader();
        scene.setFrustumCulling(true);

        boolean usingSdcard = false;
        final File file = new File(Environment.getExternalStorageDirectory()+"/gvr-cubemap");
        if (file.exists()) {
            final File[] files = file.listFiles();
            if (0 < files.length) {
                for (final File f : files) {
                    final String name = f.getName();
                    if (name.endsWith(".bmp") || name.endsWith(".png") || name.endsWith(".zip")) {
                        if (null == mSdcardResources) {
                            usingSdcard = true;
                            mEnvironmentType = 0;
                            mSdcardResources = new ArrayList<>();
                        }
                        mSdcardResources.add(f);
                    }
                }
            }
        }

        if (!usingSdcard) {
            scene.setStatsEnabled(true);
            // Uncompressed cubemap texture
            mCubemapTexture = loader.loadCubemapTexture(new SXRAndroidResource(mSXRContext, R.raw.beach));
            mCubemapMaterial = new SXRMaterial(gvrContext, SXRMaterial.SXRShaderType.Cubemap.ID);
            mCubemapMaterial.setMainTexture(mCubemapTexture);

            // Compressed cubemap texture
            final SXRTexture compressedCubemapTexture = loader.loadCompressedCubemapTexture(new SXRAndroidResource(mSXRContext,
                    R.raw.museum));
            mCompressedCubemapMaterial = new SXRMaterial(gvrContext, SXRMaterial.SXRShaderType.Cubemap.ID);
            mCompressedCubemapMaterial.setMainTexture(compressedCubemapTexture);

            // List of textures (one per face)
            mTextureList = new ArrayList<SXRTexture>(6);
            mTextureList.add(loader.loadTexture(new SXRAndroidResource(gvrContext,
                    R.drawable.back)));
            mTextureList.add(loader.loadTexture(new SXRAndroidResource(gvrContext,
                    R.drawable.right)));
            mTextureList.add(loader.loadTexture(new SXRAndroidResource(gvrContext,
                    R.drawable.front)));
            mTextureList.add(loader.loadTexture(new SXRAndroidResource(gvrContext,
                    R.drawable.left)));
            mTextureList.add(loader.loadTexture(new SXRAndroidResource(gvrContext,
                    R.drawable.top)));
            mTextureList.add(loader.loadTexture(new SXRAndroidResource(gvrContext,
                    R.drawable.bottom)));
            applyCubemap(scene);
        } else {
            applyFromSdcard(scene);
        }
    }

    private void applyCubemap(SXRScene scene) {
        switch (mEnvironmentType) {
            case 0:
                // ///////////////////////////////////////////////////////
                // create surrounding sphere using SXRSphereSceneObject //
                // ///////////////////////////////////////////////////////
                SXRSphereSceneObject mSphereEvironment = new SXRSphereSceneObject(
                        mSXRContext, 18, 36, false, mCubemapMaterial, 4, 4);
                mSphereEvironment.getTransform().setScale(CUBE_WIDTH, CUBE_WIDTH,
                        CUBE_WIDTH);
                scene.addSceneObject(mSphereEvironment);
                break;

            case 1:
                // ////////////////////////////////////////////////////////////
                // create surrounding cube using SXRCubeSceneObject method A //
                // ////////////////////////////////////////////////////////////
                SXRCubeSceneObject mCubeEvironment = new SXRCubeSceneObject(
                        mSXRContext, false, mCubemapMaterial);
                mCubeEvironment.getTransform().setScale(CUBE_WIDTH, CUBE_WIDTH,
                        CUBE_WIDTH);
                scene.addSceneObject(mCubeEvironment);
                break;

            case 2:
                // /////////////////////////////////////////////////////////////
                // create surrounding cube using compressed textures method B //
                // /////////////////////////////////////////////////////////////
                mCubeEvironment = new SXRCubeSceneObject(
                        mSXRContext, false, mCompressedCubemapMaterial);
                mCubeEvironment.getTransform().setScale(CUBE_WIDTH, CUBE_WIDTH,
                        CUBE_WIDTH);
                scene.addSceneObject(mCubeEvironment);
                break;

            case 3:
                // ////////////////////////////////////////////////////////////
                // create surrounding cube using SXRCubeSceneObject method C //
                // ////////////////////////////////////////////////////////////
                mCubeEvironment = new SXRCubeSceneObject(
                        mSXRContext, false, mTextureList, 2);
                mCubeEvironment.getTransform().setScale(CUBE_WIDTH, CUBE_WIDTH,
                        CUBE_WIDTH);
                scene.addSceneObject(mCubeEvironment);
                break;

            case 4:
                // ///////////////////////////////////////////////////////////
                // create surrounding cylinder using SXRCylinderSceneObject //
                // ///////////////////////////////////////////////////////////
                SXRCylinderSceneObject mCylinderEvironment = new SXRCylinderSceneObject(
                        mSXRContext, 0.5f, 0.5f, 1.0f, 10, 36, false, mCubemapMaterial, 2, 4);
                mCylinderEvironment.getTransform().setScale(CUBE_WIDTH, CUBE_WIDTH,
                        CUBE_WIDTH);
                scene.addSceneObject(mCylinderEvironment);
                break;

            case 5:
                // /////////////////////////////////////////////////////////////
                // create surrounding cube using six SXRSceneOjbects (quads) //
                // /////////////////////////////////////////////////////////////
                SXRSceneObject mFrontFace = new SXRSceneObject(mSXRContext,
                                                               CUBE_WIDTH, CUBE_WIDTH, mCubemapTexture);
                SXRMesh quadMesh = mFrontFace.getRenderData().getMesh();

                mFrontFace.getRenderData().setMaterial(mCubemapMaterial);
                mFrontFace.setName("front");
                scene.addSceneObject(mFrontFace);
                mFrontFace.getTransform().setPosition(0.0f, 0.0f, -CUBE_WIDTH * 0.5f);

                SXRSceneObject backFace = new SXRSceneObject(mSXRContext,
                                                             quadMesh, mCubemapTexture);
                backFace.getRenderData().setMaterial(mCubemapMaterial);
                backFace.setName("back");
                scene.addSceneObject(backFace);
                backFace.getTransform().setPosition(0.0f, 0.0f, CUBE_WIDTH * 0.5f);
                backFace.getTransform().rotateByAxis(180.0f, 0.0f, 1.0f, 0.0f);

                SXRSceneObject leftFace = new SXRSceneObject(mSXRContext,
                                                             quadMesh, mCubemapTexture);
                leftFace.getRenderData().setMaterial(mCubemapMaterial);
                leftFace.setName("left");
                scene.addSceneObject(leftFace);
                leftFace.getTransform().setPosition(-CUBE_WIDTH * 0.5f, 0.0f, 0.0f);
                leftFace.getTransform().rotateByAxis(90.0f, 0.0f, 1.0f, 0.0f);

                SXRSceneObject rightFace = new SXRSceneObject(mSXRContext,
                                                              quadMesh, mCubemapTexture);
                rightFace.getRenderData().setMaterial(mCubemapMaterial);
                rightFace.setName("right");
                scene.addSceneObject(rightFace);
                rightFace.getTransform().setPosition(CUBE_WIDTH * 0.5f, 0.0f, 0.0f);
                rightFace.getTransform().rotateByAxis(-90.0f, 0.0f, 1.0f, 0.0f);

                SXRSceneObject topFace = new SXRSceneObject(mSXRContext,
                                                            quadMesh, mCubemapTexture);
                topFace.getRenderData().setMaterial(mCubemapMaterial);
                topFace.setName("top");
                scene.addSceneObject(topFace);
                topFace.getTransform().setPosition(0.0f, CUBE_WIDTH * 0.5f, 0.0f);
                topFace.getTransform().rotateByAxis(90.0f, 1.0f, 0.0f, 0.0f);

                SXRSceneObject bottomFace = new SXRSceneObject(mSXRContext,
                                                               quadMesh, mCubemapTexture);
                bottomFace.getRenderData().setMaterial(mCubemapMaterial);
                bottomFace.setName("bottom");
                scene.addSceneObject(bottomFace);
                bottomFace.getTransform().setPosition(0.0f, -CUBE_WIDTH * 0.5f,
                        0.0f);
                bottomFace.getTransform().rotateByAxis(-90.0f, 1.0f, 0.0f, 0.0f);
                break;
        }

        SXRMaterial cubemapReflectionMaterial = new SXRMaterial(mSXRContext, SXRMaterial.SXRShaderType.CubemapReflection.ID);
        cubemapReflectionMaterial.setTexture("diffuseTexture", mCubemapTexture);
        cubemapReflectionMaterial.setMainTexture(mCubemapTexture);

        SXRSceneObject sphere = null;
        switch (mReflectiveType) {
            case 0:
                // ///////////////////////////////////////////////////////
                // create reflective sphere using SXRSphereSceneObject //
                // ///////////////////////////////////////////////////////
                sphere = new SXRSphereSceneObject(mSXRContext, 18, 36, true,
                        cubemapReflectionMaterial);
                break;

            case 1:
                // ////////////////////////////////////////////
                // create reflective sphere using OBJ model //
                // ////////////////////////////////////////////
                SXRMesh sphereMesh = mSXRContext.getAssetLoader().loadMesh(new SXRAndroidResource(mSXRContext,
                                                          R.raw.sphere));
                sphere = new SXRSceneObject(mSXRContext, sphereMesh, mCubemapTexture);
                sphere.getRenderData().setMaterial(cubemapReflectionMaterial);
                break;
        }

        if (sphere != null) {
            sphere.setName("sphere");
            scene.addSceneObject(sphere);
            sphere.getTransform().setScale(SCALE_FACTOR, SCALE_FACTOR,
                    SCALE_FACTOR);
            sphere.getTransform().setPosition(0.0f, 0.0f, -CUBE_WIDTH * 0.25f);
        }

        for (SXRSceneObject so : scene.getWholeSceneObjects()) {
            Log.v(TAG, "scene object name : " + so.getName());
        }
    }

    private void applyFromSdcard(final SXRScene scene) {
        final File file = mSdcardResources.get(mEnvironmentType);

        if (file.getName().endsWith(".zip")) {
            scene.getMainCameraRig().setCameraRigType(SXRCameraRig.SXRCameraRigType.Free.ID);

            try {
                final SXRTexture cubemapTexture = getSXRContext().getAssetLoader().loadCubemapTexture(new SXRAndroidResource(file.getAbsolutePath()));
                final SXRMaterial material = new SXRMaterial(getSXRContext(), SXRMaterial.SXRShaderType.Cubemap.ID);
                material.setMainTexture(cubemapTexture);

                final SXRSceneObject sceneObject = new SXRCubeSceneObject(mSXRContext, false, material);
                sceneObject.getTransform().setScale(CUBE_WIDTH, CUBE_WIDTH, CUBE_WIDTH);
                scene.addSceneObject(sceneObject);
            } catch(final Exception exc) {
                exc.printStackTrace();
            }

        } else {
            applyFromSdcard2dImpl(file);
        }
    }

    private void applyFromSdcard2dImpl(final File file) {
        final SXRScene scene = getSXRContext().getMainScene();
        scene.getMainCameraRig().setCameraRigType(SXRCameraRig.SXRCameraRigType.Freeze.ID);

        try {
            final BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
            bitmapOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(file.getAbsolutePath(), bitmapOptions);

            final SXRTexture t = getSXRContext().getAssetLoader().loadTexture(new SXRAndroidResource(file.getAbsolutePath()));
            final SXRSceneObject sceneObject = new SXRSceneObject(mSXRContext, 20, 20*bitmapOptions.outHeight/bitmapOptions.outWidth, t);
            sceneObject.getTransform().setPositionZ(-11);
            scene.addSceneObject(sceneObject);
        } catch(final Exception exc) {
            exc.printStackTrace();
        }
    }

    @Override
    public void onStep() {
        FPSCounter.tick();
    }

    public void onTouch() {
        if (null != mSXRContext) {
            mSXRContext.runOnGlThread(new Runnable() {
                @Override
                public void run() {
                    mSXRContext.getMainScene().clear();
                    ++mEnvironmentType;

                    if (null == mSdcardResources) {
                        mEnvironmentType %= MAX_ENVIRONMENTS;
                        applyCubemap(mSXRContext.getMainScene());
                    } else {
                        mEnvironmentType %= mSdcardResources.size();
                        applyFromSdcard(mSXRContext.getMainScene());
                    }

                    Log.i(TAG, "mEnvironmentType: " + mEnvironmentType);
                }
            });
        }
    }
}

