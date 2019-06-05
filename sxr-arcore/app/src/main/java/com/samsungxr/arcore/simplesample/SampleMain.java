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

package com.samsungxr.arcore.simplesample;

import android.view.MotionEvent;

import com.samsungxr.SXRActivity;
import com.samsungxr.SXRApplication;
import com.samsungxr.SXRBoxCollider;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRDirectLight;
import com.samsungxr.SXREventListeners;
import com.samsungxr.SXRLight;
import com.samsungxr.SXRMain;
import com.samsungxr.SXRMesh;
import com.samsungxr.SXRPicker;
import com.samsungxr.SXRPointLight;
import com.samsungxr.SXRScene;
import com.samsungxr.SXRNode;
import com.samsungxr.SXRTransform;
import com.samsungxr.ITouchEvents;
import com.samsungxr.mixedreality.SXRAnchor;
import com.samsungxr.mixedreality.SXRHitResult;
import com.samsungxr.mixedreality.SXRLightEstimate;
import com.samsungxr.mixedreality.SXRMixedReality;
import com.samsungxr.mixedreality.SXRPlane;
import com.samsungxr.mixedreality.SXRTrackingState;
import com.samsungxr.mixedreality.IAnchorEvents;
import com.samsungxr.mixedreality.IMixedReality;
import com.samsungxr.mixedreality.IPlaneEvents;
import com.samsungxr.mixedreality.IMixedRealityEvents;
import com.samsungxr.utility.Log;
import com.samsungxr.utility.VrAppSettings;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This sample illustrates how to load, place and move a 3D model
 * on a plane in the real world.
 */
public class SampleMain extends SXRMain
{
    private static String TAG = "SXR_ARCORE";
    private static float AR_TO_VR_SCALE = 100.0f;
    private static int MAX_VIRTUAL_OBJECTS = 20;

    private SXRContext mSXRContext;
    private SXRScene mainScene;
    private SXRMixedReality mixedReality;
    private SampleHelper helper;
    private DragHandler mTouchHandler;
    private List<SXRAnchor> mVirtualObjects;
    private int mVirtObjCount = 0;
    private SXRDirectLight mSceneLight;
    private SelectionHandler mSelector;
    private boolean mIsMonoscopic = false;
    private SXRAnchor mCurrentAnchor = null;


    /**
     * Initialize the MixedReality extension and
     * provide it with listeners for plane detection
     * and anchor tracking.
     *
     * A headlight is put in the scene to illuminate
     * objects the camera is pointed at.
     */
    @Override
    public void onInit(SXRContext ctx)
    {
        mIsMonoscopic = BuildConfig.APPLICATION_ID.endsWith("monoscopic");
        mSXRContext = ctx;
        mainScene = mSXRContext.getMainScene();
        helper = new SampleHelper();
        mTouchHandler = new DragHandler();
        mVirtualObjects = new ArrayList<>();
        mVirtObjCount = 0;
        mSceneLight = new SXRDirectLight(ctx);
        mainScene.getMainCameraRig().getHeadTransformObject().attachComponent(mSceneLight);
        mainScene.getMainCameraRig().setNearClippingDistance(0.1f * AR_TO_VR_SCALE);
        mainScene.getMainCameraRig().setFarClippingDistance(1000.0f * AR_TO_VR_SCALE);
        mixedReality = new SXRMixedReality(mainScene);
        mixedReality.getEventReceiver().addListener(planeEventsListener);
        mixedReality.getEventReceiver().addListener(mrEventsListener);
        mixedReality.setARToVRScale(AR_TO_VR_SCALE);
        mSelector = new SelectionHandler(ctx, mixedReality, mIsMonoscopic);
        mixedReality.resume();
    }


    /**
     * Loads a 3D model using the asset loaqder and attaches
     * a collider to it so it can be picked.
     * If you are using phone AR, the touch screen can
     * be used to drag, rotate or scale the object.
     * If you are using a headset, the controller
     * is used for picking and moving.
     */
    private SXRNode load3dModel(final SXRContext ctx) throws IOException
    {
        final SXRNode sceneObject = ctx.getAssetLoader().loadModel("objects/andy.obj");
        sceneObject.attachComponent(new SXRBoxCollider(ctx));
        sceneObject.getEventReceiver().addListener(mSelector);
        sceneObject.getTransform().setScale(AR_TO_VR_SCALE, AR_TO_VR_SCALE, AR_TO_VR_SCALE);
        return sceneObject;
    }

