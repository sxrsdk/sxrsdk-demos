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

package com.samsungxr.videoplayer.component.video.player;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.view.MotionEvent;
import android.view.Surface;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.dash.DashChunkSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.FileDataSource;
import com.google.android.exoplayer2.util.Util;

import com.samsungxr.ITouchEvents;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRExternalTexture;
import com.samsungxr.SXRMeshCollider;
import com.samsungxr.SXRNode;
import com.samsungxr.SXRPicker;
import com.samsungxr.SXRTransform;
import com.samsungxr.nodes.SXRSphereNode;
import com.samsungxr.nodes.SXRVideoNode;
import com.samsungxr.nodes.SXRVideoNode.SXRVideoType;
import com.samsungxr.videoplayer.VideoPlayerApp;
import com.samsungxr.videoplayer.component.FadeableObject;
import com.samsungxr.videoplayer.component.video.DefaultExoPlayer;
import com.samsungxr.videoplayer.model.Video;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static com.google.android.exoplayer2.Player.STATE_BUFFERING;
import static com.google.android.exoplayer2.Player.STATE_READY;

public class Player extends FadeableObject {

    private static final String TAG = Player.class.getSimpleName();
    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();

    private SXRContext mGvrContext;
    private DefaultExoPlayer mMediaPlayer;
    private Video[] mFiles;
    private LinkedList<Video> mPlayingNowQueue;
    private DataSource.Factory mFileDataSourceFactory;
    private DataSource.Factory mManifestDataSourceFactory;
    private DashChunkSource.Factory mDashChunkSourceFactory;
    private Video mPlayingNow;
    private OnPlayerListener mOnVideoPlayerListener;
    private ProgressHandler mProgressHandler = new ProgressHandler();
    private boolean mIsPlaying;
    private SXRVideoNode mVideo;
    private SXRVideoNode mFlatVideo;
    private SXRVideoNode m360Video;

    private static final float UNPICKED_COLOR_R = 0.7f;
    private static final float UNPICKED_COLOR_G = 0.7f;
    private static final float UNPICKED_COLOR_B = 0.7f;
    private static final float UNPICKED_COLOR_A = 1.0f;

    private static final float PICKED_COLOR_R = 1.0f;
    private static final float PICKED_COLOR_G = 0.0f;
    private static final float PICKED_COLOR_B = 0.0f;
    private static final float PICKED_COLOR_A = 1.0f;

    private static final float CLICKED_COLOR_R = 0.5f;
    private static final float CLICKED_COLOR_G = 0.5f;
    private static final float CLICKED_COLOR_B = 1.0f;
    private static final float CLICKED_COLOR_A = 1.0f;


    public Player(final SXRContext sxrContext) {
        super(sxrContext);

        this.mGvrContext = sxrContext;

        this.mFileDataSourceFactory = new DataSource.Factory() {
            @Override
            public DataSource createDataSource() {
                return new FileDataSource();
            }
        };

        createDashFactories();
        createVideoNode();
    }

    private void createVideoNode() {
        SXRExternalTexture texture = new SXRExternalTexture(mGvrContext);
        SurfaceTexture surfaceTexture = new SurfaceTexture(texture.getId());
        Surface surface = new Surface(surfaceTexture);

        mMediaPlayer = new DefaultExoPlayer(ExoPlayerFactory.newSimpleInstance(mGvrContext.getContext(), new DefaultTrackSelector()));
        mMediaPlayer.getPlayer().addListener(mPlayerListener);

        mFlatVideo = new SXRVideoNode(mGvrContext, mGvrContext.createQuad(1, .6f), mMediaPlayer, texture, SXRVideoType.MONO);
        mFlatVideo.attachCollider(new SXRMeshCollider(getSXRContext(), true));
        mFlatVideo.getTransform().setScale(10, 10, 1);
        mFlatVideo.getTransform().setPositionZ(-8.1f);
        addChildObject(mFlatVideo);

        SXRSphereNode sphere = new SXRSphereNode(mGvrContext, 72, 144, false);
        m360Video = new SXRVideoNode(mGvrContext, sphere.getRenderData().getMesh(), mMediaPlayer, texture, SXRVideoType.MONO);
        m360Video.getTransform().setScale(100f, 100f, 100f);
        addChildObject(m360Video);

        setFlatPlayer();
    }

    public void shadow() {
        float[] color = {0.4f, 0.4f, 0.4f};
        mFlatVideo.getRenderData().getMaterial().setColor(color[0], color[1], color[2]);
        m360Video.getRenderData().getMaterial().setColor(color[0], color[1], color[2]);
    }

    private void set360Player() {
        mFlatVideo.setEnable(false);
        m360Video.setEnable(true);
        mVideo = m360Video;
    }

    private void setFlatPlayer() {
        m360Video.setEnable(false);
        mFlatVideo.setEnable(true);
        mVideo = mFlatVideo;
    }

