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

package com.samsungxr.keyboard.util;

import com.samsungxr.SXRBitmapImage;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRSceneObject;
import com.samsungxr.SXRTexture;
import com.samsungxr.keyboard.textField.Text;

public class InteractiveText extends SXRSceneObject {

    public Text currentText;
    private int width, height;

    public InteractiveText(SXRContext gvrContext, int width, int height) {

        super(gvrContext, Util.convertPixelToVRFloatValue(width), Util
                .convertPixelToVRFloatValue(height));
        setName(SceneObjectNames.INTERACTIVE_TEXT);

        currentText = new Text();
        this.width = width;
        this.height = height;
        updateText(gvrContext);

    }

    public void updateText(SXRContext context) {

        SXRBitmapImage tex = new SXRBitmapImage(context, SXRTextBitmapFactory.create(width,
                height, currentText));
        SXRTexture texture = new SXRTexture(getSXRContext());
        texture.setImage(tex);
        getRenderData().getMaterial().setMainTexture(texture);
    }

    public void setTextAdditive(SXRContext context, String newText) {

        setText(context, currentText.text.concat(newText));
    }

    public void setText(final SXRContext context, final String newText) {

        context.runOnGlThread(new Runnable() {

            @Override
            public void run() {
                if (currentText.text == newText)
                    return;
                if (newText.length() > currentText.maxLength)
                    return;

                currentText.text = newText;
                updateText(context);

            }
        });

    }

}