    /**
     * The mixed reality extension runs in the background and does
     * light estimation. Each frame the intensity of the ambient
     * lighting is adjusted based on that estimate.
     */
    @Override
    public void onStep()
    {
        super.onStep();
        SXRLightEstimate lightEstimate = mixedReality.getLightEstimate();
        if (lightEstimate != null)
        {
            float light = lightEstimate.getPixelIntensity();
            mSceneLight.setAmbientIntensity(light, light, light, 1);
            mSceneLight.setDiffuseIntensity(0.4f, 0.4f, 0.4f, 1);
            mSceneLight.setSpecularIntensity(0.2f, 0.2f, 0.2f, 1);
        }
    }


    /**
     * Load a 3D model and place it in the virtual world
     * at the given position. The pose is a 4x4 matrix
     * giving the real world position/orientation of
     * the object. We create an anchor (and a corresponding
     * node) to link the real and virtual pose together.
     * The node attached to the anchor will be moved and
     * oriented by the framework, anything you do
     * to the transform of this node will be discarded
     * (which is why we scale/rotate the child instead).
     * @param pose
     */
    public boolean addVirtualObject(SXRPlane plane, float[] pose)
    {
        if (mVirtObjCount >= MAX_VIRTUAL_OBJECTS)
        {
            return false;
        }
        try
        {
            SXRNode andy = load3dModel(getSXRContext());
            SXRNode anchorObj = new SXRNode(mSXRContext);

            if (plane != null)
            {
                mCurrentAnchor = plane.createAnchor(pose, anchorObj);
            }
            else
            {
                mCurrentAnchor = mixedReality.createAnchor(pose, anchorObj);
            }
            anchorObj.addChildObject(andy);
            mVirtualObjects.add(mCurrentAnchor);
            mainScene.addNode(anchorObj);
            mVirtObjCount++;
            return true;
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
            Log.e(TAG, ex.getMessage());
            return false;
        }
    }

    private void processHit(MotionEvent e)
    {
        float x = e.getRawX();
        float y = e.getRawY();
        if (!mIsMonoscopic)
        {
            x /= 2;
        }
        SXRHitResult hit = mixedReality.hitTest(x, y);
        if (hit != null)
        {
            addVirtualObject(null, hit.getPose());
        }
    }

    private IMixedRealityEvents mrEventsListener = new IMixedRealityEvents()
    {
        /**
         * Get the depth of the touch screen in the 3D world
         * and give it to the cursor controller so touch
         * events will be handled properly.
         */
        @Override
        public void onMixedRealityStart(IMixedReality mr)
        {
            float screenDepth = mIsMonoscopic ? mr.getScreenDepth() : 0;
            mr.getPassThroughObject().getEventReceiver().addListener(mTouchHandler);
            mr.setPlaneFindingMode(SXRMixedReality.PlaneFindingMode.HORIZONTAL);
            helper.initCursorController(mSXRContext, mTouchHandler, screenDepth);
        }

        @Override
        public void onMixedRealityStop(IMixedReality mr) { }

        @Override
        public void onMixedRealityUpdate(IMixedReality mr) { }

        public void onMixedRealityError(IMixedReality mr, String errmsg) { }
    };

