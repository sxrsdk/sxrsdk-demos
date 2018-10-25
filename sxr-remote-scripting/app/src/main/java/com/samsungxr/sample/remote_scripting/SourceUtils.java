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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRResourceVolume;
import com.samsungxr.SXRSceneObject;
import com.samsungxr.debug.DebugServer;
import com.samsungxr.SXRAndroidResource;
import com.samsungxr.script.SXRScriptManager;
import com.samsungxr.utility.FileNameUtils;
import com.samsungxr.script.SXRScriptBehavior;
import com.samsungxr.script.SXRScriptException;
import com.samsungxr.script.SXRScriptFile;
import com.samsungxr.IErrorEvents;
import com.samsungxr.SXRResourceVolume;

public class SourceUtils {
    private SXRContext gvrContext;
    private SXRScriptManager mScriptManager;

    public SourceUtils(SXRContext context) {
        gvrContext = context;
        mScriptManager = (SXRScriptManager)gvrContext.getScriptManager();
    }

    private void logError(String message)
    {
        gvrContext.logError(message, this);
    }
    
    // from assets directory 
    public void script(String filename) {
        try {
            SXRResourceVolume.VolumeType volType = SXRResourceVolume.VolumeType.ANDROID_ASSETS;
            String lowerName = filename.toLowerCase();
            String language = FileNameUtils.getExtension(filename);
            
            if (lowerName.startsWith("sd:"))
            {
                volType = SXRResourceVolume.VolumeType.ANDROID_SDCARD;
                filename = filename.substring(3);
            }
            else if (lowerName.startsWith("http"))
            {
                volType = SXRResourceVolume.VolumeType.NETWORK;
            }
            SXRResourceVolume volume = new SXRResourceVolume(gvrContext, volType);
            SXRAndroidResource resource = volume.openResource(filename);
            SXRScriptFile script = (SXRScriptFile)mScriptManager.loadScript(resource, language);
            script.invoke();
            String err = script.getLastError();
            if (err != null) {
                logError(err);
            }
        } catch(IOException e) {
            logError(e.getMessage());
        } catch(SXRScriptException se) {
            logError(se.getMessage());
        }
    }
    
    public void scriptBundle(String filename)
    {
        SXRResourceVolume.VolumeType volType = SXRResourceVolume.VolumeType.ANDROID_ASSETS;
        String fname = filename.toLowerCase();
        if (fname.startsWith("sd:"))
        {
            volType = SXRResourceVolume.VolumeType.ANDROID_SDCARD;
        }
        else if (fname.startsWith("http"))
        {
            volType = SXRResourceVolume.VolumeType.NETWORK;
        }
        SXRResourceVolume volume = new SXRResourceVolume(gvrContext, volType, filename);
        try
        {
            mScriptManager.loadScriptBundle(filename, volume);
        }
        catch(IOException e)
        {
            logError(e.getMessage());
        }
    }
    
    public void attachScript(String filename, String sceneObjName)
    {
        SXRSceneObject sceneObj = gvrContext.getMainScene().getSceneObjectByName(sceneObjName);
        if (sceneObj == null)
        {
            logError("attachScript: scene object not found " + sceneObjName);
        }
        try
        {
            sceneObj.attachComponent(new SXRScriptBehavior(gvrContext, filename));
        }
        catch (IOException e)
        {
            logError(e.getMessage());
        }
        catch (SXRScriptException se)
        {
            logError(se.getMessage());
        }
    }

}

