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

package com.samsungxr.arpet.character;

import android.util.SparseArray;

import com.samsungxr.SXRCameraRig;
import com.samsungxr.SXRDrawFrameListener;
import com.samsungxr.SXRNode;
import com.samsungxr.SXRTransform;
import com.samsungxr.arpet.BallThrowHandler;
import com.samsungxr.arpet.PetContext;
import com.samsungxr.arpet.constant.PetConstants;
import com.samsungxr.arpet.mode.BasePetMode;
import com.samsungxr.arpet.mode.ILoadEvents;
import com.samsungxr.arpet.movement.IPetAction;
import com.samsungxr.arpet.movement.PetActionType;
import com.samsungxr.arpet.movement.PetActions;
import com.samsungxr.arpet.service.IMessageService;
import com.samsungxr.arpet.service.MessageService;
import com.samsungxr.arpet.service.data.PetActionCommand;
import com.samsungxr.arpet.service.event.PetActionCommandReceivedMessage;
import com.samsungxr.arpet.service.share.SharedMixedReality;
import com.samsungxr.arpet.util.EventBusUtils;
import com.samsungxr.utility.Log;

import org.greenrobot.eventbus.Subscribe;
import org.joml.Vector2f;

public class CharacterController extends BasePetMode {

    private IPetAction mCurrentAction = null; // default action IDLE
    private final SparseArray<IPetAction> mPetActions;
    private SXRDrawFrameListener mDrawFrameHandler;
    private BallThrowHandler mBallThrowHandler;

    private SharedMixedReality mMixedReality;
    private IMessageService mMessageService;
    private boolean mIsPlaying = false;

    private SXRNode mBedTarget;
    private SXRNode mBowlTarget;
    private SXRNode mHydrantNode;

    public CharacterController(PetContext petContext) {
        super(petContext, new CharacterView(petContext));

        mPetActions = new SparseArray<>();
        mDrawFrameHandler = null;
        mMixedReality = mPetContext.getMixedReality();
        mBallThrowHandler = petContext.getBallThrowHandlerHandler();

        mMessageService = MessageService.getInstance();

        mBedTarget = new SXRNode(petContext.getSXRContext());
        mBowlTarget = new SXRNode(petContext.getSXRContext());
        mHydrantNode = new SXRNode(petContext.getSXRContext());

        initPet((CharacterView) mModeScene);
    }

    @Subscribe
    public void handleReceivedMessage(PetActionCommandReceivedMessage message) {
        onSetCurrentAction(message.getPetActionCommand().getType());
    }

    @Override
    protected void onEnter() {
        EventBusUtils.register(this);
    }

    @Override
    protected void onExit() {
        EventBusUtils.unregister(this);
    }

    @Override
    public void load(ILoadEvents listener) {
        super.load(listener);

        mModeScene.load(listener);
    }

    @Override
    public void unload() {
        super.unload();

        mModeScene.unload();
    }

    private void initPet(CharacterView pet) {
        addAction(new PetActions.IDLE(mPetContext, pet));

        addAction(new PetActions.TO_BALL(pet, mBallThrowHandler.getBall(), (action, success) -> {
            if (success) {
                setCurrentAction(PetActions.GRAB.ID);
            } else {
                setCurrentAction(PetActions.IDLE.ID);
            }
        }));

        addAction(new PetActions.TO_PLAYER(pet, mPetContext.getPlayer(), (action, success) -> {
            setCurrentAction(PetActions.IDLE.ID);
        }));

        addAction(new PetActions.GRAB(pet, mBallThrowHandler.getBall(), (action, success) -> {
            setCurrentAction(PetActions.TO_PLAYER.ID);

        }));

        addAction(new PetActions.TO_TAP(pet, pet.getTapObject(), (action, success) -> {
            setCurrentAction(PetActions.IDLE.ID);
        }));

        addAction(new PetActions.TO_BED(pet, mBedTarget, (action, success) -> setCurrentAction(PetActions.SLEEP_ENTER.ID)));

        addAction(new PetActions.TO_BOWL(pet, mBowlTarget, (action, success) -> setCurrentAction(PetActions.DRINK_ENTER.ID)));

        addAction(new PetActions.TO_HYDRANT(pet, mHydrantNode, (action, success) -> setCurrentAction(PetActions.HYDRANT_ENTER.ID)));

        addAction(new PetActions.DRINK_ENTER(pet, mBowlTarget, (action, success) -> setCurrentAction(PetActions.DRINK_LOOP.ID)));

        addAction(new PetActions.DRINK_EXIT(pet, mBowlTarget, (action, success) -> {
            ((CharacterView) mModeScene).setTapPosition(0, 0, 0);
            setCurrentAction(PetActions.TO_TAP.ID);
        }));

        addAction(new PetActions.DRINK_LOOP(pet, mBowlTarget, (action, success) -> setCurrentAction(PetActions.DRINK_EXIT.ID)));

        addAction(new PetActions.HYDRANT_ENTER(pet, mHydrantNode, (action, success) -> setCurrentAction(PetActions.HYDRANT_LOOP.ID)));

        addAction(new PetActions.HYDRANT_EXIT(pet, mHydrantNode, (action, success) -> {
            ((CharacterView) mModeScene).setTapPosition(0, 0, 0);
            setCurrentAction(PetActions.TO_TAP.ID);
        }));

        addAction(new PetActions.HYDRANT_LOOP(pet, mHydrantNode, (action, success) -> setCurrentAction(PetActions.HYDRANT_EXIT.ID)));

        addAction(new PetActions.SLEEP_ENTER(pet, mBedTarget, (action, success) -> setCurrentAction(PetActions.SLEEP_LOOP.ID)));

        addAction(new PetActions.SLEEP_EXIT(pet, mBedTarget, (action, success) -> {
            ((CharacterView) mModeScene).setTapPosition(0, 0, 0);
            setCurrentAction(PetActions.TO_TAP.ID);
        }));

        addAction(new PetActions.SLEEP_LOOP(pet, mBedTarget, (action, success) -> {
            setCurrentAction(PetActions.SLEEP_EXIT.ID);
        }));

        addAction(new PetActions.AT_EDIT(mPetContext, pet));

        setCurrentAction(PetActions.IDLE.ID);
    }

