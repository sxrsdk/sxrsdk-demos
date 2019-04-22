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
        mixedReality = new SXRMixedReality(mainScene);
        mixedReality.getEventReceiver().addListener(planeEventsListener);
        mixedReality.getEventReceiver().addListener(anchorEventsListener);
        mixedReality.getEventReceiver().addListener(mrEventsListener);
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
        sceneObject.getTransform().setScale(50, 50, 50);
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
    public void addVirtualObject(SXRPlane plane, float[] pose)
    {
        if (mVirtObjCount >= MAX_VIRTUAL_OBJECTS)
        {
            return;
        }
        try
        {
            SXRNode andy = load3dModel(getSXRContext());
            SXRAnchor anchor;
            SXRNode anchorObj = new SXRNode(mSXRContext);

            if (plane != null)
            {
                anchor = plane.createAnchor(pose, anchorObj);
            }
            else
            {
                anchor = mixedReality.createAnchor(pose, anchorObj);
            }
            anchorObj.addChildObject(andy);
            mVirtualObjects.add(anchor);
            mainScene.addNode(anchorObj);
            mVirtObjCount++;
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
            Log.e(TAG, ex.getMessage());
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
            float screenDepth = mr.getScreenDepth();
            mr.getPassThroughObject().getEventReceiver().addListener(mTouchHandler);
            helper.initCursorController(mSXRContext, mTouchHandler, screenDepth);
            mr.setPlaneFindingMode(SXRMixedReality.PlaneFindingMode.HORIZONTAL);
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
        public void onPlaneStateChange(SXRPlane SXRPlane, SXRTrackingState state)
        {
            SXRPlane.setEnable(state == SXRTrackingState.TRACKING);
        }

        @Override
        public void onPlaneMerging(SXRPlane SXRPlane, SXRPlane SXRPlane1) { }

        @Override
        public void onPlaneGeometryChange(SXRPlane plane)
        {
            SXRMesh mesh = new SXRMesh(getSXRContext());
            mesh.setVertices(plane.get3dPolygonAsArray());

            plane.getOwnerObject().getRenderData().setMesh(mesh);
        }
    };

    /**
     * Show/hide the 3D node associated with the anchor
     * based on whether it is being tracked or not.
     */
    private IAnchorEvents anchorEventsListener = new IAnchorEvents()
    {
        @Override
        public void onAnchorStateChange(SXRAnchor SXRAnchor, SXRTrackingState state)
        {
            SXRAnchor.setEnable(state == SXRTrackingState.TRACKING);
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
        private final float[] PICKED_COLOR = {0.4f, 0.6f, 0, 1.0f};
        private final float[] UPDATE_COLOR = {0.6f, 0, 0.4f, 1.0f};
        private final float[] DRAG_COLOR = {0, 0.6f, 0.4f, 1.0f};
        private SXRNode mSelectionLight;
        private IMixedReality mMixedReality;
        private float mHitY;
        private float mHitX;
        private boolean mIsMonoscopic;

        public SelectionHandler(SXRContext ctx, IMixedReality mr, boolean mono)
        {
            super();
            mIsMonoscopic = mono;
            mMixedReality = mr;
            mSelectionLight = new SXRNode(ctx);
            mSelectionLight.setName("SelectionLight");
            SXRPointLight light = new SXRPointLight(ctx);
            light.setSpecularIntensity(0.1f, 0.1f, 0.1f, 0.1f);
            mSelectionLight.attachComponent(light);
            mSelectionLight.getTransform().setPositionZ(1.0f);
        }

        public static SXRNode getSelected() { return mSelected; }

        /*
         * When entering an anchored object, it is hilited by
         * adding a point light under its parent.
         */
        public void onEnter(SXRNode target, SXRPicker.SXRPickedObject pickInfo)
        {
            if (mSelected != null)
            {
                return;
            }
            SXRPointLight light =
                (SXRPointLight) mSelectionLight.getComponent(SXRLight.getComponentType());
            light.setDiffuseIntensity(PICKED_COLOR[0],
                                      PICKED_COLOR[1],
                                      PICKED_COLOR[1],
                                      PICKED_COLOR[2]);
            SXRNode lightParent = mSelectionLight.getParent();
            SXRNode targetParent = target.getParent();

            if (lightParent != null)
            {
                if (lightParent != targetParent)
                {
                    lightParent.removeChildObject(mSelectionLight);
                    targetParent.addChildObject(mSelectionLight);
                    mSelectionLight.getComponent(SXRLight.getComponentType()).enable();
                }
                else
                {
                    mSelectionLight.getComponent(SXRLight.getComponentType()).enable();
                }
            }
            else
            {
                targetParent.addChildObject(mSelectionLight);
                mSelectionLight.getComponent(SXRLight.getComponentType()).enable();
            }
        }

        /*
         * When the object is no longer selected, its selection light is disabled.
         */
        public void onExit(SXRNode sceneObj, SXRPicker.SXRPickedObject pickInfo)
        {
            if ((mSelected == sceneObj) || (mSelected == null))
            {
                mSelectionLight.getComponent(SXRLight.getComponentType()).disable();
                mSelected = null;
            }
        }

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
                float x = pickInfo.motionEvent.getX();
                float y = pickInfo.motionEvent.getY();
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
            float angle = rotateDelta / 10.0f;

            /*
             * rotate about Y axis
             */
            q.set(t.getRotationX(), t.getRotationY(), t.getRotationZ(), t.getRotationW());
            q.getEulerAnglesXYZ(ea);
            q.rotateAxis(angle, 0, 1, 0);

            /*
             * scale the model
             */
            scale += scaleDelta / 20.0f;
            if (scale < 0.1f)
            {
                scale = 0.1f;
            }
            else if (scale > 50.0f)
            {
                scale = 50.0f;
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

        public void update(SXRPicker.SXRPickedObject pickInfo)
        {
            float x = pickInfo.motionEvent.getX();
            float y = pickInfo.motionEvent.getY();

            if (!mIsMonoscopic)
            {
                x /= 2;
                y /= 2;
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
            SXRPointLight light =
                (SXRPointLight) mSelectionLight.getComponent(SXRLight.getComponentType());
            mSelectionMode = mode;
            mSelected = sceneObj;
            if (mode == DRAG)
            {
                light.setDiffuseIntensity(DRAG_COLOR[0],
                                          DRAG_COLOR[1],
                                          DRAG_COLOR[1],
                                          DRAG_COLOR[2]);
            }
            else
            {
                light.setDiffuseIntensity(UPDATE_COLOR[0],
                                          UPDATE_COLOR[1],
                                          UPDATE_COLOR[1],
                                          UPDATE_COLOR[2]);
            }
            mHitX = hitx;
            mHitY = hity;
        }

        public void endTouch()
        {
            SXRPointLight light =
                (SXRPointLight) mSelectionLight.getComponent(SXRLight.getComponentType());
            light.setDiffuseIntensity(PICKED_COLOR[0],
                                      PICKED_COLOR[1],
                                      PICKED_COLOR[1],
                                      PICKED_COLOR[2]);
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
                                                  300);
                if (anchor != null)
                {
                    return;
                }
                float x = pickInfo.motionEvent.getX();
                float y = pickInfo.motionEvent.getY();
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
        }

        public void onInside(SXRNode sceneObj, SXRPicker.SXRPickedObject pickInfo)
        {
            SXRNode selected = mSelector.getSelected();

            if (pickInfo.motionEvent == null)
            {
                return;
            }
            if (pickInfo.touched)           // currently touching an object?
            {
                if (selected != null)       // is a 3D object selected?
                {
                    mSelector.update(pickInfo);
                }
                else
                {
                    SXRAnchor anchor = findAnchorNear(pickInfo.hitLocation[0],
                                                      pickInfo.hitLocation[1],
                                                      pickInfo.hitLocation[2],
                                                      150);
                    if (anchor != null)
                    {
                        float x = pickInfo.motionEvent.getX();
                        float y = pickInfo.motionEvent.getY();
                        if (!mIsMonoscopic)
                        {
                            x /= 2;
                        }
                        selected = anchor.getOwnerObject();
                        mSelector.startTouch(selected.getChildByIndex(0), x, y, SelectionHandler.DRAG);
                    }
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