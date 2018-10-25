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

package com.samsungxr.keyboard.spinner;

import com.samsungxr.SXRBitmapImage;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRTexture;
import com.samsungxr.keyboard.textField.Text;
import com.samsungxr.keyboard.textField.TextFieldItem;
import com.samsungxr.keyboard.util.SXRTextBitmapFactory;

public class SpinnerItem extends TextFieldItem {

    private boolean cacheTestOn = true;

    public SpinnerItem(SXRContext gvrContext, float sceneObjectWidth, float sceneObjectHeigth,
            int bitmapWidth, int bitmapHeigth, int position,
            Text text) {
        super(gvrContext, sceneObjectWidth, sceneObjectHeigth, bitmapWidth, bitmapHeigth, text,
                position);

    }

    @Override
    public void updateText(SXRContext context) {
        if (cacheTestOn) {

            if (null != charItem) {
                SXRBitmapImage tex = new SXRBitmapImage(context, SpinnerItemFactory.getInstance(
                        getSXRContext()).getBitmap(charItem.getMode(),
                        charItem.getPosition()));
                SXRTexture texture = new SXRTexture(getSXRContext());
                texture.setImage(tex);
                getRenderData().getMaterial().setMainTexture(texture);
            }

        } else {
            SXRBitmapImage tex = new SXRBitmapImage(context, SXRTextBitmapFactory.create(
                    context.getContext(), width, height, currentText, 0));
            SXRTexture texture = new SXRTexture(getSXRContext());
            texture.setImage(tex);
            getRenderData().getMaterial().setMainTexture(texture);
        }
    }

}
