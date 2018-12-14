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

import android.opengl.GLES30;

import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRDrawFrameListener;
import com.samsungxr.SXRMaterial;
import com.samsungxr.SXRMesh;
import com.samsungxr.SXRNode;
import com.samsungxr.SXRRenderData;
import com.samsungxr.SXRShaderId;
import com.samsungxr.SXRTexture;
import com.samsungxr.SXRTextureParameters;
import com.samsungxr.arpet.PetContext;
import com.samsungxr.arpet.R;
import com.samsungxr.arpet.animations.DustyAnimation;
import com.samsungxr.arpet.character.CharacterController;
import com.samsungxr.arpet.constant.ArPetObjectType;
import com.samsungxr.arpet.constant.PetConstants;
import com.samsungxr.arpet.shaders.SXRDustyShader;
import com.samsungxr.arpet.util.LoadModelHelper;
import com.samsungxr.mixedreality.SXRPlane;
import com.samsungxr.shaders.SXRTextureShader;
import com.samsungxr.utility.Log;

import org.joml.Matrix4f;
import org.joml.Vector4f;

public class VirtualObjectController {
    private static final String TAG = VirtualObjectController.class.getSimpleName();

    private PetContext mPetContext;
    private CharacterController mPetController;
    private SXRNode mVirtualObject = null;
    private final DustyAnimation mDustyAnimation;
    private String mObjectType = "";

    private VirtualObjectShow virtualObjectShow = new VirtualObjectShow();

    public VirtualObjectController(PetContext petContext, CharacterController petController) {
        mPetContext = petContext;
        mPetController = petController;

        mDustyAnimation = new DustyAnimation(petContext.getSXRContext(), 2);
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

        if (objectType.equals(mObjectType)) {
            Log.d(TAG, "%s is already on the scene", objectType);
            return;
        } else {
            // Hide if there is a previous visible
            hideObject();
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

        final Matrix4f planeMtx = mPetController.getPlane().getTransform().getModelMatrix4f();
        final Matrix4f petMtx = mPetController.getView().getTransform().getModelMatrix4f();

        Vector4f  petOrientation = new Vector4f(petMtx.m30() - planeMtx.m30(),
                petMtx.m31() - planeMtx.m31(), petMtx.m32() - planeMtx.m32(), 0);

        // Apply plane's rotation in the vector
        orientation.mul(planeMtx);

        // Opposite side of the Pet
        if (petOrientation.x * orientation.x > 0
                || petOrientation.z * orientation.z > 0) {
            orientation.mul(-1);
        }

        // Distance from the plane's center
        if (planeWidth >= planeHeight) {
            orientation.mul(planeHeight / planeWidth);
        } else {
            orientation.mul(planeWidth / planeHeight);
        }

        final float planeX = planeMtx.m30() + orientation.x;
        final float planeY = planeMtx.m31() + orientation.y;
        final float planeZ = planeMtx.m32() + orientation.z;

        final float scale = mPetController.getView().getScale() * PetConstants.MODEL3D_DEFAULT_SCALE;

        virtualObjectShow.startAnimation(scale, planeX, planeY + 2, planeZ);
    }

    private class VirtualObjectShow implements SXRDrawFrameListener {
        private float scale;
        private float posY;
        private float countTime;
        private boolean jumpEnded;
        private final float DURATION1 = 0.5f;
        private final float DURATION2 = 0.7f;
        private final float DURATION3 = 0.9f;

        VirtualObjectShow() {

        }

        void startAnimation(float scale, float posX, float posY, float posZ) {
            this.scale = scale;
            this.posY = posY;

            float minScale = scale * 0.1f;
            mVirtualObject.getTransform().setPosition(posX, posY, posZ);
            mVirtualObject.getTransform().setScale(minScale, minScale, minScale);
            mPetContext.getMainScene().addNode(mVirtualObject);

            countTime = 0f;
            jumpEnded = false;
            mPetContext.getSXRContext().registerDrawFrameListener(this);
        }

        @Override
        public void onDrawFrame(float d) {
            if (mVirtualObject == null) {
                mPetContext.getSXRContext().unregisterDrawFrameListener(this);
                return;
            }

            if (countTime >= DURATION3) {
                mPetContext.getSXRContext().unregisterDrawFrameListener(this);
                mVirtualObject.getTransform().setPositionY(posY);
                mVirtualObject.getTransform().setScale(scale, scale, scale);

                startPetAnimation();
            } else if (countTime >= DURATION2) {
                // Scale animation 3: object will grow to 100% in Y axis only
                float t = countTime - 1.02f;
                float s = (t * t * -7f + 1.1f) * scale;
                mVirtualObject.getTransform().setScaleY(s);
            } else if (countTime >= DURATION1) {
                // Ensure that the virtual object will be at the correct position after "jump"
                // animation
                if (!jumpEnded) {
                    mVirtualObject.getTransform().setPositionY(posY);
                    startDustyAnimation();
                    jumpEnded = true;
                }

                // Scale animation 2: object will shrink to 15% in Y axis only
                float t = countTime - 0.38f;
                float s = (t * t * -7f + 1.1f) * scale;
                mVirtualObject.getTransform().setScaleY(s);
            } else {
                // Position animation: object will "jump"
                float t = countTime - 0.25f;
                float h = t * t * -160f + 10f;
                mVirtualObject.getTransform().setPositionY(posY + h);

                // Scale animation 1: object will grow from 10% to 110% scale and then shrink
                // to 100%
                t = countTime - 0.38f;
                float s = (t * t * -7f + 1.1f) * scale;
                mVirtualObject.getTransform().setScale(s, s, s);

            }
            countTime += d;
        }
    }

    private void startDustyAnimation() {
        final float x = mVirtualObject.getTransform().getPositionX();
        final float y = mVirtualObject.getTransform().getPositionY();
        final float z = mVirtualObject.getTransform().getPositionZ();

        mDustyAnimation.setDustySize(mVirtualObject.getBoundingVolume().radius * 4);
        mDustyAnimation.setDustyPosition(x, y, z);

        mPetContext.getSXRContext().getAnimationEngine().start(mDustyAnimation);
    }

    private void startPetAnimation() {
        float x = mVirtualObject.getTransform().getPositionX();
        float y = mVirtualObject.getTransform().getPositionY();
        float z = mVirtualObject.getTransform().getPositionZ();
        switch (mObjectType) {
            case ArPetObjectType.BED:
                mPetController.goToBed(x, y, z);
                break;
            case ArPetObjectType.BOWL:
                mPetController.goToBowl(x, y, z);
                break;
            case ArPetObjectType.HYDRANT:
                mPetController.goToHydrant(x, y, z);
                break;
        }
    }

    public void hideObject() {
        if (mVirtualObject != null && mVirtualObject.getParent() != null) {
            mVirtualObject.getParent().removeChildObject(mVirtualObject);
            mVirtualObject = null;
            mObjectType = "";
        }
    }
}
