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

package com.samsungxr.keyboard.textField;

import com.samsungxr.SXRBitmapImage;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRNode;
import com.samsungxr.SXRTexture;
import com.samsungxr.keyboard.model.CharItem;
import com.samsungxr.keyboard.util.SXRTextBitmapFactory;
import com.samsungxr.keyboard.util.NodeNames;

public class TextFieldItem extends SXRNode {

    public Text currentText;
    protected int width;
    protected int height;
    protected CharItem charItem;
    private int position;

    public TextFieldItem(SXRContext sxrContext, float sceneObjectWidth, float sceneObjectHeigth,
            int bitmapWidth, int bitmapHeigth, Text text,
            int position) {
        super(sxrContext, sceneObjectWidth, sceneObjectHeigth);
        setName(NodeNames.TEXT_FIELD_ITEM);

        currentText = text;
        this.width = bitmapWidth;
        this.height = bitmapHeigth;
        updateText(sxrContext);
        this.position = position;
    }

    public int getPosition() {
        return this.position;
    }

    public void updateText(SXRContext context) {
        SXRBitmapImage tex = new SXRBitmapImage(context, SXRTextBitmapFactory.create(
                context.getContext(), width, height, currentText, 0));
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

                if (currentText.text == newText) {
                    return;
                }

                if (newText.length() > currentText.maxLength) {
                    return;
                }

                currentText.text = newText;
                updateText(context);
            }
        });
    }

    public void removeCharacter(SXRContext context) {

        if (currentText.text.length() <= 1)
            setText(context, "");
        else if (currentText.text.length() > 1) {
            setText(context, currentText.text.substring(0, currentText.text.length() - 1));
        }
    }

    public void setText(SXRContext sxrContext, CharItem charItem) {
        setText(sxrContext, charItem.getCharacter());
        this.charItem = charItem;
    }

    public CharItem getCharItem() {
        return charItem;
    }

}
