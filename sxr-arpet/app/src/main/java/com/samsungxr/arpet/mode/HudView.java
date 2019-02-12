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

package com.samsungxr.arpet.mode;

import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.samsungxr.IViewEvents;
import com.samsungxr.SXRDrawFrameListener;
import com.samsungxr.SXRRenderData;
import com.samsungxr.SXRScene;
import com.samsungxr.arpet.PetContext;
import com.samsungxr.arpet.R;
import com.samsungxr.arpet.connection.socket.ConnectionMode;
import com.samsungxr.arpet.constant.PetConstants;
import com.samsungxr.arpet.util.LayoutViewUtils;
import com.samsungxr.nodes.SXRViewNode;
import com.samsungxr.utility.Log;

public class HudView extends BasePetView implements View.OnClickListener {
    private static final String TAG = "HudView";

    private View mMenuOptionsHud, mShareAnchorButton, mCameraButton, mCleanButton, mCloseButton, mMenuButton;
    private View mHydrantButton, mBedButton, mBowlButton, mSubmenuOptions, mAboutButton;
    private ImageView mActionsButton, mPlayBoneButton;
    private LinearLayout mRootLayout;
    private final SXRViewNode mHudMenuObject;
    private final SXRViewNode mStartMenuObject;
    private final SXRViewNode mConnectedLabel;
    private final SXRViewNode mDisconnectViewObject;
    private final SXRViewNode mSubmenuObject;
    private Button mConnectedButton, mCancelButton, mDisconnectButton;
    private TextView mDisconnectViewMessage;
    private OnHudItemClicked mListener;
    private OnDisconnectClicked mDisconnectListener;
    private OnClickDisconnectViewHandler mDisconnectViewHandler;
    private Animation mOpenMenuHud, mOpenSubmenu;
    private Animation mCloseMenuHud, mCloseSubmenu;
    private Animation mBounce;
    private boolean mIsActivedSubmenu = false;
    private boolean mIsActionsButtonActived = false;
    private BounceInterpolator interpolator = new BounceInterpolator(0.1, 20);

    private final PetContext mPetContext;

    private BounceView bounceView = new BounceView();

    public HudView(PetContext petContext) {
        super(petContext);

        // Create a root layout to set the display metrics on it
        mRootLayout = new LinearLayout(petContext.getActivity());
        final DisplayMetrics metrics = new DisplayMetrics();
        petContext.getActivity().getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
        mPetContext = petContext;
        mRootLayout.setLayoutParams(new LinearLayout.LayoutParams(metrics.widthPixels, metrics.heightPixels));

        View.inflate(petContext.getActivity(), R.layout.view_disconnect_sharing, mRootLayout);

        mListener = null;
        mDisconnectListener = null;
        mStartMenuObject = new SXRViewNode(petContext.getSXRContext(),
                R.layout.hud_start_layout, startMenuInitEvents);
        mSubmenuObject = new SXRViewNode(petContext.getSXRContext(),
                R.layout.actions_submenus_layout, startSubmenuInitEvents);
        mHudMenuObject = new SXRViewNode(petContext.getSXRContext(),
                R.layout.hud_menus_layout, hudMenuInitEvents);
        mConnectedLabel = new SXRViewNode(petContext.getSXRContext(),
                R.layout.share_connected_layout, connectButtonInitEvents);
        mDisconnectViewObject = new SXRViewNode(petContext.getSXRContext(), mRootLayout);
        mRootLayout.post(() -> {
            disconnectViewInitEvents.onInitView(mDisconnectViewObject, mRootLayout);
            disconnectViewInitEvents.onStartRendering(mDisconnectViewObject, mRootLayout);
        });
    }

    @Override
    protected void onShow(SXRScene mainScene) {
        mConnectedLabel.setEnable(mPetContext.getMode() != PetConstants.SHARE_MODE_NONE);
        mStartMenuObject.setEnable(mPetContext.getMode() != PetConstants.SHARE_MODE_GUEST);
        mainScene.getMainCameraRig().addChildObject(this);
    }

    public void hideDisconnectView() {
        mDisconnectViewObject.setEnable(false);
    }

    public void showDisconnectView(@ConnectionMode int mode) {
        if (mode == ConnectionMode.SERVER) {
            mDisconnectViewMessage.setText(R.string.disconnect_host);
        } else {
            mDisconnectViewMessage.setText(R.string.disconnect_guest);
        }
        mDisconnectViewObject.setEnable(true);
    }

    public void hideConnectedLabel() {
        mConnectedLabel.setEnable(false);
    }

    public void showConnectedLabel() {
        mConnectedLabel.setEnable(true);
    }

    @Override
    protected void onHide(SXRScene mainScene) {
        mainScene.getMainCameraRig().removeChildObject(this);
    }

