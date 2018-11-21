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
import com.samsungxr.nodes.SXRViewNode;
import com.samsungxr.utility.Log;

import com.samsungxr.arpet.PetContext;
import com.samsungxr.arpet.R;
import com.samsungxr.arpet.connection.socket.ConnectionMode;
import com.samsungxr.arpet.constant.PetConstants;
import com.samsungxr.arpet.util.LayoutViewUtils;

public class HudView extends BasePetView implements View.OnClickListener {
    private static final String TAG = "HudView";

    private LinearLayout rootLayout, menuHud, menuButton, closeButton, playBoneButton, shareAnchorButton, cameraButton, editModeButton;
    private final SXRViewNode mHudMenuObject;
    private final SXRViewNode mStartMenuObject;
    private final SXRViewNode mConnectedLabel;
    private final SXRViewNode mDisconnectViewObject;
    private Button connectedButton, cancelButton, disconnectButton;
    private TextView disconnectViewMessage;
    private OnHudItemClicked mListener;
    private OnDisconnectClicked mDisconnectListener;
    private OnClickDisconnectViewHandler mDisconnectViewHandler;
    private Animation openAnimation;
    private Animation closeAnimation;
    private boolean mIsBoneButtonClicked = false;

    public HudView(PetContext petContext) {
        super(petContext);

        // Create a root layout to set the display metrics on it
        rootLayout = new LinearLayout(petContext.getActivity());
        final DisplayMetrics metrics = new DisplayMetrics();
        petContext.getActivity().getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
        rootLayout.setLayoutParams(new LinearLayout.LayoutParams(metrics.widthPixels, metrics.heightPixels));

        View.inflate(petContext.getActivity(), R.layout.view_disconnect_sharing, rootLayout);

        mListener = null;
        mDisconnectListener = null;
        mStartMenuObject = new SXRViewNode(petContext.getSXRContext(),
                R.layout.hud_start_layout, startMenuInitEvents);
        mHudMenuObject = new SXRViewNode(petContext.getSXRContext(),
                R.layout.hud_menus_layout, hudMenuInitEvents);
        mConnectedLabel = new SXRViewNode(petContext.getSXRContext(),
                R.layout.share_connected_layout, connectButtonInitEvents);
        mDisconnectViewObject = new SXRViewNode(petContext.getSXRContext(), rootLayout);
        rootLayout.post(new Runnable() {
            @Override
            public void run() {
                disconnectViewInitEvents.onInitView(mDisconnectViewObject, rootLayout);
                disconnectViewInitEvents.onStartRendering(mDisconnectViewObject, rootLayout);
            }
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
            disconnectViewMessage.setText(R.string.disconnect_host);
        } else {
            disconnectViewMessage.setText(R.string.disconnect_guest);
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
                menuButton.post(new Runnable() {
                    @Override
                    public void run() {
                        menuHud.startAnimation(openAnimation);
                        menuHud.setVisibility(View.VISIBLE);
                        mHudMenuObject.setEnable(true);
                    }
                });
                menuButton.setVisibility(View.GONE);
                closeButton.setVisibility(View.VISIBLE);

                break;
            case R.id.btn_close:
                closeButton.post(new Runnable() {
                    @Override
                    public void run() {
                        menuHud.startAnimation(closeAnimation);
                        menuHud.setVisibility(View.INVISIBLE);
                        menuHud.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mHudMenuObject.setEnable(false);
                            }
                        }, 500);
                    }
                });

                menuButton.setVisibility(View.VISIBLE);
                closeButton.setVisibility(View.GONE);

                break;
            case R.id.btn_edit:
                mPetContext.getSXRContext().runOnGlThread(new Runnable() {
                    @Override
                    public void run() {
                        mListener.onEditModeClicked();
                    }
                });
                break;
            case R.id.btn_fetchbone:
                playBoneButton.post(new Runnable() {
                    @Override
                    public void run() {
                        menuHud.startAnimation(closeAnimation);
                        menuHud.setVisibility(View.INVISIBLE);
                        closeButton.setVisibility(View.GONE);
                        menuButton.setVisibility(View.VISIBLE);
                        mIsBoneButtonClicked = !mIsBoneButtonClicked;
                        playBoneButton.setBackgroundResource(mIsBoneButtonClicked
                                ? R.drawable.bg_button_ball
                                : R.drawable.bg_functions_buttons
                        );
                    }
                });
                mPetContext.getSXRContext().runOnGlThread(new Runnable() {
                    @Override
                    public void run() {
                        mListener.onBallClicked();
                    }
                });
                break;
            case R.id.btn_shareanchor:
                mPetContext.getSXRContext().runOnGlThread(new Runnable() {
                    @Override
                    public void run() {
                        mListener.onShareAnchorClicked();
                    }
                });
                break;
            case R.id.btn_camera:
                mPetContext.getSXRContext().runOnGlThread(new Runnable() {
                    @Override
                    public void run() {
                        mListener.onCameraClicked();
                    }
                });
                break;
            case R.id.btn_connected:
                mPetContext.getSXRContext().runOnGlThread(new Runnable() {
                    @Override
                    public void run() {
                        mListener.onConnectedClicked();
                    }
                });
                break;
            default:
                Log.d(TAG, "Invalid Option");
        }
    }

    IViewEvents hudMenuInitEvents = new IViewEvents() {
        @Override
        public void onInitView(SXRViewNode sxrViewNode, View view) {
            menuHud = view.findViewById(R.id.menuHud);
            editModeButton = view.findViewById(R.id.btn_edit);
            playBoneButton = view.findViewById(R.id.btn_fetchbone);
            shareAnchorButton = view.findViewById(R.id.btn_shareanchor);
            cameraButton = view.findViewById(R.id.btn_camera);
            editModeButton.setOnClickListener(HudView.this);
            playBoneButton.setOnClickListener(HudView.this);
            shareAnchorButton.setOnClickListener(HudView.this);
            cameraButton.setOnClickListener(HudView.this);
            openAnimation = AnimationUtils.loadAnimation(mPetContext.getActivity(), R.anim.open);
            closeAnimation = AnimationUtils.loadAnimation(mPetContext.getActivity(), R.anim.close);
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
            menuButton = view.findViewById(R.id.btn_start_menu);
            closeButton = view.findViewById(R.id.btn_close);
            menuButton.setOnClickListener(HudView.this);
            closeButton.setOnClickListener(HudView.this);
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
            connectedButton = view.findViewById(R.id.btn_connected);
            connectedButton.setOnClickListener(HudView.this);
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
            disconnectViewMessage = view.findViewById(R.id.disconnect_message_text);
            cancelButton = view.findViewById(R.id.button_cancel);
            disconnectButton = view.findViewById(R.id.button_disconnect);
            mDisconnectViewHandler = new OnClickDisconnectViewHandler();
            cancelButton.setOnClickListener(mDisconnectViewHandler);
            disconnectButton.setOnClickListener(mDisconnectViewHandler);
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
