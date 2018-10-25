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

package com.samsungxr.immersivepedia.model;

import android.util.Log;

import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRMeshCollider;
import com.samsungxr.SXRScene;
import com.samsungxr.SXRSceneObject;
import com.samsungxr.animation.SXRAnimation;
import com.samsungxr.animation.SXRRotationByAxisAnimation;
import com.samsungxr.immersivepedia.R;
import com.samsungxr.immersivepedia.dinosaur.DinosaurFactory;
import com.samsungxr.immersivepedia.focus.FocusListener;
import com.samsungxr.immersivepedia.focus.FocusableSceneObject;
import com.samsungxr.immersivepedia.focus.OnClickListener;
import com.samsungxr.immersivepedia.focus.OnGestureListener;
import com.samsungxr.immersivepedia.focus.SwipeIndicator;
import com.samsungxr.immersivepedia.props.Totem;
import com.samsungxr.immersivepedia.util.AudioClip;
import com.samsungxr.immersivepedia.util.PlayPauseButton;

import java.io.IOException;

public class RotateDinosaurGroup extends SXRSceneObject implements
        OnGestureListener {

    private FocusableSceneObject styrocosaurus;
    private SXRContext gvrContext;
    private SXRScene scene;
    private Totem totem;
    public boolean isPlayed;
    private SXRAnimation animation;
    private SwipeIndicator swipeIndicator;
    private int streamID;


    public RotateDinosaurGroup(SXRContext gvrContext, SXRScene scene)
            throws IOException {
        super(gvrContext);

        this.gvrContext = gvrContext;
        this.scene = scene;

        createDinosaur();
        createTotem();
        createDinoAnimation();
        createSwipeIndicator();
    }

    private void createSwipeIndicator() {
        swipeIndicator = new SwipeIndicator(gvrContext, styrocosaurus);
        swipeIndicator.getTransform().setPosition(0, 1.5f, -3f);
        swipeIndicator.init();
        addChildObject(swipeIndicator);
    }

    private void createTotem() {

        totem = new Totem(this.gvrContext,
                this.gvrContext.getAssetLoader().loadTexture(new SXRAndroidResource(gvrContext,
                        R.drawable.totem_tex_diffuse)));
        totem.setTotemEventListener(null);
        scene.addSceneObject(totem);
        totem.getTransform().setPosition(-.3f, 0f, -5.0f);
        totem.getTransform().rotateByAxis(180.0f, 0f, 1f, 0f);
        totem.getTransform().setScale(1f, 1f, 1f);
        totem.setText(gvrContext.getActivity().getResources()
                .getString(R.string.rotate_totem));
        totem.getTransform().rotateByAxisWithPivot(
                DinosaurFactory.STYRACOSAURUS_ANGLE_AROUND_CAMERA - 35.0f, 0f,
                1f, 0f, 0f, 0f, 0f);
        totem.setIcon(R.drawable.play);
        totem.setName("totem_styracosaurus");
    }

    private void createDinoAnimation() {
        final PlayPauseButton playPause = totem.getIcon();

        playPause.attachCollider(new SXRMeshCollider(getSXRContext(), false));
        playPause.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick() {

                if (animation == null) {
                    startAnimation();
                    streamID = AudioClip.getInstance(getSXRContext().getContext()).playLoop(AudioClip.getUiLoopRotateSoundID(), 1.0f, 1.0f);
                    Log.e("test", "start code: " + streamID);
                } else {
                    pauseAnimation();
                }
            }

        });

        playPause.focusListener = new FocusListener() {
            @Override
            public void lostFocus(FocusableSceneObject object) {
                if (isPlayed) {
                    renderTextureButton(PlayPauseButton.PAUSE_NORMAL, playPause);
                } else {
                    renderTextureButton(PlayPauseButton.PLAY_NORMAL, playPause);
                }
            }

            @Override
            public void inFocus(FocusableSceneObject object) {
                if (isPlayed) {
                    renderTextureButton(PlayPauseButton.PAUSE_HOVER, playPause);
                } else {
                    renderTextureButton(PlayPauseButton.PLAY_HOVER, playPause);
                }
            }

            @Override
            public void gainedFocus(FocusableSceneObject object) {
            }
        };
    }

    private void startAnimation() {
        animation = new SXRRotationByAxisAnimation(styrocosaurus, 25, 360, 0,
                1, 0).start(gvrContext.getAnimationEngine());
        animation.setRepeatMode(1);
        animation.setRepeatCount(-1);
        isPlayed = true;
        swipeIndicator.setStop(true);
    }

    private void stopAnimation() {
        gvrContext.getAnimationEngine().stop(animation);
        AudioClip.getInstance(getSXRContext().getContext())
                .pauseSound(streamID);
        animation = null;
        isPlayed = false;
    }

    private void createDinosaur() {
        styrocosaurus = DinosaurFactory.getInstance(gvrContext)
                .getStyracosaurus();
        styrocosaurus.getTransform().rotateByAxisWithPivot(-90, 1, 0, 0, 0, 0,
                0);
        styrocosaurus.getTransform().setPosition(0, 0, -8);
        styrocosaurus.setOnGestureListener(this);
        styrocosaurus.setName("styrocosaurus");
        addChildObject(styrocosaurus);
    }

    public void renderTextureButton(String textureID, SXRSceneObject sceneObject) {
        sceneObject
                .getRenderData()
                .getMaterial()
                .setMainTexture(
                        sceneObject.getRenderData().getMaterial()
                                .getTexture(textureID));
    }

    @Override
    public void onSwipeUp() {
    }

    @Override
    public void onSwipeDown() {
    }

    @Override
    public void onSwipeForward() {
        if (!isPlayed) {
            AudioClip.getInstance(getSXRContext().getContext()).playSound(
                    AudioClip.getUIRotateSoundID(), 1.0f, 1.0f);
            new SXRRotationByAxisAnimation(styrocosaurus, 4f, 45, 0, 1, 0)
                    .start(gvrContext.getAnimationEngine());
            swipeIndicator.setStop(true);
        }
    }

    @Override
    public void onSwipeBack() {
        if (!isPlayed) {
            AudioClip.getInstance(getSXRContext().getContext()).playSound(
                    AudioClip.getUIRotateSoundID(), 1.0f, 1.0f);
            new SXRRotationByAxisAnimation(styrocosaurus, 4f, -45, 0, 1, 0)
                    .start(gvrContext.getAnimationEngine());
            swipeIndicator.setStop(true);
        }
    }

    @Override
    public void onSwipeIgnore() {
    }

    public void pauseAnimation() {
        stopAnimation();
        final PlayPauseButton playPause = totem.getIcon();
        renderTextureButton(PlayPauseButton.PLAY_NORMAL, playPause);
        Log.e("test", "pause: " + streamID);
    }
}
