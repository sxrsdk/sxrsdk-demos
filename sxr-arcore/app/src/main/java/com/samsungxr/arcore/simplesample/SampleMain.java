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

import android.util.Log;

import com.samsungxr.SXRContext;
import com.samsungxr.SXREventListeners;
import com.samsungxr.SXRMain;
import com.samsungxr.SXRPicker;
import com.samsungxr.SXRScene;
import com.samsungxr.SXRSceneObject;
import com.samsungxr.mixedreality.SXRAnchor;
import com.samsungxr.mixedreality.SXRHitResult;
import com.samsungxr.mixedreality.SXRMixedReality;
import com.samsungxr.mixedreality.SXRPlane;
import com.samsungxr.mixedreality.SXRTrackingState;
import com.samsungxr.mixedreality.IAnchorEventsListener;
import com.samsungxr.mixedreality.IPlaneEventsListener;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;


public class SampleMain extends SXRMain {
    private static String TAG = "SXR_ARCORE";
    private static int MAX_VIRTUAL_OBJECTS = 20;

    private SXRContext mSXRContext;
    private SXRScene mainScene;

    private SXRMixedReality mixedReality;
    private SampleHelper helper;
    private TouchHandler mTouchHandler;



    private List<SXRAnchor> mVirtualObjects;
    private int mVirtObjCount = 0;


    @Override
    public void onInit(SXRContext gvrContext) {
        mSXRContext = gvrContext;
        mainScene = mSXRContext.getMainScene();
        helper = new SampleHelper();
        mTouchHandler = new TouchHandler();
        mVirtualObjects = new ArrayList<>() ;
        mVirtObjCount = 0;

        helper.initCursorController(gvrContext, mTouchHandler);


        mixedReality = new SXRMixedReality(gvrContext, mainScene);
        mixedReality.registerPlaneListener(planeEventsListener);
        mixedReality.registerAnchorListener(anchorEventsListener);
        mixedReality.resume();

    }

    @Override
    public void onStep() {
        super.onStep();
        for (SXRAnchor anchor: mVirtualObjects) {
            for (SXRSceneObject obj: anchor.getChildren()) {
                ((VirtualObject)obj).reactToLightEnvironment(
                        mixedReality.getLightEstimate().getPixelIntensity());
            }
        }
    }

    private IPlaneEventsListener planeEventsListener = new IPlaneEventsListener() {
        @Override
        public void onPlaneDetection(SXRPlane gvrPlane) {
            gvrPlane.setSceneObject(helper.createQuadPlane(getSXRContext()));
            mainScene.addSceneObject(gvrPlane);
        }

        @Override
        public void onPlaneStateChange(SXRPlane gvrPlane, SXRTrackingState gvrTrackingState) {
            if (gvrTrackingState != SXRTrackingState.TRACKING) {
                gvrPlane.setEnable(false);
            }
            else {
                gvrPlane.setEnable(true);
            }
        }

        @Override
        public void onPlaneMerging(SXRPlane gvrPlane, SXRPlane gvrPlane1) {
        }
    };

    private IAnchorEventsListener anchorEventsListener = new IAnchorEventsListener() {
        @Override
        public void onAnchorStateChange(SXRAnchor gvrAnchor, SXRTrackingState gvrTrackingState) {
            if (gvrTrackingState != SXRTrackingState.TRACKING) {
                gvrAnchor.setEnable(false);
            }
            else {
                gvrAnchor.setEnable(true);
            }
        }
    };

    public class TouchHandler extends SXREventListeners.TouchEvents {
        private SXRSceneObject mDraggingObject = null;
        private float mHitX;
        private float mHitY;
        private float mYaw;
        private float mScale;


        @Override
        public void onEnter(SXRSceneObject sceneObj, SXRPicker.SXRPickedObject pickInfo) {
            super.onEnter(sceneObj, pickInfo);

            if (sceneObj == mixedReality.getPassThroughObject() || mDraggingObject != null) {
                return;
            }

            ((VirtualObject)sceneObj).onPickEnter();
        }

        @Override
        public void onExit(SXRSceneObject sceneObj, SXRPicker.SXRPickedObject pickInfo) {
            super.onExit(sceneObj, pickInfo);

            if (sceneObj == mixedReality.getPassThroughObject()) {
                if (mDraggingObject != null) {
                    ((VirtualObject) mDraggingObject).onPickExit();
                    mDraggingObject = null;
                }
                return;
            }

            if (mDraggingObject == null) {
                ((VirtualObject) sceneObj).onPickExit();
            }
        }

