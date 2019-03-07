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

package com.samsungxr.arpet.character;

import android.opengl.GLES30;
import android.support.annotation.NonNull;

import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRMaterial;
import com.samsungxr.SXRMesh;
import com.samsungxr.SXRMeshCollider;
import com.samsungxr.SXRNode;
import com.samsungxr.SXRPicker;
import com.samsungxr.SXRRenderData;
import com.samsungxr.SXRScene;
import com.samsungxr.SXRShaderId;
import com.samsungxr.SXRTexture;
import com.samsungxr.SXRTextureParameters;
import com.samsungxr.animation.SXRAnimation;
import com.samsungxr.animation.SXRAnimator;
import com.samsungxr.animation.SXRAvatar;
import com.samsungxr.animation.SXRRepeatMode;
import com.samsungxr.animation.SXRSkeleton;
import com.samsungxr.utility.Log;

import com.samsungxr.arpet.PetContext;
import com.samsungxr.arpet.R;
import com.samsungxr.arpet.constant.ArPetObjectType;
import com.samsungxr.arpet.constant.PetConstants;
import com.samsungxr.arpet.gesture.OnScaleListener;
import com.samsungxr.arpet.gesture.ScalableObject;
import com.samsungxr.arpet.gesture.impl.ScaleGestureDetector;
import com.samsungxr.arpet.mode.ILoadEvents;
import com.samsungxr.arpet.mode.IPetView;
import com.samsungxr.arpet.shaders.SXRTiledMaskShader;
import com.samsungxr.arpet.util.LoadModelHelper;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CharacterView extends SXRNode implements
        IPetView,
        ScalableObject {

    private final String TAG = getClass().getSimpleName();

    private List<OnScaleListener> mOnScaleListeners = new ArrayList<>();

    private SXRNode mBoundaryPlane = null;
    private float[] mPlaneCenterPose = new float[16];
    private SXRNode mShadow;
    private SXRNode mInfinityPlan;
    public final static String PET_COLLIDER = "corpo_GEO";  // From 3D model
    private final PetContext mPetContext;
    private SXRNode m3DModel;
    private SXRAvatar mPetAvatar;
    private String mBoneMap;
    protected ILoadEvents mLoadListener = null;
    private SXRNode mTapObject;
    private SXRNode mGrabbingPivot = null;

    CharacterView(@NonNull PetContext petContext) {
        super(petContext.getSXRContext());

        mPetContext = petContext;
        mTapObject = new SXRNode(mPetContext.getSXRContext());
        mTapObject.getTransform().setScale(0.01f, 0.01f, 0.01f);
    }

    public SXRNode getTapObject() {
         return mTapObject;
    }

    public void setTapPosition(float x, float y, float z) {
        mTapObject.getTransform().setPosition(x, y, z);
    }

    public SXRAnimator getAnimation(int i) {
        if (mPetAvatar != null && mPetAvatar.getAnimationCount() > i) {
            return mPetAvatar.getAnimation(i);
        }

        return null;
    }

    public void resetAnimation() {
        SXRAnimator animatior = getAnimation(0);
        if (animatior != null) {
            animatior.animate(0);
        }
    }

    private void createShadow() {
        final SXRContext sxrContext = getSXRContext();
        SXRTexture tex = sxrContext.getAssetLoader().loadTexture(
                new SXRAndroidResource(sxrContext, R.drawable.pet_shadow));
        SXRMaterial mat = new SXRMaterial(sxrContext);
        mat.setMainTexture(tex);
        mShadow = new SXRNode(sxrContext, 0.3f, 0.6f);
        mShadow.getRenderData().setMaterial(mat);
        mShadow.getTransform().setRotationByAxis(-90f, 1f, 0f, 0f);
        mShadow.getTransform().setPosition(0f, 0.01f, 0.15f);
        mShadow.getRenderData().setAlphaBlend(true);
        mShadow.getRenderData().setRenderingOrder(SXRRenderData.SXRRenderingOrder.TRANSPARENT + 500);
        mShadow.setName("shadow");
        addChildObject(mShadow);
    }

    private void createInfinityPlan() {
        final SXRContext sxrContext = getSXRContext();
        final float width = 2.0f;
        final float height = 2.0f;

        final float[] vertices = new float[]{
                0.0f, 0.0f, 0.0f,
                width * -0.25f, height * 0.5f, 0.0F,
                width * -0.5f, height * 0.25f, 0.0F,
                width * -0.5f, height * -0.25f, 0.0f,
                width * -0.25f, height * -0.5f, 0.0f,
                width * 0.25f, height * -0.5f, 0.0f,
                width * 0.5f, height * -0.25f, 0.0f,
                width * 0.5f, height * 0.25f, 0.0f,
                width * 0.25f, height * 0.5f, 0.0f,
                width * -0.25f, height * 0.5f, 0.0F
        };

        mInfinityPlan = new SXRNode(sxrContext);
        final SXRTextureParameters texParams = new SXRTextureParameters(sxrContext);
        final SXRTexture tex = sxrContext.getAssetLoader().loadTexture(
                new SXRAndroidResource(sxrContext, R.drawable.infinity_plan));
        final SXRMaterial material = new SXRMaterial(sxrContext, new SXRShaderId(SXRTiledMaskShader.class));
        final SXRRenderData renderData = new SXRRenderData(sxrContext);
        final SXRMesh mesh = new SXRMesh(sxrContext, "float3 a_position");

        texParams.setWrapSType(SXRTextureParameters.TextureWrapType.GL_MIRRORED_REPEAT);
        texParams.setWrapTType(SXRTextureParameters.TextureWrapType.GL_MIRRORED_REPEAT);
        tex.updateTextureParameters(texParams);

        mesh.setVertices(vertices);
        renderData.setMesh(mesh);

        renderData.setAlphaBlend(true);
        material.setMainTexture(tex);
        renderData.setMaterial(material);
        renderData.setDrawMode(GLES30.GL_TRIANGLE_FAN);

        mInfinityPlan.attachComponent(renderData);
        mInfinityPlan.getTransform().setPosition(0f, 0.01f, -0.02f);
        mInfinityPlan.getTransform().setRotationByAxis(-90f, 1f, 0f, 0f);
        mInfinityPlan.setName("infinityPlan");
    }

    public boolean updatePose(float[] poseMatrix) {
        if (mPetContext.getMode() != PetConstants.SHARE_MODE_GUEST) {
            float[] planeModel = mBoundaryPlane.getTransform().getModelMatrix();
            Vector3f centerPlane = new Vector3f(planeModel[12], planeModel[13], planeModel[14]);
            poseMatrix[13] = planeModel[13] + 2;

            final boolean infinityPlane = false;
            if (!infinityPlane) {
                SXRPicker.SXRPickedObject pickedObject = SXRPicker.pickNode(mBoundaryPlane,
                        0, 0, 0, poseMatrix[12], poseMatrix[13], poseMatrix[14]);
                float[] petModel = getTransform().getModelMatrix();
                if (pickedObject == null && centerPlane.distance(petModel[12], petModel[13], petModel[14])
                        < centerPlane.distance(poseMatrix[12], poseMatrix[13], poseMatrix[14])) {
                    return false;
                }
            }
        }

        getTransform().setModelMatrix(poseMatrix);

        return true;
    }

    public void setBoundaryPlane(SXRNode boundary) {
        if (mBoundaryPlane != null) {
            mBoundaryPlane.removeChildObject(mTapObject);
            mPetContext.unregisterSharedObject(mBoundaryPlane);
        }

        boundary.addChildObject(mTapObject);
        mPetContext.registerSharedObject(boundary, ArPetObjectType.PLANE);

        mPlaneCenterPose = boundary.getTransform().getModelMatrix();
        mBoundaryPlane = boundary;
    }

    public SXRNode getBoundaryPlane() {
         return mBoundaryPlane;
    }

    @Override
    public void scale(float factor) {
        getTransform().setScale(factor, factor, factor);
        notifyScale(factor);
    }

    public float getScale() {
        return getTransform().getScaleX();
    }

    public void rotate(float angle) {
        getTransform().rotateByAxis(angle, 0, 1, 0);
    }

    public void startDragging() {
        m3DModel.getTransform().setPositionY(0.4f);
    }

    public void stopDragging() {
        m3DModel.getTransform().setPositionY(0.2f);
    }

    public boolean isDragging() {
        return m3DModel.getTransform().getPositionY() > 0.2f;
    }

    private synchronized void notifyScale(float factor) {
        for (OnScaleListener listener : mOnScaleListeners) {
            listener.onScale(factor);
        }
    }

    @Override
    public void addOnScaleListener(OnScaleListener listener) {
        if (!mOnScaleListeners.contains(listener)) {
            mOnScaleListeners.add(listener);
        }
    }

    @Override
    public void show(SXRScene mainScene) {
        mainScene.addNode(this);
        //getAnchor().attachSceneObject(mInfinityPlan);
    }

    @Override
    public void hide(SXRScene mainScene) {
        mainScene.removeNode(this);
        setDefaultScaleAndPosition();
        //getAnchor().detachSceneObject(mInfinityPlan);
    }

    @Override
    public void load(ILoadEvents listener) {
        final SXRContext sxrContext = getSXRContext();
        mLoadListener = listener;

        createShadow();

        // createInfinityPlan();

        mBoneMap = LoadModelHelper.readFile(sxrContext, LoadModelHelper.PET_BONES_MAP_PATH);
        mPetAvatar = new SXRAvatar(sxrContext, "PetModel");
        mPetAvatar.getEventReceiver().addListener(mAvatarListener);
        try
        {
            mPetAvatar.loadModel(new SXRAndroidResource(sxrContext, LoadModelHelper.PET_MODEL_PATH));
        }
        catch (IOException e)
        {
            e.printStackTrace();
            if (mLoadListener != null) {
                mLoadListener.onFailure();
            }
        }
    }

    @Override
    public void unload() {

    }

    /**
     * Sets the initial scale according to the distance between the pet and camera
     */
    public void setInitialScale() {
        Vector3f vectorDistance = new Vector3f();
        float[] modelCam = getSXRContext().getMainScene().getMainCameraRig().getTransform().getModelMatrix();
        float[] modelCharacter = getTransform().getModelMatrix();

        vectorDistance.set(modelCam[12], modelCam[13], modelCam[14]);
        // Calculates the distance in centimeters
        float factor = 0.5f * vectorDistance.distance(modelCharacter[12], modelCharacter[13], modelCharacter[14]);
        float scale = Math.max(ScaleGestureDetector.MIN_FACTOR, Math.min(factor, ScaleGestureDetector.MAX_FACTOR));

        scale(scale);
    }

    public SXRNode getGrabPivot() {
        if (mGrabbingPivot == null) {
            int i = mPetAvatar.getSkeleton().getBoneIndex(LoadModelHelper.PET_GRAB_PIVOT);
            if (!(i < 0) && i < mPetAvatar.getSkeleton().getNumBones()) {
                mGrabbingPivot = mPetAvatar.getSkeleton().getBone(i);
            }
        }

        return mGrabbingPivot;
    }

    public boolean isGrabbing(SXRNode item) {
        return item.getParent() == getGrabPivot();
    }

    public void grabItem(SXRNode item) {
        SXRNode pivot = getGrabPivot();

        if (pivot != null) {
            Matrix4f m = item.getTransform().getModelMatrix4f();
            Vector3f scale = new Vector3f();
            m.getScale(scale);

            if (item.getParent() != null) {
                item.getParent().removeChildObject(item);
            }

            item.getTransform().setRotation(1, 0, 0, 0);
            item.getTransform().setPosition(0, 0.3f, 20.0f);

            pivot.addChildObject(item);

            Matrix4f m2 = item.getTransform().getModelMatrix4f();
            Vector3f scale2 = new Vector3f();
            m2.getScale(scale2);
            scale.div(scale2);
            item.getTransform().setScale(item.getTransform().getScaleX() * scale.x,
                    item.getTransform().getScaleY() * scale.y,
                    item.getTransform().getScaleZ() * scale.z);

            BoundingVolume b = item.getBoundingVolume();
            Log.e("GRAB_ITEM", "Bounding volume=" + b.minCorner + " " + b.maxCorner + " " + b.radius);
        }
    }

    private void loadAnimations() {
        final SXRContext sxrContext = getSXRContext();
        int i = 0;
        try
        {
            for (i = 0; i < LoadModelHelper.PET_ANIMATIONS_PATH.length; i++) {
                SXRAndroidResource res = new SXRAndroidResource(sxrContext,
                        LoadModelHelper.PET_ANIMATIONS_PATH[i]);
                mPetAvatar.loadAnimation(res, mBoneMap);
            }
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
            Log.e(TAG, "Animation could not be loaded from "
                    + LoadModelHelper.PET_ANIMATIONS_PATH[i]);

            if (mLoadListener != null) {
                mLoadListener.onFailure();
            }
        }
    }

    private void setDefaultScaleAndPosition() {
        m3DModel.getTransform().setScale(PetConstants.MODEL3D_DEFAULT_SCALE,
                PetConstants.MODEL3D_DEFAULT_SCALE, PetConstants.MODEL3D_DEFAULT_SCALE);
        m3DModel.getTransform().setPosition(0, 0.2f, 0);
    }

    private SXRAvatar.IAvatarEvents mAvatarListener = new SXRAvatar.IAvatarEvents() {
        int contAnim = 0;
        @Override
        public void onAvatarLoaded(SXRAvatar avatar, SXRNode sxrNode, String s, String s1) {
            final SXRContext sxrContext = getSXRContext();
            Log.d(TAG, "onAvatarLoaded %s => %s", s, s1);

            if (sxrNode.getParent() == null) {
                sxrContext.runOnGlThread(new Runnable() {
                    public void run() {
                        SXRSkeleton skeleton = avatar.getSkeleton();

                        m3DModel = sxrNode;

                        //skeleton.createSkeletonGeometry(m3DModel);

                        SXRNode bone = skeleton.getBone(0);

                        if (bone.getParent() != null) bone.getParent().removeChildObject(bone);

                        m3DModel.addChildObject(bone);

                        setDefaultScaleAndPosition();

                        // Get the pet's body from 3D model
                        SXRNode body = m3DModel.getNodeByName(PET_COLLIDER);
                        if (body != null) {
                            // Create a mesh collider and attach it to the body
                            body.attachCollider(new SXRMeshCollider(mPetContext.getSXRContext(), true));
                        }
                        CharacterView.this.addChildObject(m3DModel);
                    }
                });
            }

            loadAnimations();
        }

        @Override
        public void onModelLoaded(SXRAvatar avatar, SXRNode sxrNode, String s, String s1) {
            Log.d(TAG, "onModelLoaded %s => %s", s, s1);
        }

        @Override
        public void onAnimationLoaded(SXRAvatar avatar, SXRAnimator animation, String s, String s1) {
            Log.d(TAG, "onAnimationLoaded  => %s", animation.getName());
            contAnim++;

            animation.setRepeatMode(SXRRepeatMode.REPEATED);
            animation.setSpeed(1f);
            /*
            if (!mPetAvatar.isRunning())
            {
                mPetAvatar.startAll(GVRRepeatMode.REPEATED);

            }*/
            //mPetAvatar.start(animation.getName());

            if (contAnim == LoadModelHelper.PET_ANIMATIONS_PATH.length) {
                if (mLoadListener != null) {
                    mLoadListener.onSuccess();
                }
            }
        }

        @Override
        public void onAnimationStarted(SXRAvatar avatar, SXRAnimator sxrAnimator) {
            Log.d(TAG, "onAnimationStarted");
        }

        @Override
        public void onAnimationFinished(SXRAvatar avatar, SXRAnimator animator) {
            Log.d(TAG, "onAnimationFinished");
        }
    };
}
