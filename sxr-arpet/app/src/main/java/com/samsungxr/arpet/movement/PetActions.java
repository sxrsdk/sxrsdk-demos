/*
 * Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.samsungxr.arpet.movement;

import android.support.annotation.IntDef;

import com.samsungxr.SXRNode;
import com.samsungxr.SXRTransform;
import com.samsungxr.animation.SXRAnimator;
import com.samsungxr.arpet.PetContext;
import com.samsungxr.arpet.character.CharacterView;
import com.samsungxr.arpet.constant.ArPetObjectType;
import com.samsungxr.utility.Log;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.Arrays;

/**
 * Represents a state of the Character.
 */
public class PetActions {
    private static final String TAG = "CharacterStates";

    @IntDef({IDLE.ID, TO_BALL.ID, TO_PLAYER.ID, TO_TAP.ID, GRAB.ID, TO_BED.ID, TO_BOWL.ID,
            TO_HYDRANT.ID, DRINK_ENTER.ID, DRINK_EXIT.ID, DRINK_LOOP.ID,
            HYDRANT_ENTER.ID, HYDRANT_EXIT.ID, HYDRANT_LOOP.ID,
            SLEEP_ENTER.ID, SLEEP_EXIT.ID, SLEEP_LOOP.ID, AT_EDIT.ID})
    public @interface Action{
    }

    private static abstract class PetAction implements IPetAction {
        protected final CharacterView mCharacter;
        protected final SXRNode mTarget;
        protected final OnPetActionListener mListener;

        protected Matrix4f mPetMtx = null;
        protected Matrix4f mTargetMtx = null;

        protected final Quaternionf mRotation = new Quaternionf();
        protected final Vector4f mPetDirection = new Vector4f();
        protected final Vector3f mTargetDirection = new Vector3f();
        protected final Vector3f mMoveTo = new Vector3f();

        protected float mPetRadius = 1.0f;
        protected float mTurnSpeed = 5f;
        protected float mWalkingSpeed = 25f;
        protected float mRunningSpeed = 100f;
        protected float mElapsedTime = 0;
        protected float mAnimDuration = 0;
        protected SXRAnimator mAnimation;

        protected PetAction(CharacterView character, SXRNode target,
                            OnPetActionListener listener) {
            mCharacter = character;
            mTarget = target;
            mListener = listener;
            mAnimation = null;
        }

        public void setAnimation(SXRAnimator animation) {
            mAnimation = animation;
            if (animation != null) {
                int count = mAnimation.getAnimationCount();

                for (int i = 0; i < count; i++) {
                    final float duration = mAnimation.getAnimation(i).getDuration();
                    if (duration > mAnimDuration) {
                        mAnimDuration = duration;
                    }
                }
                mAnimation.reset();
            }
        }

        protected void animate(float frameTime) {
            mElapsedTime += frameTime;
            mElapsedTime = ((int)(mElapsedTime * 1000)) % ((int)(mAnimDuration * 1000));
            mElapsedTime /= 1000f;

            mAnimation.animate(mElapsedTime);
        }

        @Override
        public void entry() {
            mPetRadius = mCharacter.getBoundingVolume().radius;
            mRunningSpeed = mPetRadius * 2;
            mWalkingSpeed = mPetRadius * 0.7f;

            mElapsedTime = 0;
            onEntry();
            if (mAnimation != null) {
                mAnimation.animate(0);
            }
        }

        @Override
        public void exit() {
            onExit();
        }

