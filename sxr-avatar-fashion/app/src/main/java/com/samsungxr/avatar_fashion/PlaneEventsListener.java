package com.samsungxr.avatar_fashion;

import com.samsungxr.SXRBoxCollider;
import com.samsungxr.SXRContext;
import com.samsungxr.SXREventListeners;
import com.samsungxr.SXRNode;
import com.samsungxr.SXRPicker;
import com.samsungxr.mixedreality.IMixedReality;
import com.samsungxr.mixedreality.IPlaneEvents;
import com.samsungxr.mixedreality.SXRAnchor;
import com.samsungxr.mixedreality.SXRHitResult;
import com.samsungxr.mixedreality.SXRPlane;
import com.samsungxr.mixedreality.SXRTrackingState;
import com.samsungxr.widgetlib.main.WidgetLib;
import com.samsungxr.widgetlib.widget.Widget;

public class PlaneEventsListener implements IPlaneEvents {
    private SXRContext mSXRContext;
    private SceneUtils mUtility;
    private IMixedReality mMixedReality;
    private SelectionHandler mSelector;
    private Widget mAvatarAnchor;

    public PlaneEventsListener(SXRContext ctx, SceneUtils utility,
                               IMixedReality mixedReality, Widget avatarAnchor) {
        mSXRContext = ctx;
        mUtility = utility;
        mMixedReality = mixedReality;
        mSelector = new SelectionHandler(ctx);
        mAvatarAnchor = avatarAnchor;

        initAvatar((Avatar)avatarAnchor.get(0));
    }

    @Override
    public void onStartPlaneDetection(IMixedReality mr) {
        mUtility.initCursorController(mSXRContext,
                mTouchHandler,
                mr.getScreenDepth());
    }

    @Override
    public void onStopPlaneDetection(IMixedReality mr) { }

    @Override
    public void onPlaneDetected(SXRPlane plane) {
        if (plane.getPlaneType() == SXRPlane.Type.HORIZONTAL_UPWARD_FACING) {
            Widget planeWidget = new PlaneWidget(mSXRContext, plane);
            WidgetLib.getMainScene().addNode(planeWidget);
        }
    }

    class PlaneWidget extends Widget {
        private final float[] mPose = new float[16];
        PlaneWidget(SXRContext context, SXRPlane plane) {
            super(context, mUtility.createPlane(mSXRContext));
            plane.getCenterPose(mPose);
            getNode().attachComponent(plane);
        }
    }


    private void placeAvatar(float[] pose) {
        SXRAnchor   anchor = (SXRAnchor) mAvatarAnchor.getNode().getComponent(SXRAnchor.getComponentType());

        if (anchor != null) {
            mMixedReality.updateAnchorPose(anchor, pose);
        } else {
            anchor = mMixedReality.createAnchor(pose);
            mAvatarAnchor.getNode().attachComponent(anchor);
        }
    }

    @Override
    public void onPlaneStateChange(SXRPlane SXRPlane, SXRTrackingState SXRTrackingState) {
        SXRPlane.setEnable(SXRTrackingState == SXRTrackingState.TRACKING);
    }

    @Override
    public void onPlaneMerging(SXRPlane parent, SXRPlane child) {
        SXRNode childOwner = child.getOwnerObject();
        if (childOwner != null)
        {
            childOwner.detachComponent(SXRPlane.getComponentType());
            childOwner.getParent().removeChildObject(childOwner);
        }
    }

    private SXREventListeners.TouchEvents mTouchHandler = new SXREventListeners.TouchEvents() {
        @Override
        public void onTouchEnd(SXRNode sceneObj, SXRPicker.SXRPickedObject pickInfo) {
            Widget  avatar = mAvatarAnchor.get(0);
            if (avatar == null) {
                return;
            }

            SXRNode.BoundingVolume bv = sceneObj.getBoundingVolume();

            if (pickInfo.hitDistance < bv.radius)
            {
                pickInfo.hitLocation[2] -= 1.5f * bv.radius;
            }
            SXRHitResult hit = mMixedReality.hitTest(pickInfo);

            if (hit != null) {
                avatar.setVisibility(Widget.Visibility.VISIBLE);
                placeAvatar(hit.getPose());
            }
        }
    };


    public void initAvatar(Avatar avatar)  {
        avatar.setVisibility(Widget.Visibility.HIDDEN);
        SXRNode avatarRoot = avatar.getNode();
        SXRNode.BoundingVolume bv = avatarRoot.getBoundingVolume();
        if (bv.radius > 0) {
            float scale = 0.3f /bv.radius;
            avatar.getTransform().setScale(scale, scale, scale);
            bv = avatarRoot.getBoundingVolume();
        }
        float zpos = -bv.center.z;
        if (mMixedReality.getPassThroughObject() != null) {
            zpos -= 1.5f;
        }
        avatar.getTransform().setPosition(-bv.center.x, 0, zpos);
        avatarRoot.attachComponent(new SXRBoxCollider(mSXRContext));
        avatarRoot.getEventReceiver().addListener(mSelector);
        avatar.enableAnimation();
    }
}

