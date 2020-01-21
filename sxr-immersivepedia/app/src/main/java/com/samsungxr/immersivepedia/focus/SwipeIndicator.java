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

package com.samsungxr.immersivepedia.focus;

import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRMeshCollider;
import com.samsungxr.SXRNode;
import com.samsungxr.SXRRenderData;
import com.samsungxr.animation.SXRAnimation;
import com.samsungxr.animation.SXROnFinish;
import com.samsungxr.animation.SXROpacityAnimation;
import com.samsungxr.animation.SXRPositionAnimation;
import com.samsungxr.immersivepedia.R;

public class SwipeIndicator extends SXRNode {

    private SXRContext sxrContext;
    private SXRNode swipeIndicator;
    private SXRNode hand;
    private FocusableNode dino;
    private static final float HAND_X = -.2f;
    private static final float HAND_Y = -.15f;
    private static final float HAND_Z = 0.1f;
    private long currentSecond;
    private boolean isStoping;

    public SwipeIndicator(SXRContext sxrContext, FocusableNode dino) {
        super(sxrContext);
        this.sxrContext = sxrContext;
        this.dino = dino;
        swipeIndicator = new SXRNode(sxrContext, .6f, .1f,
                sxrContext.getAssetLoader().loadTexture(new SXRAndroidResource(sxrContext, R.drawable.swipe_trace)));
        hand = new SXRNode(sxrContext, sxrContext.createQuad(.2f, .3f), sxrContext.getAssetLoader().loadTexture(new SXRAndroidResource(
                sxrContext, R.drawable.swipe_hand)));
        setAttribute();

    }

    public void init() {
        dino.attachCollider(new SXRMeshCollider(getSXRContext(), false));
        addChildObject(swipeIndicator);
        addChildObject(hand);
        dino.focusListener = new FocusListener() {
            @Override
            public void lostFocus(FocusableNode object) {
            }

            @Override
            public void inFocus(FocusableNode object) {
                currentSecond = System.currentTimeMillis() / 1000;
                if (!isStoping) {
                    if (currentSecond % 5 == 0) {
                       restartSwipeIndicator();
                    }
                }
            }

            @Override
            public void gainedFocus(FocusableNode object) {
            }
        };
    }

    private void restartSwipeIndicator() {
        swipeIndicator.getRenderData().getMaterial().setOpacity(1);
        hand.getRenderData().getMaterial().setOpacity(1);
        hand.getTransform().setPosition(HAND_X, HAND_Y, HAND_Z);
  
        swipeAnimation();
    }

    private void setAttribute() {
        hand.getRenderData().setRenderingOrder(SXRRenderData.SXRRenderingOrder.TRANSPARENT);
        hand.getTransform().setPosition(HAND_X, HAND_Y, HAND_Z);
    }

    private void swipeAnimation() {

        SXRPositionAnimation positionAnimationHand = new
                SXRPositionAnimation(hand, 1f, swipeIndicator.getTransform().getPositionX() + .3f, hand.getTransform().getPositionY()
                        , hand.getTransform().getPositionZ());

        positionAnimationHand.setOnFinish(new SXROnFinish() {

            @Override
            public void finished(SXRAnimation arg0) {
                new SXROpacityAnimation(swipeIndicator, 1, 0).start(sxrContext.getAnimationEngine());
                new SXROpacityAnimation(hand, 1, 0).start(sxrContext.getAnimationEngine());
            }
        }).start(sxrContext.getAnimationEngine());

    }

    public void setStop(boolean isStoping) {
        this.isStoping = isStoping;
    }
}
