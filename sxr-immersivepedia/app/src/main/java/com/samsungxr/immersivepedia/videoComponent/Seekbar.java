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
import com.samsungxr.SXRNode;
import com.samsungxr.SXRTexture;
import com.samsungxr.animation.SXROpacityAnimation;
import com.samsungxr.immersivepedia.R;
import com.samsungxr.immersivepedia.focus.FocusableNode;
import com.samsungxr.immersivepedia.focus.OnClickListener;

public class Seekbar extends FocusableNode {

    public static final float WIDTH = 4.2f;
    private static final float HEIGHT = 0.2f;

    private SXRNode playedSide;
    private FocusableNode seekbarHover;

    private SXRContext sxrContext;

    public Seekbar(SXRContext sxrContext, float width, float height, SXRTexture texture) {
        super(sxrContext, width, height, texture);
        this.sxrContext = sxrContext;

        addChildObject(createPlaySide());
        addChildObject(createSeekbarHover());
        setName("seekbar");
        createCollider();
    }

    private SXRNode createSeekbarHover() {

        seekbarHover = new FocusableNode(sxrContext, sxrContext.createQuad(WIDTH, HEIGHT),
                sxrContext.getAssetLoader().loadTexture(new SXRAndroidResource(
                        sxrContext.getActivity(), R.drawable.timelime_hover_mask)));
        seekbarHover.getTransform().setPositionZ(.1f);

        return seekbarHover;
    }

    private void createCollider() {
        attachCollider(new SXRMeshCollider(sxrContext, false));
        seekbarHover.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick() {

                VideoComponent videoComponent = (VideoComponent) getParent();
                videoComponent.forwardOrRewindVideo();

            }
        });
    }

    private SXRNode createPlaySide() {
        playedSide = new SXRNode(sxrContext, sxrContext.createQuad(1.0f, HEIGHT / 2), sxrContext.getAssetLoader().loadTexture(new SXRAndroidResource(
                sxrContext.getActivity(), R.drawable.timeline_watched)));

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
        new SXROpacityAnimation(this, .1f, 0).start(sxrContext.getAnimationEngine());
        new SXROpacityAnimation(seekbarHover, .1f, 0).start(sxrContext.getAnimationEngine());
        new SXROpacityAnimation(playedSide, .1f, 0).start(sxrContext.getAnimationEngine());
    }

    public void turnOnGUISeekbar() {
        seekbarHover.attachCollider(new SXRMeshCollider(sxrContext, false));
        new SXROpacityAnimation(this, .1f, 1).start(sxrContext.getAnimationEngine());
        new SXROpacityAnimation(seekbarHover, .1f, 1).start(sxrContext.getAnimationEngine());
        new SXROpacityAnimation(playedSide, .1f, 1).start(sxrContext.getAnimationEngine());
    }

}
