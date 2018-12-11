/*
 * Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.samsungxr.arpet.util;

import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRNode;
import com.samsungxr.SXRTexture;

import java.io.IOException;
import java.io.InputStream;

public class LoadModelHelper {
    public static String PET_GRAB_PIVOT = "m_DnLip_JNT";

    public static String PET_MODEL_PATH = "objects/pet_model.dae";
    public static String BALL_MODEL_PATH = "objects/bone_model.fbx";
    public static String BED_MODEL_PATH = "objects/bed.fbx";
    public static String BOWL_MODEL_PATH = "objects/bowl.fbx";
    public static String HYDRANT_MODEL_PATH = "objects/hydrant.fbx";

    public static String PET_BONES_MAP_PATH = "objects/anim/pet_skeleton_map.txt";
    public static String[] PET_ANIMATIONS_PATH = {
            "objects/anim/pet_anim_idle.bvh",
            "objects/anim/pet_anim_walk.bvh",
            "objects/anim/pet_anim_run.bvh",
            "objects/anim/pet_bone_anim_walk.bvh",
            "objects/anim/pet_bone_anim_grab.bvh",
            "objects/anim/pet_anim_drink_enter.bvh",
            "objects/anim/pet_anim_drink_exit.bvh",
            "objects/anim/pet_anim_drink_loop.bvh",
            "objects/anim/pet_anim_drink_loop.bvh",
            "objects/anim/pet_anim_hydrant_enter.bvh",
            "objects/anim/pet_anim_hydrant_exit.bvh",
            "objects/anim/pet_anim_hydrant_loop.bvh",
            "objects/anim/pet_anim_sleep_enter.bvh",
            "objects/anim/pet_anim_sleep_exit.bvh",
            "objects/anim/pet_anim_sleep_loop.bvh"
    };

    public static SXRNode loadModelSceneObject(SXRContext sxrContext, String modelPath) {
        SXRNode modelSceneObject = null;
        try {
            modelSceneObject = sxrContext.getAssetLoader().loadModel(modelPath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return modelSceneObject;
    }

    public static SXRNode loadSceneObject(SXRContext sxrContext, String modelPath) {
        SXRNode sceneObject = null;
        try {
            sceneObject = sxrContext.getAssetLoader().loadModel(modelPath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return sceneObject;
    }

    public static SXRTexture loadTexture(SXRContext sxrContext, int id) {
        final SXRAndroidResource resource = new SXRAndroidResource(sxrContext, id);
        final SXRTexture texture = sxrContext.getAssetLoader().loadTexture(resource);

        return texture;
    }

    public static String readFile(SXRContext sxrContext, String filePath) {
        try
        {
            SXRAndroidResource res = new SXRAndroidResource(sxrContext, filePath);
            InputStream stream = res.getStream();
            byte[] bytes = new byte[stream.available()];
            stream.read(bytes);
            String s = new String(bytes);
            return s;
        }
        catch (IOException ex)
        {
            return null;
        }
    }
}
