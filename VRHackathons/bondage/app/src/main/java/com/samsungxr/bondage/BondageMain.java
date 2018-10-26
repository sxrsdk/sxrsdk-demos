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

package com.samsungxr.bondage;

import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRCollider;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRDirectLight;
import com.samsungxr.SXRMaterial;
import com.samsungxr.SXRPointLight;
import com.samsungxr.SXRScene;
import com.samsungxr.SXRNode;
import com.samsungxr.SXRMain;
import com.samsungxr.SXRRenderData;
import com.samsungxr.SXRTexture;
import com.samsungxr.SXRTransform;
import com.samsungxr.nodes.SXRSphereNode;

import android.media.AudioManager;
import android.media.SoundPool;
import android.view.MotionEvent;
import com.samsungxr.SXRPicker;
import com.samsungxr.IPickEvents;
import com.samsungxr.SXRPicker.SXRPickedObject;
import com.samsungxr.utility.Log;
import org.joml.Vector3f;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.Future;

public class BondageMain extends SXRMain {

    public class GridPicker implements IPickEvents
    {
        public SXRNode   PickedObject = null;

        public void onEnter(SXRNode sceneObj, SXRPicker.SXRPickedObject pickInfo)
        {
            SXRNode parent = sceneObj.getParent();
            if ((parent != null) && (parent.getParent() == mElementGrid))
            {
                parent.getTransform().setScale(1.2f, 1.2f, 1.2f);
            }
        }
        public void onExit(SXRNode sceneObj)
        {
            SXRNode parent = sceneObj.getParent();
            if ((parent != null) && (parent.getParent() == mElementGrid))
            {
                parent.getTransform().setScale(1.0f, 1.0f, 1.0f);
            }
        }
        public void onInside(SXRNode sceneObj, SXRPicker.SXRPickedObject pickInfo) { }
        public void onNoPick(SXRPicker picker)
        {
            PickedObject = null;
        }
        public void onPick(SXRPicker picker)
        {
            for (SXRPickedObject picked : picker.getPicked())
            {
                SXRNode parent = picked.hitObject.getParent();
                if (parent.getParent() == mElementGrid)
                {
                    PickedObject = picked.hitObject;
                    return;
                }
            }
        }
    }

    private SXRScene mScene = null;
    private GridPicker mPickHandler;
    private SXRNode mElementGrid = null;
    private SXRNode mMolecule = null;
    private SXRNode mHeadTracker;
    private ElementCursor mCursor = null;
    private BondAnimator mBondAnimator = null;
    private int mNumMatched = 0;
    private float mRotAngle = 0;
    private HashMap<String, String> mMoleculeMap = new HashMap<String, String>();

    @Override
    public void onInit(SXRContext context)
    {
        /*
         * Set the background color
         */
        mScene = context.getNextMainScene();
        mScene.getMainCameraRig().getLeftCamera().setBackgroundColor(1.0f, 1.0f, 1.0f, 1.0f);
        mScene.getMainCameraRig().getRightCamera().setBackgroundColor(1.0f, 1.0f, 1.0f, 1.0f);

        /*
         * Set up a head-tracking pointer
         */
        mHeadTracker = new SXRNode(context,
                context.createQuad(0.1f, 0.1f),
                context.loadTexture(new SXRAndroidResource(context, R.drawable.headtrackingpointer)));
        mHeadTracker.getTransform().setPosition(0.0f, 0.0f, -1.0f);
        mHeadTracker.getRenderData().setDepthTest(false);
        mHeadTracker.getRenderData().setRenderingOrder(SXRRenderData.SXRRenderingOrder.OVERLAY);
        mScene.getMainCameraRig().addChildObject(mHeadTracker);
        /*
         * Add the environment and the molecule
         */;
        loadSounds(context);
        makeEnvironment(context);
        mMolecule = loadMolecule(context, "c2h4.obj", "C2H4");
        /*
         * Respond to picking events
         */
        mScene.getMainCameraRig().getOwnerObject().attachComponent(new SXRPicker(context, mScene));
        mPickHandler = new GridPicker();
        mScene.getEventReceiver().addListener(mPickHandler);
    }
    
    SXRNode loadMolecule(SXRContext ctx, String fileName, String moleculeName)
    {
        if (mBondAnimator != null)
        {
            mScene.getEventReceiver().removeListener(mBondAnimator);
        }
        try
        {
            SXRNode modelRoot = ctx.getAssetLoader().loadModel(fileName, mScene);
            SXRNode.BoundingVolume bv = modelRoot.getBoundingVolume();
            SXRTransform trans = modelRoot.getTransform();
            mBondAnimator = new BondAnimator(ctx, mMoleculeMap, mGoodSound, mBadSound);

            trans.setScale(0.1f, 0.1f, 0.1f);
            trans.setPositionZ(-1.0f);
            mBondAnimator.setEnable(false);
            modelRoot.attachComponent(mBondAnimator);
            if (mElementGrid != null)
            {
                mScene.removeNode(mElementGrid);
            }
            mElementGrid = makeElementGrid(ctx, modelRoot);
            mScene.addNode(mElementGrid);
            mScene.getEventReceiver().addListener(mBondAnimator);
            mNumMatched = 0;
            makeMoleculeMap(fileName);
            return modelRoot;
        }
        catch (IOException ex)
        {
            Log.e("bondage", "Cannot load file " + ex.getMessage());
            return null;
        }
    }