        @Override
        public void onTouchStart(SXRSceneObject sceneObj, SXRPicker.SXRPickedObject pickInfo) {
            super.onTouchStart(sceneObj, pickInfo);

            if (sceneObj == mixedReality.getPassThroughObject()) {
                return;
            }

            if (mDraggingObject == null) {
                mDraggingObject = sceneObj;

                mYaw = sceneObj.getTransform().getRotationYaw();
                mScale = sceneObj.getTransform().getScaleX();

                mHitX = pickInfo.motionEvent.getX();
                mHitY = pickInfo.motionEvent.getY();

                Log.d(TAG, "onStartDragging");
                ((VirtualObject)sceneObj).onTouchStart();
            }
        }

        @Override
        public void onTouchEnd(SXRSceneObject sceneObj, SXRPicker.SXRPickedObject pickInfo) {
            super.onTouchEnd(sceneObj, pickInfo);


            if (mDraggingObject != null) {
                Log.d(TAG, "onStopDragging");

                if (pickSceneObject(mDraggingObject) == null) {
                    ((VirtualObject) mDraggingObject).onPickExit();
                } else {
                    ((VirtualObject)mDraggingObject).onTouchEnd();
                }
                mDraggingObject = null;
            } else if (sceneObj == mixedReality.getPassThroughObject()) {
                onSingleTap(sceneObj, pickInfo);
            }
        }

        @Override
        public void onInside(SXRSceneObject sceneObj, SXRPicker.SXRPickedObject pickInfo) {
            super.onInside(sceneObj, pickInfo);

            if (mDraggingObject == null) {
                return;
            } else {
                // get the current x,y hit location
                float hitLocationX = pickInfo.motionEvent.getX();
                float hitLocationY = pickInfo.motionEvent.getY();

                // find the diff from when we first touched down
                float diffX = hitLocationX - mHitX;
                float diffY = (hitLocationY - mHitY) / 100.0f;

                // when we move along X, calculate an angle to rotate the model around the Y axis
                float angle = mYaw + (diffX * 2);

                // when we move along Y, calculate how much to scale the model
                float scale = mScale + (diffY);
                if(scale < 0.1f) {
                    scale = 0.1f;
                }

                // set rotation and scale
                mDraggingObject.getTransform().setRotationByAxis(angle, 0.0f, 1.0f, 0.0f);
                mDraggingObject.getTransform().setScale(scale, scale, scale);
            }


            pickInfo = pickSceneObject(mixedReality.getPassThroughObject());
            if (pickInfo != null) {
                SXRHitResult gvrHitResult = mixedReality.hitTest(
                        mixedReality.getPassThroughObject(), pickInfo);

                if (gvrHitResult != null) {
                    mixedReality.updateAnchorPose((SXRAnchor)mDraggingObject.getParent(),
                            gvrHitResult.getPose());
                }
            }
        }

        private SXRPicker.SXRPickedObject pickSceneObject(SXRSceneObject sceneObject) {
            Vector3f origin = new Vector3f();
            Vector3f direction = new Vector3f();

            helper.getCursorController().getPicker().getWorldPickRay(origin, direction);

            return SXRPicker.pickSceneObject(sceneObject, origin.x, origin.y, origin.z,
                    direction.x, direction.y, direction.z);
        }

        private void onSingleTap(SXRSceneObject sceneObj, SXRPicker.SXRPickedObject collision) {
            SXRHitResult gvrHitResult = mixedReality.hitTest(sceneObj, collision);
            VirtualObject andy = new VirtualObject(mSXRContext);

            if (gvrHitResult == null) {
                return;
            }

            addVirtualObject(gvrHitResult.getPose(), andy);
        }
    }

    private void addVirtualObject(float[] pose, VirtualObject andy) {
        SXRAnchor anchor;

        if (mVirtObjCount < MAX_VIRTUAL_OBJECTS) {
             anchor = mixedReality.createAnchor(pose, andy);

            mainScene.addSceneObject(anchor);
            mVirtualObjects.add(anchor);
        }
        else {
            anchor = mVirtualObjects.get(mVirtObjCount % mVirtualObjects.size());
            mixedReality.updateAnchorPose(anchor, pose);
        }

        anchor.setName("id: " + mVirtObjCount);
        Log.d(TAG, "New virtual object " + anchor.getName());

        mVirtObjCount++;
    }
}