    public void playVideo() {
        logd("Request play video");
        playCurrentPrepared();
    }

    public void pauseVideo() {
        mMediaPlayer.pause();
        logd("Video paused");
    }

    public void stop() {
        mMediaPlayer.stop();
        logd("Video stopped");
    }

    public boolean is360PlayerActive() {
        return mVideo == m360Video;
    }

    public boolean isPlaying() {
        return mIsPlaying;
    }

    public long getDuration() {
        return mMediaPlayer.getDuration();
    }

    public long getProgress() {
        return mMediaPlayer.getCurrentPosition();
    }

    public Video getPlayingNow() {
        return mPlayingNow;
    }

    public void setProgress(long progress) {
        mMediaPlayer.seekTo(progress);
    }

    public void prepare(@NonNull List<Video> videos) {
        mMediaPlayer.pause();
        if (videos.size() > 0) {
            mFiles = videos.toArray(new Video[videos.size()]);
            mPlayingNowQueue = new LinkedList<>(videos);
            logd("preparedQueue: " + mPlayingNowQueue);
            prepareNextFile();
        } else {
            logd("Files array is empty");
        }
    }

    private void resetQueue() {
        mPlayingNowQueue = new LinkedList<>(Arrays.asList(mFiles));
    }

    public void prepareNextFile() {
        if (mPlayingNowQueue != null && !mPlayingNowQueue.isEmpty()) {
            mMediaPlayer.pause();
            mPlayingNow = mPlayingNowQueue.pop();
            MediaSource mediaSource = null;
            if (mPlayingNow.getVideoType() == Video.VideoType.LOCAL) {
                mediaSource = fileMediaSource(mPlayingNow);
            } else {
                mediaSource = dashMediaSource(mPlayingNow);
            }

            mMediaPlayer.prepare(mediaSource);
            logd("selectedFile: " + mPlayingNow);
        }
    }

    private void createDashFactories() {
        Context context = VideoPlayerApp.getInstance().getApplicationContext();
        String userAgent = Util.getUserAgent(context, "videoplayer");
        mManifestDataSourceFactory = new DefaultHttpDataSourceFactory(userAgent);
        mDashChunkSourceFactory = new DefaultDashChunkSource.Factory(new DefaultHttpDataSourceFactory(userAgent, BANDWIDTH_METER));
    }

    private MediaSource dashMediaSource(Video video) {
        Uri uri = Uri.parse(video.getPath());
        return new DashMediaSource.Factory(mDashChunkSourceFactory, mManifestDataSourceFactory).createMediaSource(uri);
    }

    private MediaSource fileMediaSource(Video video) {
        return new ExtractorMediaSource(
                Uri.parse(video.getPath()),
                mFileDataSourceFactory,
                new DefaultExtractorsFactory(), null, null
        );
    }

    private void playCurrentPrepared() {
        if (mPlayingNow.getIsRatio21() || mPlayingNow.getIs360tag() || mPlayingNow.getHas360onTitle()) {
            set360Player();
        } else {
            setFlatPlayer();
        }
        mMediaPlayer.start();
    }

    public boolean hasNextToPlay() {
        return mPlayingNowQueue != null && mPlayingNowQueue.size() > 0;
    }

    public int getNextIndexToPlay() {
        return mPlayingNowQueue != null && mPlayingNowQueue.size() > 0
                ? mFiles.length - mPlayingNowQueue.size()
                : 0;
    }

    public void setOnVideoPlayerListener(OnPlayerListener listener) {
        this.mOnVideoPlayerListener = listener;
    }

    private void notifyVideoPrepared(String title, long duration) {
        if (mOnVideoPlayerListener != null) {
            mOnVideoPlayerListener.onPrepareFile(title, duration);
        }
    }

    private void notifyVideoStarted() {
        if (mOnVideoPlayerListener != null) {
            mOnVideoPlayerListener.onStart();
        }
    }

    private void notifyVideoEnded() {
        if (mOnVideoPlayerListener != null) {
            mOnVideoPlayerListener.onFileEnd();
        }
    }

    private void notifyAllVideosEnded() {
        if (mOnVideoPlayerListener != null) {
            mOnVideoPlayerListener.onAllFilesEnd();
        }
    }

    private void notifyStartBuffering() {
        if (mOnVideoPlayerListener != null) {
            mOnVideoPlayerListener.onStartBuffering();
        }
    }


    private void notifyEndBuffering() {
        if (mOnVideoPlayerListener != null) {
            mOnVideoPlayerListener.onEndBuffering();
        }
    }

    private String getPlayingNowName() {
        return mPlayingNow != null ? mPlayingNow.getTitle() : "";
    }

    private static void logd(String text) {
        android.util.Log.d(TAG, text);
    }


    @NonNull
    @Override
    protected SXRNode getFadeable() {
        return mVideo;
    }

    @SuppressLint("HandlerLeak")
    private class ProgressHandler extends Handler {

        ProgressHandler() {
            super(Looper.getMainLooper());
        }

