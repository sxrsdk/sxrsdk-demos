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

package com.samsungxr.aravatar;

import android.util.Log;

import com.samsungxr.SXRBoxCollider;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRDirectLight;
import com.samsungxr.SXREventListeners;
import com.samsungxr.SXRLight;
import com.samsungxr.SXRMain;
import com.samsungxr.SXRPicker;
import com.samsungxr.SXRPointLight;
import com.samsungxr.SXRScene;
import com.samsungxr.SXRNode;
import com.samsungxr.animation.SXRAvatar;
import com.samsungxr.animation.SXRRepeatMode;
import com.samsungxr.mixedreality.SXRAnchor;
import com.samsungxr.mixedreality.SXRHitResult;
import com.samsungxr.mixedreality.SXRMixedReality;
import com.samsungxr.mixedreality.SXRPlane;
import com.samsungxr.mixedreality.SXRTrackingState;
import com.samsungxr.mixedreality.IAnchorEvents;
import com.samsungxr.mixedreality.IMixedReality;
import com.samsungxr.mixedreality.IPlaneEvents;

public class AvatarMain extends SXRMain {
    private static String TAG = "ARAVATAR";
    private SXRContext        mContext;
    private SXRScene          mScene;
    private SXRAvatar         mAvatar;
    private SXRMixedReality   mMixedReality;
    private SceneUtils        mUtility;
    private TouchHandler      mTouchHandler;
    private SelectionHandler  mSelector;
    private SXRDirectLight    mSceneLight;
    private AvatarManager     mAvManager;

    @Override
    public void onInit(SXRContext ctx)
    {
        mContext = ctx;
        mScene = mContext.getMainScene();
        mUtility = new SceneUtils();
        mTouchHandler = new TouchHandler();
        mSelector = new SelectionHandler(ctx);
        mSceneLight = mUtility.makeSceneLight(ctx);
        mScene.addNode(mSceneLight.getOwnerObject());
        mAvManager = new AvatarManager(mContext, null);
        mAvatar = mAvManager.selectAvatar("EVA");
        if (mAvatar == null)
        {
            Log.e(TAG, "Avatar could not be found");
        }
        mAvManager.loadModel();
        mMixedReality = new SXRMixedReality(mContext);
        mMixedReality.getEventReceiver().addListener(planeEventsListener);
        mMixedReality.getEventReceiver().addListener(anchorEventsListener);
        mMixedReality.resume();
    }

    @Override
    public void onStep()
    {
        float light = mMixedReality.getLightEstimate().getPixelIntensity() * 1.5f;
        mSceneLight.setAmbientIntensity(light, light, light, 1);
        mSceneLight.setDiffuseIntensity(light, light, light, 1);
        mSceneLight.setSpecularIntensity(light, light, light, 1);
    }


    private IPlaneEvents planeEventsListener = new IPlaneEvents()
    {
        @Override
        public void onStartPlaneDetection(IMixedReality mr)
        {
            mUtility.initCursorController(getSXRContext(),
                    mTouchHandler,
                    mr.getScreenDepth());
        }

        @Override
        public void onStopPlaneDetection(IMixedReality mr) { }

        @Override
        public void onPlaneDetected(SXRPlane plane)
        {
            if (plane.getPlaneType() == SXRPlane.Type.HORIZONTAL_UPWARD_FACING)
            {
                SXRNode planeMesh = mUtility.createPlane(getSXRContext());

                planeMesh.attachComponent(plane);
                mScene.addNode(planeMesh);
            }
        }

        @Override
        public void onPlaneStateChange(SXRPlane SXRPlane, SXRTrackingState SXRTrackingState)
        {
            SXRPlane.setEnable(SXRTrackingState == SXRTrackingState.TRACKING);
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
    };

    private IAnchorEvents anchorEventsListener = new IAnchorEvents()
    {
        @Override
        public void onAnchorStateChange(SXRAnchor SXRAnchor, SXRTrackingState SXRTrackingState)
        {
            SXRAnchor.setEnable(SXRTrackingState == SXRTrackingState.TRACKING);
        }
    };

    public class SelectionHandler extends SXREventListeners.TouchEvents
    {
        private SXRNode mSelectionLight = null;

        public SelectionHandler(SXRContext ctx)
        {
            SXRPointLight light = new SXRPointLight(ctx);
            mSelectionLight = new SXRNode(mContext);
            light.setAmbientIntensity(0, 0, 0, 1);
            light.setDiffuseIntensity(0.7f, 0.7f, 0.5f, 1);
            light.setSpecularIntensity(0.7f, 0.7f, 0.5f, 1);
            mSelectionLight.getTransform().setPositionY(1);
            mSelectionLight.attachComponent(light);
        }

        public void onEnter(SXRNode sceneObj, SXRPicker.SXRPickedObject pickInfo)
        {
            SXRNode.BoundingVolume bv = sceneObj.getBoundingVolume();
            SXRNode lightParent = mSelectionLight.getParent();
            SXRNode pickedParent = sceneObj.getParent();

            mSelectionLight.getTransform().setPositionY(bv.radius);
            if (lightParent == pickedParent)
            {
                SXRLight light = mSelectionLight.getLight();
                light.setEnable(true);
                return;
            }
            if (lightParent != null)
            {
                lightParent.removeChildObject(mSelectionLight);
            }
            pickedParent.addChildObject(mSelectionLight);
        }

        public void onExit(SXRNode sceneObj, SXRPicker.SXRPickedObject pickInfo)
        {
            SXRLight light = mSelectionLight.getLight();
            light.setEnable(false);
        }
    };

    public class TouchHandler extends SXREventListeners.TouchEvents
    {
        @Override
        public void onTouchEnd(SXRNode sceneObj, SXRPicker.SXRPickedObject pickInfo)
        {
            if (mAvatar == null)
            {
                return;
            }
            SXRNode.BoundingVolume bv = sceneObj.getBoundingVolume();

            if (pickInfo.hitDistance < bv.radius)
            {
                pickInfo.hitLocation[2] -= 1.5f * bv.radius;
            }
            SXRHitResult hit = mMixedReality.hitTest(pickInfo);

            if (hit == null)
            {
                return;
            }
            SXRNode avatarModel = mAvatar.getModel();
            SXRNode avatarAnchor = avatarModel.getParent();
            SXRAnchor   anchor = null;
            float[]     pose = hit.getPose();

            if (!mAvatar.isRunning())
            {
                mAvatar.startAll(SXRRepeatMode.REPEATED);
            }
            if (avatarAnchor != null)
            {
                anchor = (SXRAnchor) avatarAnchor.getComponent(SXRAnchor.getComponentType());
                mMixedReality.updateAnchorPose(anchor, pose);
            }
            else
            {
                avatarAnchor = mMixedReality.createAnchorNode(pose);
                avatarAnchor.addChildObject(avatarModel);
                avatarModel.attachComponent(new SXRBoxCollider(mContext));
                mScene.addNode(avatarAnchor);
                avatarModel.getEventReceiver().addListener(mSelector);
            }
        }
    };



}
