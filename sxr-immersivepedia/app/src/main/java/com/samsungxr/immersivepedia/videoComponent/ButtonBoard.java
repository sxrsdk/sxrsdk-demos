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

import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRMeshCollider;
import com.samsungxr.SXRSceneObject;
import com.samsungxr.SXRTexture;
import com.samsungxr.animation.SXRAnimation;
import com.samsungxr.animation.SXROnFinish;
import com.samsungxr.animation.SXROpacityAnimation;
import com.samsungxr.immersivepedia.R;
import com.samsungxr.immersivepedia.focus.FocusListener;
import com.samsungxr.immersivepedia.focus.FocusableSceneObject;
import com.samsungxr.immersivepedia.focus.OnClickListener;
import com.samsungxr.immersivepedia.util.PlayPauseButton;
import com.samsungxr.immersivepedia.util.RenderingOrderApplication;

public class ButtonBoard extends SXRSceneObject {

    private final float PLAY_PAUSE_X_POSITION = 0.1f;
    private final float PLAY_PAUSE_Y_POSITION = -1.8f;
    private final float PLAY_PAUSE_Z_POSITION = 0.01f;

    private static final float WIDTH = .3f;
    private static final float HEIGHT = .3f;

    private PlayPauseButton playPauseButton;
    private VideoComponent videoComponent;

    private SXRContext gvrContext;

    public ButtonBoard(SXRContext gvrContext, float width, float height, SXRTexture texture,
            VideoComponent videoComponent) {
        super(gvrContext, width, height, texture);

        this.videoComponent = videoComponent;
        this.gvrContext = gvrContext;

        getRenderData().setRenderingOrder(RenderingOrderApplication.BUTTON_BOARD);

        createButtonPlayAndPause();
    }

    private void createButtonPlayAndPause() {
        playPauseButton = new PlayPauseButton(gvrContext, WIDTH, HEIGHT,
                gvrContext.getAssetLoader().loadTexture(new SXRAndroidResource(gvrContext, R.drawable.play)));
        playPauseButton.getRenderData().setRenderingOrder(RenderingOrderApplication.BUTTON_BOARD + 1);
        playPauseButton.getTransform().setPosition(PLAY_PAUSE_X_POSITION, PLAY_PAUSE_Y_POSITION, PLAY_PAUSE_Z_POSITION);
        playPauseButton.attachCollider(new SXRMeshCollider(gvrContext, false));
        renderTextureButton(PlayPauseButton.PAUSE_NORMAL, playPauseButton);
        playPauseButton.focusListener = new FocusListener() {

            @Override
            public void lostFocus(FocusableSceneObject object) {

                if (videoComponent.isPlaying()) {
                    renderTextureButton(PlayPauseButton.PAUSE_NORMAL, playPauseButton);
                } else {
                    renderTextureButton(PlayPauseButton.PLAY_NORMAL, playPauseButton);
                }
            }

            @Override
            public void inFocus(FocusableSceneObject object) {

                if (videoComponent.isPlaying()) {
                    renderTextureButton(PlayPauseButton.PAUSE_HOVER, playPauseButton);
                } else {
                    renderTextureButton(PlayPauseButton.PLAY_HOVER, playPauseButton);
                }

            }

            @Override
            public void gainedFocus(FocusableSceneObject object) {

            }
        };

        playPauseButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick() {

                if (playPauseButton.isFocus()) {

                    if (videoComponent.isPlaying()) {
                        videoComponent.pauseVideo();
                    } else {
                        videoComponent.playVideo();
                    }

                }

            }
        });
        addChildObject(playPauseButton);
    }

    public void turnOffGUIButton() {
        new SXROpacityAnimation(playPauseButton, .1f, 0).start(gvrContext.getAnimationEngine());
    }

    public void turnOnGUIButton() {
        new SXROpacityAnimation(playPauseButton, .1f, 1).start(gvrContext.getAnimationEngine());
    }

    public void turnOffGUIButtonUpdatingTexture() {
        new SXROpacityAnimation(playPauseButton, .1f, 0).start(gvrContext.getAnimationEngine()).setOnFinish(new SXROnFinish() {

            @Override
            public void finished(SXRAnimation animation) {
                renderTextureButton(PlayPauseButton.PAUSE_NORMAL, playPauseButton);
            }
        });
    }

    public void turnOnGUIButtonUpdatingTexture() {
        turnOnGUIButton();
        renderTextureButton(PlayPauseButton.PLAY_NORMAL, playPauseButton);
    }

    public void renderTextureButton(String textureID, SXRSceneObject sceneObject) {

        sceneObject.getRenderData().getMaterial()
                .setMainTexture(sceneObject.getRenderData().getMaterial().getTexture(textureID));
    }

    public void closeAction() {
        videoComponent.hideVideo();
    }

}