        @Override
        public void handleMessage(Message msg) {
            if (mOnVideoPlayerListener != null && mIsPlaying) {
                mOnVideoPlayerListener.onProgress(mMediaPlayer.getCurrentPosition());
                sendEmptyMessageDelayed(0, 100);
            } else {
                logd("Progress stopped");
            }
        }

        void start() {
            logd("Progress started");
            if (!hasMessages(0)) {
                sendEmptyMessage(0);
            }
        }
    }

    private com.google.android.exoplayer2.Player.EventListener mPlayerListener = new com.google.android.exoplayer2.Player.DefaultEventListener() {

        private int mPreviousState = -1;

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

            mIsPlaying = playbackState == STATE_READY && playWhenReady;

            if (playbackState == STATE_READY) {

                if (mPreviousState == STATE_BUFFERING) {
                    notifyEndBuffering();
                }

                if (playWhenReady) {
                    logd("Video started " + getPlayingNowName());
                    notifyVideoStarted();
                    mProgressHandler.start();
                } else {
                    if (mMediaPlayer.getCurrentPosition() == 0) {
                        logd("Video prepared: " + getPlayingNowName());
                        notifyVideoPrepared(getPlayingNowName(), mMediaPlayer.getDuration());
                    }
                }

            } else if (playbackState == com.google.android.exoplayer2.Player.STATE_ENDED) {

                logd("Video ended: " + getPlayingNowName());
                notifyVideoEnded();

                if (mPlayingNowQueue != null && mPlayingNowQueue.isEmpty()) {
                    logd("All videos ended: " + getPlayingNowName());
                    // resetQueue();
                    notifyAllVideosEnded();
                }

                // Goes to IDLE state
                mMediaPlayer.stop();

            } else if (playbackState == com.google.android.exoplayer2.Player.STATE_BUFFERING) {

                logd("Loading video: " + getPlayingNowName());
                notifyStartBuffering();
            }

            mPreviousState = playbackState;
        }

    };

    public void reposition(float[] newModelMatrix) {
        SXRTransform ownerTrans = mFlatVideo.getTransform();

        float scaleX = ownerTrans.getScaleX();
        float scaleY = ownerTrans.getScaleY();
        float scaleZ = ownerTrans.getScaleZ();

        ownerTrans.setModelMatrix(newModelMatrix);
        ownerTrans.setScale(scaleX, scaleY, scaleZ);

        ownerTrans.setPosition(newModelMatrix[8] * -8.1f, newModelMatrix[9] * -8.1f, newModelMatrix[10] * -8.1f);
    }
    private ITouchEvents mPickHandler = new ITouchEvents()
    {
        private SXRNode movingObject;

        public void onEnter(SXRNode sceneObj, SXRPicker.SXRPickedObject pickInfo)
        {
            sceneObj.getRenderData().getMaterial().setVec4("u_color", PICKED_COLOR_R,
                    PICKED_COLOR_G, PICKED_COLOR_B,
                    PICKED_COLOR_A);
        }

        public void onTouchStart(SXRNode sceneObj, SXRPicker.SXRPickedObject pickInfo)
        {
            if (movingObject == null)
            {
                sceneObj.getRenderData().getMaterial().setVec4("u_color", UNPICKED_COLOR_R,
                        UNPICKED_COLOR_G, UNPICKED_COLOR_B,
                        UNPICKED_COLOR_A);
                sceneObj.getTransform().setScale(9, 9, 9);
//                sceneObj.getTransform().rotateByAxis(1, 0, 1, 0);
//                if (controller.startDrag(sceneObj))
//                {
//                    movingObject = sceneObj;
//                }
            }
        }

        public void onTouchEnd(SXRNode sceneObj, SXRPicker.SXRPickedObject pickInfo)
        {
            sceneObj.getRenderData().getMaterial().setVec4("u_color", PICKED_COLOR_R,
                    PICKED_COLOR_G, PICKED_COLOR_B,
                    PICKED_COLOR_A);
            sceneObj.getTransform().setScale(1.5f, 1.5f, 1.5f);
//            sceneObj.getTransform().rotateByAxis(1, 0, 1, 0);
//            if (sceneObj == movingObject)
//            {
//                controller.stopDrag();
//                movingObject = null;
//            }
        }

        public void onExit(SXRNode sceneObj, SXRPicker.SXRPickedObject pickInfo)
        {
            sceneObj.getRenderData().getMaterial().setVec4("u_color", UNPICKED_COLOR_R,
                    UNPICKED_COLOR_G, UNPICKED_COLOR_B,
                    UNPICKED_COLOR_A);
//            if (sceneObj == movingObject)
//            {
//                controller.stopDrag();
//                movingObject = null;
//            }
        }
        public void onInside(SXRNode sceneObj, SXRPicker.SXRPickedObject pickInfo) { }

        public void onMotionOutside(SXRPicker p, MotionEvent e) { }
    };
}