    public void setListener(OnHudItemClicked listener) {
        mListener = listener;
    }

    public void setDisconnectListener(OnDisconnectClicked listener) {
        mDisconnectListener = listener;
    }

    private class BounceView implements SXRDrawFrameListener {
        private float scaleX, scaleY, scaleZ;
        private float countTime;
        private final float DURATION = 0.5f;
        private final float TIME_OFFSET = 0.34f;
        private final float BOUNCE_LIMIT = 1.2f;
        private final float ACCELERATION = 7.8f;
        private SXRViewNode target;

        void startAnimation(SXRViewNode viewNode) {
            scaleX = viewNode.getTransform().getScaleX();
            scaleY = viewNode.getTransform().getScaleY();
            scaleZ = viewNode.getTransform().getScaleZ();
            countTime = 0f;
            target = viewNode;

            mPetContext.getSXRContext().registerDrawFrameListener(this);
        }

        @Override
        public void onDrawFrame(float d) {
            if (countTime >= DURATION) {
                mPetContext.getSXRContext().unregisterDrawFrameListener(this);
                target.getTransform().setScale(scaleX, scaleY, scaleZ);
                target = null;
            } else {
                float t = countTime - TIME_OFFSET;
                float s = -1f * ACCELERATION * t * t + BOUNCE_LIMIT;
                target.getTransform().setScale(scaleX * s, scaleY * s, scaleZ);
                countTime += d;
            }
        }
    }

    @Override
    public void onClick(final View view) {
        if (mListener == null) {
            return;
        }

        switch (view.getId()) {
            case R.id.btn_start_menu:
                setStateInMenuButtons();
                mMenuButton.setVisibility(View.GONE);
                mCloseButton.setVisibility(View.VISIBLE);
                bounceView.startAnimation(mStartMenuObject);
                mMenuOptionsHud.startAnimation(mOpenMenuHud);
                mMenuOptionsHud.setVisibility(View.VISIBLE);
                mHudMenuObject.setEnable(true);
                break;
            case R.id.btn_close:
                closeMenu();
                break;
            case R.id.btn_clean:
                mCleanButton.startAnimation(mBounce);
                mBounce.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        mPetContext.getSXRContext().runOnGlThread(() -> mListener.onCleanClicked());
                        mBounce.setAnimationListener(null);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                mCleanButton.post(this::closeMenu);
                break;
            case R.id.btn_fetchbone:
                mPlayBoneButton.startAnimation(mBounce);
                mPlayBoneButton.setActivated(!mPlayBoneButton.isActivated());
                mBounce.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        mPetContext.getSXRContext().runOnGlThread(() -> mListener.onBoneClicked());
                        mBounce.setAnimationListener(null);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                mPlayBoneButton.post(this::closeMenu);
                break;
            case R.id.btn_bed:
                mBedButton.startAnimation(mBounce);
                mBounce.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        mPetContext.getSXRContext().runOnGlThread(() -> mListener.onBedClicked());
                        mBounce.setAnimationListener(null);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                mBedButton.post(this::closeMenu);
                break;
            case R.id.btn_hydrant:
                mHydrantButton.startAnimation(mBounce);
                mBounce.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        mPetContext.getSXRContext().runOnGlThread(() -> mListener.onHydrantClicked());
                        mBounce.setAnimationListener(null);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                mHydrantButton.post(this::closeMenu);
                break;
            case R.id.btn_bowl:
                mBowlButton.startAnimation(mBounce);
                mBounce.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        mPetContext.getSXRContext().runOnGlThread(() -> mListener.onBowlClicked());
                        mBounce.setAnimationListener(null);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                mBowlButton.post(this::closeMenu);
                break;
            case R.id.btn_shareanchor:
                mShareAnchorButton.startAnimation(mBounce);
                mBounce.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        mPetContext.getSXRContext().runOnGlThread(() -> mListener.onShareAnchorClicked());
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                break;
            case R.id.btn_camera:
                mCameraButton.startAnimation(mBounce);
                mBounce.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        mPetContext.getSXRContext().runOnGlThread(() -> mListener.onCameraClicked());
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                break;
            case R.id.btn_connected:
                mPetContext.getSXRContext().runOnGlThread(() -> mListener.onConnectedClicked());
                break;
            case R.id.btn_actions:
                mActionsButton.startAnimation(mBounce);
                mBounce.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        setStateInActionButtons();
                        mIsActionsButtonActived = !mIsActionsButtonActived;
                        mActionsButton.setImageResource(mIsActionsButtonActived
                                ? R.drawable.ic_actions_activated : R.drawable.ic_actions);
                        mIsActivedSubmenu = !mIsActivedSubmenu;
                        mSubmenuOptions.startAnimation(mIsActivedSubmenu
                                ? mOpenSubmenu : mCloseSubmenu);
                        mSubmenuOptions.setVisibility(mIsActivedSubmenu
                                ? View.VISIBLE
                                : View.INVISIBLE);
                        mSubmenuObject.setEnable(true);
                        mBounce.setAnimationListener(null);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                break;
            case R.id.btn_about:
                mAboutButton.startAnimation(mBounce);
                mBounce.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        mPetContext.getSXRContext().runOnGlThread(() -> mListener.onAbout());
                        mBounce.setAnimationListener(null);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                mAboutButton.post(this::closeMenu);
                break;
            default:
                Log.d(TAG, "Invalid Option");
        }
    }

