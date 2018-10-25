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

package com.samsungxr.keyboard.main;

import android.text.TextUtils;
import android.view.MotionEvent;

import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRCameraRig;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRImportSettings;
import com.samsungxr.SXRMaterial;
import com.samsungxr.SXRMesh;
import com.samsungxr.SXRPicker;
import com.samsungxr.SXRRenderData;
import com.samsungxr.SXRSceneObject;
import com.samsungxr.SXRMain;
import com.samsungxr.SXRShaderId;
import com.samsungxr.SXRSphereCollider;
import com.samsungxr.SXRTexture;
import com.samsungxr.SXRTransform;
import com.samsungxr.IPickEvents;
import com.samsungxr.keyboard.R;
import com.samsungxr.keyboard.keyboard.model.KeyboardEventListener;
import com.samsungxr.keyboard.keyboard.numeric.Keyboard;
import com.samsungxr.keyboard.keyboard.numeric.Keyboard.KeyboardType;
import com.samsungxr.keyboard.mic.Mic;
import com.samsungxr.keyboard.mic.RecognitionMictListener;
import com.samsungxr.keyboard.mic.model.ExceptionFeedback;
import com.samsungxr.keyboard.model.AudioClip;
import com.samsungxr.keyboard.model.CharItem;
import com.samsungxr.keyboard.model.CharList;
import com.samsungxr.keyboard.model.Dashboard;
import com.samsungxr.keyboard.model.KeyboardCharItem;
import com.samsungxr.keyboard.model.SphereFlag;
import com.samsungxr.keyboard.model.SphereStaticList;
import com.samsungxr.keyboard.shader.SXRShaderAnimation;
import com.samsungxr.keyboard.shader.SphereShader;
import com.samsungxr.keyboard.shader.TransparentButtonShaderThreeStates;
import com.samsungxr.keyboard.speech.SoundWave;
import com.samsungxr.keyboard.spinner.Spinner;
import com.samsungxr.keyboard.spinner.SpinnerItemFactory;
import com.samsungxr.keyboard.textField.TextField;
import com.samsungxr.keyboard.util.Constants;
import com.samsungxr.keyboard.util.InteractiveText;
import com.samsungxr.keyboard.util.RenderingOrder;
import com.samsungxr.keyboard.util.StringUtil;
import com.samsungxr.keyboard.util.Util;
import com.samsungxr.keyboard.util.VRSamplesTouchPadGesturesDetector.SwipeDirection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Vector;

public class Main extends SXRMain implements KeyboardEventListener {

    private SXRContext mSXRContext;
    private boolean isFirstTime = true;
    private SphereStaticList flagListCostructor;
    private SphereFlag lastSelectedSphereFlag;
    private TextField answer;
    private SXRSceneObject question;
    private Keyboard keyboard;
    private Mic mMic;
    private MainActivity mMainActivity;
    private ExceptionFeedback exceptionFeedback;
    private SoundWave soundWave1;
    private static final int QUESTION_HEIGHT = 90;
    private static final int QUESTION_WIDTH = 4000;
    private final int QUESTION_LINE_LENGTH = 45;
    private RecognitionMictListener mRecognitionMictListener;
    private boolean mDisableSnapSound = false;
    private Dashboard dashboard;
    private Spinner spinner;
    private PickHandler mPickHandler = new PickHandler();
    private SXRPicker mPicker;

    public void setActivity(MainActivity mainActivity) {
        mMainActivity = mainActivity;
    }

    public class PickHandler implements IPickEvents
    {
        public SXRPicker.SXRPickedObject Picked;

        public void onEnter(SXRSceneObject sceneObject, SXRPicker.SXRPickedObject pickInfo)
        {
            if (keyboard.isEnabled())
            {
                keyboard.update(sceneObject);
                if (spinner != null)
                {
                    answer.spinnerUpdate(sceneObject);
                }
                mMic.onUpdate(sceneObject);
            }
        }

        public void onExit(SXRSceneObject sceneObj) { }

        public void onNoPick(SXRPicker picker)
        {
            Picked = null;
        }

        public void onPick(SXRPicker picker)
        {
            SXRPicker.SXRPickedObject[] picked = picker.getPicked();

            Picked = picked[0];
        }

        public void onInside(SXRSceneObject sceneObj, SXRPicker.SXRPickedObject pickInfo) { }
    }

