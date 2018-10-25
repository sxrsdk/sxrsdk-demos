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

package com.samsungxr.immersivepedia.scene;

import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRComponent;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRMesh;
import com.samsungxr.SXRRenderData;
import com.samsungxr.SXRScene;
import com.samsungxr.SXRSceneObject;
import com.samsungxr.SXRTexture;
import com.samsungxr.SXRTextureParameters;
import com.samsungxr.SXRTextureParameters.TextureWrapType;
import com.samsungxr.animation.SXRAnimation;
import com.samsungxr.animation.SXROnFinish;
import com.samsungxr.animation.SXROpacityAnimation;
import com.samsungxr.immersivepedia.Main;
import com.samsungxr.immersivepedia.R;
import com.samsungxr.immersivepedia.focus.OnClickListener;
import com.samsungxr.immersivepedia.util.AudioClip;

public class MenuScene extends SXRScene {

    public static final float DISTANCE_TO_CAMERA = 6.0f;

    private static final float Y_ADJUST = 2f;

    public static final float CAMERA_Y = 1.6f;

    private int totalRenderObject = 0;
    private int finishCounter = 0;

    public MenuScene(SXRContext gvrContext) {
        super(gvrContext);
        createDinosaursMenuItem();
        createBirdsMenuItem();
        createFishesMenuItem();
        createMammalsMenuItem();
        addSceneObject(createSkybox()); //

        addSceneObject(createBlueSkybox()); //

        getMainCameraRig().getTransform().setPositionY(CAMERA_Y);
    }

    private SXROnFinish onAfterHide = new SXROnFinish()
    {
        @Override
        public void finished(SXRAnimation arg0)
        {
            finishCounter++;
            if (finishCounter == totalRenderObject)
            {
                totalRenderObject = 0;
                finishCounter = 0;
                Main main = (Main) getSXRContext().getMain();
                main.setMainScene(Main.dinosaurScene);
                Main.dinosaurScene.show();
            }
        }
    };

    private SXRSceneObject.ComponentVisitor hideAll = new SXRSceneObject.ComponentVisitor()
    {
        @Override
        public boolean visit(SXRComponent comp)
        {
            SXRRenderData rd = (SXRRenderData) comp;
            SXRSceneObject owner = rd.getOwnerObject();
            try
            {
                new SXROpacityAnimation(owner, 1f, 0f).start(getSXRContext().getAnimationEngine()).setOnFinish(onAfterHide);
                totalRenderObject++;
            }
            catch (UnsupportedOperationException ex)
            {
                // shader doesn't support opacity
            }
            return true;
        }
    };

