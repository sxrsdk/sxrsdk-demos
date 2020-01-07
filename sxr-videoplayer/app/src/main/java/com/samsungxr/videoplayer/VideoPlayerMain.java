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

package com.samsungxr.videoplayer;

import android.view.MotionEvent;
import android.view.View;

import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRAssetLoader;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRPicker;
import com.samsungxr.SXRRenderData;
import com.samsungxr.SXRScene;
import com.samsungxr.SXRNode;
import com.samsungxr.SXRTexture;
import com.samsungxr.ITouchEvents;
import com.samsungxr.io.SXRCursorController;
import com.samsungxr.io.SXRInputManager;
import com.samsungxr.nodes.SXRSphereNode;
import com.samsungxr.nodes.SXRCubeNode;
import com.samsungxr.utility.Log;
import com.samsungxr.videoplayer.component.DefaultFadeableObject;
import com.samsungxr.videoplayer.component.FadeableObject;
import com.samsungxr.videoplayer.component.LabelCursor;
import com.samsungxr.videoplayer.component.OnFadeFinish;
import com.samsungxr.videoplayer.component.gallery.Gallery;
import com.samsungxr.videoplayer.component.gallery.OnGalleryEventListener;
import com.samsungxr.videoplayer.component.video.VideoPlayer;
import com.samsungxr.videoplayer.component.video.player.DefaultPlayerListener;
import com.samsungxr.videoplayer.component.video.player.OnPlayerListener;
import com.samsungxr.videoplayer.event.DefaultTouchEvent;
import com.samsungxr.videoplayer.model.Video;
import com.samsungxr.videoplayer.network.NetworkListener;
import com.samsungxr.videoplayer.network.NetworkManager;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.security.SecureRandom;
import java.util.List;
import java.util.ArrayList;


public class VideoPlayerMain extends BaseVideoPlayerMain implements OnGalleryEventListener {

    private static final String TAG = VideoPlayerMain.class.getSimpleName();
    private static float CURSOR_DEPTH = -8.0f;
    private static float WIDTH_VIDEO_PLAYER = 10.0f;
    private static final float SCALE = 200.0f;
    private static final float CUBE_WIDTH = 250.0f;

    private SXRContext mContext;
    private SXRScene mScene;
    private SXRCursorController mCursorController;
    private VideoPlayer mVideoPlayer;
    private SXRNode mCurrentContainer;
    private FadeableObject mCurrentCursor, mParentCursor;
    private LabelCursor mLabelCursor;
    private Gallery mGallery;
    private NetworkManager mNetworkManager;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onInit(SXRContext sxrContext) {
        mContext = sxrContext;
        mScene = sxrContext.getMainScene();

        addSkyBox();
        initCursorController();
        createGallery();
        createVideoPlayer();
        createNetworkManager();
    }

    private void createNetworkManager() {
        mNetworkManager = new NetworkManager(mContext);
        mNetworkManager.register(new NetworkStateHandler());
        mNetworkManager.start();
    }

    private void createGallery() {
        mGallery = new Gallery(getSXRContext());
        mGallery.getTransform().setPositionZ(-8);
        mGallery.setOnGalleryEventListener(this);
        mScene.addNode(mGallery);
        mCurrentContainer = mGallery;
    }

    private void createVideoPlayer() {
        mVideoPlayer = new VideoPlayer(getSXRContext());
        mVideoPlayer.setControlWidgetAutoHide(true);
        mVideoPlayer.setPlayerListener(mOnPlayerListener);
        mVideoPlayer.setBackButtonClickListener(mBackButtonClickListener);
        mScene.addNode(mVideoPlayer);
        mVideoPlayer.setCursorObject(mParentCursor);
        mVideoPlayer.hide();
        mParentCursor.setEnable(true);
    }