    public void deactivateBoneButton() {
        mPlayBoneButton.setActivated(false);
    }

    public void setStateInMenuButtons() {
        final int shareMode = mPetContext.getMode();
        mCleanButton.setEnabled(shareMode == PetConstants.SHARE_MODE_NONE);
        mCleanButton.setClickable(shareMode == PetConstants.SHARE_MODE_NONE);
    }

    public void setStateInActionButtons() {
        final int shareMode = mPetContext.getMode();
        mHydrantButton.setEnabled(shareMode == PetConstants.SHARE_MODE_NONE);
        mHydrantButton.setClickable(shareMode == PetConstants.SHARE_MODE_NONE);
        mBedButton.setEnabled(shareMode == PetConstants.SHARE_MODE_NONE);
        mBedButton.setClickable(shareMode == PetConstants.SHARE_MODE_NONE);
        mBowlButton.setEnabled(shareMode == PetConstants.SHARE_MODE_NONE);
        mBowlButton.setClickable(shareMode == PetConstants.SHARE_MODE_NONE);
    }

    public void closeMenu() {
        mMenuButton.setVisibility(View.VISIBLE);
        mCloseButton.setVisibility(View.GONE);
        bounceView.startAnimation(mStartMenuObject);
        mMenuOptionsHud.startAnimation(mCloseMenuHud);
        mMenuOptionsHud.setVisibility(View.INVISIBLE);
        if (mIsActivedSubmenu) {
            mSubmenuOptions.startAnimation(mCloseSubmenu);
            mSubmenuOptions.setVisibility(View.INVISIBLE);
        }
        mIsActivedSubmenu = false;
        mHudMenuObject.setEnable(false);
        mSubmenuObject.setEnable(false);
    }

    IViewEvents hudMenuInitEvents = new IViewEvents() {
        @Override
        public void onInitView(SXRViewNode sxrViewNode, View view) {
            mMenuOptionsHud = view.findViewById(R.id.menuHud);
            mCleanButton = view.findViewById(R.id.btn_clean);
            mShareAnchorButton = view.findViewById(R.id.btn_shareanchor);
            mCameraButton = view.findViewById(R.id.btn_camera);
            mActionsButton = view.findViewById(R.id.btn_actions);
            mAboutButton = view.findViewById(R.id.btn_about);
            mCleanButton.setOnClickListener(HudView.this);
            mShareAnchorButton.setOnClickListener(HudView.this);
            mCameraButton.setOnClickListener(HudView.this);
            mActionsButton.setOnClickListener(HudView.this);
            mAboutButton.setOnClickListener(HudView.this);
            mAboutButton.setOnClickListener(HudView.this);
            mOpenMenuHud = AnimationUtils.loadAnimation(mPetContext.getActivity(), R.anim.open);
            mCloseMenuHud = AnimationUtils.loadAnimation(mPetContext.getActivity(), R.anim.close);
            mBounce = AnimationUtils.loadAnimation(mPetContext.getActivity(), R.anim.bounce);
            mBounce.setInterpolator(interpolator);
        }

        @Override
        public void onStartRendering(SXRViewNode sxrViewNode, View view) {
            sxrViewNode.setTextureBufferSize(PetConstants.TEXTURE_BUFFER_SIZE);
            sxrViewNode.getRenderData().setRenderingOrder(SXRRenderData.SXRRenderingOrder.OVERLAY);
            LayoutViewUtils.setWorldPosition(mPetContext.getMainScene(),
                    sxrViewNode, 593f, 20f, 44f, 270f);
            sxrViewNode.setEnable(false);
            addChildObject(sxrViewNode);
        }
    };