    @Override
    public void onInit(SXRContext gvrContext) {

        mSXRContext = gvrContext;

        SpinnerItemFactory.getInstance(gvrContext).init();

        exceptionFeedback = new ExceptionFeedback(gvrContext);
        gvrContext.getMainScene().getMainCameraRig()
                .addChildObject(exceptionFeedback);

        keyboard = new Keyboard(gvrContext);
        keyboard.setOnKeyboardEventListener(this);

        AudioClip.getInstance(mSXRContext.getActivity());

        SXRSceneObject floor = new SXRSceneObject(mSXRContext,
                mSXRContext.createQuad(120.0f, 120.0f),
                mSXRContext.getAssetLoader().loadTexture(new SXRAndroidResource(mSXRContext, R.drawable.floor)));

        floor.getTransform().setRotationByAxis(-90, 1, 0, 0);
        floor.getTransform().setPositionY(-10.0f);
        gvrContext.getMainScene().addSceneObject(floor);
        floor.getRenderData().setRenderingOrder(0);

        createSkybox();

        addCursorPosition();

        createSpinnerInvisible();

        createAnswer();

        createQuestion();

        createDashboard();

        createMic();

        createSoundWaves();

        configureKeyboardParent();

        flagListCostructor = new SphereStaticList(gvrContext);

        SXRCameraRig cameraObject = gvrContext.getMainScene()
                .getMainCameraRig();
        for (SXRSceneObject spherePack : flagListCostructor.listFlag) {
            spherePack.getRenderData().setMaterial(new SXRMaterial(gvrContext, new SXRShaderId(SphereShader.class)));
            rotateObject(spherePack, cameraObject.getTransform());

            double distance = Util.distance(spherePack, gvrContext
                    .getMainScene().getMainCameraRig().getTransform());
            float scaleFactor = Util.getHitAreaScaleFactor((float) distance);
            spherePack.getTransform().setScale(scaleFactor, scaleFactor,
                    scaleFactor);
            spherePack
                    .getChildByIndex(0)
                    .getTransform()
                    .setScale(1 / scaleFactor, 1 / scaleFactor, 1 / scaleFactor);

            gvrContext.getMainScene().addSceneObject(spherePack);
        }

        gvrContext.getMainScene().getEventReceiver().addListener(mPickHandler);
        mPicker = new SXRPicker(gvrContext, gvrContext.getMainScene());
        createAndAttachAllEyePointee();
    }

    public void createSpinnerInvisible() {
        spinner = new Spinner(mSXRContext, 0, Keyboard.NUMERIC_KEYBOARD);
        spinner.getTransform().setPositionZ(0.2f);
        spinner.getTransform().setPositionY(0.05f);
        mSXRContext.getMainScene().addSceneObject(spinner);
        spinner.off();
    }

    private void createMic() {

        mMic = new Mic(mSXRContext, mMainActivity);
        mMic.setListnerResult(getRecognitionMictListener());
        mMic.updatePosition(answer);
        answer.addChildObject(mMic);
    }

    private RecognitionMictListener getRecognitionMictListener() {
        if (mRecognitionMictListener != null) {
            return mRecognitionMictListener;
        }

        mRecognitionMictListener = (new RecognitionMictListener() {

            @Override
            public void onError(String text, int error) {
                exceptionFeedback.show();
            }

            @Override
            public void onReadyForSpeech() {
                soundWave1.enableAnimation();
            }

            @Override
            public void onResults(ArrayList<String> resultList) {

                String result = resultList.get(0);
                if (Keyboard.NUMERIC_KEYBOARD == Keyboard.mode) {
                    result = getOnlyNumbers(result);
                }

                if (answer != null) {

                    if (!TextUtils.isEmpty(result)) {
                        answer.removeAllTextFieldItem();

                        for (int i = 0; i < result.length(); i++) {

                            Character charater = result.charAt(i);
                            int mode = CharList.getInstance(mSXRContext).getMode(charater);
                            int position = CharList.getInstance(mSXRContext).indexOf(
                                    String.valueOf(charater), mode);
                            answer.append(i, new CharItem(mode, position, String.valueOf(charater)));
                        }

                    } else {
                        exceptionFeedback.show();
                    }
                }
            }
        });

        return mRecognitionMictListener;
    }

    private void configureKeyboardParent() {
        keyboard.addChildObject(answer);
        keyboard.addChildObject(question);
        answer.addChildObject(soundWave1);
    }

    private void createSoundWaves() {
        soundWave1 = new SoundWave(mSXRContext, 13, 0, 10);
        soundWave1.getTransform().setScale(0.5f, 0.5f, 0.5f);
        soundWave1.getTransform().setPositionY(1.0f);
        soundWave1.getTransform().setPositionX(0.32f);
        soundWave1.getTransform().setPositionZ(0.01f);

        mMic.setRecognitionRmsChangeListener(soundWave1);
    }

