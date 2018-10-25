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
import com.samsungxr.SXRRenderData.SXRRenderingOrder;
import com.samsungxr.SXRSceneObject;
import com.samsungxr.SXRTexture;
import com.samsungxr.animation.SXROpacityAnimation;
import com.samsungxr.immersivepedia.R;
import com.samsungxr.immersivepedia.focus.FocusableSceneObject;
import com.samsungxr.immersivepedia.focus.OnClickListener;

public class Seekbar extends FocusableSceneObject {

    public static final float WIDTH = 4.2f;
    private static final float HEIGHT = 0.2f;

    private SXRSceneObject playedSide;
    private FocusableSceneObject seekbarHover;

    private SXRContext gvrContext;

    public Seekbar(SXRContext gvrContext, float width, float height, SXRTexture texture) {
        super(gvrContext, width, height, texture);
        this.gvrContext = gvrContext;

        addChildObject(createPlaySide());
        addChildObject(createSeekbarHover());
        setName("seekbar");
        createCollider();
    }

    private SXRSceneObject createSeekbarHover() {

        seekbarHover = new FocusableSceneObject(gvrContext, gvrContext.createQuad(WIDTH, HEIGHT),
                gvrContext.getAssetLoader().loadTexture(new SXRAndroidResource(
                        gvrContext.getActivity(), R.drawable.timelime_hover_mask)));
        seekbarHover.getTransform().setPositionZ(.1f);

        return seekbarHover;
    }

    private void createCollider() {
        attachCollider(new SXRMeshCollider(gvrContext, false));
        seekbarHover.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick() {

                VideoComponent videoComponent = (VideoComponent) getParent();
                videoComponent.forwardOrRewindVideo();

            }
        });
    }

    private SXRSceneObject createPlaySide() {
        playedSide = new SXRSceneObject(gvrContext, gvrContext.createQuad(1.0f, HEIGHT / 2), gvrContext.getAssetLoader().loadTexture(new SXRAndroidResource(
                gvrContext.getActivity(), R.drawable.timeline_watched)));

        playedSide.getRenderData().setRenderingOrder(SXRRenderingOrder.TRANSPARENT + 2);
        playedSide.getRenderData().setOffset(true);
        playedSide.getRenderData().setOffsetFactor(-2.0f);
        playedSide.getRenderData().setOffsetUnits(-2.0f);
        playedSide.getTransform().setPositionZ(.1f);
        return playedSide;
    }

    public void setTime(int current, int duration) {

        float ratio = (float) current / (float) duration;
        float left = -WIDTH * 0.5f;
        float center = ratio * WIDTH + left;

        playedSide.getTransform().setPositionX((left + center) * 0.5f);
        playedSide.getTransform().setScaleX(center - left);
    }

    public void turnOffGUISeekbar() {
        seekbarHover.detachCollider();
        new SXROpacityAnimation(this, .1f, 0).start(gvrContext.getAnimationEngine());
        new SXROpacityAnimation(seekbarHover, .1f, 0).start(gvrContext.getAnimationEngine());
        new SXROpacityAnimation(playedSide, .1f, 0).start(gvrContext.getAnimationEngine());
    }

    public void turnOnGUISeekbar() {
        seekbarHover.attachCollider(new SXRMeshCollider(gvrContext, false));
        new SXROpacityAnimation(this, .1f, 1).start(gvrContext.getAnimationEngine());
        new SXROpacityAnimation(seekbarHover, .1f, 1).start(gvrContext.getAnimationEngine());
        new SXROpacityAnimation(playedSide, .1f, 1).start(gvrContext.getAnimationEngine());
    }

}