    IViewEvents startMenuInitEvents = new IViewEvents() {
        @Override
        public void onInitView(SXRViewNode sxrViewNode, View view) {
            mMenuButton = view.findViewById(R.id.btn_start_menu);
            mCloseButton = view.findViewById(R.id.btn_close);
            mMenuButton.setOnClickListener(HudView.this);
            mCloseButton.setOnClickListener(HudView.this);
        }

        @Override
        public void onStartRendering(SXRViewNode sxrViewNode, View view) {
            sxrViewNode.setTextureBufferSize(PetConstants.TEXTURE_BUFFER_SIZE);
            sxrViewNode.getRenderData().setRenderingOrder(SXRRenderData.SXRRenderingOrder.OVERLAY);
            LayoutViewUtils.setWorldPosition(mPetContext.getMainScene(),
                    sxrViewNode, 590f, 304f, 48f, 48f);
            addChildObject(sxrViewNode);
        }
    };

    IViewEvents connectButtonInitEvents = new IViewEvents() {
        @Override
        public void onInitView(SXRViewNode sxrViewNode, View view) {
            mConnectedButton = view.findViewById(R.id.btn_connected);
            mConnectedButton.setOnClickListener(HudView.this);
        }

        @Override
        public void onStartRendering(SXRViewNode sxrViewNode, View view) {
            sxrViewNode.getRenderData().setRenderingOrder(SXRRenderData.SXRRenderingOrder.OVERLAY);
            LayoutViewUtils.setWorldPosition(mPetContext.getMainScene(),
                    sxrViewNode, 4.0f, 4.0f, 144.0f, 44.0f);
            addChildObject(sxrViewNode);
        }
    };

    IViewEvents disconnectViewInitEvents = new IViewEvents() {
        @Override
        public void onInitView(SXRViewNode sxrViewNode, View view) {
            mDisconnectViewMessage = view.findViewById(R.id.disconnect_message_text);
            mCancelButton = view.findViewById(R.id.button_cancel);
            mDisconnectButton = view.findViewById(R.id.button_disconnect);
            mDisconnectViewHandler = new OnClickDisconnectViewHandler();
            mCancelButton.setOnClickListener(mDisconnectViewHandler);
            mDisconnectButton.setOnClickListener(mDisconnectViewHandler);
        }

        @Override
        public void onStartRendering(SXRViewNode sxrViewNode, View view) {
            sxrViewNode.setTextureBufferSize(PetConstants.TEXTURE_BUFFER_SIZE);
            sxrViewNode.getRenderData().setRenderingOrder(SXRRenderData.SXRRenderingOrder.OVERLAY);
            sxrViewNode.getTransform().setPosition(0.0f, 0.0f, -0.74f);
            sxrViewNode.setEnable(false);
            addChildObject(sxrViewNode);
        }
    };

    IViewEvents startSubmenuInitEvents = new IViewEvents() {

        @Override
        public void onInitView(SXRViewNode sxrViewNode, View view) {
            mSubmenuOptions = view.findViewById(R.id.submenu);
            mPlayBoneButton = view.findViewById(R.id.btn_fetchbone);
            mHydrantButton = view.findViewById(R.id.btn_hydrant);
            mBedButton = view.findViewById(R.id.btn_bed);
            mBowlButton = view.findViewById(R.id.btn_bowl);
            mPlayBoneButton.setOnClickListener(HudView.this);
            mHydrantButton.setOnClickListener(HudView.this);
            mBedButton.setOnClickListener(HudView.this);
            mBowlButton.setOnClickListener(HudView.this);
            mOpenSubmenu = AnimationUtils.loadAnimation(mPetContext.getActivity(), R.anim.open);
            mCloseSubmenu = AnimationUtils.loadAnimation(mPetContext.getActivity(), R.anim.close);
        }

        @Override
        public void onStartRendering(SXRViewNode sxrViewNode, View view) {
            sxrViewNode.setTextureBufferSize(PetConstants.TEXTURE_BUFFER_SIZE);
            sxrViewNode.getRenderData().setRenderingOrder(SXRRenderData.SXRRenderingOrder.OVERLAY);
            LayoutViewUtils.setWorldPosition(mPetContext.getMainScene(),
                    sxrViewNode, 522f, 69f, 90f, 90f);
            addChildObject(sxrViewNode);
        }
    };

    private class OnClickDisconnectViewHandler implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (mDisconnectListener == null) {
                return;
            }

            switch (v.getId()) {
                case R.id.button_cancel:
                    mDisconnectListener.onCancel();
                    break;
                case R.id.button_disconnect:
                    mDisconnectListener.onDisconnect();
                    break;
                default:
                    Log.d(TAG, "invalid ID in disconnect view handler");
            }
        }
    }

    private class BounceInterpolator implements Interpolator {
        private double mAmplitude;
        private double mFrequency;

        BounceInterpolator(double amplitude, double frequency) {
            mAmplitude = amplitude;
            mFrequency = frequency;
        }

        @Override
        public float getInterpolation(float time) {
            return (float) (-1 * Math.pow(Math.E, -time / mAmplitude) *
                    Math.cos(mFrequency * time) + 1);
        }
    }
}
