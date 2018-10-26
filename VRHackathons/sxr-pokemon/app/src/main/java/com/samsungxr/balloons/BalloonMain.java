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

import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.view.MotionEvent;

import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRDirectLight;
import com.samsungxr.SXRMain;
import com.samsungxr.SXRMaterial;
import com.samsungxr.SXRMesh;
import com.samsungxr.SXRPicker;
import com.samsungxr.SXRPicker.SXRPickedObject;
import com.samsungxr.SXRRenderData;
import com.samsungxr.SXRScene;
import com.samsungxr.SXRNode;
import com.samsungxr.SXRSphereCollider;
import com.samsungxr.SXRTexture;
import com.samsungxr.IPickEvents;
import com.samsungxr.nodes.SXRCameraNode;
import com.samsungxr.nodes.SXRSphereNode;
import com.samsungxr.nodes.SXRTextViewNode;
import com.samsungxr.utility.Log;
import org.joml.Vector2f;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Future;

public class BalloonMain extends SXRMain {

    public class PickHandler implements IPickEvents
    {
        public SXRNode   PickedObject = null;

        public void onEnter(SXRNode sceneObj, SXRPicker.SXRPickedObject pickInfo) { }
        public void onExit(SXRNode sceneObj) { }
        public void onInside(SXRNode sceneObj, SXRPicker.SXRPickedObject pickInfo) { }
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
    public static MediaPlayer sMediaPlayer;
    private SXRTextViewNode mScoreBoard;
    private boolean     mGameOver = false;
    private Integer     mScore = 0;
	private Timer		mTimer;

    private BalloonActivity mActivity;
    private SXRCameraNode cameraObject;

    private String [] pokemon_imgs = new String[] {
            "amphaos.png",
            "bulbashar.png",
            "Charizard.png",
            "charmander.png",
            "Coolfeatures.png",
            "cresselia.png",
            "evee2.png",
            "fly.png",
            "genesect.png",
            "Ivysaur.png",
            "Jigglypuff.png",
            "Landourous.png",
            "lurario.png",
            "meloetta.png",
            "meowth.png",
            "Ninetales_3d.png",
            "pikachu.png",
            "Seedot.png",
            "snorlax.png",
            "squirtle.png",
            "Tentacruel.png",
            "tornadus.png",
            "tyranitar.png",
            "unicorn.png",
            "Victini.png"
    };

    BalloonMain(BalloonActivity activity) {
        mActivity = activity;
    }

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
        mScene.setBackgroundColor(1.0f, 1.0f, 1.0f, 1.0f);

        /*
         * Set the camera passthrough
         */
        cameraObject = new SXRCameraNode(
                context, 18f, 10f, mActivity.getCamera());
        cameraObject.setUpCameraForVrMode(1); // set up 60 fps camera preview.
        cameraObject.getTransform().setPosition(0.0f, -1.8f, -10.0f);
        mScene.getMainCameraRig().addChildObject(cameraObject);

        /*
         * Set up a head-tracking pointer
         */
        SXRNode headTracker = new SXRNode(context,
                context.createQuad(0.1f, 0.1f),
                context.loadTexture(new SXRAndroidResource(context, R.drawable.headtrackingpointer)));
        headTracker.getTransform().setPosition(0.0f, 0.0f, -1.0f);
        headTracker.getRenderData().setDepthTest(false);
        headTracker.getRenderData().setRenderingOrder(100000);
        mScene.getMainCameraRig().addChildObject(headTracker);
        /*
         * Add the scoreboard
         */
        mScoreBoard = makeScoreboard(context);
        headTracker.addChildObject(mScoreBoard);
        /*
         * Add the environment
         */
//        SXRNode environment = makeEnvironment(context);
//        mScene.addNode(environment);
        /*
         * Make balloon prototype sphere mesh
         */
        mMaterials = makeMaterials(context);
        mSphereMesh = new SXRSphereNode(context, true).getRenderData().getMesh();

