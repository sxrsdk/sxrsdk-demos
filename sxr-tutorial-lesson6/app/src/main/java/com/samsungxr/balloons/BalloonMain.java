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

package com.samsungxr.balloons;

import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRCollider;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRDirectLight;
import com.samsungxr.SXRMaterial;
import com.samsungxr.SXRMesh;
import com.samsungxr.SXRMeshCollider;
import com.samsungxr.SXRScene;
import com.samsungxr.SXRSceneObject;
import com.samsungxr.SXRMain;
import com.samsungxr.SXRRenderData;
import com.samsungxr.SXRRenderData.SXRRenderingOrder;
import com.samsungxr.SXRShader;
import com.samsungxr.SXRSphereCollider;
import com.samsungxr.SXRTexture;
import com.samsungxr.SXRTransform;
import com.samsungxr.scene_objects.SXRSphereSceneObject;
;
import android.graphics.Color;
import android.media.AudioManager;
import android.view.Gravity;
import android.view.MotionEvent;
import com.samsungxr.SXRPicker;
import com.samsungxr.IPickEvents;
import com.samsungxr.SXRPicker.SXRPickedObject;
import com.samsungxr.scene_objects.SXRTextViewSceneObject;
import com.samsungxr.utility.Log;
import org.joml.Matrix4f;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.Random;
import java.util.TimerTask;
import java.io.IOException;
import java.util.Timer;
import android.media.SoundPool;

public class BalloonMain extends SXRMain {

    public class PickHandler implements IPickEvents
    {
        public SXRSceneObject   PickedObject = null;

        public void onEnter(SXRSceneObject sceneObj, SXRPicker.SXRPickedObject pickInfo) { }
        public void onExit(SXRSceneObject sceneObj) { }
        public void onInside(SXRSceneObject sceneObj, SXRPicker.SXRPickedObject pickInfo) { }
        public void onNoPick(SXRPicker picker)
        {
            PickedObject = null;
        }
        public void onPick(SXRPicker picker)
        {
            SXRPickedObject picked = picker.getPicked()[0];
            PickedObject = picked.hitObject;
        }
    }

    private SXRScene mScene = null;
    private PickHandler mPickHandler;
    private ParticleEmitter mParticleSystem;
    private ArrayList<SXRMaterial> mMaterials;
    private SXRMesh     mSphereMesh;
    private Random      mRandom = new Random();
    private SoundPool   mAudioEngine;
    private SoundEffect mPopSound;
    private SXRTextViewSceneObject mScoreBoard;
    private Integer     mScore = 0;
    private SXRPicker   mPicker;

