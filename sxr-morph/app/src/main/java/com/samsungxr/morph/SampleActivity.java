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

package com.samsungxr.morph;

import android.graphics.Color;
import android.os.Bundle;

import com.samsungxr.SXRActivity;
import com.samsungxr.SXRAssetLoader;
import com.samsungxr.SXRCameraRig;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRImportSettings;
import com.samsungxr.SXRMain;
import com.samsungxr.SXRMeshMorph;
import com.samsungxr.SXRPointLight;
import com.samsungxr.SXRScene;
import com.samsungxr.SXRNode;
import com.samsungxr.SXRVertexBuffer;
import com.samsungxr.animation.SXRAnimator;
import com.samsungxr.animation.SXRMorphAnimation;
import com.samsungxr.animation.SXRRepeatMode;
import org.joml.Vector3f;

import java.io.IOException;
import java.util.EnumSet;
import java.util.List;

public class SampleActivity extends SXRActivity {

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setMain(new SampleMain());
    }

    private  SXRNode mObjectRoot;
    private  int animDuration = 50;
    private SXRScene mScene = null;

    private class SampleMain extends SXRMain
    {
        @Override
        public void onInit(SXRContext sxrContext)
        {
            SXRScene scene = sxrContext.getMainScene();
            mScene = scene;

            SXRCameraRig rig = scene.getMainCameraRig();
            mObjectRoot = new SXRNode(sxrContext);
            rig.getCenterCamera().setBackgroundColor(Color.BLACK);
            rig.getLeftCamera().setBackgroundColor(Color.BLACK);
            rig.getRightCamera().setBackgroundColor(Color.BLACK);

            String filePath = "/sloth/sloth.gltf";

            SXRNode light1 = createLight(sxrContext, 1, 1, 1, new Vector3f(0,1.8f, 0));
            SXRNode light2 = createLight(sxrContext ,1, 1, 1, new Vector3f(0,-0.8f, 0));

            mObjectRoot.addChildObject(light1);
            mObjectRoot.addChildObject(light2);

            try
            {
                addModeltoScene(filePath, new Vector3f(0.05f,0.05f,0.05f),
                        new Vector3f(0, -2, -10));
            }
            catch (IOException ex)
            {
            }

            scene.addNode(mObjectRoot);

            SXRAnimator animator = setupAnimation(mObjectRoot);
            animator.start(getSXRContext().getAnimationEngine());
        }

        private SXRAnimator setupAnimation(SXRNode root)
        {

            SXRNode baseObject = mObjectRoot.getNodeByName("Sloth_face");
            SXRMeshMorph morph = (SXRMeshMorph)baseObject.getComponent(SXRMeshMorph.getComponentType());
            int numBlendShapes = morph.getBlendShapeCount();

            float [] keys = generateAnimationKeys(numBlendShapes, animDuration);

            SXRAnimator animator = (SXRAnimator) root.getComponent(SXRAnimator.getComponentType());
            if (animator == null)
            {
                animator = new SXRAnimator(root.getSXRContext());
                root.attachComponent(animator);
            }

            SXRMorphAnimation morphAnim = new SXRMorphAnimation(morph, keys, numBlendShapes + 1);
            animator.addAnimation(morphAnim);
            animator.setRepeatMode(SXRRepeatMode.PINGPONG);
            animator.setRepeatCount(1000);
            return animator;
        }


        /*
        create animation keys in the format:
        t1, 0, 0, 0, 0, .....0, 0, 0
        t2, 0, 1, 0, 0, .....0, 0, 0
        t3, 0, 0, 1, 0, .....0, 0, 0
        t4, 0, 0, 0, 1, .....0, 0, 0
        .
        .
        t1, t2, ... tn are timestamps in the range [0,animDuration]
         */
        private float[] generateAnimationKeys(int numBlendShapes, int timeTicks)
        {
            timeTicks ++;
            int keyArraySize = numBlendShapes * timeTicks + timeTicks;
            float [] keys = new float[keyArraySize];
            int timeCounter = 0;
            for(int i = 0; i < keyArraySize; i += (numBlendShapes + 1) )
            {
                keys[i] = timeCounter;
                for(int j = i + 1; j <= i + numBlendShapes; j ++)
                    keys[j] = (timeCounter == (j % (numBlendShapes + 1)) ) ? 1 : 0;
                timeCounter ++;
            }
            return keys;
        }

        private void addModeltoScene(String filePath, Vector3f scale, Vector3f position) throws IOException {

            SXRAssetLoader loader = getSXRContext().getAssetLoader();
            SXRNode root = loader.loadModel(filePath,SXRImportSettings.getRecommendedMorphSettings(), false, null);
            root.getTransform().setScale(scale.x,scale.y,scale.z);
            root.getTransform().setPosition(position.x, position.y, position.z);

            mObjectRoot.addChildObject(root);

        }

        private SXRNode createLight(SXRContext context, float r, float g, float b, Vector3f position)
        {
            SXRNode lightNode = new SXRNode(context);
            SXRPointLight light = new SXRPointLight(context);

            lightNode.attachLight(light);
            lightNode.getTransform().setPosition(0, 0.5f, 0);
            light.setAmbientIntensity(0.7f * r, 0.7f * g, 0.7f * b, 1);
            light.setDiffuseIntensity(r , g , b , 1);
            light.setSpecularIntensity(r, g, b, 1);

            lightNode.getTransform().setPosition(position.x,position.y,position.z);

            return lightNode;
        }

    }
}