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
import android.view.MotionEvent;

import com.samsungxr.SXRContext;
import com.samsungxr.SXRPicker;
import com.samsungxr.SXRSceneObject;
import com.samsungxr.animation.SXROpacityAnimation;
import com.samsungxr.keyboard.R;
import com.samsungxr.keyboard.keyboard.model.KeyboardBase;
import com.samsungxr.keyboard.keyboard.model.KeyboardEventListener;
import com.samsungxr.keyboard.keyboard.model.KeyboardItemBase;
import com.samsungxr.keyboard.keyboard.model.KeyboardLine;
import com.samsungxr.keyboard.model.AudioClip;
import com.samsungxr.keyboard.model.Dashboard;
import com.samsungxr.keyboard.model.KeyboardCharItem;
import com.samsungxr.keyboard.util.SceneObjectNames;

/**
 * @author Douglas and SIDIA VR TEAM
 */
public class Keyboard extends SXRSceneObject {

    public static final int SOFT_KEYBOARD_UPPERCASE = 0;
    public static final int SOFT_KEYBOARD_LOWERCASE = 1;
    public static final int NUMERIC_KEYBOARD = 2;
    public static final int SOFT_KEYBOARD_SPECIAL = 3;
    public static int mode = NUMERIC_KEYBOARD;

    public final int SHIFT_LOWERCASE = 0;
    public final int SHIFT_FIRST_LETTER_UPPERCASE = 1;
    public final int SHIFT_UPPERCASE = 2;
    public int shift = SHIFT_LOWERCASE;

    public enum KeyboardType {
        NUMERIC, ALPHA
    }

    private static final float ANIMATION_TOTAL_TIME = 2.6f;

    private SXRSceneObject currentSelection;
    private boolean isEnabled = false;
    private KeyboardBase keyboard;
    private KeyboardAlphabetic keyboardAlphabetic;
    private NumericKeyboard numericKeyboard;
    private KeyboardType currentType;
    private Resources androidResources;

    public KeyboardType getCurrentType() {
        return currentType;
    }

    private KeyboardEventListener keyboardEventListener;

    public Keyboard(SXRContext sxrContext) {
        super(sxrContext);
        setName(SceneObjectNames.KEYBOARD);
        keyboardAlphabetic = new KeyboardAlphabetic(getSXRContext());
        numericKeyboard = new NumericKeyboard(getSXRContext());
        androidResources = this.getSXRContext().getContext().getApplicationContext().getResources();
    }

    private void createSoftMode() {
        mode = SOFT_KEYBOARD_LOWERCASE;
        keyboard = keyboardAlphabetic;
        changeToLowercase();
        configureKeyboardParentation(keyboard);
        currentType = KeyboardType.ALPHA;
        getTransform().setScale(1.5f, 1.5f, 1.5f);
    }

    private void createNumericMode() {
        mode = NUMERIC_KEYBOARD;
        keyboard = numericKeyboard;
        configureKeyboardParentation(keyboard);
        currentType = KeyboardType.NUMERIC;
        getTransform().setScale(1.5f, 1.5f, 1.5f);
    }

    public void setOnKeyboardEventListener(KeyboardEventListener keyboardEventListener) {
        this.keyboardEventListener = keyboardEventListener;
    }

    private void configureKeyboardParentation(KeyboardBase keyboard) {

        if (keyboard.getListKeyboardLine() != null) {

            for (KeyboardLine item : keyboard.getListKeyboardLine()) {
                addChildObject(item);
            }
        }
    }

    private void configureKeyboardRemoveParentation() {

        if (keyboard.getListKeyboardLine() != null) {

            for (KeyboardLine item : keyboard.getListKeyboardLine()) {
                removeChildObject(item);
            }
        }
    }

