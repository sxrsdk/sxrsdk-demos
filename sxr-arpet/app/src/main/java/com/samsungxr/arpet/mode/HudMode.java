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

package com.samsungxr.arpet.mode;

import android.annotation.SuppressLint;
import android.util.Log;

import com.samsungxr.SXRCameraRig;
import com.samsungxr.arpet.PetContext;
import com.samsungxr.arpet.character.CharacterController;
import com.samsungxr.arpet.constant.ArPetObjectType;
import com.samsungxr.arpet.constant.PetConstants;
import com.samsungxr.arpet.manager.connection.PetConnectionManager;
import com.samsungxr.arpet.manager.connection.event.PetConnectionEvent;
import com.samsungxr.arpet.movement.IPetAction;
import com.samsungxr.arpet.movement.PetActions;
import com.samsungxr.arpet.service.share.SharedMixedReality;
import com.samsungxr.arpet.util.EventBusUtils;

import org.greenrobot.eventbus.Subscribe;

import static com.samsungxr.arpet.manager.connection.IPetConnectionManager.EVENT_ALL_CONNECTIONS_LOST;

public class HudMode extends BasePetMode {
    private OnModeChange mModeChangeListener;
    private HudView mHudView;

    private PetConnectionManager mConnectionManager;
    private SharedMixedReality mSharedMixedReality;
    private CharacterController mPetController;
    private VirtualObjectController mVirtualObjectController;

    public HudMode(PetContext petContext, CharacterController petController, OnModeChange listener) {
        super(petContext, new HudView(petContext));
        mModeChangeListener = listener;
        mPetController = petController;

        mHudView = (HudView) mModeScene;
        mHudView.setListener(new OnHudItemClickedHandler());
        mHudView.setDisconnectListener(new OnDisconnectClickedHandler());

        mConnectionManager = (PetConnectionManager) PetConnectionManager.getInstance();
        mSharedMixedReality = petContext.getMixedReality();

        mVirtualObjectController = new VirtualObjectController(petContext, petController);
    }

    @Override
    protected void onEnter() {
        EventBusUtils.register(this);
        if (mPetContext.getMode() != PetConstants.SHARE_MODE_NONE) {
            Log.d(TAG, "Play Ball activated by sharing mode!");
            mPetController.playBall();
        }
    }

    @Override
    protected void onExit() {
        EventBusUtils.unregister(this);
        mVirtualObjectController.hideObject();
    }

    @Override
    protected void onHandleOrientation(SXRCameraRig cameraRig) {

    }

    private class OnHudItemClickedHandler implements OnHudItemClicked {

        @Override
        public void onBoneClicked() {
            mVirtualObjectController.hideObject();
            if (mPetController.isPlaying()) {
                mPetController.stopBall();
                Log.d(TAG, "Stop Bone");
            } else {
                mPetController.playBall();
                Log.d(TAG, "Play Bone");
            }
            mPetController.setCurrentAction(PetActions.IDLE.ID);
        }

        @Override
        public void onBedClicked() {
            Log.d(TAG, "Action: go to bed");
            mVirtualObjectController.showObject(ArPetObjectType.BED);
        }

        @Override
        public void onHydrantClicked() {
            Log.d(TAG, "Action: go to hydrant");
            mVirtualObjectController.showObject(ArPetObjectType.HYDRANT);
        }

        @Override
        public void onBowlClicked() {
            Log.d(TAG, "Action: go to bowl");
            mVirtualObjectController.showObject(ArPetObjectType.BOWL);
        }

        @Override
        public void onShareAnchorClicked() {
            mModeChangeListener.onShareAnchor();
            Log.d(TAG, "Share Anchor Mode");
        }

        @Override
        public void onEditModeClicked() {
            mModeChangeListener.onEditMode();
            Log.d(TAG, "Edit Mode");
        }

        @Override
        public void onCameraClicked() {
            mModeChangeListener.onScreenshot();
            Log.d(TAG, "Camera Mode");
        }

        @Override
        public void onConnectedClicked() {
            Log.d(TAG, "Connected label clicked");
            mPetContext.getActivity().runOnUiThread(() -> {
                mHudView.showDisconnectView(mConnectionManager.getConnectionMode());
                mHudView.hideConnectedLabel();
            });
        }
    }

    private class OnDisconnectClickedHandler implements OnDisconnectClicked {
        @Override
        public void onCancel() {
            mPetContext.getActivity().runOnUiThread(() -> {
                mHudView.hideDisconnectView();
                mHudView.showConnectedLabel();
            });
        }

        @Override
        public void onDisconnect() {
            petExit();
            mSharedMixedReality.stopSharing();
            mConnectionManager.disconnect();
            mPetContext.getActivity().runOnUiThread(() -> {
                mHudView.hideDisconnectView();
                mHudView.hideConnectedLabel();
            });
            mPetController.stopBall();
        }
    }

    @SuppressLint("SwitchIntDef")
    @Subscribe
    public void handleConnectionEvent(PetConnectionEvent message) {
        if (message.getType() == EVENT_ALL_CONNECTIONS_LOST) {
            petExit();
            mPetContext.getActivity().runOnUiThread(() -> {
                mHudView.hideDisconnectView();
                mHudView.hideConnectedLabel();
            });
        }
    }

    @Subscribe
    public void onPetActionChanged(IPetAction action) {
        if (action.id() == PetActions.IDLE.ID) {
            mVirtualObjectController.hideObject();
        }
    }

    private void petExit() {
        if (mPetContext.getMode() == PetConstants.SHARE_MODE_GUEST) {
            //TODO: after finish the sharing anchor experience as guest, the scene will be reseted
            // and the user should be notified to detect planes and positioning the pet again
            mPetController.exit();
        }
    }

}