    /**
     * The plane events listener handles plane detection events.
     * It also handles initialization and shutdown.
     */
    private IPlaneEvents planeEventsListener = new IPlaneEvents()
    {
        /**
         * Place a transparent quad in the 3D scene to indicate
         * horizontally upward planes (floor, table top).
         * We don't need colliders on these since they are
         * not pickable.
          */
        @Override
        public void onPlaneDetected(SXRPlane plane)
        {
            if (plane.getPlaneType() == SXRPlane.Type.HORIZONTAL_UPWARD_FACING)
            {
                SXRNode planeNode = helper.createPlaneNode(getSXRContext());
                float[] pose = new float[16];
                SXRMesh mesh = new SXRMesh(getSXRContext());

                mesh.setVertices(plane.get3dPolygonAsArray());
                planeNode.getRenderData().setMesh(mesh);
                plane.getCenterPose(pose);
                planeNode.attachComponent(plane);
                mainScene.addNode(planeNode);
            }
        }

        /**
         * Show/hide the 3D plane node based on whether it
         * is being tracked or not.
         */
        @Override
        public void onPlaneStateChange(SXRPlane plane, SXRTrackingState state)
        {
            if (plane.getPlaneType() == SXRPlane.Type.HORIZONTAL_UPWARD_FACING)
            {
                plane.setEnable(state == SXRTrackingState.TRACKING);
            }
            else
            {
                plane.setEnable(false);
            }
        }

        @Override
        public void onPlaneMerging(SXRPlane parent, SXRPlane child)
        {
            SXRNode childOwner = child.getOwnerObject();
            if (childOwner != null)
            {
                childOwner.detachComponent(SXRPlane.getComponentType());
                childOwner.getParent().removeChildObject(childOwner);
            }
        }

        @Override
        public void onPlaneGeometryChange(SXRPlane plane)
        {
            SXRNode owner = plane.getOwnerObject();

            if (owner != null)
            {
                SXRMesh mesh = new SXRMesh(getSXRContext());
                mesh.setVertices(plane.get3dPolygonAsArray());
                owner.getRenderData().setMesh(mesh);
            }
        }
    };


    /**
     * Handles selection hilighting, rotation and scaling
     * of currently selected 3D object.
     * A light attached to the parent of the
     * selected 3D object is used for hiliting it.
     * The root of the hierarchy can be rotated or scaled.
     */
    static public class SelectionHandler implements ITouchEvents
    {
        static final int DRAG = 1;
        static final int SCALE_ROTATE = -1;
        static final int UNTOUCHED = 0;
        static private SXRNode mSelected = null;
        private int mSelectionMode = UNTOUCHED;
        private IMixedReality mMixedReality;
        private float mHitY;
        private float mHitX;
        private boolean mIsMonoscopic;

        public SelectionHandler(SXRContext ctx, IMixedReality mr, boolean mono)
        {
            super();
            mIsMonoscopic = mono;
            mMixedReality = mr;
        }

        public static SXRNode getSelected() { return mSelected; }

        /*
         * When entering an anchored object, it is hilited by
         * adding a point light under its parent.
         */
        public void onEnter(SXRNode target, SXRPicker.SXRPickedObject pickInfo) { }

        public void onExit(SXRNode sceneObj, SXRPicker.SXRPickedObject pickInfo) { }

        /*
         * The color of the selection light changes when the object is being dragged.
         * If another object is already selected, ignore the touch event.
         */
        public void onTouchStart(SXRNode sceneObj, SXRPicker.SXRPickedObject pickInfo)
        {
            if (pickInfo.motionEvent == null)
            {
                return;
            }
            if (mSelected == null)
            {
                float x = pickInfo.motionEvent.getRawX();
                float y = pickInfo.motionEvent.getRawY();
                if (!mIsMonoscopic)
                {
                    x /= 2;
                }
                startTouch(sceneObj, x, y, SCALE_ROTATE);
            }
        }

        public void onTouchEnd(SXRNode sceneObj, SXRPicker.SXRPickedObject pickInfo) { }

        public void onInside(SXRNode sceneObj, SXRPicker.SXRPickedObject pickInfo) { }

        public void onMotionOutside(SXRPicker picker, MotionEvent event) { }

        /*
         * Rotate and scale the object relative to its current state.
         * The node being rotated / scaled is a child
         * of the anchored object (which is being oriented and positioned
         * by MixedReality).
         */
        private void scaleRotate(float rotateDelta, float scaleDelta)
        {
            SXRNode selected = getSelected();
            SXRTransform t = selected.getTransform();
            float scale = t.getScaleX();
            Quaternionf q = new Quaternionf();
            Vector3f ea = new Vector3f();
            float angle = rotateDelta / 50.0f;

            /*
             * rotate about Y axis
             */
            q.set(t.getRotationX(), t.getRotationY(), t.getRotationZ(), t.getRotationW());
            q.getEulerAnglesXYZ(ea);
            q.rotateAxis(angle, 0, 1, 0);

            /*
             * scale the model
             */
            scale += scaleDelta;
            if (scale < 0.5f * AR_TO_VR_SCALE)
            {
                scale = 0.5f * AR_TO_VR_SCALE;
            }
            else if (scale > 50.0f * AR_TO_VR_SCALE)
            {
                scale = 50.0f * AR_TO_VR_SCALE;
            }
            t.setRotation(q.w, q.x, q.y, q.z);
            t.setScale(scale, scale, scale);
        }

