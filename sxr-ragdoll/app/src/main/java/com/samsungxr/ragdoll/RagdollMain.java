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

package com.samsungxr.ragdoll;

import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRContext;
import com.samsungxr.SXREventListeners;
import com.samsungxr.SXRMain;
import com.samsungxr.SXRPicker;
import com.samsungxr.SXRRenderData;
import com.samsungxr.SXRScene;
import com.samsungxr.SXRNode;
import com.samsungxr.io.SXRCursorController;
import com.samsungxr.io.SXRInputManager;
import com.samsungxr.physics.SXRPhysicsLoader;
import com.samsungxr.physics.SXRRigidBody;
import com.samsungxr.physics.SXRWorld;
import com.samsungxr.utility.Log;

import java.util.EnumSet;

public class RagdollMain extends SXRMain {
    private final static String TAG = "RagDoll";
    private final static float CURSOR_DEPTH = 6.0f;

    private SXRCursorController mCursorController = null;
    private SXRNode mCursor = null;
    private TouchHandler mTouchHandler = null;
    private SXRWorld mWorld = null;

    public RagdollMain() {}

    @Override
    public void onInit(final SXRContext sxrContext) throws Throwable {
        super.onInit(sxrContext);

        final SXRScene scene = sxrContext.getMainScene();

        scene.setBackgroundColor(0,0,0,0);

        Log.d(TAG, "Loading Rag Doll mesh...");
        SXRNode model = sxrContext.getAssetLoader().loadModel("models/ragdoll.fbx", scene);

        model.getTransform().setPosition(0,0, -3);
        scene.addNode(model);

        mWorld = new SXRWorld(sxrContext);
        mWorld.setGravity(0f, -1f, 0f);
        scene.getRoot().attachComponent(mWorld);

        Log.d(TAG, "Loading Rag Doll physics...");
        SXRPhysicsLoader.loadPhysicsFile(sxrContext,
                "models/ragdoll.bullet", true, scene);

        initCursorController(sxrContext);
    }

    /**
     * Initialize GearVR controller handler.
     *
     * @param sxrContext SXRf context.
     */
    private void initCursorController(SXRContext sxrContext) {
        SXRScene scene = sxrContext.getMainScene();
        mTouchHandler = new TouchHandler();

        scene.getEventReceiver().addListener(mTouchHandler);
        SXRInputManager inputManager = sxrContext.getInputManager();
        mCursor = new SXRNode(sxrContext,
                sxrContext.createQuad(0.2f * CURSOR_DEPTH,
                        0.2f * CURSOR_DEPTH),
                sxrContext.getAssetLoader().loadTexture(new SXRAndroidResource(sxrContext,
                        R.raw.cursor)));
        mCursor.getRenderData().setDepthTest(false);
        mCursor.getRenderData().setRenderingOrder(SXRRenderData.SXRRenderingOrder.OVERLAY);
        final EnumSet<SXRPicker.EventOptions> eventOptions = EnumSet.of(
                SXRPicker.EventOptions.SEND_TOUCH_EVENTS,
                SXRPicker.EventOptions.SEND_TO_LISTENERS);
        inputManager.selectController(new SXRInputManager.ICursorControllerSelectListener()
        {
            public void onCursorControllerSelected(SXRCursorController newController, SXRCursorController oldController)
            {
                if (oldController != null)
                {
                    oldController.removePickEventListener(mTouchHandler);
                }
                mCursorController = newController;
                newController.addPickEventListener(mTouchHandler);
                newController.setCursor(mCursor);
                newController.setCursorDepth(-CURSOR_DEPTH);
                newController.setCursorControl(SXRCursorController.CursorControl.CURSOR_CONSTANT_DEPTH);
                newController.getPicker().setEventOptions(eventOptions);
            }
        });
    }

    private class TouchHandler extends SXREventListeners.TouchEvents {
        @Override
        public void onTouchStart(SXRNode sceneObj, SXRPicker.SXRPickedObject collision) {
            super.onTouchStart(sceneObj, collision);

            mWorld.startDrag(sceneObj,
                    collision.hitLocation[0], collision.hitLocation[1], collision.hitLocation[2]);
        }

        @Override
        public void onTouchEnd(SXRNode sceneObj, SXRPicker.SXRPickedObject collision) {
            super.onTouchEnd(sceneObj, collision);
            mWorld.stopDrag();
        }
    }
}
