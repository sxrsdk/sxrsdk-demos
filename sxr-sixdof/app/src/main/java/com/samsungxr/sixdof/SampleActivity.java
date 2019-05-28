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

package com.samsungxr.sixdof;

import android.os.Bundle;
import android.util.Log;

import com.samsungxr.SXRActivity;
import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRMain;
import com.samsungxr.SXRNode;
import com.samsungxr.SXRPicker;
import com.samsungxr.SXRRenderData;
import com.samsungxr.SXRScene;
import com.samsungxr.io.SXRCursorController;
import com.samsungxr.io.SXRInputManager;

import com.samsungxr.PlatformEntitlementCheck;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.EnumSet;

public class SampleActivity extends SXRActivity {

    private static final String TAG = "sxr-sixdof";

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setMain(new SampleMain(), "sxr.xml");
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private static class SampleMain extends SXRMain {
        private SXRNode mRoom;

        public SampleMain() {
        }

        @Override
        public void onInit(final SXRContext sxrContext) {

            SXRScene scene = sxrContext.getMainScene();
            scene.setBackgroundColor(1, 1, 1, 1);

            try {
                float eyeheight = -2.0f; // default to 2 meters

                // try to read a system prop eye height if we want to override it
                String eyeHeightString = SystemProperties.read("debug.samsungxr.eyeheight");
                if(eyeHeightString != null && !eyeHeightString.isEmpty()) {
                    eyeheight = Float.parseFloat(eyeHeightString);
                    android.util.Log.d(TAG, "eye height set to: " + eyeheight);
                }

                String roomPath = new String("sample_environment.obj");
                String gizmoPath = new String("transformgizmo.obj");
                String groundPath = new String("groundplane.obj");

                mRoom = sxrContext.getAssetLoader().loadModel(roomPath, scene);
                mRoom.getTransform().setPosition(0.0f, eyeheight, 0.0f);
                SXRNode gizmo = sxrContext.getAssetLoader().loadModel(gizmoPath, scene);
                gizmo.getTransform().setPosition(0.0f, eyeheight, 0.0f);
                SXRNode groundplane = sxrContext.getAssetLoader().loadModel(groundPath, scene);
                groundplane.getTransform().setPosition(0.0f, eyeheight+0.001f, 0.0f);

                SXRNode node = new SXRNode(getSXRContext(), 0.5f, 0.5f);
                node.getRenderData().getMaterial().setDiffuseColor(1, 0,0,1);
                node.getRenderData().getMaterial().setColor(1, 0, 0);
                node.getTransform().setPositionZ(-7);
                node.getTransform().setPositionY(0.2f);
                scene.addNode(node);
            } catch(IOException e) {
                e.printStackTrace();
            }


            getSXRContext().getInputManager().selectController(new SXRInputManager.ICursorControllerSelectListener()
            {
                public void onCursorControllerSelected(SXRCursorController newController, SXRCursorController oldController)
                {
                    SXRNode cursor = new SXRNode(sxrContext, sxrContext.createQuad(1f, 1f),
                            sxrContext.getAssetLoader().loadTexture(
                                    new SXRAndroidResource(sxrContext, com.samsungxr.R.drawable.cursor)));
                    cursor.getRenderData().setDepthTest(false);
                    cursor.getRenderData().setRenderingOrder(SXRRenderData.SXRRenderingOrder.OVERLAY);
                    newController.setCursor(cursor);
                    newController.setCursorDepth(-7.0f);
                    newController.setCursorControl(SXRCursorController.CursorControl.PROJECT_CURSOR_ON_SURFACE);

                    newController.getPicker().setEventOptions(EnumSet.of(
                            SXRPicker.EventOptions.SEND_TOUCH_EVENTS,
                            SXRPicker.EventOptions.SEND_TO_LISTENERS));
                }
            });
        }
    }

    private static class SystemProperties {

        private static String GETPROP_EXECUTABLE_PATH = "/system/bin/getprop";
        private static String TAG = "TVR";

        public static String read(String propName) {
            Process process = null;
            BufferedReader bufferedReader = null;

            try {
                process = new ProcessBuilder().command(GETPROP_EXECUTABLE_PATH, propName).redirectErrorStream(true).start();
                bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line = bufferedReader.readLine();
                if (line == null){
                    line = ""; //prop not set
                }
                Log.i(TAG,"read System Property: " + propName + "=" + line);
                return line;
            } catch (Exception e) {
                Log.e(TAG,"Failed to read System Property " + propName,e);
                return "";
            } finally{
                if (bufferedReader != null){
                    try {
                        bufferedReader.close();
                    } catch (IOException e) {}
                }
                if (process != null){
                    process.destroy();
                }
            }
        }
    }

}
