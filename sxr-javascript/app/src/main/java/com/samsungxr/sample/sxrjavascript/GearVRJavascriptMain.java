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

package com.samsungxr.sample.sxrjavascript;

import com.samsungxr.SXRContext;
import com.samsungxr.io.SXRCursorController;
import com.samsungxr.SXRMaterial;
import com.samsungxr.SXRRenderData;
import com.samsungxr.SXRSceneObject;
import com.samsungxr.SXRMain;
import com.samsungxr.SXRShaderId;
import com.samsungxr.io.SXRInputManager;
import com.samsungxr.scene_objects.SXRSphereSceneObject;


public class GearVRJavascriptMain extends SXRMain {
    private static final String TAG = GearVRJavascriptMain.class.getSimpleName();
    private static final float DEPTH = -1.5f;

    private SXRContext context;
    private SXRMaterial mShaderMaterial;
    private SXRShaderId mShaderID = null;
    private SXRSceneObject cursorModel = null;

    @Override
    public void onInit(SXRContext sxrContext) {
        // The onInit function in script.js will be invoked

        sxrContext.startDebugServer();        

        context = sxrContext;
        sxrContext.getInputManager().selectController(listener);
    }

    @Override
    public void onStep() {
        // The onStep function in script.js will be invoked
    }

    private SXRSceneObject createCursor()
    {
        SXRSceneObject cursor = new SXRSphereSceneObject(context);
        SXRRenderData cursorRenderData = cursor.getRenderData();

        mShaderID = new SXRShaderId(CustomShaderManager.class);
        mShaderMaterial = new SXRMaterial(getSXRContext(), mShaderID);
        mShaderMaterial.setVec4(CustomShaderManager.COLOR_KEY, 1.0f, 0.0f, 0.0f, 0.5f);
        cursor.getTransform().setScale(-0.015f, -0.015f, -0.015f);
        cursorRenderData.setMaterial(mShaderMaterial);
        cursorRenderData.setDepthTest(false);
        cursorRenderData.setRenderingOrder(SXRRenderData.SXRRenderingOrder.OVERLAY);
        cursorModel = cursor;
        return cursor;
    }

    private SXRInputManager.ICursorControllerSelectListener listener = new SXRInputManager.ICursorControllerSelectListener() {
        @Override
        public void onCursorControllerSelected(SXRCursorController controller, SXRCursorController oldController) {
            if (cursorModel == null)
            {
                createCursor();
            }
            controller.setCursor(cursorModel);
            controller.setCursorDepth(DEPTH);
            controller.setCursorControl(SXRCursorController.CursorControl.PROJECT_CURSOR_ON_SURFACE);
        }
    };
}
