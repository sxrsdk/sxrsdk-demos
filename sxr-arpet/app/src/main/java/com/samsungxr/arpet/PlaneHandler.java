/*
 * Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.samsungxr.arpet;

import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRBoxCollider;
import com.samsungxr.SXRComponent;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRDrawFrameListener;
import com.samsungxr.SXRMaterial;
import com.samsungxr.SXRMesh;
import com.samsungxr.SXRMeshCollider;
import com.samsungxr.SXRNode;
import com.samsungxr.SXRScene;
import com.samsungxr.SXRShaderId;
import com.samsungxr.SXRTexture;
import com.samsungxr.SXRTextureParameters;
import com.samsungxr.arpet.shaders.SXRTiledMaskShader;
import com.samsungxr.arpet.util.EventBusUtils;
import com.samsungxr.mixedreality.IPlaneEvents;
import com.samsungxr.mixedreality.SXRPlane;
import com.samsungxr.mixedreality.SXRTrackingState;
import com.samsungxr.nodes.SXRCubeNode;
import com.samsungxr.physics.SXRRigidBody;
import com.samsungxr.utility.Log;

import java.util.LinkedList;

public final class PlaneHandler implements IPlaneEvents, SXRDrawFrameListener {
    private final String TAG = PlaneHandler.class.getSimpleName();

    private SXRContext mContext;
    private SXRScene mScene;
    private PetMain mPetMain;

    private SXRNode selectedPlaneObject = null;
    private PlaneBoard physicsPlane = null;
    public final static String PLANE_NAME = "Plane";
    public final static String PLANE_PHYSICS = "Plane Physics";
    public final static String PLANE_COLLIDER = "Plane Collider";

    // FIXME: move this to a utils or helper class
    private static long newComponentType(Class<? extends SXRComponent> clazz) {
        long hash = (long) clazz.hashCode() << 32;
        long t = System.currentTimeMillis() & 0xfffffff;
        return t | hash;
    }

    private static long PLANEBOARD_COMP_TYPE = newComponentType(PlaneBoard.class);

    // This will create an invisible board in which the static body will be attached. This board
    // will "follow" the A.R. plane that owns it so that it will work as if this plane has physics
    // attached to it.
    private final class PlaneBoard extends SXRComponent {
        private SXRNode physicsObject;

        PlaneBoard(SXRContext sxrContext) {
            super(sxrContext, 0);

            mType = PLANEBOARD_COMP_TYPE;

            physicsObject = new SXRNode(sxrContext);

            SXRBoxCollider collider = new SXRBoxCollider(sxrContext);
            collider.setHalfExtents(0.5f, 0.5f, 0.5f);
            physicsObject.attachComponent(collider);
            // To touch debug
            physicsObject.setName(PLANE_PHYSICS);

            // Uncomment if you want a green box to appear at the center of the invisible board.
            // Notice this green box is smaller than the board
            final boolean debugPhysics = false;
            if (debugPhysics) {
                SXRMaterial green = new SXRMaterial(sxrContext, SXRMaterial.SXRShaderType.Phong.ID);
                green.setDiffuseColor(0f, 1f, 0f, 1f);
                SXRCubeNode mark = new SXRCubeNode(sxrContext, true);
                mark.getRenderData().setMaterial(green);
                mark.getRenderData().setAlphaBlend(true);
                physicsObject.addChildObject(mark);
            }
        }

        @Override
        public void onAttach(SXRNode newOwner) {
            super.onAttach(newOwner);
            mScene.addNode(physicsObject);
        }

        @Override
        public void onDetach(SXRNode oldOwner) {
            super.onDetach(oldOwner);
            mScene.removeNode(physicsObject);
        }

        private void setBoxTransform() {
            float[] targetMtx = owner.getTransform().getModelMatrix();
            physicsObject.getTransform().setModelMatrix(targetMtx);
            physicsObject.getTransform().setScaleZ(1f);
        }

        void update() {
            if (!isEnabled()) {
                return;
            }

            setBoxTransform();

            SXRRigidBody board = (SXRRigidBody) physicsObject.getComponent(SXRRigidBody.getComponentType());
            if (board == null) {
                board = new SXRRigidBody(mContext, 0f);
                board.setRestitution(0.5f);
                board.setFriction(1.0f);
                board.setCcdMotionThreshold(0.001f);
                board.setCcdSweptSphereRadius(2f);
                physicsObject.attachComponent(board);
            }

            // This will update rigid body according to owner's transform
            board.reset(false);
        }
    }

    private LinkedList<SXRPlane> mPlanes = new LinkedList<>();

    PlaneHandler(PetMain petMain, PetContext petContext) {
        mContext = petContext.getSXRContext();
        mScene = petContext.getMainScene();
        mPetMain = petMain;

        physicsPlane = new PlaneBoard(mContext);
    }

    private SXRNode createQuadPlane() {
        SXRTextureParameters texParams = new SXRTextureParameters(mContext);
        SXRTexture tex = mContext.getAssetLoader().loadTexture(
                new SXRAndroidResource(mContext, R.drawable.gramado_01));
        SXRMaterial material = new SXRMaterial(mContext, new SXRShaderId(SXRTiledMaskShader.class));
        texParams.setWrapSType(SXRTextureParameters.TextureWrapType.GL_MIRRORED_REPEAT);
        texParams.setWrapTType(SXRTextureParameters.TextureWrapType.GL_MIRRORED_REPEAT);
        tex.updateTextureParameters(texParams);
        material.setMainTexture(tex);

        SXRMesh mesh = SXRMesh.createQuad(mContext, "float3 a_position", 1, 1);
        SXRNode polygonObject = new SXRNode(mContext, mesh, material);
        polygonObject.setName(PLANE_COLLIDER);

        polygonObject.getRenderData().disableLight();
        polygonObject.getRenderData().setAlphaBlend(true);
        polygonObject.getTransform().setRotationByAxis(-90, 1, 0, 0);

        // FIXME: BoxCollider doesn't work!
        polygonObject.attachCollider(new SXRMeshCollider(mContext, true));

        SXRNode transformNode = new SXRNode(mContext);
        transformNode.addChildObject(polygonObject);
        return transformNode;
    }

    @Override
    public void onPlaneDetected(SXRPlane plane) {
        SXRPlane.Type planeType = plane.getPlaneType();

        // Don't use planes that are downward facing (e.g ceiling)
        if (planeType == SXRPlane.Type.HORIZONTAL_DOWNWARD_FACING) {
            return;
        }

        SXRNode planeGeo = createQuadPlane();

        planeGeo.attachComponent(plane);
        plane.getSXRContext().getMainScene().addNode(planeGeo);
        plane.getOwnerObject().setEnable(selectedPlaneObject == null);

        mPlanes.add(plane);

        // Show message by tapping the plan
        if (selectedPlaneObject == null) {
            EventBusUtils.post(plane);
        }
    }

    @Override
    public void onPlaneStateChange(SXRPlane plane, SXRTrackingState trackingState) {
        if (trackingState != SXRTrackingState.TRACKING) {
            plane.setEnable(false);
        } else {
            plane.setEnable(true);
        }
    }

    @Override
    public void onPlaneMerging(SXRPlane childPlane, SXRPlane parentPlane) {
        // Will remove PlaneBoard from childPlane because this plane is not needed anymore now
        // that parentPlane "contains" childPlane
        //childPlane.getOwnerObject().detachComponent(PLANEBOARD_COMP_TYPE);
        mPlanes.remove(childPlane);
    }

    @Override
    public void onPlaneGeometryChange(SXRPlane sxrPlane) {
        if (sxrPlane.getOwnerObject() != null) {
            SXRNode quad = sxrPlane.getOwnerObject().getChildByIndex(0);
            if (quad != null) {
                quad.getTransform().setScale(
                        sxrPlane.getWidth() * 0.9f,
                        sxrPlane.getHeight() * 0.9f,
                        1f);
            }
        }
    }

    public void setSelectedPlane(SXRPlane mainPlane, SXRNode visibleColliderPlane) {
        for (SXRPlane plane : mPlanes) {
            if (plane != mainPlane) {
                plane.getOwnerObject().setEnable(mainPlane == null);
            }
        }

        if (selectedPlaneObject != null) {
            selectedPlaneObject.detachComponent(PLANEBOARD_COMP_TYPE);
        }

        if (mainPlane != null) {
            selectedPlaneObject = visibleColliderPlane;
            selectedPlaneObject.attachComponent(physicsPlane);
            selectedPlaneObject.setName(PLANE_NAME);
            EventBusUtils.post(new PlaneDetectedEvent(mainPlane));
            mContext.registerDrawFrameListener(this);
        } else {
            mContext.unregisterDrawFrameListener(this);
            selectedPlaneObject = null;
        }
    }

    public SXRNode getSelectedPlane() {
        return selectedPlaneObject;
    }

    public void resetPlanes() {
        Log.d(TAG, "resetting planes");
        if (selectedPlaneObject != null) {
            selectedPlaneObject.detachComponent(PLANEBOARD_COMP_TYPE);
            selectedPlaneObject = null;
        }

        for (SXRPlane plane : mPlanes) {
            SXRNode ownerObject = plane.getOwnerObject();
            if (ownerObject != null) {
                ownerObject.setEnable(true);
                // Just to notify as a new plane detected
                EventBusUtils.post(plane);
            }
        }
        mContext.unregisterDrawFrameListener(this);
    }

    @Override
    public void onDrawFrame(float t) {
        if (selectedPlaneObject != null) {
            ((PlaneBoard) selectedPlaneObject.getComponent(PLANEBOARD_COMP_TYPE)).update();
        }
    }
}