    SXRNode makeEnvironment(SXRContext context)
    {
        SXRNode environment;
        try
        {
            environment = context.getAssetLoader().loadModel("playarea.obj", mScene);
            SXRDirectLight light = new SXRDirectLight(context);
            light.setAmbientIntensity(0.4f, 0.4f, 0.4f, 1.0f);
            light.setDiffuseIntensity(0.6f, 0.6f, 0.6f, 1.0f);
            environment.attachComponent(light);
            return environment;
        }
        catch (IOException ex)
        {
            Log.e("bondage", ex.getMessage());
            SXRTexture tex = context.loadTexture(new SXRAndroidResource(context, R.drawable.gearvrf));
            environment = new SXRNode(context, 4, 4, tex);
            SXRNode lightObj = new SXRNode(context);
            SXRDirectLight light = new SXRDirectLight(context);
            light.setAmbientIntensity(0.1f, 0.1f, 0.1f, 1.0f);
            light.setDiffuseIntensity(0.4f, 0.4f, 0.4f, 1.0f);
            lightObj.attachComponent(light);
            lightObj.getTransform().setPosition(0, 1.0f, 1.0f);
            environment.getTransform().setPositionZ(-3.0f);
            environment.addChildObject(lightObj);
            mScene.addNode(environment);
            return environment;
        }
    }

    private SXRNode makeElementGrid(SXRContext ctx, SXRNode srcRoot)
    {
        SXRNode gridRoot = new SXRNode(ctx);
        SXRTransform trans = gridRoot.getTransform();
        float sf = 0.15f;
        ElementGrid elementGrid = new ElementGrid(ctx);

        gridRoot.setName("ElementGrid");
        trans.setScale(sf, sf, sf);
        trans.setPosition(-0.8f, 0.80f, -1.0f);
        gridRoot.attachComponent(elementGrid);
        elementGrid.makeGrid(srcRoot);
        return gridRoot;
    }

    @Override
    public void onStep()
    {
        FPSCounter.tick();
    }

    public void onTouchEvent(MotionEvent event)
    {
        switch (event.getAction() & MotionEvent.ACTION_MASK)
        {
            case MotionEvent.ACTION_DOWN:
                if ((mPickHandler != null) && (mPickHandler.PickedObject != null))
                {
                    onHitGrid(mPickHandler.PickedObject);
                    return;
                }
                if (mBondAnimator != null)
                {
                    SXRNode target = mBondAnimator.getTarget();
                    mBondAnimator.onTouch();
                    if (mBondAnimator.WrongAnswer && (target != null))
                    {
                        attachToGrid(target);
                    }
                    return;
                }
                break;

            default:
                break;
        }
        if (mCursor != null)
        {
            mCursor.onTouchEvent(event);
        }
    }

    private void onHitGrid(SXRNode sceneObj)
    {
        SXRNode parent = sceneObj.getParent();
        if ((parent != null) && (parent.getParent() == mElementGrid))
        {
            if (mNumMatched == 0)
            {
                String elemName = BondAnimator.getElementName(sceneObj);
                NameMatcher matcher = new NameMatcher(elemName);
                Log.d("bondage", "Match " + elemName);
                matcher.Match = null;
                mMolecule.forAllDescendants(matcher);
                SXRNode match = matcher.Match;
                if (match != null)
                {
                    String name = match.getName();
                    SXRRenderData rdata = match.getRenderData();
                    if (rdata != null)
                    {
                        rdata.setEnable(true);
                        sceneObj.setEnable(false);
                        ++mNumMatched;
                    }
                }
                return;
            }
            attachToCursor(sceneObj);
        }
    }

    private void attachToCursor(SXRNode elemObj)
    {
        float sf = 0.1f;
        SXRNode parent = elemObj.getParent();
        elemObj.getTransform().setScale(sf, sf, sf);

        parent.getParent().removeChildObject(elemObj);
        elemObj.getTransform().setPosition(0, 0, 0);
        mHeadTracker.addChildObject(elemObj);
        elemObj.getComponent(SXRCollider.getComponentType()).setEnable(false);
        mBondAnimator.setTarget(elemObj);
        mBondAnimator.setEnable(true);
    }

    private void attachToGrid(SXRNode elemObj)
    {
        ElementGrid grid = (ElementGrid) mElementGrid.getComponent(ElementGrid.getComponentType());
        mHeadTracker.removeChildObject(elemObj);
        grid.addToGrid(elemObj);
        elemObj.getComponent(SXRCollider.getComponentType()).setEnable(true);
        mBondAnimator.setEnable(false);
        mCursor = null;
    }

    private void makeMoleculeMap(String name)
    {
        mMoleculeMap.clear();
        if (name.equals("c2h4.obj"))
        {
            mMoleculeMap.put("c1", "h1_Sphere.003 h2_Sphere.001 c2_Sphere.002");
            mMoleculeMap.put("c2", "h4_Sphere.004 h3_Sphere.005 c1_Sphere");
            mMoleculeMap.put("h1", "c1_Sphere");
            mMoleculeMap.put("h2", "c1_Sphere");
            mMoleculeMap.put("h3", "c2_Sphere.002");
            mMoleculeMap.put("h4", "c2_Sphere.002");
        }
    }

    private SoundPool   mAudioEngine;
    private SoundEffect mGoodSound;
    private SoundEffect mBadSound;

    private void loadSounds(SXRContext context)
    {
        mAudioEngine = new SoundPool(4, AudioManager.STREAM_MUSIC, 0);
        try
        {
            mGoodSound = new SoundEffect(context, mAudioEngine, "cashreg.wav", false);
            mGoodSound.setVolume(0.6f);
            mBadSound = new SoundEffect(context, mAudioEngine, "hammer.wav", false);
            mBadSound.setVolume(0.6f);
        }
        catch (IOException ex)
        {
            Log.e("Audio", "Cannot load pop.wav");
        }
    }
}
