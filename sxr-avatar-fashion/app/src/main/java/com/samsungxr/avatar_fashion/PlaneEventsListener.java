package com.samsungxr.avatar_fashion;

import com.samsungxr.SXRBoxCollider;
import com.samsungxr.SXRContext;
import com.samsungxr.SXREventListeners;
import com.samsungxr.SXRNode;
import com.samsungxr.SXRPicker;
import com.samsungxr.SXRScene;
import com.samsungxr.animation.SXRAvatar;
import com.samsungxr.animation.SXRRepeatMode;
import com.samsungxr.mixedreality.IMixedReality;
import com.samsungxr.mixedreality.IPlaneEvents;
import com.samsungxr.mixedreality.SXRAnchor;
import com.samsungxr.mixedreality.SXRHitResult;
import com.samsungxr.mixedreality.SXRPlane;
import com.samsungxr.mixedreality.SXRTrackingState;

public class PlaneEventsListener implements IPlaneEvents {
    private SXRContext mSXRContext;
    private SceneUtils mUtility;
    private IMixedReality mMixedReality;
    private SelectionHandler mSelector;
    private SXRScene mScene;
    private SXRAvatar mAvatar;

    public PlaneEventsListener(SXRContext ctx, SceneUtils utility,
                               IMixedReality mixedReality, SXRAvatar avatar) {
        mSXRContext = ctx;
        mUtility = utility;
        mMixedReality = mixedReality;
        mSelector = new SelectionHandler(ctx);
        mScene = ctx.getMainScene();
        mAvatar = avatar;
    }

    @Override
    public void onStartPlaneDetection(IMixedReality mr)
    {
        mUtility.initCursorController(mSXRContext,
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
            SXRNode planeMesh = mUtility.createPlane(mSXRContext);

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

    private SXREventListeners.TouchEvents mTouchHandler = new SXREventListeners.TouchEvents() {
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
            SXRAnchor anchor = null;
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
                avatarModel.attachComponent(new SXRBoxCollider(mSXRContext));
                mScene.addNode(avatarAnchor);
                avatarModel.getEventReceiver().addListener(mSelector);
            }
        }
    };
}