        /*
         * Start the particle emitter making balloons
         */
        SXRNode particleRoot = new SXRNode(context);
        particleRoot.setName("ParticleSystem");
        ParticleEmitter.MakeParticle particleCreator = new ParticleEmitter.MakeParticle()
        {
            public SXRNode create(SXRContext context, Integer index) { return makeBalloon(context, index); }
        };
        mParticleSystem = new ParticleEmitter(context, mScene, particleCreator);
        mParticleSystem.MaxDistance = 10.0f;
        //mParticleSystem.TotalParticles = 10;
        //mParticleSystem.EmissionRate = 3;
        mParticleSystem.Velocity = new ParticleEmitter.Range<Float>(2.0f, 6.0f);
        mParticleSystem.EmitterArea = new ParticleEmitter.Range<Vector2f>(new Vector2f(-5.0f, -2.0f), new Vector2f(5.0f, 2.0f));
        particleRoot.getTransform().setRotationByAxis(-90.0f, 1, 0, 0);
        particleRoot.getTransform().setPosition(0, -3.0f, -3.0f);
        particleRoot.attachComponent(mParticleSystem);
        mScene.addNode(particleRoot);
        /*
         * Respond to picking events
         */
        mScene.getMainCameraRig().getOwnerObject().attachComponent(new SXRPicker(context, mScene));
        mPickHandler = new PickHandler();
        mScene.getEventReceiver().addListener(mPickHandler);
		/*
		 * start the game timer
		 */
		mTimer = new Timer();
		TimerTask gameOver = new TimerTask()
		{
			public void run() { gameOver(); }
		};
		long oneMinute = 60 * 1000;
		mTimer.schedule(gameOver, oneMinute);
    }

    @Override
    public void onAfterInit() {
        sMediaPlayer = MediaPlayer.create(getSXRContext().getContext(), R.raw.backgroundmusic);
        sMediaPlayer.setLooping(true);
        sMediaPlayer.start();
    }

    public void gameOver()
    {
        mParticleSystem.setEnable(false);
        //mScoreBoard.getTransform().setPosition(0, 0, -2);
        mScoreBoard.setBackgroundColor(Color.RED);
        mScoreBoard.setText(mScoreBoard.getTextString() + " Time's Up");
        mGameOver = true;
    }

    SXRNode makeBalloon(SXRContext context, Integer index)
    {
        //String Tag = "makeBalloon";
        //android.util.Log.e(Tag, "enter make ballon...");

        //android.util.Log.e(Tag, "Calling random...");
        //Random rand = new Random();
        //int img_index = rand.nextInt(25);
        //android.util.Log.e(Tag, "img_index: " + img_index);



        SXRTexture texture = context.loadTexture(pokemon_imgs[index]);
        // create a a scene object (this constructor creates a rectangular scene*
        // object that uses the standard 'unlit' shader)*
        SXRNode sceneObject = new SXRNode(context, 2.0f, 2.0f, texture);
        // set the scene object position*
        sceneObject.getTransform().setPosition(0.0f, 0.0f, -3.0f);
        SXRSphereCollider collider = new SXRSphereCollider(context);
        sceneObject.attachComponent(collider);
        return sceneObject;
    }

    SXRNode makeEnvironment(SXRContext context)
    {
        Future<SXRTexture> tex = context.loadFutureCubemapTexture(new SXRAndroidResource(context, R.raw.lycksele3));
        SXRMaterial material = new SXRMaterial(context, SXRMaterial.SXRShaderType.Cubemap.ID);
        material.setMainTexture(tex);
        SXRSphereNode environment = new SXRSphereNode(context, 18, 36, false, material, 4, 4);
        environment.getTransform().setScale(20.0f, 20.0f, 20.0f);

        SXRDirectLight sunLight = new SXRDirectLight(context);
        sunLight.setAmbientIntensity(0.4f, 0.4f, 0.4f, 1.0f);
        sunLight.setDiffuseIntensity(0.6f, 0.6f, 0.6f, 1.0f);
        environment.attachComponent(sunLight);
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
            SXRMaterial mtl = new SXRMaterial(ctx);
            mtl.setDiffuseColor(colors[i][0], colors[i][1], colors[i][2], colors[i][3]);
            materials.add(mtl);
        }
        return materials;
    }

    /*
     * Make the scoreboard
     */
    SXRTextViewNode makeScoreboard(SXRContext ctx)
    {
        SXRTextViewNode scoreBoard = new SXRTextViewNode(ctx, 5, 0.7f, "Score: 0");
        SXRRenderData rdata = scoreBoard.getRenderData();
        scoreBoard.getTransform().setPosition(0.3f, 1.8f, -3.0f);
        scoreBoard.setTextColor(Color.BLACK);
        scoreBoard.setBackgroundColor(Color.TRANSPARENT);

        return scoreBoard;
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

    private void onHit(SXRNode sceneObj)
    {
        Particle particle = (Particle) sceneObj.getComponent(Particle.getComponentType());
        if (!mGameOver && (particle != null))
        {
            mPopSound.play();
            mParticleSystem.stop(particle);
            ++mScore;
            mScoreBoard.setText("Score: " + mScore.toString());
        }
    }
}
