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

package com.samsungxr.arpet.mode;

import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.samsungxr.IViewEvents;
import com.samsungxr.SXRNode;
import com.samsungxr.SXRRenderData;
import com.samsungxr.SXRScene;
import com.samsungxr.nodes.SXRViewNode;

import com.samsungxr.arpet.PetContext;
import com.samsungxr.arpet.R;
import com.samsungxr.arpet.constant.PetConstants;
import com.samsungxr.arpet.util.LayoutViewUtils;

public class EditView extends BasePetView implements View.OnClickListener {
    private SXRNode mEditBackObject;
    private SXRNode mEditSaveObject;
    private Button mSaveButton;
    private LinearLayout mBackButton;
    private OnEditModeClickedListener mListenerEditMode;

    public EditView(PetContext petContext) {
        super(petContext);
        mEditBackObject = new SXRViewNode(petContext.getSXRContext(),
                R.layout.edit_back_layout, new IViewEvents() {
            @Override
            public void onInitView(SXRViewNode sxrViewNode, View view) {
                mBackButton = view.findViewById(R.id.btn_back);
                mBackButton.setOnClickListener(EditView.this);
            }

            @Override
            public void onStartRendering(SXRViewNode sxrViewNode, View view) {
                sxrViewNode.setTextureBufferSize(PetConstants.TEXTURE_BUFFER_SIZE);
                sxrViewNode.getRenderData().setRenderingOrder(
                        SXRRenderData.SXRRenderingOrder.OVERLAY);
                LayoutViewUtils.setWorldPosition(mPetContext.getMainScene(),
                        sxrViewNode, 5f, 11f, 44 + 136, 44);

                EditView.this.addChildObject(sxrViewNode);
            }
        });

        mEditSaveObject = new SXRViewNode(petContext.getSXRContext(),
                R.layout.edit_save_layout, new IViewEvents() {
            @Override
            public void onInitView(SXRViewNode sxrViewNode, View view) {
                mSaveButton = view.findViewById(R.id.btn_save);
                mSaveButton.setOnClickListener(EditView.this);
            }

            @Override
            public void onStartRendering(SXRViewNode sxrViewNode, View view) {
                sxrViewNode.setTextureBufferSize(PetConstants.TEXTURE_BUFFER_SIZE);
                sxrViewNode.getRenderData().setRenderingOrder(
                        SXRRenderData.SXRRenderingOrder.OVERLAY);
                LayoutViewUtils.setWorldPosition(mPetContext.getMainScene(),
                        sxrViewNode, 510f, 12f, 121, 44);

                EditView.this.addChildObject(sxrViewNode);
            }
        });
    }

    @Override
    protected void onShow(SXRScene mainScene) {
        mainScene.getMainCameraRig().addChildObject(this);
    }

    @Override
    protected void onHide(SXRScene mainScene) {
        mainScene.getMainCameraRig().removeChildObject(this);
    }

    public void setListenerEditMode(OnEditModeClickedListener listenerEditMode) {
        mListenerEditMode = listenerEditMode;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_back) {
            mBackButton.post(new Runnable() {
                @Override
                public void run() {
                    mListenerEditMode.OnBack();
                }
            });
        } else if (view.getId() == R.id.btn_save) {
            mSaveButton.post(new Runnable() {
                @Override
                public void run() {
                    mSaveButton.setBackgroundResource(R.drawable.bg_save_button);
                    mSaveButton.setTextColor(Color.parseColor("#ffffff"));
                    mListenerEditMode.OnSave();
                    mListenerEditMode.OnBack();
                }
            });
        }
    }
}
