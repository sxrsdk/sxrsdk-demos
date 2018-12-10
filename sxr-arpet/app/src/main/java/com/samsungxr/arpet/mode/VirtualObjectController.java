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

package com.samsungxr.arpet.mode;

import com.samsungxr.SXRNode;
import com.samsungxr.arpet.PetContext;
import com.samsungxr.arpet.character.CharacterController;
import com.samsungxr.arpet.constant.ArPetObjectType;
import com.samsungxr.arpet.constant.PetConstants;
import com.samsungxr.arpet.util.LoadModelHelper;
import com.samsungxr.mixedreality.SXRPlane;
import com.samsungxr.utility.Log;

import org.joml.Matrix4f;
import org.joml.Vector4f;

public class VirtualObjectController {
    private static final String TAG = VirtualObjectController.class.getSimpleName();

    private PetContext mPetContext;
    private CharacterController mPetController;
    private SXRNode mVirtualObject = null;
    private String mObjectType = "";

    public VirtualObjectController(PetContext petContext, CharacterController petController) {
        mPetContext = petContext;
        mPetController = petController;
    }

    private SXRNode load3DModel(@ArPetObjectType String type) {
        SXRNode objectModel = null;

        switch (type) {
            case ArPetObjectType.BED:
                objectModel = LoadModelHelper.loadModelSceneObject(mPetContext.getSXRContext(), LoadModelHelper.BED_MODEL_PATH);
                break;
            case ArPetObjectType.BOWL:
                objectModel = LoadModelHelper.loadModelSceneObject(mPetContext.getSXRContext(), LoadModelHelper.BOWL_MODEL_PATH);
                break;
            case ArPetObjectType.HYDRANT:
                objectModel = LoadModelHelper.loadModelSceneObject(mPetContext.getSXRContext(), LoadModelHelper.HYDRANT_MODEL_PATH);
                break;
        }
        return objectModel;
    }

    public void showObject(@ArPetObjectType String objectType) {
        final SXRPlane mainPlane = (SXRPlane)mPetController.getPlane().getParent().getComponent(SXRPlane.getComponentType());
        if (mainPlane == null) {
            Log.d(TAG, "no plane detected");
            return;
        }

        if (mObjectType.equals(objectType)) {
            Log.d(TAG, "%s is already on the scene", objectType);
            return;
        } else if (hasActiveObject()) {
            mPetContext.getMainScene().removeNode(mVirtualObject);
            mVirtualObject = null;
        }

        mVirtualObject = load3DModel(objectType);
        mObjectType = objectType;

        final float planeWidth = mainPlane.getWidth();
        final float planeHeight = mainPlane.getHeight();

        // vector to store plane's orientation
        Vector4f orientation;
        if (planeWidth >= planeHeight) {
            orientation = new Vector4f(0.5f, 0f, 0f, 0);
        } else {
            orientation = new Vector4f(0.0f, 0.5f, 0f, 0);
        }

        Matrix4f planeMtx = mPetController.getPlane().getTransform().getModelMatrix4f();
        // Apply plane's rotation in the vector
        orientation.mul(planeMtx);
        // Distance from the plane's center
        if (planeWidth >= planeHeight) {
            orientation.mul(planeHeight / planeWidth);
        } else {
            orientation.mul(planeWidth / planeHeight);
        }

        final float planeX = planeMtx.m30() + orientation.x;
        final float planeY = planeMtx.m31() + orientation.y;
        final float planeZ = planeMtx.m32() + orientation.z;

        final float petScale = mPetController.getView().getScale();
        mVirtualObject.getTransform().setScale(PetConstants.MODEL3D_DEFAULT_SCALE * petScale,
                PetConstants.MODEL3D_DEFAULT_SCALE * petScale,
                PetConstants.MODEL3D_DEFAULT_SCALE * petScale);
        mVirtualObject.getTransform().setPosition(planeX, planeY, planeZ);
        mVirtualObject.setEnable(true);

        mPetContext.getMainScene().addNode(mVirtualObject);
    }

    public boolean hasActiveObject() {
        return (mVirtualObject != null && mVirtualObject.isEnabled());
    }

    public void hideObject() {
        mPetContext.getMainScene().removeNode(mVirtualObject);
        mVirtualObject = null;
        mObjectType = "";
    }
}
