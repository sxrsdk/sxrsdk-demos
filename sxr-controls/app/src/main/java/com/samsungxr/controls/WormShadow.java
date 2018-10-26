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

package com.samsungxr.controls;

import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRMesh;
import com.samsungxr.SXRRenderPass.SXRCullFaceEnum;
import com.samsungxr.SXRSceneObject;
import com.samsungxr.SXRTexture;


public class WormShadow extends SXRSceneObject {

    public WormShadow(SXRContext sxrContext, float width, float height, int renderingOrder) {
        super(sxrContext);

        createShadowObject(width, height, renderingOrder);
    }

    private void createShadowObject(float width, float height, int renderingOrder) {

        SXRMesh checkMesh = getSXRContext().createQuad(width, height);

        SXRTexture checkTexture = getSXRContext().getAssetLoader().loadTexture(
                new SXRAndroidResource(getSXRContext(), R.drawable.shadow));

        SXRSceneObject shadowObject = new SXRSceneObject(getSXRContext(), checkMesh, checkTexture);
        shadowObject.getTransform().rotateByAxis(90, 1, 0, 0);
        shadowObject.getRenderData().setRenderingOrder(renderingOrder);

        shadowObject.getRenderData().setCullFace(SXRCullFaceEnum.None);

        addChildObject(shadowObject);
    }
}