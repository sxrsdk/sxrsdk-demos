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

import android.graphics.Bitmap;
import android.graphics.Color;

import com.samsungxr.SXRContext;
import com.samsungxr.keyboard.keyboard.numeric.Keyboard;
import com.samsungxr.keyboard.model.CharList;
import com.samsungxr.keyboard.textField.Text;
import com.samsungxr.keyboard.util.SXRTextBitmapFactory;

import java.util.ArrayList;
import java.util.List;

public class SpinnerItemFactory {

    private static SpinnerItemFactory instance;
    private List<Bitmap> numericBitmapList = new ArrayList<Bitmap>();
    private List<Bitmap> alphaLowerBitmapList = new ArrayList<Bitmap>();
    private List<Bitmap> alphaUpperBitmapList = new ArrayList<Bitmap>();
    private List<Bitmap> specialBitmapList = new ArrayList<Bitmap>();
    private SXRContext sxrContext;

    public SpinnerItemFactory(SXRContext sxrContext) {
        this.sxrContext = sxrContext;
    }

    public static synchronized SpinnerItemFactory getInstance(SXRContext sxrContext) {

        if (instance == null) {
            instance = new SpinnerItemFactory(sxrContext);
        }

        return instance;
    }

    private Bitmap createSpinnerItem(String spinnerText) {

        int bitmapWidth = 45;
        int bitmapHeigth = 72;

        Text text = new Text();
        text.textSize = 75;
        // text.backgroundColor = Color.BLACK;
        text.backgroundColor = Color.parseColor("#00204d");
        text.text = spinnerText;

        return SXRTextBitmapFactory.create(sxrContext.getContext(), bitmapWidth, bitmapHeigth,
                text, 0);
    }

    public void init() {

        for (int i = 0; i < CharList.getInstance(sxrContext)
                .getList(Keyboard.SOFT_KEYBOARD_LOWERCASE).size(); i++) {
            alphaLowerBitmapList
                    .add(createSpinnerItem(CharList.getInstance(sxrContext)
                            .getList(Keyboard.SOFT_KEYBOARD_LOWERCASE).get(i)));

        }

        for (int i = 0; i < CharList.getInstance(sxrContext)
                .getList(Keyboard.SOFT_KEYBOARD_UPPERCASE).size(); i++) {
            alphaUpperBitmapList.add(createSpinnerItem(CharList.getInstance(sxrContext)
                    .getList(Keyboard.SOFT_KEYBOARD_UPPERCASE).get(i)
                    .toUpperCase()));
        }

        for (int i = 0; i < CharList.getInstance(sxrContext).getList(Keyboard.NUMERIC_KEYBOARD)
                .size(); i++) {
            numericBitmapList.add(createSpinnerItem(CharList.getInstance(sxrContext)
                    .getList(Keyboard.NUMERIC_KEYBOARD).get(i)));
        }

        for (int i = 0; i < CharList.getInstance(sxrContext)
                .getList(Keyboard.SOFT_KEYBOARD_SPECIAL).size(); i++) {
            specialBitmapList.add(createSpinnerItem(CharList.getInstance(sxrContext)
                    .getList(Keyboard.SOFT_KEYBOARD_SPECIAL).get(i)));
        }

    }

    public Bitmap getBitmap(int mode, int position) {
        return getList(mode).get(position);

    }

    public List<Bitmap> getList(int mode) {
        List<Bitmap> list = null;

        switch (mode) {
            case Keyboard.NUMERIC_KEYBOARD:
                list = numericBitmapList;
                break;
            case Keyboard.SOFT_KEYBOARD_LOWERCASE:
                list = alphaLowerBitmapList;
                break;
            case Keyboard.SOFT_KEYBOARD_SPECIAL:
                list = specialBitmapList;
                break;
            case Keyboard.SOFT_KEYBOARD_UPPERCASE:
                list = alphaUpperBitmapList;
                break;
        }

        return list;
    }

}