        @Override
        public void run(float frameTime) {
            SXRTransform petTrans = mCharacter.getTransform();
            // Vector of Character toward to Camera
            mPetMtx = petTrans.getModelMatrix4f();
            mTargetMtx = mTarget.getTransform().getModelMatrix4f();

            /* Pet world space vector */
            mPetDirection.set(0, 0, 1, 0);
            mPetDirection.mul(mPetMtx);
            mPetDirection.normalize();

            mTargetDirection.set(mTargetMtx.m30(), mTargetMtx.m31(), mTargetMtx.m32());
            mTargetDirection.sub(mPetMtx.m30(), mPetMtx.m31(), mPetMtx.m32());

            /* Target direction to look at */
            mMoveTo.set(mTargetDirection);
            mMoveTo.normalize();
            // Speed vector to create a smooth rotation
            mMoveTo.mul(mTurnSpeed * frameTime);
            mMoveTo.add(mPetDirection.x, 0, mPetDirection.z);
            mMoveTo.normalize();

            // Calc the rotation toward the camera and put it in pRot
            mRotation.rotationTo(mPetDirection.x, 0, mPetDirection.z,
                    mMoveTo.x, 0, mMoveTo.z);

            petTrans.rotate(mRotation.w, mRotation.x, mRotation.y, mRotation.z);

            onRun(frameTime);
        }

        protected abstract void onEntry();

        protected abstract void onExit();

        protected abstract void onRun(float fimeTime);
    }

    private static abstract class LoopAction extends PetAction {
        public LoopAction(CharacterView character, SXRNode targetObject,
                          OnPetActionListener listener) {
            super(character, targetObject, listener);
        }

        @Override
        public void onRun(float frameTime) {
            if (mAnimation != null) {
                animate(frameTime);
            }
        }
    }

    private static abstract class WalkAction extends LoopAction {
        private final float mWalkError;

        public WalkAction(CharacterView character, SXRNode targetObject,
                      OnPetActionListener listener, float error) {
            super(character, targetObject, listener);
            mWalkError = error;
        }

        @Override
        public void onEntry() {
            if (mAnimation == null) {
                setAnimation(mCharacter.getAnimation(1));
            }
        }

        @Override
        public void onRun(float frameTime) {
            super.onRun(frameTime);

            mTargetDirection.y = 0;
            // Min distance to the tap position
            boolean moveTowardToTapPosition = mTargetDirection.length() > mPetRadius * mWalkError;

            if (moveTowardToTapPosition) {
                mRotation.rotationTo(mPetDirection.x, 0, mPetDirection.z,
                        mTargetDirection.x, 0, mTargetDirection.z);

                if (mRotation.angle() < Math.PI * 0.25f) {
                    // acceleration logic
                    float[] pose = mCharacter.getTransform().getModelMatrix();
                    mMoveTo.mul(mWalkingSpeed * frameTime);

                    pose[12] = pose[12] + mMoveTo.x;
                    pose[14] = pose[14] + mMoveTo.z;

                    mCharacter.updatePose(pose);
                }
            } else {
                mListener.onActionEnd(this, true);
            }
        }
    }

    public static class IDLE extends LoopAction {
        public static final int ID = 0;
        private final PetContext mPetContext;

        public IDLE(PetContext petContext, CharacterView character) {
            super(character, petContext.getPlayer(), null);
            mPetContext = petContext;
        }

        @Override
        public int id() {
            return ID;
        }

        @Override
        public void onEntry() {
            Log.w(TAG, "entry => IDLE");
            if (mAnimation == null) {
                setAnimation(mCharacter.getAnimation(0));
            }
            mPetContext.registerSharedObject(mCharacter, ArPetObjectType.PET, false);
        }

        @Override
        public void onExit() {
            Log.w(TAG, "exit => IDLE");
            mPetContext.unregisterSharedObject(mCharacter);
        }
    }

    public static class TO_PLAYER extends PetAction {
        public static final int ID = 1;

        public TO_PLAYER(CharacterView character, SXRNode player,
                         OnPetActionListener listener) {
            super(character, player, listener);
        }

        @Override
        public int id() {
            return ID;
        }

        @Override
        public void onEntry() {
            Log.w(TAG, "entry => MOVING_TO_PLAYER");
            setAnimation(mCharacter.getAnimation(3));
        }

        @Override
        public void onExit() {
            Log.w(TAG, "exit => MOVING_TO_PLAYER");
        }

