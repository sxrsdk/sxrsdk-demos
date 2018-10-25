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

package com.samsungxr.video.focus;

import com.samsungxr.SXRContext;
import com.samsungxr.SXRPicker;
import com.samsungxr.SXRSceneObject;
import com.samsungxr.IPickEvents;

public final class FocusableController implements IPickEvents {

    private FocusableSceneObject currentFocused = null;

    @Override
    public void onPick(SXRPicker picker) {

    }

    @Override
    public void onNoPick(SXRPicker picker) {

    }

    @Override
    public void onEnter(SXRSceneObject sceneObj, SXRPicker.SXRPickedObject collision) {
        if (FocusableSceneObject.class.isAssignableFrom(sceneObj.getClass())) {
            currentFocused = (FocusableSceneObject)sceneObj;
            currentFocused.setFocus(true);
        }
    }

    @Override
    public void onExit(SXRSceneObject sceneObj) {
        if (FocusableSceneObject.class.isAssignableFrom(sceneObj.getClass())) {
            currentFocused = (FocusableSceneObject)sceneObj;
            currentFocused.setFocus(false);
        }
        currentFocused = null;
    }

    @Override
    public void onInside(SXRSceneObject sceneObj, SXRPicker.SXRPickedObject collision) {

    }

    public boolean processClick(SXRContext context)
    {
        if (currentFocused != null) {
            currentFocused.dispatchInClick();
            return true;
        }
        return false;
    }
}