    private void createSkybox() {

        mSXRContext.getMainScene().getMainCameraRig()
                .getTransform().setPosition(-0f, Util.applyRatioAt(1.70), 0f);

        SXRSceneObject mSpaceSceneObject = null;
        EnumSet<SXRImportSettings> settings = SXRImportSettings.getRecommendedSettings();
        try {
            mSpaceSceneObject = mSXRContext.getAssetLoader().loadModel(new SXRAndroidResource(mSXRContext, R.raw.skybox_esphere), settings, false, mSXRContext.getMainScene());
        } catch (IOException e) {
            e.printStackTrace();
        }
        mSXRContext.getMainScene().addSceneObject(mSpaceSceneObject);
        List<SXRRenderData> rdatas = mSpaceSceneObject.getAllComponents(SXRRenderData.getComponentType());
        SXRRenderData rdata = rdatas.get(0);
        rdata.setRenderingOrder(0);
    }

    private void addCursorPosition() {

        SXRSceneObject headTracker = new SXRSceneObject(mSXRContext,
                mSXRContext.createQuad(0.5f, 0.5f), mSXRContext.getAssetLoader().loadTexture(new SXRAndroidResource(
                mSXRContext, R.drawable.head_tracker)));

        headTracker.getTransform().setPositionZ(-9.0f);
        headTracker.getRenderData().setRenderingOrder(
                SXRRenderData.SXRRenderingOrder.OVERLAY);
        headTracker.getRenderData().setDepthTest(false);
        headTracker.getRenderData().setRenderingOrder(100000);
        mSXRContext.getMainScene().getMainCameraRig().getRightCamera()
                .addChildObject(headTracker);
    }

    private void rotateObject(SXRSceneObject spherePack,
                              SXRTransform cameraObject) {
        spherePack.getTransform().rotateByAxis(
                Util.getZRotationAngle(spherePack, cameraObject), 0, 0, 1);
        spherePack.getTransform().rotateByAxis(
                Util.getYRotationAngle(spherePack, cameraObject), 0, 1, 0);
        spherePack.getChildByIndex(0).getTransform()
                .rotateByAxis(-Util.getZRotationAngle(spherePack, cameraObject), 0, 0, 1);
    }

    @Override
    public void onStep() {
        flagListCostructor.updateSpheresMaterial();
        if (!keyboard.isEnabled()) {
            interactWithVisibleObjects(mPickHandler.Picked);
        }
    }

    private void interactWithVisibleObjects(SXRPicker.SXRPickedObject picked) {
        if ((lastSelectedSphereFlag != null) &&
                (lastSelectedSphereFlag.answerState == SphereStaticList.ANSWERING))
        {
            lastSelectedSphereFlag.moveToCursor();
        }
        if (picked != null)
        {
            SXRSceneObject sceneObject = picked.getHitObject();
            if ((lastSelectedSphereFlag == null ||
                    lastSelectedSphereFlag.answerState != SphereStaticList.ANSWERING))
            {
                // TODO: Fix (SphereFlag) object.getChildByIndex(0) this
                // object can be any object .... not only a Sphere.
                if (sceneObject.getChildrenCount() > 0)
                {
                    lastSelectedSphereFlag = (SphereFlag) sceneObject.getChildByIndex(0);
                    if (lastSelectedSphereFlag.answerState == SphereStaticList.MOVEABLE)
                    {
                        moveObject(sceneObject, picked.getHitLocation());
                        if (this.mDisableSnapSound == false)
                        {
                            this.mDisableSnapSound = true;
                            AudioClip.getInstance(mSXRContext.getContext()).playSound(
                                    AudioClip.getSnapSoundID(), 1.0f, 1.0f);
                        }
                    }
                }
            }
            else
            {
                if (lastSelectedSphereFlag != null)
                {
                    if (lastSelectedSphereFlag.answerState == SphereStaticList.MOVEABLE)
                    {
                        restoreObjectToItsDefaultPosition(lastSelectedSphereFlag);
                        mDisableSnapSound = false;
                        lastSelectedSphereFlag = null;
                    }
                }
            }
        }
    }

    private void restoreObjectToItsDefaultPosition(SXRSceneObject object) {
        ((SphereFlag) object).stopFloatingSphere();
        ((SphereFlag) object).unspotSphere();
        ((SphereFlag) object).unsnapSphere(1.2f);
    }

    private void moveObject(SXRSceneObject object, float [] hitLocation) {
        ((SphereFlag) object.getChildByIndex(0))
                .stopFloatingSphere();
        ((SphereFlag) (object.getChildByIndex(0))).snapSphere(hitLocation);
        ((SphereFlag) (object.getChildByIndex(0))).spotSphere();
    }

