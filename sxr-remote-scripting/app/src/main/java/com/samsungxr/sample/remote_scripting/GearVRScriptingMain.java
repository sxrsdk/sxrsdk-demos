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

package com.samsungxr.sample.remote_scripting;

import android.view.Gravity;

import com.samsungxr.SXRContext;
import com.samsungxr.SXRMain;
import com.samsungxr.SXRScene;
import com.samsungxr.debug.DebugServer;

import com.samsungxr.IErrorEvents;

import com.samsungxr.scene_objects.SXRTextViewSceneObject;
import com.samsungxr.script.SXRScriptManager;

import smcl.samsung.com.debugwebserver.DebugWebServer;

public class GearVRScriptingMain extends SXRMain
{
    private static final String TAG = GearVRScriptingMain.class.getSimpleName();
    private static final int DEBUG_SERVER_PORT = 5000;
    DebugWebServer server;
    private SXRContext gvrContext;

    @Override
    public void onInit(SXRContext context) {
        gvrContext = context;
        final DebugServer debug = gvrContext.startDebugServer();
        SXRScene scene = gvrContext.getMainScene();
        IErrorEvents errorHandler = new IErrorEvents()
        {
            public void onError(String message, Object source)
            {
                debug.logError(message);
            }
        };
        gvrContext.getEventReceiver().addListener(errorHandler);
        // get the ip address
        GearVRScripting activity = (GearVRScripting) gvrContext.getActivity();
        String ipAddress = activity.getIpAddress();
        String debugUrl = "http://" + ipAddress + ":" + DEBUG_SERVER_PORT;
        String telnetString = "telnet " + ipAddress + " " + DebugServer.DEFAULT_DEBUG_PORT;

        // create text object to tell the user where to connect
        SXRTextViewSceneObject textViewSceneObject = new SXRTextViewSceneObject(gvrContext, 2.0f,
                0.5f, debugUrl + "\n" + telnetString);
        textViewSceneObject.setGravity(Gravity.CENTER);
        textViewSceneObject.setTextSize(5);
        textViewSceneObject.getTransform().setPosition(0.0f, 0.0f, -3.0f);

        // make sure to set a name so we can reference it when we log in
        textViewSceneObject.setName("text");

        // add it to the scene
        scene.addSceneObject(textViewSceneObject);
        context.getInputManager().selectController();

        // Add display utils for scripts
        SXRScriptManager scriptManager = (SXRScriptManager)gvrContext.getScriptManager();
        scriptManager.addVariable("display", new DisplayUtils(gvrContext));
        scriptManager.addVariable("editor", new EditorUtils(gvrContext));
        scriptManager.addVariable("passthrough", new PassthroughUtils(gvrContext, activity));
        scriptManager.addVariable("filebrowser", new FileBrowserUtils(gvrContext));
        scriptManager.addVariable("source", new SourceUtils(gvrContext));
        gvrContext.startDebugServer();
        server = new DebugWebServer(gvrContext);
        server.listen(DEBUG_SERVER_PORT);
    }

    @Override
    public void onStep() {
    }

    public void stop() {
        if(server != null) {
            server.stop();
        }
        if (null != gvrContext){
            gvrContext.stopDebugServer();
        }
    }
}
