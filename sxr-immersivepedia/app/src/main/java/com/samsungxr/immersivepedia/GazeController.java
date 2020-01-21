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

package com.samsungxr.immersivepedia;

import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRRenderData;
import com.samsungxr.SXRNode;
import com.samsungxr.SXRSwitch;
import com.samsungxr.immersivepedia.util.RenderingOrderApplication;
import com.samsungxr.io.SXRCursorController;

public class GazeController {
    private SXRNode cursorRoot;
    private SXRNode cursor;
    private SXRNode highlightCursor;
    private SXRSwitch cursorSelector;

    private static float NORMAL_CURSOR_SIZE = 0.5f;
    private static float HIGHLIGHT_CURSOR_SIZE = 0.6f;
    private static float CURSOR_Z_POSITION = -9.0f;

    private static int CURSOR_RENDER_ORDER = RenderingOrderApplication.CURSOR;
    private static GazeController mSingleton = null;

    public  GazeController(SXRCursorController controller) {
        SXRContext sxrContext = controller.getSXRContext();

        cursorRoot = new SXRNode(sxrContext);
        cursorSelector = new SXRSwitch(sxrContext);
        cursorRoot.attachComponent(cursorSelector);
        cursor = new SXRNode(sxrContext,
                                    sxrContext.createQuad(NORMAL_CURSOR_SIZE,
                                                          NORMAL_CURSOR_SIZE),
                                    sxrContext.getAssetLoader().loadTexture(
                                            new SXRAndroidResource(sxrContext,
                                                                   R.drawable.head_tracker)));
        cursor.getTransform().setPositionZ(CURSOR_Z_POSITION);
        cursor.getRenderData().setDepthTest(false);
        cursor.getRenderData().setRenderingOrder(CURSOR_RENDER_ORDER);
        cursorRoot.addChildObject(cursor);
        cursor.setName("cursor");
        highlightCursor = new SXRNode(sxrContext,
                                             sxrContext.createQuad(HIGHLIGHT_CURSOR_SIZE,
                                                                   HIGHLIGHT_CURSOR_SIZE),
                                             sxrContext.getAssetLoader().loadTexture(
                                                     new SXRAndroidResource(sxrContext,
                                                                            R.drawable.highlightcursor)));
        highlightCursor.getTransform().setPositionZ(CURSOR_Z_POSITION);
        highlightCursor.getRenderData().setDepthTest(false);
        highlightCursor.getRenderData().setRenderingOrder(CURSOR_RENDER_ORDER);

        highlightCursor.getRenderData().getMaterial().setOpacity(0f);
        cursorRoot.addChildObject(highlightCursor);
        controller.setCursor(cursorRoot);
        mSingleton = this;
    }

    public static GazeController get() {
        return mSingleton;
    }

    public SXRNode getCursor()
    {
       return cursorRoot;
    }

    public void enableInteractiveCursor() {
        cursorSelector.setSwitchIndex(1);
    }

    public void disableInteractiveCursor() {
        cursorSelector.setSwitchIndex(0);
    }

    public void enableGaze() {
        cursorRoot.setEnable(true);
    }

    public void disableGaze() {
        cursorRoot.setEnable(false);
    }

}
