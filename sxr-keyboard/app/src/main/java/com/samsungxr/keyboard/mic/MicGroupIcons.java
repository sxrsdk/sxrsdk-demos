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

package com.samsungxr.keyboard.mic;

import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRNode;
import com.samsungxr.keyboard.R;
import com.samsungxr.keyboard.mic.model.MicItem;
import com.samsungxr.keyboard.util.RenderingOrder;
import com.samsungxr.keyboard.util.NodeNames;

public class MicGroupIcons extends SXRNode {

    private MicItem mIcon;
    int mOff = R.drawable.mic_icon_off;
    int mOn = R.drawable.mic_icon_on;

    public MicGroupIcons(SXRContext sxrContext) {
        super(sxrContext);
        setName(NodeNames.MIC_GROUP_ICONS);

        mIcon = new MicItem(sxrContext, mOff);
        mIcon.getRenderData().setRenderingOrder(RenderingOrder.ORDER_RENDERING_ICON);
        this.addChildObject(mIcon);
    }

    public void show() {

        changeMicIcon(mOn);
    }

    private void changeMicIcon(final int res) {
        if (mIcon != null) {

            this.getSXRContext().runOnGlThread(new Runnable() {

                @Override
                public void run() {
                    mIcon.getRenderData()
                            .getMaterial()
                            .setMainTexture(
                                    MicGroupIcons.this.getSXRContext().getAssetLoader().loadTexture(
                                            new SXRAndroidResource(MicGroupIcons.this
                                                    .getSXRContext(), res)));
                }

            });

        }

    }

    public void hide() {
        changeMicIcon(mOff);
    }

    // public void animateOpacity(SXRContext context) {
    // if (!isVisibleByOpacity) {
    // isVisibleByOpacity = true;
    // opacityAnimation = new SXROpacityAnimation(this, 1, 1);
    // opacityAnimation.start(context.getAnimationEngine());
    // }
    // }

}
