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

package com.samsungxr.immersivepedia.videoComponent;

import android.media.MediaPlayer;

import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRMeshCollider;
import com.samsungxr.SXRSceneObject;
import com.samsungxr.animation.SXROpacityAnimation;
import com.samsungxr.immersivepedia.GazeController;
import com.samsungxr.immersivepedia.R;
import com.samsungxr.immersivepedia.focus.FocusListener;
import com.samsungxr.immersivepedia.focus.FocusableSceneObject;
import com.samsungxr.immersivepedia.focus.OnClickListener;
import com.samsungxr.scene_objects.SXRVideoSceneObject;
import com.samsungxr.scene_objects.SXRVideoSceneObject.SXRVideoType;

public class VideoComponent extends SXRSceneObject {

    private Seekbar seekbar;
    private ButtonBoard buttonBoard;
    private MediaPlayer mediaPlayer;
    private SXRContext sxrContext;

    public static float INITIAL_POSITION_X = .0f;
    public static float INITIAL_POSITION_Y = .5f;
    public static float INITIAL_POSITION_Z = -5.1f;

    public static final float WIDTH = 4.5f;
    public static final float HEIGHT = 3f;
    private static final float DURATION = 0.5f;

    private boolean active = false;

    private SXRVideoSceneObject video;
    private FocusableSceneObject focus;

    public VideoComponent(SXRContext sxrContext, float WIDTH, float HEIGHT) {
        super(sxrContext, 0, 0);

        this.sxrContext = sxrContext;

        setVideoAttribute();
        createVideo();
        createSeekbar();
        createButtonBoard();

        mediaPlayer.start();
    }

    private void createSeekbar() {
        seekbar = new Seekbar(sxrContext, Seekbar.WIDTH, 0.05f, sxrContext.getAssetLoader().loadTexture(new SXRAndroidResource(
                sxrContext, R.drawable.timeline_towatch)));
        seekbar.getTransform().setPosition(0.0f, -1.5f, video.getTransform().getPositionZ() + 0.1f);
        addChildObject(seekbar);
    }

    private void createButtonBoard() {
        buttonBoard = new ButtonBoard(sxrContext, 1.7f, .4f,
                sxrContext.getAssetLoader().loadTexture(new SXRAndroidResource(sxrContext.getActivity(),
                        R.drawable.empty)), this);
        buttonBoard.getTransform().setPosition(0f, 0f, video.getTransform().getPositionZ() + 0.1f);
        addChildObject(buttonBoard);
    }

    private void createVideo() {

        mediaPlayer = MediaPlayer.create(sxrContext.getContext(), R.raw.dinos_videos_wip);
        video = new SXRVideoSceneObject(sxrContext, WIDTH, HEIGHT, mediaPlayer, SXRVideoType.MONO);
        focus = new FocusableSceneObject(sxrContext, WIDTH, HEIGHT, sxrContext.getAssetLoader().loadTexture(new SXRAndroidResource(sxrContext,
                R.drawable.empty_clickable)));
        focus.attachCollider(new SXRMeshCollider(sxrContext, false));
        focus.setName("video");
        focus.focusListener = new FocusListener() {

            @Override
            public void lostFocus(FocusableSceneObject object) {
                if (isActive()) {
                    getButtonbar().turnOnGUIButton();
                    getSeekbar().turnOnGUISeekbar();
                    GazeController.get().enableGaze();
                }
            }

            @Override
            public void inFocus(FocusableSceneObject object) {
            }

            @Override
            public void gainedFocus(FocusableSceneObject object) {
                if (isActive() && isPlaying()) {
                    getButtonbar().turnOffGUIButton();
                    getSeekbar().turnOffGUISeekbar();
                    GazeController.get().disableGaze();
                }
            }
        };

        focus.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick() {
                if (isPlaying()) {
                    pauseVideo();
                    getButtonbar().turnOnGUIButtonUpdatingTexture();
                    getSeekbar().turnOnGUISeekbar();
                    GazeController.get().enableGaze();
                } else {
                    playVideo();
                    getButtonbar().turnOffGUIButtonUpdatingTexture();
                    getSeekbar().turnOffGUISeekbar();
                    GazeController.get().disableGaze();
                }
            }
        });

        addChildObject(video);
        addChildObject(focus);
    }

    private void setVideoAttribute() {

        getTransform().setPosition(INITIAL_POSITION_X, INITIAL_POSITION_Y, INITIAL_POSITION_Z);
    }

    public void playVideo() {
        mediaPlayer.start();
    }

    public void pauseVideo() {
        mediaPlayer.pause();
    }

    public Seekbar getSeekbar() {
        return seekbar;
    }

    public ButtonBoard getButtonbar() {
        return buttonBoard;
    }

    public boolean isPlaying() {
        if (mediaPlayer != null) {
            try {
                return mediaPlayer.isPlaying();
            } catch (IllegalStateException e) {
                return false;
            }
        }
        return false;
    }

    public int getDuration() {

        return mediaPlayer.getDuration();
    }

    public int getCurrentPosition() {

        return mediaPlayer.getCurrentPosition();
    }

    public void showVideo() {
        new SXROpacityAnimation(this, DURATION, 1).start(sxrContext.getAnimationEngine());
        active = true;
    }

    public void hideVideo() {
        active = false;
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;
        new SXROpacityAnimation(this, .1f, 0).start(sxrContext.getAnimationEngine());
        sxrContext.getMainScene().removeSceneObject(this);
    }

    public boolean isActive() {
        return active;
    }

    public void forwardOrRewindVideo() {
        float x = seekbar.hitLocation[0];
        float posX = Seekbar.WIDTH / 2 + x;
        int current = (int) (mediaPlayer.getDuration() * posX / Seekbar.WIDTH);
        mediaPlayer.seekTo(current);
        seekbar.setTime(current, mediaPlayer.getDuration());
    }

}
