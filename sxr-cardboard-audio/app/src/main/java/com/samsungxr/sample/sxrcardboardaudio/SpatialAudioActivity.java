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

package com.samsungxr.sample.sxrcardboardaudio;

import android.os.Bundle;
import android.view.MotionEvent;

import com.samsungxr.SXRActivity;
import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRMain;
import com.samsungxr.SXRScene;
import com.samsungxr.SXRNode;
import com.samsungxr.SXRTexture;
import com.samsungxr.resonanceaudio.SXRAudioManager;
import com.samsungxr.resonanceaudio.SXRAudioSource;
import com.samsungxr.utility.Threads;

public final class SpatialAudioActivity extends SXRActivity
{
    private SpatialAudioMain mMain;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mMain = new SpatialAudioMain();

        setMain(mMain, "sxr.xml");
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            mMain.toggleListener();
        }

        return super.onTouchEvent(event);
    }

    private static final class SpatialAudioMain extends SXRMain
    {
        private static final String SOUND_FILE = "cube_sound.wav";
        SXRNode r2d2Model;
        private float modelY = -1.5f;
        private SXRAudioManager audioListener;
        private float time = 0f;
        private final float distance = 10;

        @Override
        public void onInit(SXRContext sxrContext) throws Throwable {
            SXRScene scene = sxrContext.getMainScene();
            audioListener = new SXRAudioManager(sxrContext);

            r2d2Model = sxrContext.getAssetLoader().loadModel("R2D2/R2D2.dae");
            r2d2Model.getTransform().setPosition(distance * (float)Math.sin(time), modelY,
                    distance * (float)Math.cos(time));
            scene.addNode(r2d2Model);

            final SXRAudioSource audioSource = new SXRAudioSource(sxrContext);
            audioListener.addSource(audioSource);
            r2d2Model.attachComponent(audioSource);

            // add a floor
            final SXRTexture texture = sxrContext.getAssetLoader().loadTexture(new SXRAndroidResource(sxrContext, R.drawable.floor));
            SXRNode floor = new SXRNode(sxrContext, 120.0f, 120.0f, texture);

            floor.getTransform().setRotationByAxis(-90, 1, 0, 0);
            floor.getTransform().setPositionY(-1.5f);
            floor.getRenderData().setRenderingOrder(0);
            scene.addNode(floor);

            // Avoid any delays during start-up due to decoding of sound files.
            Threads.spawn(
                    new Runnable() {
                        public void run() {
                            audioSource.load(SOUND_FILE);
                            audioSource.play(true);
                            audioListener.setEnable(true);
                        }
                    });
        }

        public void toggleListener() {
            if (audioListener != null) {
                audioListener.setEnable(!audioListener.isEnabled());
            }
        }

        @Override
        public void onStep() {
            if (r2d2Model != null) {
                time += 0.016f;

                r2d2Model.getTransform().setPosition(distance * (float) Math.sin(time), modelY,
                        distance * (float) Math.cos(time));
            }
        }
    }

}