    @Override
    public void onInit(SXRContext context)
    {
        /*
         * Load the balloon popping sound
         */
        mAudioEngine = new SoundPool(4, AudioManager.STREAM_MUSIC, 0);
        try
        {
            mPopSound = new SoundEffect(context, mAudioEngine, "pop.wav", false);
            mPopSound.setVolume(0.6f);
        }
        catch (IOException ex)
        {
            Log.e("Audio", "Cannot load pop.wav");
        }        /*
         * Set the background color
         */
        mScene = context.getMainScene();
        mScene.getMainCameraRig().getLeftCamera().setBackgroundColor(1.0f, 1.0f, 1.0f, 1.0f);
        mScene.getMainCameraRig().getRightCamera().setBackgroundColor(1.0f, 1.0f, 1.0f, 1.0f);

        /*
         * Set up a head-tracking pointer
         */
        SXRSceneObject headTracker = new SXRSceneObject(context,
                context.createQuad(0.1f, 0.1f),
                context.getAssetLoader().loadTexture(new SXRAndroidResource(context, R.drawable.headtrackingpointer)));
        headTracker.getTransform().setPosition(0.0f, 0.0f, -1.0f);
        headTracker.getRenderData().setDepthTest(false);
        headTracker.getRenderData().setRenderingOrder(100000);
        mScene.getMainCameraRig().addChildObject(headTracker);
        /*
         * Add the scoreboard
         */
        mScoreBoard = makeScoreboard(context, headTracker);
        /*
         * Add the environment
         */
        SXRSceneObject environment = makeEnvironment(context);
        mScene.addSceneObject(environment);
        /*
         * Make balloon prototype sphere mesh
         */
        mMaterials = makeMaterials(context);
        mSphereMesh = new SXRSphereSceneObject(context, true).getRenderData().getMesh();

        /*
         * Start the particle emitter making balloons
         */
        SXRSceneObject particleRoot = new SXRSceneObject(context);
        particleRoot.setName("ParticleSystem");
        ParticleEmitter.MakeParticle particleCreator = new ParticleEmitter.MakeParticle()
        {
            public SXRSceneObject create(SXRContext context) { return makeBalloon(context); }
        };
        mParticleSystem = new ParticleEmitter(context, mScene, particleCreator);
        mParticleSystem.MaxDistance = 10.0f;
        mParticleSystem.TotalParticles = 10;
        mParticleSystem.EmissionRate = 3;
        mParticleSystem.Velocity = new ParticleEmitter.Range<Float>(2.0f, 6.0f);
        mParticleSystem.EmitterArea = new ParticleEmitter.Range<Vector2f>(new Vector2f(-5.0f, -2.0f), new Vector2f(5.0f, 2.0f));
        particleRoot.getTransform().setRotationByAxis(-90.0f, 1, 0, 0);
        particleRoot.getTransform().setPosition(0, -3.0f, -3.0f);
        particleRoot.attachComponent(mParticleSystem);
        mScene.addSceneObject(particleRoot);
        /*
         * Respond to picking events
         */
        mPicker = new SXRPicker(context, mScene);
        mPickHandler = new PickHandler();
        mScene.getEventReceiver().addListener(mPickHandler);
		/*
		 * start the game timer
		 */
        gameStart();
    }

    public void gameOver()
    {
        mParticleSystem.setEnable(false);
        mScoreBoard.getTransform().setPosition(0, 0, -1.0f);
        mScoreBoard.getCollider().setEnable(true);
        mScoreBoard.setTextSize(10.0f);
        mScoreBoard.setText(mScoreBoard.getTextString() + "\nTap to play again");
    }

    public void gameStart()
    {
        mScoreBoard.getTransform().setPosition(-1.2f, 1.2f, -2.2f);
        mScore = 0;
        float s = mScoreBoard.getTextSize();
        mScoreBoard.setTextSize(15.0f);
        mScoreBoard.setText("000");
        mScoreBoard.getCollider().setEnable(false);
        mParticleSystem.setEnable(true);
        Timer timer = new Timer();
        TimerTask gameOver = new TimerTask()
        {
            public void run() { gameOver(); }
        };
        long oneMinute = 60 * 1000;
        timer.schedule(gameOver, oneMinute);
    }

    SXRSceneObject makeBalloon(SXRContext context)
    {
        SXRSceneObject balloon = new SXRSceneObject(context, mSphereMesh);
        SXRRenderData rdata = balloon.getRenderData();
        SXRSphereCollider collider = new SXRSphereCollider(context);
        Random rand = new Random();
        int mtlIndex = rand.nextInt(mMaterials.size() - 1);

        balloon.setName("balloon");
        rdata.setAlphaBlend(true);
        rdata.setMaterial(mMaterials.get(mtlIndex));
        rdata.setRenderingOrder(SXRRenderingOrder.TRANSPARENT);
        collider.setRadius(0.8f);
        balloon.attachComponent(collider);
        return balloon;
    }

    SXRSceneObject makeEnvironment(SXRContext context)
    {
        SXRTexture tex = context.getAssetLoader().loadCubemapTexture(new SXRAndroidResource(context, R.raw.lycksele3));
        SXRMaterial material = new SXRMaterial(context, SXRMaterial.SXRShaderType.Cubemap.ID);
        material.setMainTexture(tex);
        SXRSphereSceneObject environment = new SXRSphereSceneObject(context, 18, 36, false, material, 4, 4);
        environment.getTransform().setScale(20.0f, 20.0f, 20.0f);

        if (!SXRShader.isVulkanInstance())
        {
            SXRDirectLight sunLight = new SXRDirectLight(context);
            sunLight.setAmbientIntensity(0.4f, 0.4f, 0.4f, 1.0f);
            sunLight.setDiffuseIntensity(0.6f, 0.6f, 0.6f, 1.0f);
            environment.attachComponent(sunLight);
        }
        return environment;
    }