    private void createDinosaursMenuItem() {
        MenuItem dinosaurs = new MenuItem(getSXRContext(), R.drawable.dinosaurs_front_idle, R.drawable.dinosaurs_front_hover,
                R.drawable.dinosaurs_back_idle, R.drawable.dinosaurs_back_hover);
        dinosaurs.getTransform().setPositionZ(-DISTANCE_TO_CAMERA);
        dinosaurs.getTransform().setPositionY(Y_ADJUST);
        dinosaurs.setTexts(getSXRContext().getContext().getString(R.string.dinosaurs), getSXRContext().getContext().getString(R.string.empty));
        dinosaurs.setName("menu_dinosaurs");
        dinosaurs.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick() {
                AudioClip.getInstance(getSXRContext().getContext()).playSound(AudioClip.getUIMenuSelectSoundID(), 1.0f, 1.0f);
                getRoot().forAllComponents(hideAll, SXRRenderData.getComponentType());
            }
        });
        addSceneObject(dinosaurs);
    }

    private void createBirdsMenuItem() {
        MenuItem birds = new MenuItem(getSXRContext(), R.drawable.birds_front_idle, R.drawable.birds_front_hover,
                R.drawable.birds_back_idle, R.drawable.birds_back_hover);
        birds.getTransform().setPositionX(DISTANCE_TO_CAMERA);
        birds.getTransform().setPositionY(Y_ADJUST);
        birds.getTransform().rotateByAxis(-90.0f, 0f, 1f, 0f);
        birds.setTexts(getSXRContext().getContext().getString(R.string.birds), getSXRContext().getContext().getString(R.string.unavailable));
        birds.setOnClickListener(getUnavailableMenuItemClick());
        birds.setName("menu_birds");
        addSceneObject(birds);
    }

    private void createFishesMenuItem() {
        MenuItem fishes = new MenuItem(getSXRContext(), R.drawable.fishes_front_idle, R.drawable.fishes_front_hover,
                R.drawable.fishes_back_idle, R.drawable.fishes_back_hover);
        fishes.getTransform().setPositionZ(DISTANCE_TO_CAMERA);
        fishes.getTransform().rotateByAxis(180.0f, 0f, 1f, 0f);
        fishes.setTexts(getSXRContext().getContext().getString(R.string.fishes), getSXRContext().getContext().getString(R.string.unavailable));
        fishes.setOnClickListener(getUnavailableMenuItemClick());
        fishes.getTransform().setPositionY(Y_ADJUST);
        fishes.setName("menu_fishes");
        addSceneObject(fishes);
    }

    private void createMammalsMenuItem() {
        MenuItem mammals = new MenuItem(getSXRContext(), R.drawable.mammals_front_idle, R.drawable.mammals_front_hover,
                R.drawable.mammals_back_idle, R.drawable.mammals_back_hover);
        mammals.getTransform().setPositionX(-DISTANCE_TO_CAMERA);
        mammals.getTransform().rotateByAxis(90.0f, 0f, 1f, 0f);
        mammals.setTexts(getSXRContext().getContext().getString(R.string.mammals), getSXRContext().getContext().getString(R.string.unavailable));
        mammals.setOnClickListener(getUnavailableMenuItemClick());
        mammals.getTransform().setPositionY(Y_ADJUST);
        mammals.setName("menu_mammals");
        addSceneObject(mammals);
    }

    private OnClickListener getUnavailableMenuItemClick() {
        return new OnClickListener() {

            @Override
            public void onClick() {
                AudioClip.getInstance(getSXRContext().getContext()).playSound(AudioClip.getUIMenuSelectWrongSoundID(), 1.0f, 1.0f);
            }
        };
    }

    private SXRSceneObject createSkybox() {

        SXRMesh mesh = getSXRContext().getAssetLoader().loadMesh(new SXRAndroidResource(getSXRContext(), R.raw.environment_walls_mesh));
        SXRTexture texture = getSXRContext().getAssetLoader().loadTexture(new SXRAndroidResource(getSXRContext(), R.drawable.menu_walls_tex_diffuse));
        SXRSceneObject skybox = new SXRSceneObject(getSXRContext(), mesh, texture);
        skybox.getTransform().rotateByAxisWithPivot(-90, 1, 0, 0, 0, 0, 0);
        skybox.getRenderData().setRenderingOrder(0);

        SXRMesh meshGround = getSXRContext().getAssetLoader().loadMesh(new SXRAndroidResource(getSXRContext(), R.raw.environment_ground_mesh));
        SXRTexture textureGround = getSXRContext().getAssetLoader().loadTexture(new SXRAndroidResource(getSXRContext(), R.drawable.menu_ground_tex_diffuse));
        SXRSceneObject skyboxGround = new SXRSceneObject(getSXRContext(), meshGround, textureGround);
        skyboxGround.getRenderData().setRenderingOrder(0);

        skybox.addChildObject(skyboxGround);
        return skybox;
    }

    private SXRSceneObject createBlueSkybox() {

        SXRMesh mesh = getSXRContext().getAssetLoader().loadMesh(new SXRAndroidResource(getSXRContext(), R.raw.skybox_mesh));
        SXRTextureParameters textureParameters = new SXRTextureParameters(getSXRContext());
        textureParameters.setWrapSType(TextureWrapType.GL_REPEAT);
        textureParameters.setWrapTType(TextureWrapType.GL_REPEAT);

        SXRTexture texture = getSXRContext().getAssetLoader().loadTexture(new SXRAndroidResource(getSXRContext(), R.drawable.starfield_tex_diffuse),
                textureParameters);
        SXRSceneObject skybox = new SXRSceneObject(getSXRContext(), mesh, texture);
        skybox.getTransform().setScale(1, 1, 1);
        skybox.getRenderData().setRenderingOrder(0);
        return skybox;
    }

}
