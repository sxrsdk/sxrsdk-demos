/*
 * Copyright 2015 Samsung Electronics Co., LTD
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */

package com.samsung.accessibility.focus;

import com.samsungxr.SXRSceneObject;
import com.samsungxr.utility.Log;

import java.util.ArrayList;

public final class FocusableController {

    public static boolean clickProcess(SXRSceneObject pickedObject) {
        if(pickedObject != null) {
            FocusableSceneObject object = (FocusableSceneObject) pickedObject;
            Log.d("NOLA", "clickProcess " + object.getClass().getSimpleName());
            object.dispatchInClick();
            return true;
        }
        return false;
    }

}