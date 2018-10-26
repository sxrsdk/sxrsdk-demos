/*
 * Copyright 2015 Samsung Electronics Co., LTD
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package com.samsung.accessibility.scene;

import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRMaterial;
import com.samsungxr.SXRMesh;
import com.samsungxr.SXRMeshCollider;
import com.samsungxr.SXRScene;
import com.samsungxr.SXRNode;
import com.samsungxr.SXRShaderId;
import com.samsungxr.SXRSphereCollider;
import com.samsungxr.SXRTexture;
import com.samsungxr.accessibility.SXRAccessiblityObject;

import com.samsung.accessibility.R;
import com.samsung.accessibility.focus.OnClickListener;
import com.samsung.accessibility.main.Main;
import com.samsung.accessibility.shortcut.ShortcutMenu;
import com.samsung.accessibility.shortcut.ShortcutMenuItem;
import com.samsung.accessibility.shortcut.ShortcutMenuItem.TypeItem;
import com.samsung.accessibility.util.AccessibilitySceneShader;
import com.samsung.accessibility.util.AccessibilityTexture;

public class AccessibilityScene extends SXRScene {

    private SXRContext sxrContext;
    private SXRScene mainApplicationScene;
    private ShortcutMenu shortcutMenu;
    private AccessibilityTexture textures;

    public AccessibilityScene(SXRContext sxrContext, SXRScene mainApplicationScene, ShortcutMenu shortcutMenu) {
        super(sxrContext);
        this.sxrContext = sxrContext;
        textures = AccessibilityTexture.getInstance(sxrContext);
        this.mainApplicationScene = mainApplicationScene;
        this.shortcutMenu = shortcutMenu;

        SXRNode skyBox = createSkybox();
        applyShaderOnSkyBox(skyBox);
        addNode(skyBox);
        createItems();
    }

    private SXRNode createSkybox() {
        SXRMesh mesh = sxrContext.getAssetLoader().loadMesh(new SXRAndroidResource(sxrContext, R.raw.environment_walls_mesh));
        SXRTexture texture = sxrContext.getAssetLoader().loadTexture(new SXRAndroidResource(sxrContext, R.drawable.environment_walls_tex_diffuse));
        SXRNode skybox = new SXRNode(sxrContext, mesh, texture);
        skybox.getTransform().rotateByAxisWithPivot(-90, 1, 0, 0, 0, 0, 0);
        skybox.getTransform().setPositionY(-1.6f);
        skybox.getRenderData().setRenderingOrder(0);

        SXRMesh meshGround = sxrContext.getAssetLoader().loadMesh(new SXRAndroidResource(sxrContext, R.raw.environment_ground_mesh));
        SXRTexture textureGround = sxrContext.getAssetLoader().loadTexture(new SXRAndroidResource(sxrContext, R.drawable.environment_ground_tex_diffuse));
        SXRNode skyboxGround = new SXRNode(sxrContext, meshGround, textureGround);
        skyboxGround.getRenderData().setRenderingOrder(0);

        SXRMesh meshFx = sxrContext.getAssetLoader().loadMesh(new SXRAndroidResource(sxrContext, R.raw.windows_fx_mesh));
        SXRTexture textureFx = sxrContext.getAssetLoader().loadTexture(new SXRAndroidResource(sxrContext, R.drawable.windows_fx_tex_diffuse));
        SXRNode skyboxFx = new SXRNode(sxrContext, meshFx, textureFx);
        skyboxGround.getRenderData().setRenderingOrder(0);
        skybox.addChildObject(skyboxFx);
        skybox.addChildObject(skyboxGround);
        return skybox;
    }

    private void createItems() {
        float positionX = 0f;
        float positionY = -1f;
        float positionZ = -10f;
        float scale = 0.03f;
        SXRMesh mesh = getSXRContext().getAssetLoader().loadMesh(new SXRAndroidResource(getSXRContext(), R.raw.accessibility_item));
        final SceneItem invertedColors = new SceneItem(getSXRContext(), mesh, getSXRContext()
                .getAssetLoader().loadTexture(new SXRAndroidResource(getSXRContext(), R.drawable.inverted_colors)));
        invertedColors.getTransform().setPosition(positionX, positionY, positionZ);
        invertedColors.getTransform().setScale(scale, scale, scale);
        invertedColors.attachComponent(new SXRMeshCollider(sxrContext, null, true));
        invertedColors.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick() {
                invertedColors.animate();
                if (invertedColors.isActive) {
                    shortcutMenu.addShortcut(TypeItem.INVERTED_COLORS, textures.getInvertedColorsIcon());
                } else {
                    ShortcutMenuItem shortcut = shortcutMenu.removeShortcut(TypeItem.INVERTED_COLORS);
                    if (shortcut != null) {
                        shortcut.resetClick();
                        Main.manager.getInvertedColors().turnOff(Main.accessibilityScene.getMainApplicationScene());
                        Main.manager.getInvertedColors().turnOff(Main.accessibilityScene);
                     }
                }
            }
        });
        this.addNode(invertedColors);

        final SceneItem zoom = new SceneItem(getSXRContext(), mesh, getSXRContext()
                .getAssetLoader().loadTexture(new SXRAndroidResource(getSXRContext(), R.drawable.zoom)));
        zoom.getTransform().setPosition(positionX, positionY, positionZ);
        zoom.getTransform().setScale(scale, scale, scale);
        zoom.attachComponent(new SXRSphereCollider(sxrContext));
        zoom.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick() {
                zoom.animate();
                if (zoom.isActive) {
                    shortcutMenu.addShortcut(TypeItem.ZOOM, textures.getZoomOut());
                    shortcutMenu.addShortcut(TypeItem.ZOOM, textures.getZoomIn());
                } else {
                    shortcutMenu.removeShortcut(TypeItem.ZOOM);
                }
            }
        });
        this.addNode(zoom);

        final SceneItem talkBack = new SceneItem(getSXRContext(), mesh, getSXRContext()
                .getAssetLoader().loadTexture(new SXRAndroidResource(getSXRContext(), R.drawable.talk_back)));
        talkBack.getTransform().setPosition(positionX, positionY, positionZ);
        talkBack.getTransform().setScale(scale, scale, scale);
        talkBack.attachComponent(new SXRSphereCollider(sxrContext));
        talkBack.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick() {
                talkBack.animate();
                setActivityOrInactiveTalkBackObjects(talkBack.isActive);
                if (talkBack.isActive) {
                    shortcutMenu.addShortcut(TypeItem.TALK_BACK, textures.getTalkBackLess());
                    shortcutMenu.addShortcut(TypeItem.TALK_BACK, textures.getTalkBackMore());
                } else {
                    shortcutMenu.removeShortcut(TypeItem.TALK_BACK);
                }
            }
        });

        this.addNode(talkBack);

        final SceneItem speech = new SceneItem(getSXRContext(), mesh, getSXRContext()
                .getAssetLoader().loadTexture(new SXRAndroidResource(getSXRContext(), R.drawable.speech)));
        speech.getTransform().setPosition(positionX, positionY, positionZ);
        speech.getTransform().setScale(scale, scale, scale);
        speech.attachComponent(new SXRSphereCollider(sxrContext));
        speech.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick() {
                speech.animate();
                if (speech.isActive) {
                    shortcutMenu.addShortcut(TypeItem.SPEECH, textures.getSpeechIcon());
                } else {
                    shortcutMenu.removeShortcut(TypeItem.SPEECH);
                }
            }
        });

        this.addNode(speech);
        float angle = -20;
        invertedColors.getTransform().rotateByAxisWithPivot(-1 * angle, 0, 1, 0, 0, 0, 0);
        zoom.getTransform().rotateByAxisWithPivot(0 * angle, 0, 1, 0, 0, 0, 0);
        talkBack.getTransform().rotateByAxisWithPivot(1 * angle, 0, 1, 0, 0, 0, 0);
        speech.getTransform().rotateByAxisWithPivot(2 * angle, 0, 1, 0, 0, 0, 0);

    }

    private void applyShaderOnSkyBox(SXRNode skyBox) {
        applyShader(skyBox);
        for (SXRNode object : skyBox.getChildren()) {
            applyShader(object);
        }
    }

    private void applyShader(SXRNode object) {
        if (object != null && object.getRenderData() != null && object.getRenderData().getMaterial() != null) {

            SXRMaterial shader = new SXRMaterial(sxrContext, new SXRShaderId(AccessibilitySceneShader.class));
            SXRTexture texture = object.getRenderData().getMaterial().getMainTexture();

            object.getRenderData().setMaterial(shader);
            object.getRenderData().getMaterial().setFloat(AccessibilitySceneShader.BLUR_INTENSITY, 1);
            object.getRenderData().getMaterial().setTexture(AccessibilitySceneShader.TEXTURE_KEY,
                    texture);
        }
    }

    public SXRScene getMainApplicationScene() {
        return mainApplicationScene;
    }

    public ShortcutMenu getShortcutMenu() {
        return shortcutMenu;
    }

    private void setActivityOrInactiveTalkBackObjects(boolean active) {
        for (SXRNode object : mainApplicationScene.getWholeNodes()) {
            if (object instanceof SXRAccessiblityObject && ((SXRAccessiblityObject) object).getTalkBack() != null) {
                ((SXRAccessiblityObject) object).getTalkBack().setActive(active);
            }
        }
    }

    public void show() {
        Main main = (Main) sxrContext.getMain();

        main.setScene(this);
        mainApplicationScene.removeNode(shortcutMenu);
        addNode(shortcutMenu);
    }

}