        @Override
        public void onRun(float frameTime) {
            mTargetDirection.y = 0;
            // Min distance to the camera/player
            boolean moveTowardToCam = mTargetDirection.length() > mPetRadius * 2.0f;

            if (moveTowardToCam) {
                if (mAnimation != null) {
                    animate(frameTime);
                }

                float[] pose = mCharacter.getTransform().getModelMatrix();
                mMoveTo.mul(mWalkingSpeed * frameTime);

                pose[12] = pose[12] + mMoveTo.x;
                pose[14] = pose[14] + mMoveTo.z;

                if (!mCharacter.updatePose(pose) && mRotation.angle() < 0.2f) {
                    mListener.onActionEnd(this, false);
                }
            } else {
                mListener.onActionEnd(this, true);
            }
        }
    }

    public static class TO_BALL extends PetAction {
        public static final int ID = 2;

        private float mRunningSpeed2;

        // Used to calculate in frame ball speed
        private Vector3f mOldBallPos = new Vector3f();

        // Used to calculate the moving average of ball speed
        private float[] mSpeedSamp = { 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f };
        private int mSampIdx = 0;
        private float mSpeedSum;

        private boolean mSpeedingUp;
        private boolean mApproaching;

        public TO_BALL(CharacterView character, SXRNode target,
                       OnPetActionListener listener) {
            super(character, target, listener);
        }

        @Override
        public int id() {
            return ID;
        }

        @Override
        public void onEntry() {
            Log.w(TAG, "entry => MOVING_TO_BALL");
            setAnimation(mCharacter.getAnimation(2));

            Matrix4f m = mTarget.getTransform().getModelMatrix4f();
            m.getTranslation(mOldBallPos);
            mSpeedingUp = true;
            mApproaching = false;
            mRunningSpeed2 = 0.95f;
            Arrays.fill(mSpeedSamp, 0f);
            mSpeedSum = 0f;
        }

        @Override
        public void onExit() {
            Log.w(TAG, "exit => MOVING_TO_BALL");
        }

        @Override
        public void onRun(float frameTime) {

            // Min distance to ball
            boolean success  = mTarget.getTransform().getPositionY() > mCharacter.getTransform().getPositionY()
                    - mPetRadius;

            boolean moveTowardToBall = mTargetDirection.length() > mPetRadius * 0.7f
                    && success;

            if (moveTowardToBall) {
                if (mAnimation != null) {
                    animate(frameTime);
                }

                /* TODO: test this with share
                mRotation.rotationTo(mPetDirection.x, 0, mPetDirection.z,
                        mTargetDirection.x, 0, mTargetDirection.z);
                        */

                if (mRotation.angle() < Math.PI * 0.25f) {
					/* TODO: test this with share
                    float a = mPetMtx.m30() - mTargetMtx.m30();
                    float b = mPetMtx.m32() - mTargetMtx.m32();
                    float toPet = (float)Math.sqrt(a * a + b * b);

                    a = mOldBallPos.x - mTargetMtx.m30();
                    b = mOldBallPos.z - mTargetMtx.m32();
                    float d = (float)Math.sqrt(a * a + b * b);
                    mSpeedSum -= mSpeedSamp[mSampIdx];
                    mSpeedSamp[mSampIdx] = d / frameTime;
                    mSpeedSum += mSpeedSamp[mSampIdx];
                    mSampIdx = (mSampIdx + 1) & 0x7;

                    float speed = mSpeedSum * 0.125f * 0.016f;

                    if (mApproaching) {
                        speed += 0.5f;
                        if (speed > 5f) {
                            speed = 5f;
                        }

                        if (speed < mRunningSpeed2) {
                            // Can reduce the speed
                            float delta = (mRunningSpeed2 - speed) * 0.5f;
                            mRunningSpeed2 = mRunningSpeed2 - delta;
                        } else {
                            mRunningSpeed2 = speed;
                        }
                    } else  if (toPet < (mCharacterHalfSize * 0.75f) && speed < mRunningSpeed2) {
                        mApproaching = true;
                    } else if (mSpeedingUp) {
                        // Will increase speed
                        mRunningSpeed2 = mRunningSpeed2 * 1.06f;

                        if (mRunningSpeed2 > 5f) {
                            // Maximum speed reached
                            mRunningSpeed2 = 5f;
                            mSpeedingUp = false;
                        }
                    }

                    mTargetMtx.getTranslation(mOldBallPos);

                    float[] pose = mCharacter.getAnchor().getTransform().getModelMatrix();

                    mMoveTo.mul(mRunningSpeed2);
					*/
                    float[] pose = mCharacter.getTransform().getModelMatrix();
                    // TODO: Create pose
                    mMoveTo.mul(mRunningSpeed * frameTime);

                    pose[12] = pose[12] + mMoveTo.x;
                    pose[14] = pose[14] + mMoveTo.z;

                    mCharacter.updatePose(pose);
                }
            } else {
                mListener.onActionEnd(this, success);
            }
        }
    }

