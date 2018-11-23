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

package com.samsungxr.arpet;

import android.view.MotionEvent;

import com.samsungxr.ITouchEvents;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRNode;
import com.samsungxr.SXRPicker;
import com.samsungxr.io.SXRCursorController;
import com.samsungxr.io.SXRGazeCursorController;
import com.samsungxr.io.SXRInputManager;
import com.samsungxr.mixedreality.IMixedReality;
import com.samsungxr.mixedreality.SXRPlane;
import com.samsungxr.utility.Log;

import com.samsungxr.arpet.character.CharacterController;
import com.samsungxr.arpet.constant.PetConstants;
import com.samsungxr.arpet.mainview.IConnectionFinishedView;
import com.samsungxr.arpet.mainview.IExitView;
import com.samsungxr.arpet.mainview.MainViewController;
import com.samsungxr.arpet.manager.connection.event.PetConnectionEvent;
import com.samsungxr.arpet.mode.EditMode;
import com.samsungxr.arpet.mode.HudMode;
import com.samsungxr.arpet.mode.ILoadEvents;
import com.samsungxr.arpet.mode.IPetMode;
import com.samsungxr.arpet.mode.OnBackToHudModeListener;
import com.samsungxr.arpet.mode.OnModeChange;
import com.samsungxr.arpet.mode.photo.ScreenshotMode;
import com.samsungxr.arpet.mode.sharing.ShareAnchorMode;
import com.samsungxr.arpet.movement.PetActions;
import com.samsungxr.arpet.service.share.SharedMixedReality;
import com.samsungxr.arpet.util.EventBusUtils;
import org.greenrobot.eventbus.Subscribe;

import java.util.EnumSet;

import static com.samsungxr.arpet.manager.connection.IPetConnectionManager.EVENT_ALL_CONNECTIONS_LOST;


public class PetMain extends DisableNativeSplashScreen {
    private static final String TAG = "SXR_ARPET";

    private PetContext mPetContext;

    private PlaneHandler mPlaneHandler;

    private IPetMode mCurrentMode;
    private HandlerModeChange mHandlerModeChange;
    private HandlerBackToHud mHandlerBackToHud;

    private CharacterController mPet = null;

    private SXRCursorController mCursorController = null;

    private CurrentSplashScreen mCurrentSplashScreen;
    private SharedMixedReality mSharedMixedReality;
    private ExitApplicationScreen mExitApplicationScreen;

    private MainViewController mMainViewController;

    public PetMain(PetContext petContext) {
        mPetContext = petContext;
        EventBusUtils.register(this);
    }

    @Override
    public void onInit(final SXRContext sxrContext) throws Throwable {
        super.onInit(sxrContext);

        mCurrentSplashScreen = new CurrentSplashScreen(sxrContext);
        mCurrentSplashScreen.onShow();

        mPetContext.init(sxrContext);

        mHandlerModeChange = new HandlerModeChange();
        mHandlerBackToHud = new HandlerBackToHud();

        mPlaneHandler = new PlaneHandler(this, mPetContext);

        // FIXME: resume after plane listening
        mPetContext.registerPlaneListener(mPlaneHandler);
        mPetContext.getMixedReality().resume();

        mSharedMixedReality = mPetContext.getMixedReality();
        mExitApplicationScreen = new ExitApplicationScreen(mPetContext);

        mPet = new CharacterController(mPetContext);
        mPet.load(new ILoadEvents() {
            @Override
            public void onSuccess() {
                // Will wet pet's scene as the main scene
                mCurrentSplashScreen.onHide(mPetContext.getMainScene());
                // Start detecting planes
                mPetContext.startDetectingPlanes();
                // Set pet controller in pet context
                mPetContext.setPetController(mPet);
            }

            @Override
            public void onFailure() {
                mPetContext.getActivity().finish();
            }
        });
    }

