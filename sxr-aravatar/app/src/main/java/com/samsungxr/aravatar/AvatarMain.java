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

import com.samsungxr.SXRBoxCollider;
import com.samsungxr.SXRComponent;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRDirectLight;
import com.samsungxr.SXREventListeners;
import com.samsungxr.SXRLight;
import com.samsungxr.SXRMain;
import com.samsungxr.SXRMesh;
import com.samsungxr.SXRPicker;
import com.samsungxr.SXRPointLight;
import com.samsungxr.SXRRenderData;
import com.samsungxr.SXRScene;
import com.samsungxr.SXRNode;
import com.samsungxr.SystemPropertyUtil;
import com.samsungxr.animation.SXRAnimation;
import com.samsungxr.animation.SXRAnimator;
import com.samsungxr.animation.SXRAvatar;
import com.samsungxr.animation.SXRRepeatMode;
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
    public SXRNode            mAvatarAnchor;

    @Override
    public void onInit(SXRContext ctx)
    {
        mContext = ctx;
        mScene = mContext.getMainScene();
        mUtility = new SceneUtils();
        mTouchHandler = new TouchHandler();
        mSelector = new SelectionHandler(ctx);
        mSceneLight = mUtility.makeSceneLight(ctx);
        mScene.getMainCameraRig().getHeadTransformObject().addChildObject(mSceneLight.getOwnerObject());

        String avatarName = SystemPropertyUtil.getSystemPropertyString("debug.samsungxr.avatarname");

        if ((avatarName == null) || (avatarName == ""))
        {
            avatarName = "GYLE";
        }
        mAvManager = new AvatarManager(mContext, mAvatarListener);
        mAvatar = mAvManager.selectAvatar(avatarName.toUpperCase());
        if (mAvatar == null)
        {
            Log.e(TAG, "Avatar could not be found");
        }
        mAvatarAnchor = new SXRNode(mContext);
        mAvatarAnchor.setName("Avatar_Anchor");
        mAvManager.loadModel();
        mMixedReality = new SXRMixedReality(mScene, false);
        mMixedReality.getEventReceiver().addListener(planeEventsListener);
        mMixedReality.getEventReceiver().addListener(anchorEventsListener);
        mMixedReality.getEventReceiver().addListener(mrEventsListener);
        mMixedReality.setARToVRScale(1);
        mMixedReality.resume();
    }

    @Override
    public void onStep()
    {
        SXRLightEstimate lightEstimate = mMixedReality.getLightEstimate();
        if (lightEstimate != null)
        {
            float light = lightEstimate.getPixelIntensity();
            mSceneLight.setAmbientIntensity(light, light, light, 1);
            mSceneLight.setDiffuseIntensity(light, light, light, 1);
            mSceneLight.setSpecularIntensity(light, light, light, 1);
        }
    }

    private IMixedRealityEvents mrEventsListener = new IMixedRealityEvents() {
        @Override
        public void onMixedRealityStart(IMixedReality mr)
        {
            mUtility.initCursorController(getSXRContext(),
                    mTouchHandler,
                    mr.getScreenDepth());
        }

        @Override
        public void onMixedRealityStop(IMixedReality mr) { }

        @Override
        public void onMixedRealityUpdate(IMixedReality mr) { }

        public void onMixedRealityError(IMixedReality mr, String errmsg) { }
    };

    private IPlaneEvents planeEventsListener = new IPlaneEvents()
    {
        @Override
        public void onPlaneDetected(SXRPlane plane)
        {
            if (plane.getPlaneType() == SXRPlane.Type.HORIZONTAL_UPWARD_FACING)
            {
                SXRNode planeMesh = mUtility.createPlane(getSXRContext());
                float[] pose = new float[16];

                plane.getCenterPose(pose);
                planeMesh.attachComponent(plane);
                mScene.addNode(planeMesh);
                placeAvatar(pose);
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

        @Override
        public void onPlaneGeometryChange(SXRPlane plane)
        {
            if (plane.getPlaneType() == SXRPlane.Type.HORIZONTAL_UPWARD_FACING)
            {
                SXRNode owner = plane.getOwnerObject();

                if (owner != null)
                {
                    SXRMesh mesh = new SXRMesh(getSXRContext());
                    mesh.setVertices(plane.get3dPolygonAsArray());
                    owner.getRenderData().setMesh(mesh);
                }
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

            if (hit != null)
            {
                placeAvatar(hit.getPose());
            }
        }
    };

    private void placeAvatar(float[] pose)
    {
        SXRAnchor   anchor = (SXRAnchor) mAvatarAnchor.getComponent(SXRAnchor.getComponentType());;

        if (anchor != null)
        {
            mMixedReality.updateAnchorPose(anchor, pose);
        }
        else
        {
            anchor = mMixedReality.createAnchor(pose, mAvatarAnchor);
        }
    }

    public  SXRAvatar.IAvatarEvents mAvatarListener = new SXRAvatar.IAvatarEvents()
    {
        @Override
        public void onAvatarLoaded(final SXRAvatar avatar, final SXRNode avatarRoot, String filePath, String errors)
        {
            SXRNode.BoundingVolume bv = avatarRoot.getBoundingVolume();
            if (bv.radius > 0)
            {
                float scale = 0.3f /bv.radius;
                avatarRoot.getTransform().setScale(scale, scale, scale);
                bv = avatarRoot.getBoundingVolume();
                avatarRoot.getTransform().setPosition(-bv.center.x, 0.3f - bv.center.y, -bv.center.z);
            }
            avatarRoot.attachComponent(new SXRBoxCollider(mContext));
            mAvatarAnchor.addChildObject(avatarRoot);
            avatarRoot.getEventReceiver().addListener(mSelector);
            mScene.addNode(mAvatarAnchor);
            mAvManager.loadNextAnimation();
        }

        @Override
        public void onAnimationLoaded(SXRAvatar avatar, SXRAnimator animation, String filePath, String errors)
        {
            if (animation == null)
            {
                return;
            }
            animation.setRepeatMode(SXRRepeatMode.ONCE);
            animation.setSpeed(1f);
            if (!avatar.isRunning())
            {
                avatar.startAll(SXRRepeatMode.REPEATED);
            }
            else
            {
                avatar.start(animation.getName());
            }
            mAvManager.loadNextAnimation();
        }

        public void onModelLoaded(SXRAvatar avatar, SXRNode avatarRoot, String filePath, String errors)
        {
            SXRNode.BoundingVolume bv = avatarRoot.getBoundingVolume();
            if (bv.radius > 0)
            {
                float scale = 0.3f / bv.radius;
                avatarRoot.getTransform().setScale(scale, scale, scale);
                bv = avatarRoot.getBoundingVolume();
            }
            avatarRoot.getTransform().setPosition(-bv.center.x, 0, -bv.center.z);
            avatarRoot.attachComponent(new SXRBoxCollider(mContext));
            avatarRoot.forAllComponents(new SXRNode.ComponentVisitor() {
                @Override
                public boolean visit(SXRComponent c)
                {
                    ((SXRRenderData) c).disableLight();
                    return true;
                }
            }, SXRRenderData.getComponentType());
            mAvatarAnchor.addChildObject(avatarRoot);
            mContext.getMainScene().addNode(mAvatarAnchor);
        }

        public void onAnimationFinished(SXRAvatar avatar, SXRAnimator animator) { }

        public void onAnimationStarted(SXRAvatar avatar, SXRAnimator animator) { }
    };
}
