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

import com.samsungxr.nodes.SXRTextViewNode;
import com.samsungxr.script.SXRScriptManager;

import smcl.samsung.com.debugwebserver.DebugWebServer;

public class GearVRScriptingMain extends SXRMain
{
    private static final String TAG = GearVRScriptingMain.class.getSimpleName();
    private static final int DEBUG_SERVER_PORT = 5000;
    DebugWebServer server;
    private SXRContext sxrContext;

    @Override
    public void onInit(SXRContext context) {
        sxrContext = context;
        final DebugServer debug = sxrContext.startDebugServer();
        SXRScene scene = sxrContext.getMainScene();
        IErrorEvents errorHandler = new IErrorEvents()
        {
            public void onError(String message, Object source)
            {
                debug.logError(message);
            }
        };
        sxrContext.getEventReceiver().addListener(errorHandler);
        // get the ip address
        GearVRScripting activity = (GearVRScripting) sxrContext.getActivity();
        String ipAddress = activity.getIpAddress();
        String debugUrl = "http://" + ipAddress + ":" + DEBUG_SERVER_PORT;
        String telnetString = "telnet " + ipAddress + " " + DebugServer.DEFAULT_DEBUG_PORT;

        // create text object to tell the user where to connect
        SXRTextViewNode textViewNode = new SXRTextViewNode(sxrContext, 2.0f,
                0.5f, debugUrl + "\n" + telnetString);
        textViewNode.setGravity(Gravity.CENTER);
        textViewNode.setTextSize(5);
        textViewNode.getTransform().setPosition(0.0f, 0.0f, -3.0f);

        // make sure to set a name so we can reference it when we log in
        textViewNode.setName("text");

        // add it to the scene
        scene.addNode(textViewNode);
        context.getInputManager().selectController();

        // Add display utils for scripts
        SXRScriptManager scriptManager = (SXRScriptManager)sxrContext.getScriptManager();
        scriptManager.addVariable("display", new DisplayUtils(sxrContext));
        scriptManager.addVariable("editor", new EditorUtils(sxrContext));
        scriptManager.addVariable("passthrough", new PassthroughUtils(sxrContext, activity));
        scriptManager.addVariable("filebrowser", new FileBrowserUtils(sxrContext));
        scriptManager.addVariable("source", new SourceUtils(sxrContext));
        sxrContext.startDebugServer();
        server = new DebugWebServer(sxrContext);
        server.listen(DEBUG_SERVER_PORT);
    }

    @Override
    public void onStep() {
    }

    public void stop() {
        if(server != null) {
            server.stop();
        }
        if (null != sxrContext){
            sxrContext.stopDebugServer();
        }
    }
}
