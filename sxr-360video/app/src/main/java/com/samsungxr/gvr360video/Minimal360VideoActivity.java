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

package com.samsungxr.gvr360video;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.MotionEvent;
import android.view.Surface;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.upstream.AssetDataSource;
import com.google.android.exoplayer2.upstream.DataSource;

import com.samsungxr.SXRActivity;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRMain;
import com.samsungxr.SXRMesh;
import com.samsungxr.SXRScene;
import com.samsungxr.scene_objects.SXRSphereSceneObject;
import com.samsungxr.scene_objects.SXRVideoSceneObject;
import com.samsungxr.scene_objects.SXRVideoSceneObjectPlayer;

import java.io.File;
import java.io.IOException;

import static android.view.MotionEvent.ACTION_DOWN;

public class Minimal360VideoActivity extends SXRActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!USE_EXO_PLAYER) {
            videoSceneObjectPlayer = makeMediaPlayer();
        } else {
            videoSceneObjectPlayer = makeExoPlayer();
        }

        if (null != videoSceneObjectPlayer) {
            final Minimal360Video main = new Minimal360Video(videoSceneObjectPlayer);
            setMain(main, "gvr.xml");
        }
    }

    private SXRVideoSceneObjectPlayer<MediaPlayer> makeMediaPlayer() {
        final MediaPlayer mediaPlayer = new MediaPlayer();

        try {
            final File file = new File(Environment.getExternalStorageDirectory() + "/gvr-360video/video.mp4");
            if (!file.exists()) {
                final AssetFileDescriptor afd = getAssets().openFd("video.mp4");
                android.util.Log.d("Minimal360Video", "Assets was found.");
                mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                android.util.Log.d("Minimal360Video", "DataSource was set.");
                afd.close();
            } else {
                mediaPlayer.setDataSource(file.getAbsolutePath());
            }

            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
            finish();
            android.util.Log.e("Minimal360Video", "Assets were not loaded. Stopping application!");
            return null;
        }

        mediaPlayer.setLooping(true);
        android.util.Log.d("Minimal360Video", "starting player.");

        return SXRVideoSceneObject.makePlayerInstance(mediaPlayer);
    }

    private SXRVideoSceneObjectPlayer<ExoPlayer> makeExoPlayer() {
        final Context context = this;
        final DataSource.Factory dataSourceFactory = new DataSource.Factory() {
            @Override
            public DataSource createDataSource() {
                return new AssetDataSource(context);
            }
        };
        final MediaSource mediaSource = new ExtractorMediaSource(Uri.parse("asset:///video.mp4"),
                dataSourceFactory,
                new DefaultExtractorsFactory(), null, null);

        final SimpleExoPlayer player = ExoPlayerFactory.newSimpleInstance(context,
                new DefaultTrackSelector());
        player.prepare(mediaSource);

        return new SXRVideoSceneObjectPlayer<ExoPlayer>() {
            @Override
            public ExoPlayer getPlayer() {
                return player;
            }

            @Override
            public void setSurface(final Surface surface) {
                player.addListener(new Player.DefaultEventListener() {
                    @Override
                    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                        switch (playbackState) {
                            case Player.STATE_BUFFERING:
                                break;
                            case Player.STATE_ENDED:
                                player.seekTo(0);
                                break;
                            case Player.STATE_IDLE:
                                break;
                            case Player.STATE_READY:
                                break;
                            default:
                                break;
                        }
                    }
                });

                player.setVideoSurface(surface);
            }

            @Override
            public void release() {
                player.release();
            }

            @Override
            public boolean canReleaseSurfaceImmediately() {
                return false;
            }

            @Override
            public void pause() {
                player.setPlayWhenReady(false);
            }

            @Override
            public void start() {
                player.setPlayWhenReady(true);
            }

            @Override
            public boolean isPlaying() {
                return player.getPlayWhenReady();
            }
        };
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (null != videoSceneObjectPlayer) {
            if (ACTION_DOWN == event.getAction()) {
                if (mPlaying) {
                    videoSceneObjectPlayer.pause();
                } else {
                    videoSceneObjectPlayer.start();
                }
                mPlaying = !mPlaying;
            }
        }
        return super.onTouchEvent(event);
    }

    private boolean mPlaying = true;
    private SXRVideoSceneObjectPlayer<?> videoSceneObjectPlayer;

    static final boolean USE_EXO_PLAYER = false;

    private final static class Minimal360Video extends SXRMain
    {
        Minimal360Video(SXRVideoSceneObjectPlayer<?> player) {
            mPlayer = player;
        }

        @Override
        public void onInit(SXRContext gvrContext) {

            SXRScene scene = gvrContext.getMainScene();

            SXRSphereSceneObject sphere = new SXRSphereSceneObject(gvrContext, 72, 144, false);
            SXRMesh mesh = sphere.getRenderData().getMesh();

            SXRVideoSceneObject video = new SXRVideoSceneObject( gvrContext, mesh, mPlayer, SXRVideoSceneObject.SXRVideoType.MONO );
            video.getTransform().setScale(100f, 100f, 100f);
            video.setName( "video" );

            scene.addSceneObject( video );
            video.getMediaPlayer().start();
        }

        private final SXRVideoSceneObjectPlayer<?> mPlayer;
    }
}
