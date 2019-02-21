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

import com.samsungxr.ITouchEvents;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRNode;
import com.samsungxr.SXRPicker;
import com.samsungxr.arpet.character.CharacterController;
import com.samsungxr.arpet.constant.PetConstants;
import com.samsungxr.arpet.custom.TouchEventsAdapter;
import com.samsungxr.arpet.mainview.IExitView;
import com.samsungxr.arpet.mainview.MainViewController;
import com.samsungxr.arpet.manager.connection.event.PetConnectionEvent;
import com.samsungxr.arpet.mode.HudMode;
import com.samsungxr.arpet.mode.ILoadEvents;
import com.samsungxr.arpet.mode.IPetMode;
import com.samsungxr.arpet.mode.OnBackToHudModeListener;
import com.samsungxr.arpet.mode.OnModeChange;
import com.samsungxr.arpet.mode.photo.ScreenshotMode;
import com.samsungxr.arpet.mode.sharinganchor.SharingAnchorMode;
import com.samsungxr.arpet.movement.PetActions;
import com.samsungxr.arpet.service.share.SharedMixedReality;
import com.samsungxr.arpet.util.EventBusUtils;
import com.samsungxr.arpet.view.shared.IConnectionFinishedView;
import com.samsungxr.io.SXRCursorController;
import com.samsungxr.io.SXRGazeCursorController;
import com.samsungxr.io.SXRInputManager;
import com.samsungxr.mixedreality.IMixedReality;
import com.samsungxr.mixedreality.IMixedRealityEvents;
import com.samsungxr.mixedreality.SXRMixedReality;
import com.samsungxr.mixedreality.SXRPlane;
import com.samsungxr.nodes.SXRViewNode;
import com.samsungxr.utility.Log;

import org.greenrobot.eventbus.Subscribe;

import java.util.Arrays;
import java.util.EnumSet;

import static com.samsungxr.arpet.manager.connection.IPetConnectionManager.EVENT_ALL_CONNECTIONS_LOST;


public class PetMain extends DisableNativeSplashScreen {
    private static final String TAG = "SXR_ARPET";

    private PetContext mPetContext;

    private PlaneHandler mPlaneHandler;

    private PointCloudHandler mPointCloudHandler;

    private IPetMode mCurrentMode;
    private HandlerModeChange mHandlerModeChange;
    private HandlerBackToHud mHandlerBackToHud;

    private CharacterController mPet = null;

    private SXRCursorController mCursorController = null;

    private CurrentSplashScreen mCurrentSplashScreen;
    private SharedMixedReality mSharedMixedReality;

    private MainViewController mMainViewController = null;

    private ViewInitialMessage mViewInitialMessage;
    private ViewChoosePlan mChoosePlan = null;

    PetMain(PetContext petContext) {
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
        mPointCloudHandler = new PointCloudHandler(mPetContext);

        mSharedMixedReality = mPetContext.getMixedReality();

        mPetContext.registerPlaneListener(mPlaneHandler);
        mSharedMixedReality.getEventReceiver().addListener(mMixedRealityHandler);
        mSharedMixedReality.getEventReceiver().addListener(mPointCloudHandler);
        mPetContext.getMixedReality().resume();


        mPet = new CharacterController(mPetContext);
        mPet.load(new ILoadEvents() {
            @Override
            public void onSuccess() {
                // Will wet pet's scene as the main scene
                mCurrentSplashScreen.onHide(mPetContext.getMainScene());
                //Show initial message
                mViewInitialMessage = new ViewInitialMessage(mPetContext);
                mViewInitialMessage.onShow(mPetContext.getMainScene());

                // Set plane handler in pet context
                mPetContext.setPlaneHandler(mPlaneHandler);

                // Set pet controller in pet context
                mPetContext.setPetController(mPet);

            }

            @Override
            public void onFailure() {
                mPetContext.getActivity().finish();
            }
        });
    }

