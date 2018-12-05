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
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.samsungxr.IViewEvents;
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

    private LinearLayout mRootLayout, mMenuOptionsHud, mMenuButton, mCloseButton, mShareAnchorButton, mCameraButton, mEditModeButton;
    private LinearLayout mActionsButton, mPlayBoneButton, mHydrantButton, mToSleepButton, mDrinkWater, mSubmenuOptions;
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
    private boolean mIsBoneButtonClicked = false;
    private boolean mIsActiveSubmenu = false;

    public HudView(PetContext petContext) {
        super(petContext);

        // Create a root layout to set the display metrics on it
        mRootLayout = new LinearLayout(petContext.getActivity());
        final DisplayMetrics metrics = new DisplayMetrics();
        petContext.getActivity().getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
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

    @Override
    public void onClick(final View view) {
        if (mListener == null) {
            return;
        }

        switch (view.getId()) {
            case R.id.btn_start_menu:
                mMenuButton.post(() -> {
                    mMenuOptionsHud.startAnimation(mOpenMenuHud);
                    mMenuOptionsHud.setVisibility(View.VISIBLE);
                    mHudMenuObject.setEnable(true);
                });
                mMenuButton.setVisibility(View.GONE);
                mCloseButton.setVisibility(View.VISIBLE);
                break;
            case R.id.btn_close:
                mCloseButton.post(() -> {
                    mMenuOptionsHud.startAnimation(mCloseMenuHud);
                    mMenuOptionsHud.setVisibility(View.INVISIBLE);
                    mMenuOptionsHud.postDelayed(() -> mHudMenuObject.setEnable(false), 500);
                    if (mIsActiveSubmenu) {
                        mSubmenuOptions.startAnimation(mCloseSubmenu);
                        mSubmenuOptions.setVisibility(View.INVISIBLE);
                        mSubmenuOptions.postDelayed(() -> mSubmenuObject.setEnable(false), 500);
                    }
                    mIsActiveSubmenu = false;
                });
                mMenuButton.setVisibility(View.VISIBLE);
                mCloseButton.setVisibility(View.GONE);
                break;
            case R.id.btn_edit:
                mPetContext.getSXRContext().runOnGlThread(() -> mListener.onEditModeClicked());
                break;
            case R.id.btn_fetchbone:
                mPlayBoneButton.post(() -> {
                    mIsBoneButtonClicked = !mIsBoneButtonClicked;
                    mPlayBoneButton.setBackgroundResource(mIsBoneButtonClicked
                            ? R.drawable.bg_button_ball
                            : R.drawable.bg_functions_buttons
                    );
                });
                mPetContext.getSXRContext().runOnGlThread(() -> mListener.onBallClicked());
                break;
            case R.id.btn_shareanchor:
                mPetContext.getSXRContext().runOnGlThread(() -> mListener.onShareAnchorClicked());
                break;
            case R.id.btn_camera:
                mPetContext.getSXRContext().runOnGlThread(() -> mListener.onCameraClicked());
                break;
            case R.id.btn_connected:
                mPetContext.getSXRContext().runOnGlThread(() -> mListener.onConnectedClicked());
            case R.id.btn_actions:
                mActionsButton.post(() -> {
                    mIsActiveSubmenu = !mIsActiveSubmenu;
                    mSubmenuOptions.startAnimation(mIsActiveSubmenu
                            ? mOpenSubmenu : mCloseSubmenu);
                    mSubmenuOptions.setVisibility(mIsActiveSubmenu
                            ? View.VISIBLE
                            : View.INVISIBLE);
                    mSubmenuObject.setEnable(true);
                });
                break;
            default:
                Log.d(TAG, "Invalid Option");
        }
    }

    IViewEvents hudMenuInitEvents = new IViewEvents() {
        @Override
        public void onInitView(SXRViewNode sxrViewNode, View view) {
            mMenuOptionsHud = view.findViewById(R.id.menuHud);
            mEditModeButton = view.findViewById(R.id.btn_edit);
            mShareAnchorButton = view.findViewById(R.id.btn_shareanchor);
            mCameraButton = view.findViewById(R.id.btn_camera);
            mActionsButton = view.findViewById(R.id.btn_actions);
            mEditModeButton.setOnClickListener(HudView.this);
            mShareAnchorButton.setOnClickListener(HudView.this);
            mCameraButton.setOnClickListener(HudView.this);
            mActionsButton.setOnClickListener(HudView.this);
            mOpenMenuHud = AnimationUtils.loadAnimation(mPetContext.getActivity(), R.anim.open);
            mCloseMenuHud = AnimationUtils.loadAnimation(mPetContext.getActivity(), R.anim.close);
        }

        @Override
        public void onStartRendering(SXRViewNode sxrViewNode, View view) {
            sxrViewNode.setTextureBufferSize(PetConstants.TEXTURE_BUFFER_SIZE);
            sxrViewNode.getRenderData().setRenderingOrder(SXRRenderData.SXRRenderingOrder.OVERLAY);
            LayoutViewUtils.setWorldPosition(mPetContext.getMainScene(),
                    sxrViewNode, 590f, 20f, 44f, 270f);
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
                    sxrViewNode, 590f, 304f, 44f, 44f);
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
            mToSleepButton = view.findViewById(R.id.btn_toSleep);
            mDrinkWater = view.findViewById(R.id.drinkWater);
            mPlayBoneButton.setOnClickListener(HudView.this);
            mHydrantButton.setOnClickListener(HudView.this);
            mToSleepButton.setOnClickListener(HudView.this);
            mDrinkWater.setOnClickListener(HudView.this);
            mOpenSubmenu = AnimationUtils.loadAnimation(mPetContext.getActivity(), R.anim.open);
            mCloseSubmenu = AnimationUtils.loadAnimation(mPetContext.getActivity(), R.anim.close);
        }

        @Override
        public void onStartRendering(SXRViewNode sxrViewNode, View view) {
            sxrViewNode.setTextureBufferSize(PetConstants.TEXTURE_BUFFER_SIZE);
            sxrViewNode.getRenderData().setRenderingOrder(SXRRenderData.SXRRenderingOrder.OVERLAY);
            LayoutViewUtils.setWorldPosition(mPetContext.getMainScene(),
                    sxrViewNode, 525f, 74f, 90f, 90f);
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
}
