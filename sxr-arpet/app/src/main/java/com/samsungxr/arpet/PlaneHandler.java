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

import android.graphics.Color;

import com.samsungxr.SXRBoxCollider;
import com.samsungxr.SXRComponent;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRDrawFrameListener;
import com.samsungxr.SXRMaterial;
import com.samsungxr.SXRMesh;
import com.samsungxr.SXRMeshCollider;
import com.samsungxr.SXRNode;
import com.samsungxr.SXRRenderData;
import com.samsungxr.SXRScene;
import com.samsungxr.mixedreality.IMixedReality;
import com.samsungxr.mixedreality.IPlaneEvents;
import com.samsungxr.mixedreality.SXRPlane;
import com.samsungxr.mixedreality.SXRTrackingState;
import com.samsungxr.nodes.SXRCubeNode;
import com.samsungxr.physics.SXRRigidBody;
import com.samsungxr.utility.Log;

import com.samsungxr.arpet.util.EventBusUtils;

import java.util.LinkedList;

public final class PlaneHandler implements IPlaneEvents, SXRDrawFrameListener {
    private final String TAG = PlaneHandler.class.getSimpleName();

    private SXRContext mContext;
    private SXRScene mScene;
    private PetMain mPetMain;
    private int hsvHUE = 0;

    private boolean planeDetected = false;
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

    private IMixedReality mixedReality;

    PlaneHandler(PetMain petMain, PetContext petContext) {
        mContext = petContext.getSXRContext();
        mScene = petContext.getMainScene();
        mPetMain = petMain;

        physicsPlane = new PlaneBoard(mContext);
    }

    private SXRNode createQuadPlane() {
        SXRMesh mesh = SXRMesh.createQuad(mContext, "float3 a_position", 1, 1);
        SXRMaterial mat = new SXRMaterial(mContext, SXRMaterial.SXRShaderType.Phong.ID);
        SXRNode polygonObject = new SXRNode(mContext, mesh, mat);
        polygonObject.setName(PLANE_COLLIDER);

        hsvHUE += 35;
        float[] hsv = new float[3];
        hsv[0] = hsvHUE % 360;
        hsv[1] = 1f;
        hsv[2] = 1f;

        int c = Color.HSVToColor(50, hsv);
        mat.setDiffuseColor(Color.red(c) / 255f, Color.green(c) / 255f,
                Color.blue(c) / 255f, 0.5f);
        polygonObject.getRenderData().setMaterial(mat);
        polygonObject.getRenderData().disableLight();
        polygonObject.getRenderData().setRenderingOrder(SXRRenderData.SXRRenderingOrder.TRANSPARENT);
        polygonObject.getRenderData().setAlphaBlend(true);
        polygonObject.getTransform().setRotationByAxis(-90, 1, 0, 0);
        // FIXME: BoxCollider doesn't work!
        polygonObject.attachCollider(new SXRMeshCollider(mContext, true));
        // See setSelectedPlane(...) will set the touched visible quad as selected plane

        SXRNode transformNode = new SXRNode(mContext);
        transformNode.addChildObject(polygonObject);
        return transformNode;
    }

    private boolean updatePlanes = true;

    /*
     * ARCore session guaranteed to be initialized here.
     */
    @Override
    public void onStartPlaneDetection(IMixedReality mr)
    {
        mixedReality = mr;
        mPetMain.onARInit(mContext, mr);
    }

    @Override
    public void onStopPlaneDetection(IMixedReality mr) { }

    @Override
    public void onPlaneDetected(SXRPlane plane) {
        SXRPlane.Type planeType = plane.getPlaneType();

        // Don't use planes that are downward facing, e.g ceiling
        if (planeType == SXRPlane.Type.HORIZONTAL_DOWNWARD_FACING || selectedPlaneObject != null) {
            return;
        }
        SXRNode planeGeo = createQuadPlane();

        planeGeo.attachComponent(plane);
//        mScene.addSceneObject(planeGeo);
        plane.getSXRContext().getMainScene().addNode(planeGeo);


        mPlanes.add(plane);

        if (!planeDetected && planeType == SXRPlane.Type.HORIZONTAL_UPWARD_FACING) {
            planeDetected = true;

            // Now physics starts working and then boards must be continuously updated

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

    public void setSelectedPlane(SXRPlane mainPlane, SXRNode visibleColliderPlane) {
        for (SXRPlane plane: mPlanes) {
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

    public void reset() {
        Log.d(TAG, "reseting planes");
        if (selectedPlaneObject != null) {
            selectedPlaneObject.detachComponent(PLANEBOARD_COMP_TYPE);
            selectedPlaneObject = null;
        }

        for (SXRPlane plane : mPlanes) {
            mScene.removeNode(plane.getOwnerObject());
        }
        mPlanes.clear();
        mContext.unregisterDrawFrameListener(this);
        planeDetected = false;
    }

    @Override
    public void onDrawFrame(float t) {
        if (selectedPlaneObject != null) {
            ((PlaneBoard) selectedPlaneObject.getComponent(PLANEBOARD_COMP_TYPE)).update();
        }
    }
}
