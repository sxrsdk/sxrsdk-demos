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

package com.samsungxr.modelviewer2;

import android.util.Log;

import com.samsungxr.SXRContext;
import com.samsungxr.SXRMaterial;
import com.samsungxr.SXRMeshCollider;
import com.samsungxr.SXRRenderData;
import com.samsungxr.SXRNode;
import com.samsungxr.SXRSphereCollider;
import com.samsungxr.animation.SXRAnimation;
import com.samsungxr.animation.SXRAnimator;
import com.samsungxr.util.BoundingBoxCreator;
import org.joml.Vector3f;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Model {
    String name;
    String location;

    SXRNode model;
    ArrayList<SXRMaterial> originalMaterial;
    SXRAnimator animation;
    private float currentZoom = 0;


    private static final String TAG = "Model";

    public Model(String name, String location) {
        this.name = name;
        this.location = location;
    }

    String getModelName() {
        return name;
    }

    private void saveRenderData() {
        originalMaterial = new ArrayList<SXRMaterial>();
        ArrayList<SXRRenderData> rdata = model.getAllComponents(SXRRenderData.getComponentType());
        for (SXRRenderData r : rdata) {
            originalMaterial.add(r.getMaterial());
        }
    }

    private void loadModel(SXRContext context) {
        try {
            Log.d(TAG, "Absent so loading" + name);
            model = context.getAssetLoader().loadModel("sd:" + location);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Unable to load model");
            return;
        }

        SXRNode.BoundingVolume bv = model.getBoundingVolume();
        model.attachComponent(new SXRMeshCollider(context, true));

        // Adding Pointee to Model
        bv = model.getBoundingVolume();
        float originalRadius = bv.radius;
        Log.i(TAG, "Radius" + Float.toString(originalRadius));

        // TODO Scale Appropriately
        if (originalRadius > 7.0f || originalRadius < 5.0f) {
            float scaleFactor = 7 / originalRadius;
            model.getTransform().setScale(scaleFactor, scaleFactor, scaleFactor);
        }

        // Make Copy of Original Render Data
        saveRenderData();

        // Load Animations
        animation = (SXRAnimator) model.getComponent(SXRAnimator.getComponentType());
    }

    public SXRAnimator getAnimation() {
        return animation;
    }

    public SXRNode getModel(SXRContext context) {
        if (model == null) {
            loadModel(context);
        }
        return model;
    }

    public float getCurrentZoom() {
        return currentZoom;
    }

    public void setCurrentZoom(float zoom) {
        currentZoom = zoom;
    }
}
