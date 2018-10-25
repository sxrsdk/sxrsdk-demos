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

package com.samsungxr.keyboard.keyboard.numeric;

import android.content.res.Resources;
import android.content.res.TypedArray;

import com.samsungxr.SXRContext;
import com.samsungxr.keyboard.R;
import com.samsungxr.keyboard.keyboard.model.KeyboardBase;
import com.samsungxr.keyboard.keyboard.model.KeyboardItemStyle;
import com.samsungxr.keyboard.keyboard.model.KeyboardLine;
import com.samsungxr.keyboard.keyboard.model.KeyboardSoftItem;
import com.samsungxr.keyboard.model.KeyboardCharItem;
import com.samsungxr.keyboard.util.SceneObjectNames;

/**
 * @author Douglas
 */
public class KeyboardAlphabetic extends KeyboardBase {

    private Resources res = null;
    private static final String RESOURCE_TYPE = "array";
    private SXRContext gvrContext;
    private int notFoundResource = -1;

    public KeyboardAlphabetic(SXRContext gvrContext) {
        super(gvrContext);
        setName(SceneObjectNames.KEYBOARD_ALPHABETIC);

        this.gvrContext = gvrContext;

        res = gvrContext.getContext().getResources();
        TypedArray softKeyboard = res.obtainTypedArray(R.array.new_soft_lines);

        int n = softKeyboard.length();

        for (int lineIndex = 0; lineIndex < n; ++lineIndex) {

            int lineId = softKeyboard.getResourceId(lineIndex, notFoundResource);
            int keyId = getResourceId(res.getResourceEntryName(lineId), RESOURCE_TYPE, gvrContext
                    .getContext().getPackageName());

            parserLinesArray(keyId, lineIndex);
        }

        softKeyboard.recycle();
    }

    private void parserLinesArray(int keyId, int lineIndex2) {

        TypedArray keys = res.obtainTypedArray(keyId);
        int linesLenght = keys.length();

        KeyboardLine mKeyboardLine = new KeyboardLine(this.getSXRContext());

        for (int lineIndex = 0; lineIndex < linesLenght; ++lineIndex) {

            int idRow = keys.getResourceId(lineIndex, notFoundResource);
            TypedArray key = res.obtainTypedArray(idRow);

            String character = key.getString(0);
            String specialCharacter = key.getString(1);

            KeyboardItemStyle style = getStyleFromTypedArray(key);

            mKeyboardLine.addItemKeyboard(new KeyboardSoftItem(getSXRContext(),
                    new KeyboardCharItem(character, specialCharacter), style));

            key.recycle();
        }

        mKeyboardLine.alingCenter(lineIndex2);
        addLine(mKeyboardLine);

        keys.recycle();
    }

    private KeyboardItemStyle getStyleFromTypedArray(TypedArray key) {

        TypedArray styles = res.obtainTypedArray(key.getResourceId(2, notFoundResource));
        KeyboardItemStyle keyStyle = new KeyboardItemStyle(this.gvrContext.getContext(), styles);

        return keyStyle;
    }

    public int getResourceId(String resourceName, String resourceType, String packageName) {

        try {
            return res.getIdentifier(resourceName, resourceType, packageName);
        } catch (Exception e) {
            return notFoundResource;
        }
    }
}
