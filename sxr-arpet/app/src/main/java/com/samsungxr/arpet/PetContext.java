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

package com.samsungxr.arpet;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.support.annotation.NonNull;

import com.samsungxr.SXRActivity;
import com.samsungxr.SXRCameraRig;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRNode;
import com.samsungxr.SXRPerspectiveCamera;
import com.samsungxr.SXRScene;
import com.samsungxr.mixedreality.IPlaneEvents;
import com.samsungxr.mixedreality.SXRAnchor;
import com.samsungxr.physics.SXRWorld;

import com.samsungxr.arpet.character.CharacterController;
import com.samsungxr.arpet.constant.ArPetObjectType;
import com.samsungxr.arpet.manager.connection.PetConnectionManager;
import com.samsungxr.arpet.service.share.PlayerSceneObject;
import com.samsungxr.arpet.service.share.SharedMixedReality;

public class PetContext {
    private final SXRActivity mActivity;
    private final HandlerThread mHandlerThread;
    private final Handler mHandler;
    private final Runnable mPauseTask;
    private boolean mPaused;
    private long mResumeTime;
    private SXRContext mSxrContext;
    private SharedMixedReality mMixedReality;
    private PlayerSceneObject mPlayer;
    private PlaneHandler mPlaneHandler;
    private SXRScene mMainScene = null;
    private CharacterController mPetController = null;
    private BallThrowHandler mBallThrowHandler = null;

    public PetContext(SXRActivity activity) {
        mActivity = activity;
        mHandlerThread = new HandlerThread("com.samsungxr.arpet-main");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());

        mPaused = true;
        mResumeTime = 0;

        mPauseTask = new Runnable() {
            @Override
            public void run() {
                try {
                    synchronized (mPauseTask) {
                        mPauseTask.wait();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    public void init(SXRContext context) {
        mSxrContext = context;
        mMainScene = new SXRScene(context);
        configCameraClipping(mMainScene);

        SXRWorld world = new SXRWorld(mSxrContext);
        world.setGravity(0f, -200f, 0f);
        mMainScene.getRoot().attachComponent(world);

        PetConnectionManager.getInstance().init(this);
        mMixedReality = new SharedMixedReality(this);

        mPlayer = new PlayerSceneObject(mSxrContext);
        mMainScene.getMainCameraRig().addChildObject(mPlayer);

        registerSharedObject(mPlayer, ArPetObjectType.PLAYER);

        // FIXME: Workaround to
        // You may only use GestureDetector constructor from a {@link android.os.Looper} thread.

        mBallThrowHandler = new BallThrowHandler(this);
    }

    public BallThrowHandler getBallThrowHandlerHandler() {
        return mBallThrowHandler;
    }

    public void setPetController(CharacterController controller) {
        mPetController = controller;
    }

    public CharacterController getPetController() {
        return mPetController;
    }

    private static void configCameraClipping(SXRScene scene) {
        SXRCameraRig rig = scene.getMainCameraRig();
        //rig.getCenterCamera().setNearClippingDistance(1);
        //((GVRPerspectiveCamera) rig.getLeftCamera()).setNearClippingDistance(1);
        //((GVRPerspectiveCamera) rig.getRightCamera()).setNearClippingDistance(1);
        rig.getCenterCamera().setFarClippingDistance(2000);
        ((SXRPerspectiveCamera) rig.getLeftCamera()).setFarClippingDistance(2000);
        ((SXRPerspectiveCamera) rig.getRightCamera()).setFarClippingDistance(2000);
    }

    public SXRActivity getActivity() {
        return mActivity;
    }

    public SXRContext getSXRContext() {
        return mSxrContext;
    }

    public SharedMixedReality getMixedReality() {
        return mMixedReality;
    }

    public void setPlaneHandler(PlaneHandler planeHandler) {
        mPlaneHandler = planeHandler;
    }

    public PlaneHandler getPlaneHandler() {
        return mPlaneHandler;
    }

    public void registerPlaneListener(@NonNull IPlaneEvents listener) {
        mMixedReality.getEventReceiver().addListener(listener);
    }

    public void unregisterPlaneListener(@NonNull IPlaneEvents listener) {
        mMixedReality.getEventReceiver().removeListener(listener);
    }

    public SXRScene getMainScene() {
        return mMainScene;
    }

    public PlayerSceneObject getPlayer() {
        return mPlayer;
    }

    public long getResumeTime() {
        return mResumeTime;
    }

    public boolean runOnPetThread(Runnable r) {
        return mHandler.post(r);
    }

    public boolean runDelayedOnPetThread(Runnable r, long delayMillis) {
        return mHandler.postDelayed(r, delayMillis);
    }

    public void removeTask(Runnable r) {
        mHandler.removeCallbacks(r);
    }

    void pause() {
        mPaused = true;
        synchronized (mPauseTask) {
            mHandler.postAtFrontOfQueue(mPauseTask);
        }
    }

    void resume() {
        mPaused = false;
        mResumeTime = SystemClock.uptimeMillis();
        synchronized (mPauseTask) {
            mHandler.removeCallbacks(mPauseTask);
            mPauseTask.notify();
        }
    }

    public boolean isPaused() {
        return mPaused;
    }

    public int getMode() {
        return mMixedReality.getMode();
    }

    public void registerSharedObject(SXRNode object, @ArPetObjectType String type,
                                     boolean repeat) {
        mMixedReality.registerSharedObject(object, type, repeat);
    }

    public void registerSharedObject(SXRNode object, @ArPetObjectType String type) {
        mMixedReality.registerSharedObject(object, type, true);
    }

    public void unregisterSharedObject(SXRNode object) {
        mMixedReality.unregisterSharedObject(object);
    }

    public SXRAnchor getSharedAnchor() {
        return mMixedReality.getSharedAnchor();
    }
}
