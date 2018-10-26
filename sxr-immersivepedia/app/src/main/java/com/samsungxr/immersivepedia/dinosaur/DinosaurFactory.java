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

package com.samsungxr.immersivepedia.dinosaur;

import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRImportSettings;
import com.samsungxr.SXRMesh;
import com.samsungxr.SXRTexture;
import com.samsungxr.immersivepedia.R;
import com.samsungxr.immersivepedia.focus.FocusableSceneObject;

import java.util.EnumSet;

public class DinosaurFactory {

    private static final int NEGATIVE_DEGRES_90 = 90;
    private static float adujstRelative = 1f; // default = 1 (close as possible)
    private static float adjustAbsolute = 3f; // default = 0 (close as possible)
    private static float adjustAngleArroundCamera = 0f;

    public static float ANKYLOSAURUS_DISTANCE = (4.41f * adujstRelative) + adjustAbsolute;
    public static float APATOSAURUS_DISTANCE = (8.1883f * adujstRelative) + adjustAbsolute;
    public static float TREX_DISTANCE = (6.4987f * adujstRelative) + adjustAbsolute;

    public static float STYRACOSAURUS_ANGLE_AROUND_CAMERA = (4 * NEGATIVE_DEGRES_90) + adjustAngleArroundCamera;
    public static float ANKYLOSAURUS_ANGLE_AROUND_CAMERA = (3 * NEGATIVE_DEGRES_90) + adjustAngleArroundCamera;
    public static float APATOSAURUS_ANGLE_AROUND_CAMERA = (2 * NEGATIVE_DEGRES_90) + adjustAngleArroundCamera;
    public static float TREX_ANGLE_AROUND_CAMERA = (1 * NEGATIVE_DEGRES_90) + adjustAngleArroundCamera;

    private static DinosaurFactory instance;
    private SXRContext sxrContext;

    EnumSet<SXRImportSettings> additionalSettings = EnumSet
            .of(SXRImportSettings.CALCULATE_SMOOTH_NORMALS);
    EnumSet<SXRImportSettings> settings = SXRImportSettings
            .getRecommendedSettingsWith(additionalSettings);

    private Dinosaur styracosaurus;
    private Dinosaur ankylosaurus;
    private Dinosaur apatosaurus;
    private Dinosaur tRex;

    private DinosaurFactory(SXRContext sxrContext) {
        this.sxrContext = sxrContext;
        styracosaurus = createStyrocosaurus();
        ankylosaurus = createAnkylosaurus();
        apatosaurus = createApatosaurus();
        tRex = createTRex();

    }

    public static synchronized DinosaurFactory getInstance(SXRContext sxrContext) {
        if (instance == null) {
            instance = new DinosaurFactory(sxrContext);
        }
        return instance;
    }

    private Dinosaur createDinosauros(int dinoMeshId, int dinoTextureId, int baseMeshId, int groundMeshId) {

        FocusableSceneObject dino = createDinosaur(dinoMeshId, dinoTextureId);

        FocusableSceneObject base = createDinosaurBase(baseMeshId);
        FocusableSceneObject ground = createDinosaurGround(groundMeshId);

        return new Dinosaur(sxrContext, dino, base, ground);
    }

    private FocusableSceneObject createDinosaur(int dinoMeshId, int dinoTextureId) {
        SXRMesh baseMesh = sxrContext.getAssetLoader().loadMesh(new SXRAndroidResource(sxrContext, dinoMeshId), settings);
        SXRTexture baseTexture = sxrContext.getAssetLoader().loadTexture(new SXRAndroidResource(sxrContext, dinoTextureId));
        final FocusableSceneObject dino = new FocusableSceneObject(sxrContext, baseMesh, baseTexture);

        return dino;
    }

    private FocusableSceneObject createDinosaurBase(int baseMeshId) {
        SXRMesh baseMesh = sxrContext.getAssetLoader().loadMesh(new SXRAndroidResource(sxrContext, baseMeshId), settings);
        SXRTexture baseTexture = sxrContext.getAssetLoader().loadTexture(new SXRAndroidResource(sxrContext, R.drawable.base_tex_diffuse));
        FocusableSceneObject dinosaurBase = new FocusableSceneObject(sxrContext, baseMesh, baseTexture);
        return dinosaurBase;
    }

    private FocusableSceneObject createDinosaurGround(int groundMesh) {

        SXRMesh mesh = sxrContext.getAssetLoader().loadMesh(new SXRAndroidResource(sxrContext, groundMesh), settings);
        SXRTexture groundTexture = sxrContext.getAssetLoader().loadTexture(new SXRAndroidResource(sxrContext, R.raw.ground_tex_diffuse));
        final FocusableSceneObject dinosaurGround = new FocusableSceneObject(sxrContext, mesh, groundTexture);

        return dinosaurGround;
    }

    private Dinosaur createStyrocosaurus() {
        return createDinosauros(
                R.raw.styracosaurus_mesh, R.raw.styracosaurus_tex_diffuse,
                R.raw.styracosaurus_base_mesh, R.raw.styracosaurus_ground_mesh);
    }

    private Dinosaur createAnkylosaurus() {
        return createDinosauros(
                R.raw.ankylosaurus_mesh, R.raw.ankyosaurus_tex_diffuse,
                R.raw.ankylosaurus_base_mesh, R.raw.ankylosaurus_ground_mesh);
    }

    private Dinosaur createApatosaurus() {
        return createDinosauros(
                R.raw.apatosaurus_mesh, R.raw.apatosaurus_tex_diffuse,
                R.raw.apatosaurus_base_mesh, R.raw.apatosaurus_ground_mesh);
    }

    private Dinosaur createTRex() {
        return createDinosauros(
                R.raw.trex_mesh, R.raw.trex_tex_diffuse,
                R.raw.trex_base_mesh, R.raw.trex_ground_mesh);
    }

    public Dinosaur getStyracosaurus() {
        return styracosaurus;
    }

    public Dinosaur getAnkylosaurus() {
        return ankylosaurus;
    }

    public Dinosaur getApatosaurus() {
        return apatosaurus;
    }

    public Dinosaur getTRex() {
        return tRex;
    }

}
