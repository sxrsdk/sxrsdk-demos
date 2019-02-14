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

package com.samsungxr.immersivepedia.props;

import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRMeshCollider;
import com.samsungxr.SXRRenderPass.SXRCullFaceEnum;
import com.samsungxr.SXRTexture;
import com.samsungxr.immersivepedia.R;
import com.samsungxr.immersivepedia.focus.FocusListener;
import com.samsungxr.immersivepedia.focus.FocusableNode;
import com.samsungxr.immersivepedia.focus.OnClickListener;
import com.samsungxr.immersivepedia.loadComponent.LoadComponent;
import com.samsungxr.immersivepedia.loadComponent.LoadComponentListener;
import com.samsungxr.immersivepedia.util.AudioClip;
import com.samsungxr.immersivepedia.util.PlayPauseButton;
import com.samsungxr.immersivepedia.util.RenderingOrderApplication;
import com.samsungxr.nodes.SXRTextViewNode;

import static com.samsungxr.immersivepedia.util.RenderingOrderApplication.TOTEM;

public class Totem extends FocusableNode implements FocusListener {

    private static final float TEXT_HEIGHT = 2f;
    private static final float TEXT_WIDTH = 5f;
    private LoadComponent loadComponent = null;
    private SXRContext sxrContext = null;
    private TotemEventListener totemEventListener;
    private PlayPauseButton icon;
    private int streamIDTotem;

    public Totem(SXRContext sxrContext, SXRTexture t) {
        super(sxrContext, sxrContext.getAssetLoader().loadMesh(new SXRAndroidResource(sxrContext.getActivity(),
                R.raw.totem_standup_mesh)), t);

        prepareTotem(sxrContext);

        this.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick() {
                if (loadComponent != null && loadComponent.isLoading()) {
                    loadComponent.finishLoadComponent();
                    AudioClip.getInstance(getSXRContext().getContext()).pauseSound(streamIDTotem);
                }
            }
        });
    }

    private void prepareTotem(SXRContext sxrContext) {
        this.sxrContext = sxrContext;
        this.getRenderData().setCullFace(SXRCullFaceEnum.None);
        getRenderData().setRenderingOrder(TOTEM);
        this.attachCollider(new SXRMeshCollider(getSXRContext(), true));
        this.focusListener = this;
    }

    @Override
    public void gainedFocus(FocusableNode object) {
        if (this.totemEventListener != null
                && this.totemEventListener.shouldTotemAppear(this) == true) {
            createLoadComponent();
            streamIDTotem = AudioClip.getInstance(getSXRContext().getContext()).playLoop(AudioClip.getUILoadingSoundID(), 1.0f, 1.0f);
        }
    }

    @Override
    public void lostFocus(FocusableNode object) {
        if (this.loadComponent != null) {
            this.loadComponent.disableListener();
            this.removeChildObject(this.loadComponent);
            AudioClip.getInstance(getSXRContext().getContext()).stopSound(streamIDTotem);
        }
    }

    @Override
    public void inFocus(FocusableNode object) {

    }

    private void createLoadComponent() {

        loadComponent = new LoadComponent(sxrContext, new LoadComponentListener() {

            @Override
            public void onFinishLoadComponent() {
                Totem.this.onFinishLoadComponent();

            }
        });
        this.addChildObject(loadComponent);

        loadComponent.setFloatTexture();
        loadComponent.getTransform().setPosition(0f, 1f, -0.11f);
        loadComponent.getTransform().rotateByAxis(180f, 0f, 1f, 0f);
        loadComponent.getTransform().setScale(1f, 1f, 1f);

    }

    private void onFinishLoadComponent() {

        if (this.totemEventListener != null) {
            this.totemEventListener.onFinishLoadingTotem(this);
            removeChildObject(loadComponent);
        }
    }

    public void setText(String text) {
        SXRTextViewNode textTitle = new SXRTextViewNode(sxrContext, TEXT_WIDTH, TEXT_HEIGHT, text);
        textTitle.setTextSize(7);
        textTitle.setGravity(android.view.Gravity.CENTER_HORIZONTAL);
        textTitle.getTransform().setPosition(0f, .6f, -0.1f);
        textTitle.getTransform().rotateByAxis(-180, 0, 1, 0);
        textTitle.getRenderData().setRenderingOrder(TOTEM);
        addChildObject(textTitle);
    }

    public void setIcon(int iconPath) {
        icon = new PlayPauseButton(sxrContext, .3f, .3f,
                sxrContext.getAssetLoader().loadTexture(new SXRAndroidResource(sxrContext, iconPath)));
        icon.getTransform().setPosition(0f, 1f, -0.3f);
        icon.getTransform().rotateByAxis(-180, 0, 1, 0);
        icon.getRenderData().setRenderingOrder(TOTEM);
        this.attachCollider(new SXRMeshCollider(getSXRContext(), false));
        addChildObject(icon);
    }

    public PlayPauseButton getIcon() {
        return icon;
    }

    public void setTotemEventListener(TotemEventListener listener) {
        this.totemEventListener = listener;
    }

}