    public static class TO_TAP extends WalkAction {
        public static final int ID = 3;

        public TO_TAP(CharacterView character, SXRNode tapObject,
                         OnPetActionListener listener) {
            super(character, tapObject, listener, 0.1f);
        }

        @Override
        public int id() {
            return ID;
        }

        @Override
        public void onEntry() {
            super.onEntry();
            Log.w(TAG, "entry => MOVING_TO_TAP");
        }

        @Override
        public void onExit() {
            Log.w(TAG, "exit => MOVING_TO_TAP");
        }
    }

    public static class GRAB extends PetAction {
        public static final int ID = 4;

        public GRAB(CharacterView character, SXRNode target,
                       OnPetActionListener listener) {
            super(character, target, listener);
        }

        @Override
        public int id() {
            return ID;
        }

        @Override
        public void onEntry() {
            Log.w(TAG, "entry => GRAB");
            setAnimation(mCharacter.getAnimation(4));
        }

        @Override
        public void onExit() {
            Log.w(TAG, "exit => GRAB");
        }

        @Override
        public void onRun(float frameTime) {
            if (mElapsedTime > mAnimDuration * 0.5f
                    && !mCharacter.isGrabbing(mTarget)) {
                mCharacter.grabItem(mTarget);
            }

            if (mAnimation != null) {
                float time = mElapsedTime;

                animate(frameTime);

                if (mElapsedTime < time) {
                    mListener.onActionEnd(this, true);
                }
            } else {
                mListener.onActionEnd(this, false);
            }
        }
    }

    public static class TO_BED extends WalkAction {
        public static final int ID = 5;

        public TO_BED(CharacterView character, SXRNode bedObject,
                      OnPetActionListener listener) {
            super(character, bedObject, listener, 0.4f);
        }

        @Override
        public int id() {
            return ID;
        }

        @Override
        public void onEntry() {
            super.onEntry();
            Log.w(TAG, "entry => TO_BED");
        }

        @Override
        public void onExit() {
            Log.w(TAG, "exit => TO_BED");
            mCharacter.getTransform().setPositionY(mCharacter.getTransform().getPositionY() + 0.6f);
        }
    }

    public static class TO_BOWL extends WalkAction {
        public static final int ID = 6;

        public TO_BOWL(CharacterView character, SXRNode bowlObject,
                      OnPetActionListener listener) {
            super(character, bowlObject, listener, 0.7f);
        }

        @Override
        public int id() {
            return ID;
        }

        @Override
        public void onEntry() {
            super.onEntry();
            Log.w(TAG, "entry => TO_BOWL");
        }

        @Override
        public void onExit() {
            Log.w(TAG, "exit => TO_BOWL");
        }
    }

    public static class TO_HYDRANT extends WalkAction {
        public static final int ID = 7;

        public TO_HYDRANT(CharacterView character, SXRNode hydrantObject,
                      OnPetActionListener listener) {
            super(character, hydrantObject, listener, 0.1f);
        }

        @Override
        public int id() {
            return ID;
        }

        @Override
        public void onEntry() {
            super.onEntry();
            Log.w(TAG, "entry => TO_HYDRANT");
        }

        @Override
        public void onExit() {
            Log.w(TAG, "exit => TO_HYDRANT");
        }
    }

    public static class DRINK_ENTER extends LoopAction {
        public static final int ID = 8;

