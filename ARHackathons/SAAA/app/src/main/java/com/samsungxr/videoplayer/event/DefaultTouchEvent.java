/*
 * Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.samsungxr.videoplayer.event;

import android.view.MotionEvent;

import com.samsungxr.SXRPicker;
import com.samsungxr.SXRNode;
import com.samsungxr.ITouchEvents;

public class DefaultTouchEvent implements ITouchEvents {

    @Override
    public void onEnter(SXRNode sxrNode, SXRPicker.SXRPickedObject sxrPickedObject) {
    }

    @Override
    public void onExit(SXRNode sxrNode, SXRPicker.SXRPickedObject sxrPickedObject) {
    }

    @Override
    public void onTouchStart(SXRNode sxrNode, SXRPicker.SXRPickedObject sxrPickedObject) {
    }

    @Override
    public void onTouchEnd(SXRNode sxrNode, SXRPicker.SXRPickedObject sxrPickedObject) {
    }

    @Override
    public void onInside(SXRNode sxrNode, SXRPicker.SXRPickedObject sxrPickedObject) {
    }

    @Override
    public void onMotionOutside(SXRPicker sxrPicker, MotionEvent motionEvent) {
    }
}
