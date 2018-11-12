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
import com.samsungxr.SXRBillboard;
import com.samsungxr.SXRCollider;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRDirectLight;
import com.samsungxr.SXRMaterial;
import com.samsungxr.SXRMesh;
import com.samsungxr.SXRMeshCollider;
import com.samsungxr.SXRScene;
import com.samsungxr.SXRNode;
import com.samsungxr.SXRMain;
import com.samsungxr.SXRRenderData;
import com.samsungxr.SXRRenderData.SXRRenderingOrder;
import com.samsungxr.SXRShader;
import com.samsungxr.SXRSphereCollider;
import com.samsungxr.SXRTexture;
import com.samsungxr.SXRTransform;
import com.samsungxr.animation.SXRAvatar;
import com.samsungxr.nodes.SXRSphereNode;
;
import android.graphics.Color;
import android.media.AudioManager;
import android.view.Gravity;
import android.view.MotionEvent;
import com.samsungxr.SXRPicker;
import com.samsungxr.IPickEvents;
import com.samsungxr.SXRPicker.SXRPickedObject;
import com.samsungxr.nodes.SXRTextViewNode;
import com.samsungxr.utility.Log;
import org.joml.Matrix4f;
import org.joml.Vector2f;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Random;
import java.util.TimerTask;
import java.io.IOException;
import java.util.Timer;
import android.media.SoundPool;

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

    private SXRContext mContext;
    private SXRScene mScene = null;
    private PickHandler mPickHandler;
    private ParticleEmitter mParticleSystem;
    private ArrayList<SXRMaterial> mMaterials;
    private SXRMesh     mSphereMesh;
    private Random      mRandom = new Random();
    private SoundPool   mAudioEngine;
    private SoundEffect mPopSound;
    private SoundEffect mGoodJobSound;
    private SoundEffect mWrongSound;
    private SXRTextViewNode mScoreBoard;
    private String     mScore = "";
    private SXRPicker   mPicker;
    private String [] alphabetImages = new String[]{
            "a.png", "a.png", "p.png", "p.png", "l.png", "l.png", "e.png", "e.png",
            "a.png", "a.png", "p.png", "p.png", "l.png", "l.png", "e.png", "e.png",
            "a.png", "a.png", "p.png", "p.png", "l.png", "l.png", "e.png", "e.png",
            "d.png", "b.png", "c.png", "f.png",
//            "g.png", "h.png", "i.png", "j.png", "k.png", "l.png", "m.png", "n.png", "o.png", "p.png",
//            "q.png", "r.png", "s.png", "t.png", "u.png", "v.png", "w.png", "x.png", "y.png", "z.png"
    };
    private SXRNode apple;
    private SXRNode apple1;
    private SXRNode apple2;
    private SXRNode apple3;

    @Override
    public void onInit(SXRContext context)
    {
        /*
         * Load the balloon popping sound
         */
        mAudioEngine = new SoundPool(4, AudioManager.STREAM_MUSIC, 0);
        try
        {
            mPopSound = new SoundEffect(context, mAudioEngine, "ding.wav", false);
            mPopSound.setVolume(0.6f);
        }
        catch (IOException ex) {
            Log.e("Audio", "Cannot load ding.wav");
        }
        try
        {
            mGoodJobSound = new SoundEffect(context, mAudioEngine, "good-job.mp3", false);
            mGoodJobSound.setVolume(0.6f);
        }
        catch (IOException ex) {
            Log.e("Audio", "Cannot load good-job.mp3");
        }
        try
        {
            mWrongSound = new SoundEffect(context, mAudioEngine, "wrong.mp3", false);
            mWrongSound.setVolume(1.0f);
        }
        catch (IOException ex) {
            Log.e("Audio", "Cannot load wrong.mp3");
        }
        /*
         * Set the background color
         */
        mScene = context.getMainScene();
        mContext = context;

        /*
         * Set up a head-tracking pointer
         */
        SXRNode headTracker = new SXRNode(context,
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
//        SXRNode environment = makeEnvironment(context);
//        mScene.addNode(environment);

        /*
         * Add the object
         */
        if (apple1 != null) {
            mScene.removeNode(apple1);
        }
        if (apple2 != null) {
            mScene.removeNode(apple2);
        }
        if (apple3 != null) {
            mScene.removeNode(apple3);
        }

        if (apple == null) {
            apple = makeObject(context, "apple.fbx");
            apple.getTransform().setPosition(0.0f, 0.0f, -0.5f).setScale(0.002f, 0.002f, 0.002f);
            mScene.addNode(apple);
        }

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
            public SXRNode create(SXRContext context) {
                SXRNode balloon = makeBalloon(context, (int) (Math.random()*alphabetImages.length));
//                SXRTransform cameraTransform = mScene.getMainCameraRig().getHeadTransform();
//                balloon.getTransform().setRotation(
//                        cameraTransform.getRotationW(),
//                        cameraTransform.getRotationX(),
//                        cameraTransform.getRotationY(),
//                        cameraTransform.getRotationZ());

                balloon.getTransform().rotateByAxis(90.0f, 1, 0, 0);
                return balloon;
            }
        };
        mParticleSystem = new ParticleEmitter(context, mScene, particleCreator);
        mParticleSystem.MaxDistance = 10.0f;
        mParticleSystem.TotalParticles = 20;
        mParticleSystem.EmissionRate = 2;
        mParticleSystem.Velocity = new ParticleEmitter.Range<Float>(1.5f, 2.0f);
        mParticleSystem.EmitterArea = new ParticleEmitter.Range<Vector2f>(new Vector2f(-5.0f, -2.0f), new Vector2f(5.0f, 2.0f));
        particleRoot.getTransform().setRotationByAxis(-90.0f, 1, 0, 0);
        particleRoot.getTransform().setPosition(0, -5.0f, -5.0f);
        particleRoot.attachComponent(mParticleSystem);
        mScene.addNode(particleRoot);

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

    public SXRNode makeObject(SXRContext context, String path) {
        try {
            return context.getAssetLoader().loadModel(path);
        } catch (IOException e) {
            return null;
        }
    }

    private String readFile(String filePath) {
        try {
            SXRAndroidResource res = new SXRAndroidResource(getSXRContext(), filePath);
            InputStream stream = res.getStream();
            byte[] bytes = new byte[stream.available()];
            stream.read(bytes);
            String s = new String(bytes);
            return s;
        } catch (IOException ex) {
            return null;
        }
    }

    public void win()
    {
        mParticleSystem.setEnable(false);
        mScoreBoard.getTransform().setPosition(0, 0, -1.0f);
        mScoreBoard.getCollider().setEnable(true);
        mScoreBoard.setTextSize(10.0f);
        mScoreBoard.setText(mScoreBoard.getTextString() + "\nGood job!");
    }

    public void gameOver()
    {
        mParticleSystem.setEnable(false);
        mScoreBoard.getTransform().setPosition(0, 0, -1.0f);
        mScoreBoard.getCollider().setEnable(true);
        mScoreBoard.setTextSize(10.0f);
        mScoreBoard.setText(mScoreBoard.getTextString() + "\nTap to try again!");
    }

    public void gameStart()
    {
        if (apple1 != null) {
            mScene.removeNode(apple1);
        }
        if (apple2 != null) {
            mScene.removeNode(apple2);
        }
        if (apple3 != null) {
            mScene.removeNode(apple3);
        }

        if (apple == null) {
            apple = makeObject(mContext, "apple.fbx");
            apple.getTransform().setPosition(0.0f, 0.0f, -0.5f).setScale(0.002f, 0.002f, 0.002f);
            mScene.addNode(apple);
        }
        mScoreBoard.getTransform().setPosition(-1.2f, 1.2f, -2.2f);
        mScore = "";
        float s = mScoreBoard.getTextSize();
        mScoreBoard.setTextSize(15.0f);
        mScoreBoard.setText(mScore);
        mScoreBoard.getCollider().setEnable(false);
        mParticleSystem.setEnable(true);
//        Timer timer = new Timer();
//        TimerTask gameOver = new TimerTask()
//        {
//            public void run() { gameOver(); }
//        };
//        long oneMinute = 60 * 1000;
//        timer.schedule(gameOver, oneMinute);
    }

    SXRNode makeBalloon(SXRContext context, Integer index)
    {
        SXRTexture texture = null;

        try {
            SXRAndroidResource res = new SXRAndroidResource(context, alphabetImages[index]);
            texture = context.getAssetLoader().loadTexture(res);
        } catch (IOException ioe) {
            throw new RuntimeException();
        }

        SXRNode balloon = new SXRNode(context, 1.5f, 1.5f, texture);
//        SXRNode balloon = new SXRBillboard(context);
//        balloon.getTransform().setScale(0.5f,0.5f, 0.5f);
//        SXRRenderData rdata = balloon.getRenderData();
//        SXRSphereCollider collider = new SXRSphereCollider(context);
        SXRMeshCollider collider = new SXRMeshCollider(context, false);
//        Random rand = new Random();
//        int mtlIndex = rand.nextInt(mMaterials.size() - 1);
        balloon.setName(alphabetImages[index]);
//        rdata.setAlphaBlend(true);
//        rdata.setMaterial(mMaterials.get(mtlIndex));
//        rdata.setRenderingOrder(SXRRenderingOrder.TRANSPARENT);
//        collider.setRadius(0.8f);
        balloon.attachComponent(collider);
        return balloon;
    }

    SXRNode makeEnvironment(SXRContext context)
    {
        SXRTexture tex = context.getAssetLoader().loadCubemapTexture(new SXRAndroidResource(context, R.raw.lycksele3));
        SXRMaterial material = new SXRMaterial(context, SXRMaterial.SXRShaderType.Cubemap.ID);
        material.setMainTexture(tex);
        SXRSphereNode environment = new SXRSphereNode(context, 18, 36, false, material, 4, 4);
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
    SXRTextViewNode makeScoreboard(SXRContext ctx, SXRNode parent)
    {
        SXRTextViewNode scoreBoard = new SXRTextViewNode(ctx, 2.0f, 1.5f, "000");
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
        SXRNode boardFrame = null;
        try
        {
            boardFrame = ctx.getAssetLoader().loadModel("mirror.3ds");
            SXRNode.BoundingVolume bv = boardFrame.getBoundingVolume();
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
                if (mPickHandler != null && mPickHandler.PickedObject != null)
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
        if (particle != null)
        {
            mParticleSystem.stop(particle);
            mScore += sceneObj.getName().substring(0, 1);
            mScoreBoard.setText(mScore);
            if (mScore.equals("apple") && apple != null) {
                mGoodJobSound.play();
                mScene.removeNode(apple);
                apple = null;

                float scale = 0.008f;
                SXRNode object;
                apple1 = makeObject(mContext, "apple1.fbx");
                apple1.getTransform().setPosition(0.0f, 0.2f, -0.5f).setScale(scale, scale, scale);
                mScene.addNode(apple1);
                apple2 = makeObject(mContext, "apple2.fbx");
                apple2.getTransform().setPosition(0.0f, 0.0f, -0.5f).setScale(scale, scale, scale);
                mScene.addNode(apple2);
                apple3 = makeObject(mContext, "apple3.fbx");
                apple3.getTransform().setPosition(0.0f, -0.2f, -0.5f).setScale(scale, scale, scale);
                mScene.addNode(apple3);
                win();
            } else if (!"apple".startsWith(mScore)) {
                gameOver();
                mWrongSound.play();
            } else {
                mPopSound.play();
            }
        }
        else if (sceneObj == mScoreBoard)
        {
            gameStart();
        }
    }
}