    public void onARInit(SXRContext ctx, IMixedReality mr) {
        mCursorController = null;
        SXRInputManager inputManager = ctx.getInputManager();
        final int cursorDepth = 5;
        final EnumSet<SXRPicker.EventOptions> eventOptions = EnumSet.of(
                SXRPicker.EventOptions.SEND_TOUCH_EVENTS,
                SXRPicker.EventOptions.SEND_TO_LISTENERS,
                SXRPicker.EventOptions.SEND_TO_HIT_OBJECT);

        inputManager.selectController((newController, oldController) -> {
            if (mCursorController != null) {
                mCursorController.removePickEventListener(mTouchEventsHandler);
            }
            newController.addPickEventListener(mTouchEventsHandler);
            newController.setCursorDepth(cursorDepth);
            newController.setCursorControl(SXRCursorController.CursorControl.CURSOR_CONSTANT_DEPTH);
            newController.getPicker().setPickClosest(false);
            newController.getPicker().setEventOptions(eventOptions);
            mCursorController = newController;
            if (newController instanceof SXRGazeCursorController) {
                ((SXRGazeCursorController) newController).setTouchScreenDepth(mr.getScreenDepth());
                // Don't show any cursor
                newController.setCursor(null);
            }
        });
    }

    public void resume() {
        EventBusUtils.register(this);
    }

    public void pause() {
        EventBusUtils.unregister(this);
    }

    private void showViewExit() {

        mMainViewController = new MainViewController(mPetContext);
        mMainViewController.onShow(mPetContext.getMainScene());
        IExitView iExitView = mMainViewController.makeView(IExitView.class);

        iExitView.setOnCancelClickListener(view -> {
            mMainViewController.onHide(mPetContext.getMainScene());
            mMainViewController = null;
        });
        iExitView.setOnConfirmClickListener(view -> {
            getSXRContext().getActivity().finish();
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        });

        iExitView.show();
    }

    private void showViewConnectionFinished(@PetConstants.ShareMode int mode) {

        mMainViewController = new MainViewController(mPetContext);
        mMainViewController.onShow(mPetContext.getMainScene());

        IConnectionFinishedView iFinishedView =
                mMainViewController.makeView(IConnectionFinishedView.class);

        iFinishedView.setOkClickListener(view -> {
            mMainViewController.onHide(mPetContext.getMainScene());
            mMainViewController = null;
        });

        String text = getSXRContext().getActivity().getString(
                mode == PetConstants.SHARE_MODE_GUEST
                        ? R.string.view_host_disconnected
                        : R.string.view_guests_disconnected);
        iFinishedView.setStatusText(text);
        iFinishedView.show();
    }

    @Override
    public boolean onBackPress() {
        if (mCurrentMode instanceof ShareAnchorMode || mCurrentMode instanceof EditMode || mCurrentMode instanceof ScreenshotMode) {
            getSXRContext().runOnGlThread(() -> mHandlerBackToHud.OnBackToHud());
        }

        if (mCurrentMode instanceof HudMode || mCurrentMode == null) {
            getSXRContext().runOnGlThread(this::showViewExit);
        }
        return true;
    }

    @Subscribe
    public void handleConnectionEvent(PetConnectionEvent message) {
        if (message.getType() == EVENT_ALL_CONNECTIONS_LOST) {
            if (mCurrentMode instanceof HudMode) {
                int mode = mSharedMixedReality.getMode();
                getSXRContext().runOnGlThread(() -> showViewConnectionFinished(mode));
                mSharedMixedReality.stopSharing();
                mPet.stopBall();
            }
        }
    }

    @Override
    public void onStep() {
        super.onStep();
        if (mCurrentMode != null) {
            mCurrentMode.handleOrientation();
        }
    }

    @Subscribe
    public void handleBallEvent(BallThrowHandlerEvent event) {
        if (event.getPerformedAction().equals(BallThrowHandlerEvent.THROWN)) {
            mPet.setCurrentAction(PetActions.TO_BALL.ID);
        } else if (event.getPerformedAction().equals(BallThrowHandlerEvent.RESET)) {
        }
    }

    public class HandlerModeChange implements OnModeChange {

        @Override
        public void onPlayBall() {
            if (mPet.isPlaying()) {
                mPet.stopBall();
            } else {
                mPet.playBall();
            }
            mPet.setCurrentAction(PetActions.IDLE.ID);
        }

        @Override
        public void onShareAnchor() {
            if (mCurrentMode instanceof ShareAnchorMode) {
                return;
            }

            if (mCurrentMode != null) {
                mCurrentMode.exit();
            }

            mCurrentMode = new ShareAnchorMode(mPetContext, mHandlerBackToHud);
            mCurrentMode.enter();
            mPet.stopBall();
            mPet.setCurrentAction(PetActions.IDLE.ID);
        }