    /*
     * Make an array of materials for the particles
     * so they will not all be the same.
     */
    ArrayList<SXRMaterial> makeMaterials(SXRContext ctx)
    {
        float[][] colors = new float[][] {
                { 1.0f,   0.0f,   0.0f,   0.8f },
                { 0.0f,   1.0f,   0.0f,   0.8f },
                { 0.0f,   0.0f,   1.0f,   0.8f },
                { 1.0f,   0.0f,   1.0f,   0.8f },
                { 1.0f,   1.0f,   0.0f,   0.8f },
                { 0.0f,   1.0f,   1.0f,   0.8f }
        };
        ArrayList<SXRMaterial> materials = new ArrayList<SXRMaterial>();
        for (int i = 0; i < 6; ++i)
        {
            SXRMaterial mtl = new SXRMaterial(ctx, SXRMaterial.SXRShaderType.Phong.ID);
            mtl.setDiffuseColor(colors[i][0], colors[i][1], colors[i][2], colors[i][3]);
            materials.add(mtl);
        }
        return materials;
    }

    /*
     * Make the scoreboard
     */
    SXRTextViewSceneObject makeScoreboard(SXRContext ctx, SXRSceneObject parent)
    {
        SXRTextViewSceneObject scoreBoard = new SXRTextViewSceneObject(ctx, 2.0f, 1.5f, "000");
        SXRRenderData rdata = scoreBoard.getRenderData();
        SXRCollider collider = new SXRMeshCollider(ctx, true);

        collider.setEnable(false);
        scoreBoard.attachComponent(collider);
        scoreBoard.setTextColor(Color.YELLOW);
        scoreBoard.setBackgroundColor(Color.argb(0, 0, 0, 0));
        scoreBoard.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        rdata.setDepthTest(false);
        rdata.setAlphaBlend(true);
        rdata.setRenderingOrder(SXRRenderingOrder.OVERLAY);
        SXRSceneObject boardFrame = null;
        try
        {
            boardFrame = ctx.getAssetLoader().loadModel("mirror.3ds");
            SXRSceneObject.BoundingVolume bv = boardFrame.getBoundingVolume();
            SXRTransform trans = boardFrame.getTransform();
            Matrix4f mtx = new Matrix4f();
            float sf = 1.5f / bv.radius;

            trans.setScale(sf, sf, sf);
            trans.rotateByAxis(-90.0f, 0, 1, 0);
            trans.rotateByAxis(90.0f, 0, 0, 1);
            bv = boardFrame.getBoundingVolume();
            trans.setPosition(-bv.center.x, -bv.center.y, -bv.center.z + 0.1f);
            scoreBoard.addChildObject(boardFrame);
        }
        catch (IOException ex)
        {
            Log.e("Balloons", "Cannot load scoreboard frame " + ex.getMessage());
        }
        parent.addChildObject(scoreBoard);
        return scoreBoard;
    }

    @Override
    public void onStep() {
        FPSCounter.tick();
    }

    public void onTouchEvent(MotionEvent event)
    {
        switch (event.getAction() & MotionEvent.ACTION_MASK)
        {
            case MotionEvent.ACTION_DOWN:
                if (mPickHandler.PickedObject != null)
                {
                    onHit(mPickHandler.PickedObject);
                }
                break;

            default:
                break;
        }
    }

    private void onHit(SXRSceneObject sceneObj)
    {
        Particle particle = (Particle) sceneObj.getComponent(Particle.getComponentType());
        if (particle != null)
        {
            mPopSound.play();
            mParticleSystem.stop(particle);
            mScore += Math.round(particle.Velocity);
            mScoreBoard.setText(mScore.toString());
        }
        else if (sceneObj == mScoreBoard)
        {
            gameStart();
        }
    }
}