        private void drag(float x, float y)
        {
            SXRAnchor anchor = (SXRAnchor) mSelected.getParent().getComponent(SXRAnchor.getComponentType());

            if (anchor != null)
            {
                SXRHitResult hit = mMixedReality.hitTest(x, y);

                if (hit != null)
                {                           // move the object to a new position
                    mMixedReality.updateAnchorPose(anchor, hit.getPose());
                }
            }
        }

        public void update(MotionEvent event)
        {
            float x = event.getRawX();
            float y = event.getRawY();

            if (!mIsMonoscopic)
            {
                x /= 2;
            }
            if (mSelectionMode == SCALE_ROTATE)
            {
                float dx = (x - mHitX) / 100.0f;
                float dy = (y - mHitY) / 100.0f;
                scaleRotate(dx, dy);
            }
            else if (mSelectionMode == DRAG)
            {
                drag(x, y);
            }
        }

        public void startTouch(SXRNode sceneObj, float hitx, float hity, int mode)
        {
            mSelectionMode = mode;
            mSelected = sceneObj;

            mHitX = hitx;
            mHitY = hity;
        }

        public void endTouch()
        {
            mSelected = null;
            mSelectionMode = UNTOUCHED;
        }
    }


    /**
     * Handles touch events for the screen
     * (those not inside 3D anchored objects).
     * If phone AR is being used with passthru video,
     * the object displaying the camera output also
     * has a collider and is touchable.
     * This is how picking is handled when using
     * the touch screen.
     *
     * Tapping the screen or clicking on a plane
     * will cause a 3D object to be placed there.
     * Dragging with the controller or your finger
     * inside the object will scale it (Y direction)
     * and rotate it (X direction). Dragging outside
     * a 3D object will drag the currently selected
     * object (the last one you added/manipulated).
     */
    public class DragHandler extends SXREventListeners.TouchEvents
    {

        @Override
        public void onTouchStart(SXRNode sceneObj, SXRPicker.SXRPickedObject pickInfo)
        { }

        @Override
        public void onTouchEnd(SXRNode sceneObj, SXRPicker.SXRPickedObject pickInfo)
        {
            if (SelectionHandler.getSelected() != null)
            {
                mSelector.endTouch();
            }
            else
            {
                SXRAnchor anchor = findAnchorNear(pickInfo.hitLocation[0],
                                                  pickInfo.hitLocation[1],
                                                  pickInfo.hitLocation[2],
                                                  5.0f * AR_TO_VR_SCALE);
                if (anchor == null)
                {
                    processHit(pickInfo.motionEvent);
                }
            }
        }

        public void onInside(SXRNode sceneObj, SXRPicker.SXRPickedObject pickInfo)
        {
            SXRNode selected = mSelector.getSelected();

            if (pickInfo.motionEvent == null)
            {
                return;
            }
            if (selected != null)       // is a 3D object selected?
            {
                mSelector.update(pickInfo.motionEvent);
            }
            else
            {
                SXRAnchor anchor = findAnchorNear(pickInfo.hitLocation[0],
                                                  pickInfo.hitLocation[1],
                                                  pickInfo.hitLocation[2],
                                                  3.0f * AR_TO_VR_SCALE);
                if (anchor != null)
                {
                    float x = pickInfo.motionEvent.getRawX();
                    float y = pickInfo.motionEvent.getRawY();
                    if (!mIsMonoscopic)
                    {
                        x /= 2;
                    }
                    selected = anchor.getOwnerObject();
                    mSelector.startTouch(selected.getChildByIndex(0), x, y, SelectionHandler.DRAG);
                }
            }
        }


        /**
         * Look for a 3D object in the scene near the given position.
         * Used ro prevent objects from being placed too close together.
         */
        private SXRAnchor findAnchorNear(float x, float y, float z, float maxdist)
        {
            Matrix4f anchorMtx = new Matrix4f();
            Vector3f v = new Vector3f();
            for (SXRAnchor anchor : mVirtualObjects)
            {
                float[] anchorPose = anchor.getPose();
                anchorMtx.set(anchorPose);
                anchorMtx.getTranslation(v);
                v.x -= x;
                v.y -= y;
                v.z -= z;
                float d = v.length();
                if (d < maxdist)
                {
                    return anchor;
                }
            }
            return null;
        }
    }

}