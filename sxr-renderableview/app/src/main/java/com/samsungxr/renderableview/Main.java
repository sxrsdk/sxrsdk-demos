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

package com.samsungxr.renderableview;

import android.graphics.Color;
import android.widget.TextView;

import com.samsungxr.SXRContext;
import com.samsungxr.SXRMain;
import com.samsungxr.nodes.SXRCubeNode;
import com.samsungxr.nodes.SXRViewNode;

public class Main extends SXRMain {
    private final MainActivity mActivity;

    private SXRViewNode mLayoutLeftNode;
    private SXRViewNode mWebNode;
    private SXRViewNode mTextNode;

    public Main(MainActivity activity) {
        mActivity = activity;
    }

    @Override
    public void onInit(final SXRContext sxrContext) throws Throwable {
        // SXRCubeNode - Just to take cube mesh.
        SXRCubeNode cube = new SXRCubeNode(sxrContext);

        mLayoutLeftNode = new SXRViewNode(sxrContext,
                R.layout.activity_main, cube.getRenderData().getMesh());

        sxrContext.getMainScene().addNode(mLayoutLeftNode);

        mLayoutLeftNode.getTransform().setPosition(-1.0f, 0.0f, -2.5f);
        mLayoutLeftNode.setTextureBufferSize(512);

        mWebNode = new SXRViewNode(sxrContext,
                mActivity.getWebView(), cube.getRenderData().getMesh());

        sxrContext.getMainScene().addNode(mWebNode);

        mWebNode.getTransform().setPosition(1.0f, 0.0f, -2.5f);
        mWebNode.setTextureBufferSize(512);

        TextView  textView = new TextView(sxrContext.getActivity());
        textView.setText("Android's Renderable Views");
        textView.setTextColor(Color.WHITE);

        mTextNode = new SXRViewNode(sxrContext, textView, 2.0f, 1.0f);
        sxrContext.getMainScene().addNode(mTextNode);
        mTextNode.getTransform().setPosition(0.0f, -2.0f, -2.5f);
        mTextNode.setTextureBufferSize(512);
    }

    @Override
    public void onStep() {
        mLayoutLeftNode.getTransform().rotateByAxis(0.5f, 1, 1, 0);

        mWebNode.getTransform().rotateByAxis(-0.5f, 1, 1, 0);
    }

}
