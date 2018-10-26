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

import android.util.Log;

import com.samsungxr.SXRContext;
import com.samsungxr.keyboard.model.CharItem;
import com.samsungxr.keyboard.util.CircularList;

public class SpinnerAdapter {

    private CircularList<CharItem> characterList;
    private CircularList<SpinnerItem> spinnerItems;
    float oldRotation = 0;
    private int centralPosition = 0;
    private int centralPositionCharacter = 0;

    public SpinnerAdapter(CircularList<SpinnerItem> spinnerItems,
            CircularList<CharItem> characterList) {
        this.characterList = characterList;
        this.spinnerItems = spinnerItems;
    }

    public void updateSpinnerCentral(float actualRotation, SXRContext context) {
        updateCentralValues(actualRotation, context);
        updateTextOnRig(actualRotation, context);
    }

    private void updateTextOnRig(float degree, SXRContext sxrContext) {

        if (degree < 0.0f) {
            int howFar = 1;
            int upSpinnerPosition = spinnerItems.getNextPosition(centralPosition + howFar + 1);
            int upCharacterPosition = characterList.getNextPosition(centralPositionCharacter
                    + howFar + 1);
            spinnerItems.get(upSpinnerPosition).setText(sxrContext,
                    characterList.get(upCharacterPosition));
            Log.d("SpinnerTest", "upSpinnerPosition" + upSpinnerPosition);
        } else {
            int howFar = 1;
            int downSpinnerPosition = spinnerItems
                    .getPreviousPosition(centralPosition - howFar - 1);
            int downCharacterPosition = characterList.getPreviousPosition(centralPositionCharacter
                    - howFar - 1);
            spinnerItems.get(downSpinnerPosition).setText(sxrContext,
                    characterList.get(downCharacterPosition));
            Log.d("SpinnerTest", "downSpinnerPosition" + downSpinnerPosition);
        }
    }

    protected void updateCentralValues(float degree, SXRContext context) {

        if (degree < 0.0f) {

            centralPositionCharacter = characterList.getNextPosition(centralPositionCharacter);
            centralPosition = spinnerItems.getNextPosition(centralPosition);

        } else {

            centralPositionCharacter = characterList.getPreviousPosition(centralPositionCharacter);
            centralPosition =
                    spinnerItems.getPreviousPosition(centralPosition);

        }

    }

    public CharItem getCurrentCharItem() {
        return characterList.get(centralPositionCharacter);

    }

    public int getCentralPosition() {
        return centralPosition;
    }

    public CircularList<CharItem> getCharacterList() {
        return characterList;
    }

    public void setCharacterList(CircularList<CharItem> characterList) {
        this.characterList = characterList;
    }

    public CircularList<SpinnerItem> getSpinnerItems() {
        return spinnerItems;
    }

    public void setSpinnerItems(CircularList<SpinnerItem> spinnerItems) {
        this.spinnerItems = spinnerItems;
    }

    public void clean() {
        centralPosition = 0;
        centralPositionCharacter = 0;
        oldRotation = 0;

    }

    public void setInitialPosition(int initialPosition) {
        centralPositionCharacter = initialPosition;

    }
}