        @Override
        public void onEditMode() {
            if (mCurrentMode instanceof EditMode) {
                return;
            }

            if (mCurrentMode != null) {
                mCurrentMode.exit();
            }

            mCurrentMode = new EditMode(mPetContext, mHandlerBackToHud, mPet);
            mCurrentMode.enter();
            ((EditMode) mCurrentMode).onEnableGesture(mCursorController);
            mPet.stopBall();
            mPet.setCurrentAction(PetActions.AT_EDIT.ID);

            // Edit mode will handle picker events
            mCursorController.removePickEventListener(mTouchEventsHandler);
        }

        @Override
        public void onScreenshot() {
            if (mCurrentMode instanceof ScreenshotMode) {
                return;
            }

            if (mCurrentMode != null) {
                mCurrentMode.exit();
            }

            mCurrentMode = new ScreenshotMode(mPetContext, mHandlerBackToHud);
            mCurrentMode.enter();
        }
    }

    public class HandlerBackToHud implements OnBackToHudModeListener {

        @Override
        public void OnBackToHud() {
            if (mCurrentMode instanceof EditMode) {
                mCursorController.addPickEventListener(mTouchEventsHandler);
            }

            mCurrentMode.exit();
            mCurrentMode = new HudMode(mPetContext, mPet, mHandlerModeChange);
            mCurrentMode.enter();

            if (mPetContext.getMode() != PetConstants.SHARE_MODE_NONE) {
                mPet.playBall();
            }
            mPet.setCurrentAction(PetActions.IDLE.ID);
        }
    }

    ITouchEvents mTouchEventsHandler = new ITouchEvents() {
        @Override
        public void onEnter(SXRNode sxrNode, SXRPicker.SXRPickedObject sxrPickedObject) {

        }

        @Override
        public void onExit(SXRNode sxrNode, SXRPicker.SXRPickedObject sxrPickedObject) {

        }

        @Override
        public void onTouchStart(SXRNode sxrNode, SXRPicker.SXRPickedObject sxrPickedObject) {

        }

        @Override
        public void onTouchEnd(SXRNode sxrNode, SXRPicker.SXRPickedObject sxrPickedObject) {
            if (sxrNode == null)
                return;

            Log.d(TAG, "onTouchEnd " + sxrNode.getName());

            if (mMainViewController != null && mMainViewController.isEnabled()) {
                return;
            }

            if (sxrNode.getParent() == null)
                return;

            SXRPlane selectedPlane = (SXRPlane)sxrNode.getParent().getComponent(SXRPlane.getComponentType());

            // TODO: Improve this if
            if (selectedPlane != null) {
                final float[] modelMtx = sxrNode.getTransform().getModelMatrix();

                if (!mPet.isRunning()) {
                    mPet.setPlane(sxrNode);
                    mPet.getView().getTransform().setPosition(modelMtx[12], modelMtx[13], modelMtx[14]);
                    mPet.enter();
                    mPet.setInitialScale();
                    mPet.enableActions();

                    if (mCurrentMode == null) {
                        mCurrentMode = new HudMode(mPetContext, mPet, mHandlerModeChange);
                        mCurrentMode.enter();
                    } else if (mCurrentMode instanceof HudMode) {
                        mCurrentMode.view().show(mPetContext.getMainScene());
                    }

                    mPlaneHandler.setSelectedPlane(selectedPlane, sxrNode);
                }

                if (sxrNode == mPet.getPlane() && mCurrentMode instanceof  HudMode) {
                    final float[] hitPos = sxrPickedObject.hitLocation;
                    Log.d(TAG, "goToTap(%f, %f, %f)", hitPos[0], hitPos[1], hitPos[2]);
                    mPet.goToTap(hitPos[0], hitPos[1], hitPos[2]);
                }
            }
        }

        @Override
        public void onInside(SXRNode sxrNode, SXRPicker.SXRPickedObject sxrPickedObject) {

        }

        @Override
        public void onMotionOutside(SXRPicker sxrPicker, MotionEvent motionEvent) {

        }
    };
}