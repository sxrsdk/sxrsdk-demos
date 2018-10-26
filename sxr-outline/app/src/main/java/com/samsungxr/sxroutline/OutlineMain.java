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

package com.samsungxr.sxroutline;

import java.io.IOException;
import java.util.EnumSet;

import com.samsungxr.SXRActivity;
import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRScene;
import com.samsungxr.SXRSceneObject;
import com.samsungxr.SXRMain;
import com.samsungxr.SXRShaderId;
import com.samsungxr.SXRTexture;
import com.samsungxr.SXRMaterial;
import com.samsungxr.SXRMesh;
import com.samsungxr.SXRMaterial.SXRShaderType;
import com.samsungxr.SXRRenderPass;
import com.samsungxr.SXRRenderPass.SXRCullFaceEnum;
import com.samsungxr.SXRImportSettings;

import android.util.Log;

public class OutlineMain extends SXRMain {

    private SXRContext mSXRContext;
    private SXRSceneObject mCharacter;

    private static final float ROTATION_SPEED = 0.75f;
    private final String mModelPath = "FreeCharacter_01.fbx";
    private final String mDiffuseTexturePath = "Body_Diffuse_01.jpg";

    private SXRActivity mActivity;

    private static final String TAG = "OutlineSample";
    private SXRShaderId outlineID;

    public OutlineMain(SXRActivity activity) {
        mActivity = activity;
    }

        @Override
        public SplashMode getSplashMode() {
            return SplashMode.NONE;
        }

    @Override
    public void onInit(SXRContext sxrContext) {
        mSXRContext = sxrContext;
        SXRScene outlineScene = sxrContext.getMainScene();

        try {
            EnumSet<SXRImportSettings> additionalSettings = EnumSet.of(SXRImportSettings.CALCULATE_SMOOTH_NORMALS,  SXRImportSettings.NO_ANIMATION);
            EnumSet<SXRImportSettings> settings = SXRImportSettings.getRecommendedSettingsWith(additionalSettings);
            SXRMesh characterMesh = mSXRContext.getAssetLoader().loadMesh(new SXRAndroidResource(mSXRContext,
                    mModelPath), settings);

            // Setup Scene - Alternatively to set character transform, one could
            // achieve same effect by setting camera transform (outlineScene->getMainCameraRig)
            // passing inverse transformation values.
            mCharacter = new SXRSceneObject(mSXRContext, characterMesh);
            mCharacter.getTransform().setPosition(0.0f, -300.0f, -200.0f);
            mCharacter.getTransform().setRotationByAxis(-90.0f, 1.0f, 0.0f, 0.0f);

            // Create Base Material Pass
            // --------------------------------------------------------------
            outlineID = new SXRShaderId(OutlineShader.class);
            SXRMaterial outlineMaterial = new SXRMaterial(mSXRContext, outlineID);

            // Brown-ish outline color
            outlineMaterial.setVec4(OutlineShader.COLOR_KEY, 0.4f, 0.1725f,
                    0.1725f, 1.0f);
            outlineMaterial.setFloat(OutlineShader.THICKNESS_KEY, 2.0f);

            // For outline we want to cull front faces
            mCharacter.getRenderData().setMaterial(outlineMaterial);
            mCharacter.getRenderData().setCullFace(SXRCullFaceEnum.Front);

            // Create Additional Pass
            // ----------------------------------------------------------------
            // load texture
            SXRTexture texture = sxrContext.getAssetLoader().loadTexture(new SXRAndroidResource(
                    mSXRContext, mDiffuseTexturePath));

            SXRMaterial material = new SXRMaterial(mSXRContext, SXRShaderType.Texture.ID);
            material.setMainTexture(texture);

            SXRRenderPass pass = new SXRRenderPass(mSXRContext);
            pass.setMaterial(material);
            pass.setCullFace(SXRCullFaceEnum.Back);
            mCharacter.getRenderData().addPass(pass);

            // Finally Add Cube to Scene
            outlineScene.addSceneObject(mCharacter);

        } catch(IOException e) {
            e.printStackTrace();
            mActivity.finish();
            mActivity = null;
            Log.e(TAG, "One or more assets could not be loaded.");
        }
    }

    @Override
    public void onStep() {
        if (mCharacter != null) {
            mCharacter.getTransform().rotateByAxis(ROTATION_SPEED, 0.0f, 1.0f, 0.0f);
        }
    }

}
