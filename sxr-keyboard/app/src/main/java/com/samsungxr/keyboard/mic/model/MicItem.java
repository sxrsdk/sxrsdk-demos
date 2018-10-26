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
import com.samsungxr.SXRNode;
import com.samsungxr.keyboard.util.NodeNames;

public class MicItem extends SXRNode {

    public static final float WIDTH = 1.2f;
    public static float HIGHT = 1.2f;

    public MicItem(SXRContext sxrContext, int gVRAndroidResourceTexture) {

        super(sxrContext, HIGHT, WIDTH, sxrContext.getAssetLoader().loadTexture(new SXRAndroidResource(sxrContext,
                gVRAndroidResourceTexture)));
        setName(NodeNames.MIC_ITEM);

    }

}