    public void onBackPressed() {

    }

    public void onSingleTap(MotionEvent e) {

        if (mMic != null) {

            mMic.onSingleTap();
        }

        if (keyboard.isEnabled()) {
            keyboard.tapKeyboard();

        } else if (lastSelectedSphereFlag != null
                && lastSelectedSphereFlag.answerState == SphereStaticList.MOVEABLE) {

            AudioClip.getInstance(mSXRContext.getContext()).playSound(
                    AudioClip.getSelectionSoundID(), 1.0f, 1.0f);

            lastSelectedSphereFlag.stopFloatingSphere();
            lastSelectedSphereFlag.answerState = SphereStaticList.ANSWERING;

            animateSpheresBlurState(1, lastSelectedSphereFlag);
            lastSelectedSphereFlag.tapSphere();

            splitQuestion();

            answer.setNumberOfCharecters(lastSelectedSphereFlag.getAnswer().length());
            mMic.updatePosition(answer);
            float positionX = -(mMic.getTransform().getPositionX()) / 2;
            answer.getTransform().setPosition(positionX, 0.87f, Constants.CAMERA_DISTANCE);

            float[] keyboardPosition = Util.calculatePointBetweenTwoObjects(mSXRContext
                            .getMainScene().getMainCameraRig().getTransform(),
                    lastSelectedSphereFlag.getInitialPositionVector(),
                    Constants.SPHERE_SELECTION_DISTANCE);

            showKeyboard();

            soundWave1.update(answer.getSize(), answer.getInitialPosition());

            keyboard.getTransform().setPosition(
                    keyboardPosition[0] - 0.03f,
                    keyboardPosition[1] - 3.0f,
                    keyboardPosition[2]);
            keyboard.getTransform().rotateByAxis(
                    Util.getYRotationAngle(keyboard, mSXRContext.getMainScene()
                            .getMainCameraRig().getTransform()), 0, 1, 0);
            if (dashboard != null) {
                dashboard.show();
                dashboard.reset();
                dashboard.getTransform().setPosition(
                        keyboardPosition[0] + Dashboard.getXPositionOffset(keyboardPosition[0]),
                        keyboardPosition[1] + Dashboard.Y_POSITION_OFFSET,
                        keyboardPosition[2] + Dashboard.getZPositionOffset(keyboardPosition[2]));

                dashboard.getTransform().rotateByAxis(
                        Util.getYRotationAngle(dashboard,
                                mSXRContext.getMainScene().getMainCameraRig()
                                        .getTransform()), 0, 1, 0);
            }

        }
    }

    private void splitQuestion() {
        removeQuestionChildren();

        String questionString = lastSelectedSphereFlag.getQuestion();
        Vector<StringBuffer> lines = StringUtil.splitStringInLines(questionString,
                QUESTION_LINE_LENGTH);

        addQuestionLines(lines);
    }

    private void removeQuestionChildren() {
        List<SXRSceneObject> children = question.getChildren();
        final int size = children.size();
        for (int i = 0; i < size; i++) {
            question.removeChildObject(children.get(0));
        }
    }

    private void addQuestionLines(Vector<StringBuffer> lines) {
        for (int i = 0; i < lines.size(); i++) {
            InteractiveText line = new InteractiveText(mSXRContext, QUESTION_WIDTH, QUESTION_HEIGHT);
            line.currentText.maxLength = 9999;
            line.currentText.textSize = 80;
            line.setText(mSXRContext, lines.get(i).toString());
            line.getTransform().setPosition(0, 3.33f + 0.4f * (lines.size() - 1 - i),
                    Constants.CAMERA_DISTANCE);
            line.getRenderData().getMaterial()
                    .setFloat(TransparentButtonShaderThreeStates.OPACITY, 0);
            line.getRenderData().setRenderingOrder(RenderingOrder.KEYBOARD);
            line.getRenderData().getMaterial()
                    .setFloat(TransparentButtonShaderThreeStates.OPACITY, 1);

            question.addChildObject(line);
        }
    }

    private void showKeyboard() {

        this.mSXRContext.runOnGlThread(new Runnable() {

            @Override
            public void run() {

                if (getOnlyNumbers(lastSelectedSphereFlag.getAnswer()).equals("")) {

                    keyboard.showKeyboard(KeyboardType.ALPHA);

                } else {

                    keyboard.showKeyboard(KeyboardType.NUMERIC);
                }
            }
        });
    }

