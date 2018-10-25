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

package com.samsungxr.immersivepedia;

import android.media.MediaPlayer;
import android.view.MotionEvent;

import com.samsungxr.SXRContext;
import com.samsungxr.SXRMain;
import com.samsungxr.SXRScene;
import com.samsungxr.immersivepedia.focus.FocusableController;
import com.samsungxr.immersivepedia.focus.PickHandler;
import com.samsungxr.immersivepedia.input.TouchPadInput;
import com.samsungxr.immersivepedia.scene.DinosaurScene;
import com.samsungxr.immersivepedia.scene.MenuScene;
import com.samsungxr.immersivepedia.util.AudioClip;
import com.samsungxr.immersivepedia.util.FPSCounter;
import com.samsungxr.io.SXRCursorController;
import com.samsungxr.io.SXRInputManager;
import com.samsungxr.io.SXRTouchPadGestureListener;

public class Main extends SXRMain {

    private static SXRContext mGvrContext;

    private MenuScene menuScene;
    public static DinosaurScene dinosaurScene;
    private static MediaPlayer mediaPlayer;
    private SXRCursorController mController;
    private PickHandler pickHandler;

    @Override
    public void onInit(final SXRContext gvrContext) throws Throwable {
        mGvrContext = gvrContext;

        AudioClip.getInstance(gvrContext.getContext());
        mediaPlayer = MediaPlayer.create(gvrContext.getContext(),
                R.raw.sfx_ambient_1_1);
        mediaPlayer.setLooping(true);
        mediaPlayer.setVolume(1.0f, 1.0f);
        mediaPlayer.start();

        dinosaurScene = new DinosaurScene(gvrContext);
        menuScene = new MenuScene(gvrContext);
        pickHandler = new PickHandler();
        closeSplashScreen();

        gvrContext.runOnGlThreadPostRender(64, new Runnable() {
            @Override
            public void run() {
                setMainScene(menuScene);
            }
        });
        gvrContext.getInputManager().selectController(new SXRInputManager.ICursorControllerSelectListener()
        {
            public void onCursorControllerSelected(SXRCursorController newController, SXRCursorController oldController)
            {
                GazeController cursor = GazeController.get();

                if (cursor == null)
                {
                    new GazeController(newController);
                }
                else
                {
                    newController.setCursor(cursor.getCursor());
                }
                mController = newController;
                newController.addPickEventListener(pickHandler);
            }
        });
    }

    @Override
    public void onStep() {
        TouchPadInput.process();
        FPSCounter.tick();

        if (mGvrContext.getMainScene().equals(dinosaurScene)) {
            dinosaurScene.onStep();
        }
    }

    @Override
    public void onSingleTapUp(MotionEvent event) {
        if (null != mGvrContext) {
            FocusableController.clickProcess(mGvrContext, pickHandler);
        }
    }

    @Override
    public void onSwipe(SXRTouchPadGestureListener.Action action, float vx) {
        TouchPadInput.onSwipe(action);
        FocusableController.swipeProcess(mGvrContext, pickHandler);
    }

    public static void clickOut() {
        if (null != dinosaurScene && mGvrContext.getMainScene().equals(Main.dinosaurScene)) {
            Main.dinosaurScene.closeObjectsInScene();
        }
    }

    public void onPause() {
        if (null != mediaPlayer) {
            mediaPlayer.stop();
        }
        if (null != dinosaurScene) {
            dinosaurScene.onPause();
        }
        // Pause all active streams.
        if (null != mGvrContext) {
            AudioClip.getInstance(mGvrContext.getContext()).autoPause();
        }
    }

    public void setMainScene(SXRScene newScene)
    {
        if (mController != null)
        {
            mController.setScene(newScene);
        }
        getSXRContext().setMainScene(newScene);
    }

    @Override
    public boolean onBackPress() {
        final SXRScene mainScene = getSXRContext().getMainScene();
        if (dinosaurScene == mainScene) {
            menuScene = new MenuScene(getSXRContext());
            setMainScene(menuScene);
            GazeController.get().enableGaze();
            return true;
        }
        return false;
    }
}