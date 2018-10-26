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

package com.samsungxr.keyboard.spinner;

import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRMaterial;
import com.samsungxr.SXRMesh;
import com.samsungxr.SXRRenderData;
import com.samsungxr.SXRSceneObject;
import com.samsungxr.SXRSphereCollider;
import com.samsungxr.keyboard.R;
import com.samsungxr.keyboard.util.RenderingOrder;
import com.samsungxr.keyboard.util.SceneObjectNames;

public class SpinnerSkeleton extends SXRSceneObject {

    private SXRContext sxrContext;
    private SXRSceneObject spinnerBox;
    private SXRSceneObject spinnerShadow;

    public SpinnerSkeleton(SXRContext sxrContext) {
        super(sxrContext);

        setName(SceneObjectNames.SPINNER_SKELETON);
        this.sxrContext = sxrContext;
        createSkeletonSpinner();
    }

    private SXRSceneObject createSkeletonSpinner() {

        spinnerBox = getSpinnerBackground(R.drawable.spinner_asset_box);
        spinnerShadow = getSpinnerBackground(R.drawable.spinner_asset_shadow);

        spinnerBox.getRenderData().setRenderingOrder(RenderingOrder.SPINNER_BOX);
        spinnerShadow.getRenderData().setRenderingOrder(RenderingOrder.SPINNER_SHADOW);

        spinnerBox.attachComponent(new SXRSphereCollider(getSXRContext()));
        addChildObject(spinnerBox);
        addChildObject(spinnerShadow);

        return this;

    }

    private SXRSceneObject getSpinnerBackground(int resourceTextureID) {

        SXRSceneObject object = new SXRSceneObject(sxrContext);
        SXRRenderData renderData = new SXRRenderData(sxrContext);
        SXRMaterial material = new SXRMaterial(sxrContext);
        SXRMesh mesh = sxrContext.createQuad(0.49f / 2, 1.63f / 2 /**
         * - 0.01f
         * 1.1f
         */
        );

        renderData.setMaterial(material);
        renderData.setMesh(mesh);
        object.attachRenderData(renderData);
        object.getRenderData()
                .getMaterial()
                .setMainTexture(
                        sxrContext.getAssetLoader().loadTexture(new SXRAndroidResource(sxrContext.getActivity(),
                                resourceTextureID)));
        return object;

    }

    public SXRSceneObject getSpinnerBox() {
        return spinnerBox;
    }

    public SXRSceneObject getSpinnerShadow() {
        return spinnerShadow;
    }

}
