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

package com.samsungxr.arpet.mode;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

import com.samsungxr.ITouchEvents;
import com.samsungxr.SXRCameraRig;
import com.samsungxr.SXREventListeners;
import com.samsungxr.SXRNode;
import com.samsungxr.SXRPicker;
import com.samsungxr.io.SXRCursorController;
import com.samsungxr.mixedreality.IMixedReality;

import com.samsungxr.arpet.PetContext;
import com.samsungxr.arpet.character.CharacterController;
import com.samsungxr.arpet.character.CharacterView;
import com.samsungxr.arpet.gesture.GestureDetector;
import com.samsungxr.arpet.gesture.OnGestureListener;
import com.samsungxr.arpet.gesture.impl.GestureDetectorFactory;
import com.samsungxr.arpet.gesture.impl.ScaleGestureDetector;
import org.joml.Matrix4f;
import org.joml.Vector4f;

public class EditMode extends BasePetMode {
    private OnBackToHudModeListener mBackToHudModeListener;
    private final CharacterView mCharacterView;
    private GestureDetector mRotationDetector;
    private GestureDetector mScaleDetector;
    private SXRCursorController mCursorController = null;
    private final GestureHandler mGestureHandler;
    private Vibrator mVibrator;

    public EditMode(PetContext petContext, OnBackToHudModeListener listener, CharacterController controller) {
        super(petContext, new EditView(petContext));
        mBackToHudModeListener = listener;
        ((EditView) mModeScene).setListenerEditMode(new OnEditModeClickedListenerHandler());
        mCharacterView = controller.getView();

        mVibrator = (Vibrator) petContext.getActivity().getSystemService(Context.VIBRATOR_SERVICE);

        mGestureHandler = new GestureHandler();
        // FIXME: remove listener from constructor
        mRotationDetector = GestureDetectorFactory.INSTANCE.getSwipeRotationGestureDetector(
                mPetContext.getSXRContext(), mGestureHandler);
        mScaleDetector = GestureDetectorFactory.INSTANCE.getScaleGestureDetector(
                mPetContext.getSXRContext(), mGestureHandler);
    }

    @Override
    protected void onEnter() {
        ((ScaleGestureDetector)mScaleDetector).setScale(mCharacterView.getTransform().getScaleX());
    }

    @Override
    protected void onExit() {
        onDisableGesture();
    }

    private void onDisableGesture() {
        if (mCursorController != null) {
            mCursorController.removePickEventListener(mGestureHandler);
            mPetContext.getSXRContext().getApplication().getEventReceiver().removeListener(mGestureHandler);

            mScaleDetector.setEnabled(false);
            mRotationDetector.setEnabled(false);

            mCursorController = null;
        }
    }

    private void vibrate() {
        final int vibrateTime = 100;  // in ms

        // Check API version once the vibrate method was deprecated in API level 26
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mVibrator.vibrate(VibrationEffect.createOneShot(vibrateTime, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            mVibrator.vibrate(vibrateTime);
        }
    }

    private class OnEditModeClickedListenerHandler implements OnEditModeClickedListener {

        @Override
        public void OnBack() {
            mPetContext.getSXRContext().runOnGlThread(new Runnable() {
                @Override
                public void run() {
                    if (mCharacterView.isDragging()) {
                        mCharacterView.stopDragging();
                    }
                    mBackToHudModeListener.OnBackToHud();
                }
            });
            Log.d(TAG, "On Back");
        }

        @Override
        public void OnSave() {

        }
    }

    private class GestureHandler extends SXREventListeners.ApplicationEvents
            implements OnGestureListener, ITouchEvents, Runnable {

        private float[] mDraggingOffset = null;

        @Override
        public void dispatchTouchEvent(MotionEvent event) {
            mRotationDetector.onTouchEvent(event);
            mScaleDetector.onTouchEvent(event);
        }

        @Override
        public void onGesture(GestureDetector detector) {
            if (!mCharacterView.isDragging()) {
                Log.d(TAG, "onGesture detected");
                mDraggingOffset = null;
                if (detector == mRotationDetector) {
                    mCharacterView.rotate(detector.getValue());
                } else if (detector == mScaleDetector) {
                    mCharacterView.scale(detector.getValue());
                }
            }
        }

        @Override
        public void onEnter(SXRNode sxrNode, SXRPicker.SXRPickedObject sxrPickedObject) {

        }

        @Override
        public void onExit(SXRNode sxrNode, SXRPicker.SXRPickedObject sxrPickedObject) {

        }

        @Override
        public void onTouchStart(SXRNode sceneObject, SXRPicker.SXRPickedObject pickedObject) {
            Log.d(TAG, "onTouchStart " + sceneObject.getName());
            if (mCharacterView.isDragging()) {
                return;
            }

            if (CharacterView.PET_COLLIDER.equals(sceneObject.getName()) && mDraggingOffset == null) {
                mDraggingOffset = pickedObject.hitLocation;
                mPetContext.runDelayedOnPetThread(this, ViewConfiguration.getLongPressTimeout());
            }
        }

        @Override
        public void onTouchEnd(SXRNode sceneObject, SXRPicker.SXRPickedObject pickedObject) {
            Log.d(TAG, "onTouchEnd");
            if (mCharacterView.isDragging() && sceneObject == mCharacterView.getBoundaryPlane()) {
                Log.d(TAG, "onDrag stop");
                mCharacterView.stopDragging();
                mDraggingOffset = null;
            }
        }

        @Override
        public void onInside(SXRNode sceneObj, SXRPicker.SXRPickedObject collision) {
            if (mCharacterView.isDragging()) {
                // Use hitlocation from plane only
                if (sceneObj == mCharacterView.getBoundaryPlane()) {
                    float[] hit = collision.getHitLocation();

                    Matrix4f mat = sceneObj.getTransform().getModelMatrix4f();
                    Vector4f hitVector = new Vector4f(hit[0], hit[1], hit[2], 0);
                    hitVector.mul(mat);

                    // FIXME: make the pet be put inside the plane only
                    // Set the pet's position according to plane and hit location
                    mCharacterView.getTransform().setPosition(mat.m30() + hitVector.x,
                            mat.m31(), mat.m32() + hitVector.z);
                }
            }
        }

        @Override
        public void onMotionOutside(SXRPicker sxrPicker, MotionEvent motionEvent) {

        }

        @Override
        public void run() {
            if (mDraggingOffset != null) {
                Log.d(TAG, "onDrag start");
                mCharacterView.startDragging();
                vibrate();
                mDraggingOffset = null;
            }
        }
    }
}