    void onARInit(IMixedReality mr) {
        mCursorController = null;
        SXRInputManager inputManager = mPetContext.getSXRContext().getInputManager();
        final int cursorDepth = 5;
        final EnumSet<SXRPicker.EventOptions> eventOptions = EnumSet.of(
                SXRPicker.EventOptions.SEND_PICK_EVENTS,
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

    void resume() {
        EventBusUtils.register(this);
    }

    void pause() {
        EventBusUtils.unregister(this);
    }

    private void showViewExit() {
        if (mMainViewController == null) {
            mMainViewController = new MainViewController(mPetContext);
            mMainViewController.onShow(mPetContext.getMainScene());
            IExitView iExitView = mMainViewController.makeView(IExitView.class);

            iExitView.setOnCancelClickListener(view -> {
                if (mMainViewController != null) {
                    mMainViewController.onHide(mPetContext.getMainScene());
                    mMainViewController = null;
                }
            });
            iExitView.setOnConfirmClickListener(view -> {
                getSXRContext().getActivity().finish();
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(1);
            });

            iExitView.show();
        }
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
        if (mCurrentMode instanceof SharingAnchorMode || mCurrentMode instanceof ScreenshotMode) {
            getSXRContext().runOnGlThread(() -> mHandlerBackToHud.OnBackToHud());
        }

        if (mCurrentMode instanceof HudMode || mCurrentMode == null) {
            if (mCurrentMode != null && !((HudMode) mCurrentMode).isPromptEnabled()) {
                getSXRContext().runOnGlThread(this::showViewExit);
            }
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
                mPet.stopBone();
                if (mode == PetConstants.SHARE_MODE_GUEST) {
                    mPet.exit();
                }
            }
        }
    }

    @Subscribe
    public void handlePlaneDetected(SXRPlane plane) {
        mViewInitialMessage.onHide(mPetContext.getMainScene());
        if (mChoosePlan == null) {
            mChoosePlan = new ViewChoosePlan(mPetContext);
            mChoosePlan.onShow(mPetContext.getMainScene());
        }
    }

    @Override
    public void onStep() {
        super.onStep();
    }

    @Subscribe
    public void handleBallEvent(BallThrowHandlerEvent event) {
        if (event.getPerformedAction().equals(BallThrowHandlerEvent.THROWN)) {
            mPet.setCurrentAction(PetActions.TO_BALL.ID);
        }
    }

    public class HandlerModeChange implements OnModeChange {

        @Override
        public void onShareAnchor() {
            if (mCurrentMode instanceof SharingAnchorMode) {
                return;
            }

            if (mCurrentMode != null) {
                mCurrentMode.exit();
            }

            mCurrentMode = new SharingAnchorMode(mPetContext, mHandlerBackToHud);
            mCurrentMode.enter();
            mPet.stopBone();
            mPet.setCurrentAction(PetActions.IDLE.ID);
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
            if (mCurrentMode instanceof ScreenshotMode) {
                mCursorController.addPickEventListener(mTouchEventsHandler);
            }

            mCurrentMode.exit();
            mCurrentMode = new HudMode(mPetContext, mPet, mHandlerModeChange);
            mCurrentMode.enter();

            mPet.setCurrentAction(PetActions.IDLE.ID);
        }
    }

    /**
     * Checks if the given picked object contains some {@link SXRViewNode}
     *
     * @param sxrPickedObject Holds the picked objects array
     * @return Whether exists some clicked object of type {@link SXRViewNode}
     */
    private boolean hasViewNode(SXRPicker.SXRPickedObject sxrPickedObject) {
        SXRPicker.SXRPickedObject[] picked = sxrPickedObject.getPicker().getPicked();
        if (picked != null) {
            return Arrays.stream(picked).anyMatch(p -> p.hitObject instanceof SXRViewNode);
        }
        return false;
    }

    private ITouchEvents mTouchEventsHandler = new TouchEventsAdapter() {

        @Override
        public void onTouchEnd(SXRNode sxrNode, SXRPicker.SXRPickedObject sxrPickedObject) {

            // Ignores if is playing with bone
            if (mPet.isPlaying()) {
                return;
            }

            // Ignores if some view is clicked
            if (hasViewNode(sxrPickedObject)) {
                return;
            }

            // The MainViewController manages views in full screen
            if (mMainViewController != null && mMainViewController.isEnabled()) {
                return;
            }

            if (sxrNode == null || sxrNode.getParent() == null) {
                return;
            }

            Log.d(TAG, "onTouchEnd " + sxrNode.getName());

            SXRPlane selectedPlane = (SXRPlane) sxrNode.getParent().getComponent(SXRPlane.getComponentType());

            // TODO: Improve this if
            if (selectedPlane != null) {
                if (mChoosePlan != null) {
                    mChoosePlan.onHide(mPetContext.getMainScene());
                    mChoosePlan = null;
                }

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

                    // remove point cloud
                    mPointCloudHandler.removeFromScene();
                    mSharedMixedReality.getEventReceiver().removeListener(mPointCloudHandler);
                }

                if (sxrNode == mPet.getPlane() && mCurrentMode instanceof HudMode) {
                    final float[] hitPos = sxrPickedObject.hitLocation;
                    mPet.goToTap(hitPos[0], hitPos[1], hitPos[2]);
                }
            }
        }
    };

    private IMixedRealityEvents mMixedRealityHandler = new IMixedRealityEvents() {
        @Override
        public void onMixedRealityStart(IMixedReality mixedReality) {
            onARInit(mixedReality);
            mixedReality.setPlaneFindingMode(SXRMixedReality.PlaneFindingMode.HORIZONTAL);
        }

        @Override
        public void onMixedRealityStop(IMixedReality mixedReality) {

        }

        @Override
        public void onMixedRealityUpdate(IMixedReality mixedReality) {

        }
    };
}