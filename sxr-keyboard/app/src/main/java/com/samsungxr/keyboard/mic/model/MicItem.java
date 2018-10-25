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

package com.samsungxr.keyboard.mic.model;

import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRSceneObject;
import com.samsungxr.keyboard.util.SceneObjectNames;

public class MicItem extends SXRSceneObject {

    public static final float WIDTH = 1.2f;
    public static float HIGHT = 1.2f;

    public MicItem(SXRContext gvrContext, int gVRAndroidResourceTexture) {

        super(gvrContext, HIGHT, WIDTH, gvrContext.getAssetLoader().loadTexture(new SXRAndroidResource(gvrContext,
                gVRAndroidResourceTexture)));
        setName(SceneObjectNames.MIC_ITEM);

    }

}
