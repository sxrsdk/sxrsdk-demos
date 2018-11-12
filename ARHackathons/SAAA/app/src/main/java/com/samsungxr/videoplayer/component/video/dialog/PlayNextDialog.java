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

package com.samsungxr.videoplayer.component.video.dialog;

import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.samsungxr.SXRContext;
import com.samsungxr.SXRNode;
import com.samsungxr.IViewEvents;
import com.samsungxr.nodes.SXRViewNode;
import com.samsungxr.videoplayer.R;
import com.samsungxr.videoplayer.component.FadeableObject;
import com.samsungxr.videoplayer.component.video.player.Player;
import com.samsungxr.videoplayer.focus.Focusable;
import com.samsungxr.videoplayer.model.Video;
import com.samsungxr.videoplayer.provider.asyntask.ThumbnailLoader;
import com.samsungxr.videoplayer.util.TimeUtils;

public class PlayNextDialog extends FadeableObject implements View.OnClickListener, Focusable, IViewEvents {
    private static final String TAG = Player.class.getSimpleName();
    private TextView mTitle;
    private TextView mDuration;
    private ImageView mThumbnail;
    private ImageView ic_time;
    private TextView mTime;
    private CountdownTimer mCountdownTimer;
    private OnPlayNextListener mOnPlayNextListener;
    private RelativeLayout OverlayScene;
    private SXRViewNode mPlayNextObject;

    public PlayNextDialog(SXRContext sxrContext, @NonNull OnPlayNextListener listener) {
        super(sxrContext);
        mPlayNextObject = new SXRViewNode(sxrContext, R.layout.layout_play_next, this);
        setName(getClass().getSimpleName());
        mOnPlayNextListener = listener;
        mCountdownTimer = new CountdownTimer(this);
    }

    @Override
    public void onInitView(SXRViewNode sxrViewNode, View view) {
        mThumbnail = view.findViewById(R.id.thumbnail);
        mTime = view.findViewById(R.id.time);
        mTitle = view.findViewById(R.id.title);
        mDuration = view.findViewById(R.id.duration);
        OverlayScene = view.findViewById(R.id.overlay_video);
        ic_time = view.findViewById(R.id.ic_count);
        OverlayScene.setOnClickListener(this);
        OverlayScene.setOnHoverListener(new View.OnHoverListener() {
            @Override
            public boolean onHover(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_HOVER_ENTER) {
                    mTitle.setVisibility(View.VISIBLE);
                    mDuration.setVisibility(View.VISIBLE);
                    ic_time.setVisibility(View.VISIBLE);
                } else {
                    if (motionEvent.getAction() == MotionEvent.ACTION_HOVER_EXIT) {
                        mTitle.setVisibility(View.INVISIBLE);
                        mDuration.setVisibility(View.INVISIBLE);
                        ic_time.setVisibility(View.INVISIBLE);
                    }
                }
                return false;
            }
        });
    }

    @Override
    public void onStartRendering(SXRViewNode sxrViewNode, View view) {
        addChildObject(sxrViewNode);
    }

    private void setTime(final int time) {
        OverlayScene.post(new Runnable() {
            @Override
            public void run() {
                mTime.setText(String.valueOf(time));
            }
        });
    }

    private void notifyTimesUp() {
        mOnPlayNextListener.onTimesUp();
    }

    public void startTimer() {
        mCountdownTimer.start();
    }

    public void cancelTimer() {
        mCountdownTimer.cancel();
    }

    public void setVideoData(final Video video) {
        OverlayScene.post(new Runnable() {
            @Override
            public void run() {
                mTitle.setText(video.getTitle());
                mDuration.setText(TimeUtils.formatDurationFull(video.getDuration()));
                if (video.getVideoType() == Video.VideoType.LOCAL) {
                    new ThumbnailLoader(mThumbnail).execute(video.getId());
                } else {
                    mThumbnail.setImageBitmap(video.getThumbnail());
                }
            }
        });
    }

    @NonNull
    @Override
    protected SXRNode getFadeable() {
        return mPlayNextObject;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.overlay_video) {
            mOnPlayNextListener.onThumbClicked();
        }
    }

    @Override
    public void gainFocus() {

    }

    @Override
    public void loseFocus() {

    }

    private static class CountdownTimer extends Handler {

        static final int MAX_COUNT = 5;
        PlayNextDialog mPlayNextDialog;
        int count = MAX_COUNT;

        CountdownTimer(PlayNextDialog mPlayNextDialog) {
            this.mPlayNextDialog = mPlayNextDialog;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (count < 0) {
                mPlayNextDialog.notifyTimesUp();
                reset();
            } else {
                tick();
            }
        }

        private void tick() {
            mPlayNextDialog.setTime(count);
            count--;
            sendEmptyMessageDelayed(0, 1000);
        }

        void start() {
            reset();
            tick();
        }

        void cancel() {
            reset();
        }

        private void reset() {
            removeMessages(0);
            count = MAX_COUNT;
        }
    }
}
