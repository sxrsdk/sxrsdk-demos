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

import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRMeshCollider;
import com.samsungxr.SXRScene;
import com.samsungxr.SXRNode;
import com.samsungxr.animation.SXRRotationByAxisAnimation;
import com.samsungxr.immersivepedia.R;
import com.samsungxr.immersivepedia.dinosaur.Dinosaur;
import com.samsungxr.immersivepedia.dinosaur.DinosaurFactory;
import com.samsungxr.immersivepedia.focus.OnGestureListener;
import com.samsungxr.immersivepedia.props.Totem;
import com.samsungxr.immersivepedia.props.TotemEventListener;
import com.samsungxr.immersivepedia.util.AudioClip;
import com.samsungxr.immersivepedia.videoComponent.VideoComponent;

import java.io.IOException;

public class VideoDinosaurGroup extends SXRNode implements TotemEventListener, OnGestureListener {

    private VideoComponent videoComponent;
    private Dinosaur trex;
    private SXRContext sxrContext;
    private SXRScene scene;

    public VideoDinosaurGroup(SXRContext sxrContext, SXRScene scene) throws IOException {
        super(sxrContext);

        this.sxrContext = sxrContext;
        this.scene = scene;

        createDinosaur();
        createTotem();
    }

    private void createVideoComponent() {
        videoComponent = new VideoComponent(getSXRContext(), VideoComponent.WIDTH, VideoComponent.HEIGHT);
        videoComponent.getTransform().setPosition(0f, 0f, 0f);
        videoComponent.getTransform().setPosition(0f, 2.0f, -3.0f);
        videoComponent.getTransform().rotateByAxisWithPivot(
                DinosaurFactory.TREX_ANGLE_AROUND_CAMERA, 0f, 1f, 0f, 0f, 0f, 0f);
        scene.addNode(videoComponent);
    }

    private void createDinosaur() {

        trex = DinosaurFactory.getInstance(getSXRContext()).getTRex();
        trex.attachCollider(new SXRMeshCollider(getSXRContext(), true));
        trex.setOnGestureListener(this);
        trex.getTransform().setRotationByAxis(-90, 1, 0, 0);
        trex.setName("trex");
        addChildObject(trex);
    }

    private void createTotem() {
        Totem totem = new Totem(this.sxrContext,
                this.sxrContext.getAssetLoader().loadTexture(new SXRAndroidResource(sxrContext,
                        R.drawable.totem_tex_diffuse)));

        totem.getTransform().setPosition(0f, 0f, 0f);
        totem.setName("totem_trex");
        totem.setTotemEventListener(this);
        scene.addNode(totem);
        totem.getTransform().setPosition(.3f, 0f, -5.0f);
        totem.getTransform().rotateByAxis(180.0f, 0f, 1f, 0f);
        totem.getTransform().setScale(1f, 1f, 1f);
        totem.setText(sxrContext.getActivity().getResources().getString(R.string.video_totem));
        totem.getTransform().rotateByAxisWithPivot(
                DinosaurFactory.TREX_ANGLE_AROUND_CAMERA - 35.0f, 0f, 1f, 0f, 0f, 0f, 0f);
    }

    public void onStep() {
        if (videoComponent != null && videoComponent.isPlaying()) {
            videoComponent.getSeekbar().setTime(videoComponent.getCurrentPosition(),
                    videoComponent.getDuration());
        }
    }

    @Override
    public void onFinishLoadingTotem(Totem totem) {
        sxrContext.runOnGlThread(new Runnable() {
            @Override
            public void run() {
                createVideoComponent();
                videoComponent.showVideo();
            }
        });
    }

    @Override
    public boolean shouldTotemAppear(Totem totem) {
        if (videoComponent != null) {
            return !videoComponent.isActive();
        } else {
            return true;
        }
    }

    public boolean isOpen() {
        if (videoComponent != null)
            return videoComponent.isActive();
        return false;
    }

    public void closeAction() {
        videoComponent.getButtonbar().closeAction();
    }

    @Override
    public void onSwipeUp() {
    }

    @Override
    public void onSwipeDown() {
    }

    @Override
    public void onSwipeForward() {
        AudioClip.getInstance(getSXRContext().getContext()).playSound(AudioClip.getUIRotateSoundID(), 1.0f, 1.0f);
        new SXRRotationByAxisAnimation(trex, 4f, 45, 0, 1, 0).start(sxrContext.getAnimationEngine());
    }

    @Override
    public void onSwipeBack() {
        AudioClip.getInstance(getSXRContext().getContext()).playSound(AudioClip.getUIRotateSoundID(), 1.0f, 1.0f);
        new SXRRotationByAxisAnimation(trex, 4f, -45, 0, 1, 0).start(sxrContext.getAnimationEngine());
    }

    @Override
    public void onSwipeIgnore() {
    }

    public void pauseVideo() {
        if (null != videoComponent && videoComponent.isPlaying()) {
            videoComponent.pauseVideo();
        }
    }

}