    private void addSkyBox() {
        SXRAssetLoader loader = mContext.getAssetLoader();
        ArrayList<SXRTexture> mTextureList2;
        mTextureList2 = new ArrayList<SXRTexture>(6);
        mTextureList2.add(loader.loadTexture(new SXRAndroidResource(mContext, R.raw.city_cubeback)));
        mTextureList2.add(loader.loadTexture(new SXRAndroidResource(mContext, R.raw.city_cuberight)));
        mTextureList2.add(loader.loadTexture(new SXRAndroidResource(mContext, R.raw.city_cubefront)));
        mTextureList2.add(loader.loadTexture(new SXRAndroidResource(mContext, R.raw.city_cubeleft)));
        mTextureList2.add(loader.loadTexture(new SXRAndroidResource(mContext, R.raw.city_cubetop)));
        mTextureList2.add(loader.loadTexture(new SXRAndroidResource(mContext, R.raw.city_cubebottom)));
        SXRCubeNode mCubeEnvironment2 = new SXRCubeNode( mContext, false, mTextureList2, 2);
        mCubeEnvironment2.getTransform().setScale(CUBE_WIDTH, CUBE_WIDTH, CUBE_WIDTH);
        mScene.addNode(mCubeEnvironment2);

    }

    private void initCursorController() {
        mScene.getEventReceiver().addListener(mTouchHandler);
        SXRInputManager inputManager = mContext.getInputManager();
        inputManager.selectController(new SXRInputManager.ICursorControllerSelectListener() {
            public void onCursorControllerSelected(SXRCursorController newController, SXRCursorController oldController) {
                if (oldController != null) {
                    oldController.removePickEventListener(mTouchHandler);
                }
                mCursorController = newController;
                newController.addPickEventListener(mTouchHandler);
                newController.setCursorDepth(-CURSOR_DEPTH);
                newController.setCursorControl(SXRCursorController.CursorControl.CURSOR_CONSTANT_DEPTH);
                newController.setCursor(createCursor());
            }
        });
    }

    @Override
    public void onStep() {
        super.onStep();
        hideCursor();
    }

    private void createLabel() {
        String text = mContext.getActivity().getString(R.string.tap_to_reposition_view);
        mLabelCursor = new LabelCursor(mContext, 0.45f * WIDTH_VIDEO_PLAYER * 1.2f, 0.2f * WIDTH_VIDEO_PLAYER, text);
    }

    private SXRNode createCursor() {
        createLabel();
        mCurrentCursor = new DefaultFadeableObject(
                mContext,
                mContext.createQuad(0.1f * CURSOR_DEPTH, 0.1f * CURSOR_DEPTH),
                mContext.getAssetLoader().loadTexture(new SXRAndroidResource(mContext, R.raw.cursor))
        );
        mCurrentCursor.getRenderData().setDepthTest(false);
        mCurrentCursor.getRenderData().setRenderingOrder(SXRRenderData.SXRRenderingOrder.OVERLAY);
        mParentCursor = new DefaultFadeableObject(mContext);
        mParentCursor.addChildObject(mCurrentCursor);
        mParentCursor.addChildObject(mLabelCursor);
        return mParentCursor;
    }

    private ITouchEvents mTouchHandler = new DefaultTouchEvent() {
        @Override
        public void onMotionOutside(SXRPicker sxrPicker, MotionEvent motionEvent) {
            if (mVideoPlayer.is360VideoPlaying()) {
                mVideoPlayer.showAllControls();
            }
            repositionScene();
        }

        @Override
        public void onTouchStart(SXRNode sxrNode, SXRPicker.SXRPickedObject sxrPickedObject) {

            mVideoPlayer.showAllControls();

        }
    };

    private OnPlayerListener mOnPlayerListener = new DefaultPlayerListener() {
        @Override
        public void onPrepareFile(String title, long duration) {
            mVideoPlayer.show(new OnFadeFinish() {
                @Override
                public void onFadeFinished() {
                    mVideoPlayer.play();
                }
            });

        }
    };