    public void tapKeyboard() {

        AudioClip.getInstance(getSXRContext().getContext()).playSound(
                AudioClip.getKeyEnterSoundID(), 1.0f, 1.0f);

        if (currentSelection != null) {

            if (keyboardEventListener != null) {

                KeyboardCharItem currentItem = ((KeyboardItemBase) currentSelection)
                        .getKeyboardCharItem();

                if (currentItem.getCharacter().equalsIgnoreCase(
                        androidResources.getString(R.string.btn_rmv))) {

                    keyboardEventListener.onKeyDelete();

                } else if (currentItem.getCharacter().equalsIgnoreCase(
                        androidResources.getString(R.string.btn_ok))) {

                    getTransform().setRotation(1, 0, 0, 0);
                    getTransform().setPosition(0, 0, 0);
                    hideKeyboard();

                    keyboardEventListener.onKeyConfirm();

                } else if (currentItem.getCharacter().equalsIgnoreCase(
                        androidResources.getString(R.string.btn_shift))) {
                    shiftKeys();
                }

                else if (currentItem.getCharacter().equalsIgnoreCase(
                        androidResources.getString(R.string.btn_123))) {
                    changeToSpecialCharacter();

                } else if (currentItem.getCharacter().equalsIgnoreCase(
                        androidResources.getString(R.string.btn_abc))) {
                    changeToLowercase();

                } else {

                    keyboardEventListener.onKeyPressedWhitItem(currentItem);

                    if (getShift() == SHIFT_FIRST_LETTER_UPPERCASE) {
                        shiftKeys();
                        shiftKeys();
                    }
                }
            }
        }
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void showKeyboard(KeyboardType keyboardType) {

        if (KeyboardType.ALPHA == keyboardType) {

            createSoftMode();

        } else {
            createNumericMode();
        }

        isEnabled = true;

        getSXRContext().getMainScene().addSceneObject(this);

        for (KeyboardLine item : keyboard.getListKeyboardLine()) {

            for (int i = 0; i < item.getChildrenCount(); i++) {

                if (item.getChildByIndex(i).getRenderData() == null) {
                    return;
                }

                SXROpacityAnimation anim3 = new SXROpacityAnimation(item.getChildByIndex(i),
                        ANIMATION_TOTAL_TIME, 1);

                anim3.start(getSXRContext().getAnimationEngine());
            }
        }
    }

    public void hideKeyboard() {

        isEnabled = false;

        getSXRContext().getMainScene().removeSceneObject(this);
        configureKeyboardRemoveParentation();
    }

    public void setHoverMaterial(SXRSceneObject obj) {

        KeyboardItemBase t = (KeyboardItemBase) obj;
        t.setHoverMaterial();
    }

    public void setNormalMaterial(SXRSceneObject obj) {

        KeyboardItemBase t = (KeyboardItemBase) obj;
        t.setNormalMaterial();
    }

    public void onSingleTap(MotionEvent e) {
        tapKeyboard();
    }

    public void update(SXRSceneObject sceneObject) {
        changeTexture(sceneObject);
    }

    boolean test = true;

    private void changeTexture(SXRSceneObject sceneObject) {

        if (currentSelection != null) {
            setNormalMaterial(currentSelection);
        }

        currentSelection = null;

        if (sceneObject.hashCode() == Dashboard.currentDashboardHashCode) {
            return;
        }

        if (sceneObject instanceof KeyboardItemBase) {

            setHoverMaterial(sceneObject);

            if (sceneObject.equals(currentSelection)) {
                setHoverMaterial(sceneObject);
            } else {

                if (currentSelection != null) {
                    setNormalMaterial(currentSelection);
                }

                currentSelection = sceneObject;
            }

        } else {

            if (currentSelection != null) {
                setNormalMaterial(currentSelection);
            }

            currentSelection = null;
        }
    }

    public void shiftKeys() {
        switch (shift) {
            case SHIFT_LOWERCASE:
                changeToUppercase();
                shift = SHIFT_FIRST_LETTER_UPPERCASE;
                break;
            case SHIFT_FIRST_LETTER_UPPERCASE:
                shift = SHIFT_UPPERCASE;
                break;
            case SHIFT_UPPERCASE:
                changeToLowercase();
                shift = SHIFT_LOWERCASE;
                break;
            default:
                changeToLowercase();
                shift = SHIFT_LOWERCASE;
                break;
        }
    }

    public int getShift() {
        return shift;
    }

    private void changeToLowercase() {
        mode = SOFT_KEYBOARD_LOWERCASE;
        for (SXRSceneObject object : keyboard.getObjects()) {
            ((KeyboardItemBase) object).switchMaterialState(SOFT_KEYBOARD_LOWERCASE);
        }
    }

    private void changeToUppercase() {
        mode = SOFT_KEYBOARD_UPPERCASE;
        for (SXRSceneObject object : keyboard.getObjects()) {
            ((KeyboardItemBase) object).switchMaterialState(SOFT_KEYBOARD_UPPERCASE);
        }
    }

    public void changeToSpecialCharacter() {

        mode = SOFT_KEYBOARD_SPECIAL;

        for (SXRSceneObject object : keyboard.getObjects()) {
            ((KeyboardItemBase) object).switchMaterialState(SOFT_KEYBOARD_SPECIAL);
        }
    }

    public void changeToNormalCharacter() {

        switch (mode) {

            case SHIFT_LOWERCASE:
                changeToUppercase();
                shift = SHIFT_FIRST_LETTER_UPPERCASE;
                break;
            case SHIFT_FIRST_LETTER_UPPERCASE:
                shift = SHIFT_UPPERCASE;
                break;
            case SHIFT_UPPERCASE:
                changeToLowercase();
                shift = SHIFT_LOWERCASE;
                break;
            default:
                changeToLowercase();
                shift = SHIFT_LOWERCASE;
                break;
        }
    }
}
