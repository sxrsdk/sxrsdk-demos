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

package com.samsungxr.sample.gvrjavascript;

import java.io.IOException;

import com.samsungxr.SXRActivity;
import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRResourceVolume;
import com.samsungxr.SXRMain;
import com.samsungxr.script.SXRScriptBundle;
import com.samsungxr.script.SXRScriptException;
import com.samsungxr.script.SXRScriptFile;
import com.samsungxr.script.SXRScriptManager;
import com.samsungxr.script.IScriptFile;
import com.samsungxr.script.IScriptBundle;
import com.samsungxr.script.IScriptManager;

import android.os.Bundle;

public class GearVRJavascriptActivity extends SXRActivity {
    enum DemoMode {
        USE_SINGLE_SCRIPT,
        USE_SCRIPT_BUNDLE,
    }

    // Set the demo mode here
    DemoMode mode = DemoMode.USE_SCRIPT_BUNDLE;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Instantiate your script class
        // Note: you could just use SXRMain if everything is in lua script
        GearVRJavascriptMain main = new GearVRJavascriptMain();
        setMain(main, "gvr.xml");

        IScriptManager sm = getSXRContext().getScriptManager();

        // Add utils for scripts
        sm.addVariable("utils", new ScriptUtils());

        switch (mode) {
            case USE_SINGLE_SCRIPT: {
                // Attach a script file directly (without using a SXRScriptBundle)
                IScriptFile scriptFile;
                try {
                    scriptFile = (SXRScriptFile)sm.loadScript(
                            new SXRAndroidResource(getSXRContext(), "script.js"),
                            IScriptManager.LANG_JAVASCRIPT);
                    sm.attachScriptFile(main, scriptFile);
                } catch (IOException | SXRScriptException e) {
                    e.printStackTrace();
                }
                break;
            }

            case USE_SCRIPT_BUNDLE: {
                // Load a script bundle
                IScriptBundle scriptBundle;
                try {
                    scriptBundle = sm.loadScriptBundle("script_bundle.json",
                            new SXRResourceVolume(getSXRContext(), SXRResourceVolume.VolumeType.ANDROID_ASSETS));
                    sm.bindScriptBundle(scriptBundle, main, true);
                } catch (IOException | SXRScriptException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
