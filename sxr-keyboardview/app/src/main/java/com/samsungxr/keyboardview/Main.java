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

package com.samsungxr.keyboardview;

import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRMain;
import com.samsungxr.SXRPicker;
import com.samsungxr.SXRScene;
import com.samsungxr.SXRSceneObject;
import com.samsungxr.IViewEvents;
import com.samsungxr.io.SXRCursorController;
import com.samsungxr.io.SXRInputManager;
import com.samsungxr.scene_objects.SXRKeyboardSceneObject;
import com.samsungxr.scene_objects.SXRViewSceneObject;

public class Main extends SXRMain {
    private final MainActivity mActivity;
    private SXRScene mScene;

    private SXRKeyboardSceneObject mKeyboardSceneObject;

    private SXRViewSceneObject mFrameLayoutFormSceneObject;
    private EditText mFocusedEdit;
    final float DEPTH = -2f;


    public Main(MainActivity activity) {
        mActivity = activity;

        mFocusedEdit = null;
    }

    @Override
    public void onInit(final SXRContext sxrContext) throws Throwable {
        mScene = sxrContext.getMainScene();

        mFrameLayoutFormSceneObject = new SXRViewSceneObject(sxrContext, R.layout.main_form, new ViewEventsHandler());
        mFrameLayoutFormSceneObject.getTransform().setPosition(0.0f, -0.3f, DEPTH);
        mFrameLayoutFormSceneObject.setName("frame");
        mScene.addSceneObject(mFrameLayoutFormSceneObject);

        mKeyboardSceneObject = new SXRKeyboardSceneObject.Builder()
                .setKeyboardTexture(sxrContext.getAssetLoader().loadTexture(
                        new SXRAndroidResource(sxrContext, R.drawable.keyboard_background)))
                .setKeyBackground(mActivity.getDrawable(R.drawable.key_background))
                .build(sxrContext, R.xml.qwerty);
        mKeyboardSceneObject.setName("keyboard");
        // Add frames per second display
        SXRSceneObject fpsObject = new SXRFPSCounter(sxrContext);
        fpsObject.getTransform().setPosition(0.0f, -1.0f, -0.1f);
        fpsObject.getTransform().setScale(0.2f, 0.2f, 1.0f);
        mScene.getMainCameraRig().addChildObject(fpsObject);

        sxrContext.getInputManager().selectController(new SXRInputManager.ICursorControllerSelectListener()
        {
            public void onCursorControllerSelected(SXRCursorController newController, SXRCursorController oldController)
            {
                SXRPicker picker = newController.getPicker();
                mKeyboardSceneObject.setPicker(picker);
            }
        });
    }

    private class ViewEventsHandler implements IViewEvents {

        @Override
        public void onInitView(SXRViewSceneObject sxrViewSceneObject, View view) {
            view.findViewById(R.id.nameEdit).setOnClickListener(mTextEditClickHandler);
            view.findViewById(R.id.emailEdit).setOnClickListener(mTextEditClickHandler);
            view.findViewById(R.id.phoneEdit).setOnClickListener(mTextEditClickHandler);
            view.findViewById(R.id.addButton).setOnClickListener(mTextEditClickHandler);

            view.findViewById(R.id.nameEdit).setOnFocusChangeListener(mTextEditFocusHandler);
            view.findViewById(R.id.emailEdit).setOnFocusChangeListener(mTextEditFocusHandler);
            view.findViewById(R.id.phoneEdit).setOnFocusChangeListener(mTextEditFocusHandler);
            view.findViewById(R.id.nameEdit).setOnKeyListener(mKeyListener);
            view.findViewById(R.id.emailEdit).setOnKeyListener(mKeyListener);
            view.findViewById(R.id.phoneEdit).setOnKeyListener(mKeyListener);
        }

        @Override
        public void onStartRendering(SXRViewSceneObject sxrViewSceneObject, View view) {
        }
    }

