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

        final float scale = mPetContext.getMixedReality().getARToVRScale();
        final float planeWidth = mainPlane.getWidth() * 0.25f * scale;
        final float[] centerPoseMtx = mPetController.getPlane().getTransform().getModelMatrix();

        final float planeX = centerPoseMtx[12] + planeWidth;
        final float planeY = centerPoseMtx[13];
        final float planeZ = centerPoseMtx[14];

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
