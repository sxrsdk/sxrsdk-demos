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

package com.samsungxr.immersivepedia.focus;

import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRNode;
import com.samsungxr.immersivepedia.Main;
import com.samsungxr.immersivepedia.R;
import com.samsungxr.immersivepedia.input.TouchPadInput;

public final class FocusableController {

    public static boolean swipeProcess(SXRContext context, PickHandler mPickHandler)
    {
        if (mPickHandler == null)
        {
            return false;
        }
        if (mPickHandler.PickedObject != null &&
            isAVisibleObjectBeingSeen(context, mPickHandler.PickedObject))
        {
            FocusableNode object = (FocusableNode) mPickHandler.PickedObject;
            object.dispatchInGesture(TouchPadInput.getCurrent().swipeDirection);
            return true;
        }
        return false;
    }

    public static boolean clickProcess(SXRContext context, PickHandler mPickHandler)
    {
        if (mPickHandler == null)
        {
            return false;
        }

        if (mPickHandler.PickedObject == null || !isAVisibleObjectBeingSeen(context, mPickHandler.PickedObject)) {
            Main.clickOut();
        }
        else{
            FocusableNode object = (FocusableNode) mPickHandler.PickedObject;
            object.dispatchInClick();
            return true;
        }
        return false;
    }

    private static boolean isAVisibleObjectBeingSeen(SXRContext sxrContext, SXRNode object) {
        return (isVisible(object) && !hasEmptyTexture(sxrContext, object));
    }

    private static boolean isVisible(SXRNode object) {
        return object.getRenderData() != null && object.getRenderData().getMaterial() != null
                && object.getRenderData().getMaterial().getOpacity() > 0;
    }

    private static boolean hasEmptyTexture(SXRContext sxrContext, SXRNode object) {
        return object.getRenderData().getMaterial().getMainTexture() != null
                && object.getRenderData().getMaterial().getMainTexture()
                .equals(sxrContext.getAssetLoader().loadTexture(new SXRAndroidResource(sxrContext, R.drawable.empty)));
    }

}