        public DRINK_ENTER(CharacterView character, SXRNode target,
                    OnPetActionListener listener) {
            super(character, target, listener);
        }

        @Override
        public int id() {
            return ID;
        }

        @Override
        public void onEntry() {
            Log.w(TAG, "entry => DRINK_ENTER");
            if (mAnimation == null) {
                setAnimation(mCharacter.getAnimation(5));
            }
        }

        @Override
        public void onExit() {
            Log.w(TAG, "exit => DRINK_ENTER");
        }

        @Override
        public void onRun(float frameTime) {
            if (mElapsedTime + frameTime < mAnimDuration) {
                super.onRun(frameTime);
            } else {
                mListener.onActionEnd(this, true);
            }
        }
    }

    public static class DRINK_EXIT extends LoopAction {
        public static final int ID = 9;

        public DRINK_EXIT(CharacterView character, SXRNode target,
                           OnPetActionListener listener) {
            super(character, target, listener);
        }

        @Override
        public int id() {
            return ID;
        }

        @Override
        public void onEntry() {
            Log.w(TAG, "entry => DRINK_EXIT");
            if (mAnimation == null) {
                setAnimation(mCharacter.getAnimation(6));
            }
        }

        @Override
        public void onExit() {
            Log.w(TAG, "exit => DRINK_EXIT");
        }

        @Override
        public void onRun(float frameTime) {
            if (mElapsedTime + frameTime < mAnimDuration) {
                super.onRun(frameTime);
            } else {
                mListener.onActionEnd(this, true);
            }
        }
    }

    public static class DRINK_LOOP extends LoopAction {
        public static final int ID = 10;

        public DRINK_LOOP(CharacterView character, SXRNode target,
                          OnPetActionListener listener) {
            super(character, target, listener);
        }

        @Override
        public int id() {
            return ID;
        }

        @Override
        public void onEntry() {
            Log.w(TAG, "entry => DRINK_LOOP");
            if (mAnimation == null) {
                setAnimation(mCharacter.getAnimation(7));
            }
        }

        @Override
        public void onExit() {
            Log.w(TAG, "exit => DRINK_LOOP");
        }

        @Override
        public void onRun(float frameTime) {
            if (mElapsedTime + frameTime < mAnimDuration) {
                super.onRun(frameTime);
            } else {
                mListener.onActionEnd(this, true);
            }
        }
    }

    public static class HYDRANT_ENTER extends LoopAction {
        public static final int ID = 11;

        public HYDRANT_ENTER(CharacterView character, SXRNode target,
                           OnPetActionListener listener) {
            super(character, target, listener);
        }

        @Override
        public int id() {
            return ID;
        }

        @Override
        public void onEntry() {
            Log.w(TAG, "entry => HYDRANT_ENTER");
            if (mAnimation == null) {
                setAnimation(mCharacter.getAnimation(8));
            }
        }

        @Override
        public void onExit() {
            Log.w(TAG, "exit => HYDRANT_ENTER");
        }

        @Override
        public void onRun(float frameTime) {
            if (mElapsedTime + frameTime < mAnimDuration) {
                super.onRun(frameTime);
            } else {
                mListener.onActionEnd(this, true);
            }
        }
    }

    public static class HYDRANT_EXIT extends LoopAction {
        public static final int ID = 12;

        public HYDRANT_EXIT(CharacterView character, SXRNode target,
                          OnPetActionListener listener) {
            super(character, target, listener);
        }

        @Override
        public int id() {
            return ID;
        }

        @Override
        public void onEntry() {
            Log.w(TAG, "entry => HYDRANT_EXIT");
            if (mAnimation == null) {
                setAnimation(mCharacter.getAnimation(9));
            }
        }

        @Override
        public void onExit() {
            Log.w(TAG, "exit => HYDRANT_EXIT");
        }

        @Override
        public void onRun(float frameTime) {
            if (mElapsedTime + frameTime < mAnimDuration) {
                super.onRun(frameTime);
            } else {
                mListener.onActionEnd(this, true);
            }
        }
    }

    public static class HYDRANT_LOOP extends LoopAction {
        public static final int ID = 13;