    private View.OnClickListener mBackButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mVideoPlayer.hide(new OnFadeFinish() {
                @Override
                public void onFadeFinished() {
                    mGallery.setEnable(true);
                    mGallery.fadeIn();
                    mCurrentContainer = mGallery;
                    mParentCursor.setEnable(true);
                }
            });
        }
    };

    private class NetworkStateHandler implements NetworkListener {

        @Override
        public void onConnected(boolean isConnected) {
            mGallery.setIsConnected(isConnected);
            mVideoPlayer.setIsConnected(isConnected);
        }
    }

    private void repositionScene() {
        final float rotationX = getSXRContext().getMainScene().getMainCameraRig().getHeadTransform().getRotationX();
        final float rotationY = getSXRContext().getMainScene().getMainCameraRig().getHeadTransform().getRotationY();
        final float rotationZ = getSXRContext().getMainScene().getMainCameraRig().getHeadTransform().getRotationZ();
        final float rotationW = getSXRContext().getMainScene().getMainCameraRig().getHeadTransform().getRotationW();

        Quaternionf cursorRotation = new Quaternionf(rotationX, rotationY, rotationZ, rotationW);
        Vector3f lookat = new Vector3f(0, 0, 1);
        lookat.rotate(cursorRotation);
        lookat = lookat.normalize();

        com.samsungxr.utility.Log.d(TAG, "LookAt: " + lookat.x + ", " + lookat.y + ", " + lookat.z);

        Vector3f up = new Vector3f(0, 1, 0);
        Vector3f ownerXaxis = new Vector3f(0, 0, 0);
        Vector3f ownerYaxis = new Vector3f(0, 0, 0);

        up.cross(lookat.x, lookat.y, lookat.z, ownerXaxis);
        ownerXaxis = ownerXaxis.normalize();
        lookat.cross(ownerXaxis.x, ownerXaxis.y, ownerXaxis.z, ownerYaxis);
        ownerYaxis = ownerYaxis.normalize();

        float[] newModelMatrix = new float[]{
                ownerXaxis.x, ownerXaxis.y, ownerXaxis.z, 0.0f,
                ownerYaxis.x, ownerYaxis.y, ownerYaxis.z, 0.0f,
                lookat.x, lookat.y, lookat.z, 0.0f,
                0, 0, 0, 1.0f
        };

        mGallery.reposition(newModelMatrix);
        mVideoPlayer.reposition(newModelMatrix);
    }

    @Override
    public void onVideosSelected(final List<Video> videoList) {
        Log.d(TAG, "onVideosSelected: " + videoList);
        mGallery.fadeOut(new OnFadeFinish() {
            @Override
            public void onFadeFinished() {
                mGallery.setEnable(false);
                mCurrentContainer = mVideoPlayer.getWidgetsContainer();
                mVideoPlayer.prepare(videoList);
            }
        });
    }

    public void onResume() {
        if (mVideoPlayer != null) {
            mVideoPlayer.play();
        }
    }

    private void hideCursor() {
        final float rotateXScene = mCurrentContainer.getTransform().getRotationX();
        final float rotateYScene = mCurrentContainer.getTransform().getRotationY();
        final float rotateZScene = mCurrentContainer.getTransform().getRotationZ();
        final float rotateWScene = mCurrentContainer.getTransform().getRotationW();
        final float rotateXCamera = getSXRContext().getMainScene().getMainCameraRig().getHeadTransform().getRotationX();
        final float rotateYCamera = getSXRContext().getMainScene().getMainCameraRig().getHeadTransform().getRotationY();
        final float rotateZCamera = getSXRContext().getMainScene().getMainCameraRig().getHeadTransform().getRotationZ();
        final float rotateWCamera = getSXRContext().getMainScene().getMainCameraRig().getHeadTransform().getRotationW();

        Quaternionf quaternionfScene = new Quaternionf(rotateXScene, rotateYScene, rotateZScene, rotateWScene);
        Quaternionf quaternionfCamera = new Quaternionf(rotateXCamera, rotateYCamera, rotateZCamera, rotateWCamera);

        quaternionfScene.difference(quaternionfCamera);

        if (quaternionfScene.angle() > Math.PI / 4.0f) {
            enableInteractiveCursor();
        } else {
            disableInteractiveCursor();
        }
    }

    public void onPause() {
        if (mVideoPlayer != null) {
            mVideoPlayer.pause();
        }
    }

    public void enableInteractiveCursor() {
        if (!mVideoPlayer.is360VideoPlaying()) {
            mLabelCursor.show();
        }
        mCurrentCursor.hide();
    }

    public void disableInteractiveCursor() {
        mCurrentCursor.show();
        mLabelCursor.hide();
    }

    @Override
    public boolean onBackPress() {
        if (mVideoPlayer.isEnabled()) {
            return mVideoPlayer.onBackPressed();
        }
        return mGallery.onBackPressed();
    }
}
