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

package com.samsungxr.complexscene;

import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRImportSettings;
import com.samsungxr.SXRMain;
import com.samsungxr.SXRMaterial;
import com.samsungxr.SXRMesh;
import com.samsungxr.SXRRenderData;
import com.samsungxr.SXRScene;
import com.samsungxr.SXRSceneObject;

import java.io.IOException;
import java.util.EnumSet;

import static com.samsungxr.SXRImportSettings.NO_LIGHTING;

public class SampleMain extends SXRMain {
    @Override
    public SplashMode getSplashMode() {
        return SplashMode.NONE;
    }

    @Override
    public void onInit(SXRContext gvrContext) throws IOException {
        // set background color
        SXRScene scene = gvrContext.getMainScene();
        scene.setBackgroundColor(1, 1, 1, 1);
        scene.setFrustumCulling(false);

        float NORMAL_CURSOR_SIZE = 0.4f;
        float CURSOR_Z_POSITION = -9.0f;
        int CURSOR_RENDER_ORDER = 100000;

        SXRSceneObject cursor = new SXRSceneObject(gvrContext,
                NORMAL_CURSOR_SIZE, NORMAL_CURSOR_SIZE,
                gvrContext.getAssetLoader().loadTexture(new SXRAndroidResource(gvrContext, "cursor_idle.png")));
        cursor.getTransform().setPositionZ(CURSOR_Z_POSITION);
        cursor.setName("cursor");
        cursor.getRenderData()
                .setRenderingOrder(SXRRenderData.SXRRenderingOrder.OVERLAY)
                .setDepthTest(false)
                .setRenderingOrder(CURSOR_RENDER_ORDER);
        gvrContext.getMainScene().getMainCameraRig().addChildObject(cursor);

        try {
            EnumSet<SXRImportSettings> settings = SXRImportSettings.getRecommendedSettingsWith(EnumSet.of(NO_LIGHTING));
            SXRMesh mesh = gvrContext.getAssetLoader().loadMesh(
                    new SXRAndroidResource(gvrContext, "bunny.obj"),
                    settings);

            final int OBJECTS_CNT = 8;
            for (int x=-OBJECTS_CNT; x<=OBJECTS_CNT; ++x) {
                for (int y=-OBJECTS_CNT; y<=OBJECTS_CNT; ++y) {
                    SXRSceneObject sceneObject = getColorMesh(1.0f, mesh);
                    sceneObject.getTransform().setPosition(1.0f*x, 1.0f*y, -7.5f);
                    sceneObject.getTransform().setScale(0.5f, 0.5f, 1.0f);
                    scene.addSceneObject(sceneObject);
                }

        }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private SXRSceneObject getColorMesh(float scale, SXRMesh mesh) {
        SXRMaterial material = new SXRMaterial(getSXRContext(), SXRMaterial.SXRShaderType.Color.ID);
        material.setColor(1.0f, 0.0f, 1.0f);

        SXRSceneObject meshObject = new SXRSceneObject(getSXRContext(), mesh);
        meshObject.getTransform().setScale(scale, scale, scale);
        meshObject.getRenderData().setMaterial(material);

        return meshObject;
    }
}