    public void goToTap(float x, float y, float z) {
        if (mCurrentAction == null
                || mCurrentAction.id() == PetActions.IDLE.ID
                || mCurrentAction.id() == PetActions.TO_TAP.ID) {
            Log.d(TAG, "goToTap(%f, %f, %f)", x, y, z);
            ((CharacterView) mModeScene).setTapPosition(x, y, z);
            setCurrentAction(PetActions.TO_TAP.ID);
        }
    }

    public void goToBed(float x, float y, float z) {
        mBedTarget.getTransform().setPosition(x, y, z);
        setCurrentAction(PetActions.TO_BED.ID);
    }

    public void goToBowl(float x, float y, float z) {
        mBowlTarget.getTransform().setPosition(x, y, z);
        setCurrentAction(PetActions.TO_BOWL.ID);
    }

    public void goToHydrant(float x, float y, float z) {
        SXRTransform t = getView().getTransform();
        float x0 = t.getPositionX();
        float z0 = t.getPositionZ();
        Vector2f vecHydrant = new Vector2f(x - x0, z - z0);
        Vector2f perpendicular = new Vector2f(vecHydrant.y, vecHydrant.x * -1);

        perpendicular.normalize();
        perpendicular.mul(getView().getBoundingVolume().radius * 0.5f);

        mHydrantNode.getTransform().setPosition(x + perpendicular.x, y, z + perpendicular.y);

        setCurrentAction(PetActions.TO_HYDRANT.ID);
    }

    public void playBone() {
        mIsPlaying = true;
        mBallThrowHandler.enable();
    }

    public void stopBone() {
        mIsPlaying = false;
        mBallThrowHandler.disable();
    }

    public boolean isPlaying() {
        return mIsPlaying;
    }

    public void setPlane(SXRNode plane) {
        CharacterView petView = (CharacterView) view();

        petView.setBoundaryPlane(plane);
    }

    public SXRNode getPlane() {
        CharacterView petView = (CharacterView) view();

        return petView.getBoundaryPlane();
    }

    public CharacterView getView() {
        return (CharacterView) view();
    }

    public void setCurrentAction(@PetActionType int action) {
        onSetCurrentAction(action);
        onSendCurrentAction(action);
    }

    private void onSetCurrentAction(@PetActionType int action) {
        mCurrentAction = mPetActions.get(action);

        if (mIsPlaying || mPetContext.getMode() == PetConstants.SHARE_MODE_GUEST) {
            if (mCurrentAction.id() == PetActions.IDLE.ID) {
                mBallThrowHandler.reset();
            } if (mCurrentAction.id() == PetActions.GRAB.ID) {
                mBallThrowHandler.disableBallsPhysics();
            }
        }

        if (mCurrentAction != null && action == PetActions.IDLE.ID) {
            EventBusUtils.post(mCurrentAction);
        }
    }

    private void onSendCurrentAction(@PetActionType int action) {
        if (mPetContext.getMode() == PetConstants.SHARE_MODE_HOST) {
            mMessageService.sendPetActionCommand(new PetActionCommand(action));
        }
    }

    public void addAction(IPetAction action) {
        mPetActions.put(action.id(), action);
    }

    public void removeAction(@PetActionType int action) {
        mPetActions.remove(action);
    }

    public void enableActions() {
        if (mDrawFrameHandler == null) {
            Log.w(TAG, "On actions enabled");
            mDrawFrameHandler = new DrawFrameHandler();
            mPetContext.getSXRContext().registerDrawFrameListener(mDrawFrameHandler);
        }
    }

    public void disableActions() {
        if (mDrawFrameHandler != null) {
            Log.w(TAG, "On actions disabled");
            mPetContext.getSXRContext().unregisterDrawFrameListener(mDrawFrameHandler);
            mDrawFrameHandler = null;
        }
    }

    public void setInitialScale() {
        CharacterView petView = (CharacterView) view();
        petView.setInitialScale();
    }

    private class DrawFrameHandler implements SXRDrawFrameListener {
        IPetAction activeAction = null;

        @Override
        public void onDrawFrame(float frameTime) {
            if (mCurrentAction != activeAction) {
                if (activeAction != null) {
                    activeAction.exit();
                }
                activeAction = mCurrentAction;
                activeAction.entry();
            } else if (activeAction != null) {
                activeAction.run(frameTime);
            }
        }
    }
}