    @Override
    public void onStep() {

    }

    /*
     * Ignore Gear controller keys so they don't show up
     * in the edit boxes
     */
    private View.OnKeyListener mKeyListener = new View.OnKeyListener() {

        @Override
        public boolean onKey(View v, int keyCode, KeyEvent keyEvent) {
            return false;
        }
    };

    private View.OnFocusChangeListener mTextEditFocusHandler = new View.OnFocusChangeListener() {

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (mKeyboardSceneObject != null
                    && mKeyboardSceneObject.getParent() != null) {
                hideKeyboard();
            }
        }
    };

    private void showKeyboard(final EditText editText) {
        getSXRContext().runOnGlThread(new Runnable() {
            @Override
            public void run() {
                onShowKeyboard(editText);
            }
        });
    }

    private void onShowKeyboard(EditText editText) {
        switch (editText.getInputType()) {
            case EditorInfo.TYPE_CLASS_PHONE:
                mKeyboardSceneObject.setKeyboard(R.xml.numkbd);
                break;
            default:
                mKeyboardSceneObject.setKeyboard(R.xml.qwerty);
                break;
        }

        mScene.addSceneObject(mKeyboardSceneObject);
        mKeyboardSceneObject.startInput(mFrameLayoutFormSceneObject);
    }

    private void hideKeyboard() {
        getSXRContext().runOnGlThread(new Runnable() {
            @Override
            public void run() {
                onHideKeyboard();
            }
        });
    }

    private void onHideKeyboard() {
        mKeyboardSceneObject.stopInput();
        SXRSceneObject parent = mKeyboardSceneObject.getParent();
        if (parent != null) {
            parent.removeChildObject(mKeyboardSceneObject);
        }
    }

    private View.OnClickListener mTextEditClickHandler = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            mKeyboardSceneObject.getTransform().setScale(1.0f, 1.0f, 1.0f);

            switch (v.getId()) {
                case R.id.nameEdit:
                    mKeyboardSceneObject.getTransform().setPosition(0.0f,  -0.45f, DEPTH + 0.20f);
                    mKeyboardSceneObject.getTransform().setRotationByAxis(-10, 1, 0, 0);
                    mKeyboardSceneObject.getTransform().setScale(1.4f, 1.4f, 1.0f);


                    break;
                case R.id.phoneEdit:
                    mKeyboardSceneObject.getTransform().setPosition(0.0f,  -0.5f, DEPTH + 0.4f);
                    mKeyboardSceneObject.getTransform().setRotationByAxis(-15, 1, 0, 0);
                    mKeyboardSceneObject.getTransform().setScale(0.4f, 0.4f, 1.0f);

                    break;
                case R.id.emailEdit:
                    mKeyboardSceneObject.getTransform().setPosition(0.0f,  -0.9f, DEPTH + 0.20f);
                    mKeyboardSceneObject.getTransform().setRotationByAxis(-20, 1, 0, 0);
                    mKeyboardSceneObject.getTransform().setScale(1.5f, 1.5f, 1.0f);


                    break;
                case R.id.addButton:
                    ((EditText)mFrameLayoutFormSceneObject.findViewById(R.id.nameEdit)).setText("");
                    ((EditText)mFrameLayoutFormSceneObject.findViewById(R.id.emailEdit)).setText("");
                    ((EditText)mFrameLayoutFormSceneObject.findViewById(R.id.phoneEdit)).setText("");
                    break;
            }

            if (v.hasFocus()) {
                mFocusedEdit = (EditText) v;
                if (mKeyboardSceneObject.getParent() == null) {
                    mFocusedEdit.setCursorVisible(true);
                    showKeyboard(mFocusedEdit);
                } else {
                    mFocusedEdit.setCursorVisible(false);
                    hideKeyboard();
                }
            } else if (mKeyboardSceneObject.getParent() != null) {
                hideKeyboard();
            }
        }
    };
}

