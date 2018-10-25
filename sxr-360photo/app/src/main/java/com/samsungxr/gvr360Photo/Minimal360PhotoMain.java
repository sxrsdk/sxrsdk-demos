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

package com.samsungxr.gvr360Photo;

import java.util.concurrent.Future;

import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRScene;
import com.samsungxr.SXRMain;
import com.samsungxr.SXRTexture;
import com.samsungxr.SXRMaterial;
import com.samsungxr.scene_objects.SXRSphereSceneObject;

public class Minimal360PhotoMain extends SXRMain {

    @Override
    public void onInit(SXRContext gvrContext) {

        // get a handle to the scene
        SXRScene scene = gvrContext.getMainScene();

        SXRSphereSceneObject sphereObject = null;

        // load texture
        SXRTexture texture = gvrContext.getAssetLoader().loadTexture(new SXRAndroidResource(gvrContext, R.raw.photosphere));

        // create a sphere scene object with the specified texture and triangles facing inward (the 'false' argument)
        sphereObject = new SXRSphereSceneObject(gvrContext, 72, 144, false, texture);

        // add the scene object to the scene graph
        scene.addSceneObject(sphereObject);
    }

}