        public HYDRANT_LOOP(CharacterView character, SXRNode target,
                          OnPetActionListener listener) {
            super(character, target, listener);
        }

        @Override
        public int id() {
            return ID;
        }

        @Override
        public void onEntry() {
            Log.w(TAG, "entry => HYDRANT_LOOP");
            if (mAnimation == null) {
                setAnimation(mCharacter.getAnimation(10));
            }
        }

        @Override
        public void onExit() {
            Log.w(TAG, "exit => HYDRANT_LOOP");
        }

        @Override
        public void onRun(float frameTime) {
            if (mElapsedTime + frameTime < mAnimDuration) {
                super.onRun(frameTime);
            } else {
                mListener.onActionEnd(this, true);
            }
        }
    }

    public static class SLEEP_ENTER extends LoopAction {
        public static final int ID = 14;

        public SLEEP_ENTER(CharacterView character, SXRNode target,
                             OnPetActionListener listener) {
            super(character, target, listener);
        }

        @Override
        public int id() {
            return ID;
        }

        @Override
        public void onEntry() {
            Log.w(TAG, "entry => SLEEP_ENTER");
            if (mAnimation == null) {
                setAnimation(mCharacter.getAnimation(11));
            }
        }

        @Override
        public void onExit() {
            Log.w(TAG, "exit => SLEEP_ENTER");
        }

        @Override
        public void onRun(float frameTime) {
            if (mElapsedTime + frameTime < mAnimDuration) {
                super.onRun(frameTime);
            } else {
                mListener.onActionEnd(this, true);
            }
        }
    }

    public static class SLEEP_EXIT extends LoopAction {
        public static final int ID = 15;

        public SLEEP_EXIT(CharacterView character, SXRNode target,
                          OnPetActionListener listener) {
            super(character, target, listener);
        }

        @Override
        public int id() {
            return ID;
        }

        @Override
        public void onEntry() {
            Log.w(TAG, "entry => SLEEP_EXIT");
            if (mAnimation == null) {
                setAnimation(mCharacter.getAnimation(12));
            }
        }

        @Override
        public void onExit() {
            Log.w(TAG, "exit => SLEEP_EXIT");
        }

        @Override
        public void onRun(float frameTime) {
            if (mElapsedTime + frameTime < mAnimDuration) {
                super.onRun(frameTime);
            } else {
                mListener.onActionEnd(this, true);
            }
        }
    }

    public static class SLEEP_LOOP extends LoopAction {
        public static final int ID = 16;

        public SLEEP_LOOP(CharacterView character, SXRNode target,
                          OnPetActionListener listener) {
            super(character, target, listener);
        }

        @Override
        public int id() {
            return ID;
        }

        @Override
        public void onEntry() {
            Log.w(TAG, "entry => SLEEP_LOOP");
            if (mAnimation == null) {
                setAnimation(mCharacter.getAnimation(13));
            }
        }

        @Override
        public void onExit() {
            Log.w(TAG, "exit => SLEEP_LOOP");
        }

        @Override
        public void onRun(float frameTime) {
            if (mElapsedTime + frameTime < mAnimDuration) {
                super.onRun(frameTime);
            } else {
                mListener.onActionEnd(this, true);
            }
        }
    }

    public static class AT_EDIT implements IPetAction {
        public static final int ID = 20;

        private final PetContext mPetContext;
        private final CharacterView mCharacter;

        public AT_EDIT(PetContext petContext, CharacterView character) {
            mPetContext = petContext;
            mCharacter = character;
        }

        @Override
        public int id() {
            return ID;
        }

        @Override
        public void entry() {
            Log.w(TAG, "entry => AT_EDIT");
            // Return the pet to the first position in the animation (IDLE)
            mCharacter.resetAnimation();
            mPetContext.registerSharedObject(mCharacter, ArPetObjectType.PET);
        }

        @Override
        public void exit() {
            Log.w(TAG, "exit => AT_EDIT");
            mPetContext.unregisterSharedObject(mCharacter);
        }

        @Override
        public void run(float frameTime) {

        }
    }
}
