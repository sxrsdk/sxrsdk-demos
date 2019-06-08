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

package com.samsungxr.controls.menu;

import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRMesh;
import com.samsungxr.SXRNode;
import com.samsungxr.SXRTexture;
import com.samsungxr.animation.SXRAnimation;
import com.samsungxr.animation.SXROnFinish;
import com.samsungxr.animation.SXRPositionAnimation;
import com.samsungxr.animation.SXRRepeatMode;
import com.samsungxr.animation.SXRScaleAnimation;
import com.samsungxr.controls.R;
import com.samsungxr.controls.util.RenderingOrder;

public class MenuFrame extends SXRNode {

    private static final float ANIMATION_TIME = 0.4f;
    private static final float ANIMATION_FRAME_RESIZE = .8f;
    private static final float FRAME_POSITION_Z = -.02f;

    private static final float FRAME_EXPAND_SCALE_X = 160f;
    private static final float PIVOT_OFFSET_Y = .8f;

    private SXRNode mMenuFrame;
    public static boolean isOpen = false;

    private SXRScaleAnimation scaleCollapse;
    private SXRPositionAnimation rmCollapse;
    private SXRScaleAnimation scaleExpand;
    private SXRPositionAnimation rmExpand;

    private SXRNode pivot = null;

    public MenuFrame(SXRContext sxrContext) {
        super(sxrContext);

        pivot = new SXRNode(sxrContext);
        pivot.getTransform().setPosition(0, PIVOT_OFFSET_Y,FRAME_POSITION_Z);

        SXRMesh mesh = getSXRContext().createQuad(3.57f, 0.01f);
        SXRTexture texture = getSXRContext().getAssetLoader().loadTexture(new SXRAndroidResource(this.getSXRContext(), R.drawable.background_frame));

        mMenuFrame = new SXRNode(getSXRContext(), mesh, texture);
        mMenuFrame.getRenderData().getMaterial().setOpacity(0);
        mMenuFrame.getRenderData().setRenderingOrder(RenderingOrder.MENU_FRAME_BG);

        getTransform().setPosition(0f, MenuBox.FRAME_INITITAL_POSITION_Y, FRAME_POSITION_Z);

        pivot.addChildObject(mMenuFrame);

        addChildObject(pivot);
    }

    public void expandFrame(final MenuHeader menuHeader) {

        if (!isOpen) {

            stopAnimations();

            mMenuFrame.getRenderData().getMaterial().setOpacity(0.5f);

            scaleExpand = new SXRScaleAnimation(pivot, ANIMATION_TIME, 1, FRAME_EXPAND_SCALE_X, 1);
            scaleExpand.setRepeatMode(SXRRepeatMode.ONCE);
            scaleExpand.start(this.getSXRContext().getAnimationEngine());

            rmExpand = new SXRPositionAnimation(this, ANIMATION_TIME, 0, -ANIMATION_FRAME_RESIZE, 0);
            rmExpand.setRepeatMode(SXRRepeatMode.ONCE);
            rmExpand.start(this.getSXRContext().getAnimationEngine()).setOnFinish(new SXROnFinish() {

                @Override
                public void finished(SXRAnimation arg0) {
                    menuHeader.show();
                }
            });

            isOpen = true;

        } else {
            menuHeader.show();
        }
    }

    private void stopAnimations() {

        if (scaleExpand != null) {
            this.getSXRContext().getAnimationEngine().stop(scaleExpand);
        }

        if (rmExpand != null) {
            this.getSXRContext().getAnimationEngine().stop(rmExpand);
        }

        if (scaleCollapse != null) {
            this.getSXRContext().getAnimationEngine().stop(scaleCollapse);
        }

        if (rmCollapse != null) {
            this.getSXRContext().getAnimationEngine().stop(rmCollapse);
        }
    }

    public void collapseFrame() {

        if (isOpen) {

            stopAnimations();

            scaleCollapse = new SXRScaleAnimation(pivot, ANIMATION_TIME, 1, 0, 1);
            scaleCollapse.setRepeatMode(SXRRepeatMode.ONCE);
            scaleCollapse.start(getSXRContext().getAnimationEngine());

            rmCollapse = new SXRPositionAnimation(this, ANIMATION_TIME, 0, ANIMATION_FRAME_RESIZE, 0);
            rmCollapse.setRepeatMode(SXRRepeatMode.ONCE);
            rmCollapse.setOnFinish(new SXROnFinish() {

                @Override
                public void finished(SXRAnimation arg0) {

                    mMenuFrame.getRenderData().getMaterial().setOpacity(0);
                    getTransform().setPosition(0f, MenuBox.FRAME_INITITAL_POSITION_Y, FRAME_POSITION_Z);
                }
            });

            rmCollapse.start(getSXRContext().getAnimationEngine());

            isOpen = false;
        }
    }

    public boolean isOpen() {
        return isOpen;
    }
}