    public void animateSpheresBlurState(int on) {

        for (SXRSceneObject sphereFlag : flagListCostructor.listFlag) {
            if (lastSelectedSphereFlag != null
                    && !lastSelectedSphereFlag.equals(sphereFlag.getChildByIndex(0))) {
                new SXRShaderAnimation(sphereFlag.getChildByIndex(0),
                        SphereShader.BLUR_INTENSITY, 1, on).start(mSXRContext
                        .getAnimationEngine());
                ((SphereFlag) sphereFlag.getChildByIndex(0))
                        .restoreSpherePosition(2f);
            }
        }
    }

    public void animateSpheresBlurState(int on, SXRSceneObject lastIntem2) {

        for (SXRSceneObject sphereFlag : flagListCostructor.listFlag) {

            if (lastIntem2 != null
                    && !lastIntem2.equals(sphereFlag.getChildByIndex(0))) {
                ((SphereFlag) sphereFlag.getChildByIndex(0)).unselectSphere();
            }
        }
    }

    private void createAndAttachAllEyePointee() {
        for (SXRSceneObject object : mSXRContext.getMainScene()
                .getWholeSceneObjects()) {
            if (object instanceof SphereFlag) {

                ((SphereFlag) object).animateFloating();

                attachDefaultEyePointee(object.getParent());
            }
        }
    }

    private void attachDefaultEyePointee(SXRSceneObject sceneObject) {
        sceneObject.attachComponent(new SXRSphereCollider(getSXRContext()));
    }

    public void spinnerListenerAnimation(SwipeDirection swipeDirection, float velocityY) {

        if (spinner != null && spinner.isActive()) {
            spinner.getSpinnerRoulette().animate(swipeDirection, velocityY);
        }
    }

    public void createAnswer() {
        answer = new TextField(mSXRContext, this);
        answer.setSpinner(spinner);
        final SXRSceneObject parent = spinner.getParent();
        if (null != parent) {
            parent.removeChildObject(spinner);
        }
        answer.addChildObject(spinner);
    }

    public void createQuestion() {
        question = new SXRSceneObject(mSXRContext);
    }

    @Override
    public void onKeyDelete() {
        answer.removeCharacter(TextField.LAST_CHARACTER);
        AudioClip.getInstance(mSXRContext.getContext()).playSound(AudioClip.getKeyEnterSoundID(),
                1.0f, 1.0f);
    }

    @Override
    public void onKeyConfirm() {

        dashboard.hide();

        lastSelectedSphereFlag.giveAnswer(answer.getCurrentText());
        answer.cleanText();
        mSXRContext.getPeriodicEngine().runAfter(new Runnable() {
            @Override
            public void run() {
                animateSpheresBlurState(0);
            }
        }, 3f);
    }

    @Override
    public void onKeyPressedWhitItem(KeyboardCharItem keyboarCharItem) {

        int position = CharList.getInstance(mSXRContext).indexOf(keyboarCharItem.getCharacter());
        CharItem charItem = new CharItem(Keyboard.mode, position, keyboarCharItem.getCharacter());

        answer.append(charItem);
    }

    private String getOnlyNumbers(String string) {

        String numberOnly = string.replaceAll("[^0-9]", "");

        return numberOnly;
    }

    private void createDashboard() {
        dashboard = new Dashboard(mSXRContext, R.raw.empty);
        dashboard.getTransform().setPosition(0, 0, 0);
    }

    public void heightSync() {

        if (lastSelectedSphereFlag != null && (lastSelectedSphereFlag.canMoveTogetherDashboard())
                && (dashboard.isHeightSyncLocked() == false)) {

            if (dashboard.isAboveAnchorPoint()) {
                moveDashDown();

            } else {

                moveDashUp();
            }
        }
    }

    private void moveDashDown() {

        float moveFactor = dashboard.getDeltaY() * Dashboard.HEIGHT_SYNC_SPEED_FACTOR;

        if (moveFactor < 0) {
            moveDash();
        }
    }

    private void moveDashUp() {

        float moveFactor = dashboard.getDeltaY() * Dashboard.HEIGHT_SYNC_SPEED_FACTOR;

        if (moveFactor > 0) {
            moveDash();
        }
    }

    private void moveDash() {

        float moveFactor = dashboard.getDeltaY() * Dashboard.HEIGHT_SYNC_SPEED_FACTOR;

        keyboard.getTransform().setPositionY(
                keyboard.getTransform().getPositionY() + moveFactor);
        dashboard.getTransform().setPositionY(
                dashboard.getTransform().getPositionY() + moveFactor);

        lastSelectedSphereFlag.getParent()
                .getTransform()
                .setPositionY(
                        lastSelectedSphereFlag.getParent().getTransform().getPositionY()
                                + moveFactor);
    }

}
