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
package com.samsung.accessibility.main;

import android.view.MotionEvent;

import com.samsung.accessibility.R;
import com.samsung.accessibility.focus.FocusableController;
import com.samsung.accessibility.focus.FocusableNode;
import com.samsung.accessibility.focus.OnFocusListener;
import com.samsung.accessibility.gaze.GazeCursorNode;
import com.samsung.accessibility.scene.AccessibilityScene;
import com.samsung.accessibility.shortcut.ShortcutMenu;
import com.samsung.accessibility.shortcut.ShortcutMenuItem;
import com.samsung.accessibility.shortcut.ShortcutMenuItem.TypeItem;
import com.samsung.accessibility.util.AccessibilityManager;
import com.samsung.accessibility.util.AccessibilityTexture;

import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRBoxCollider;
import com.samsungxr.SXRContext;
import com.samsungxr.SXREventListeners;
import com.samsungxr.SXRImportSettings;
import com.samsungxr.SXRMain;
import com.samsungxr.SXRMesh;
import com.samsungxr.SXRMeshCollider;
import com.samsungxr.SXRPicker;
import com.samsungxr.SXRRenderData;
import com.samsungxr.SXRScene;
import com.samsungxr.SXRNode;
import com.samsungxr.SXRSphereCollider;
import com.samsungxr.SXRTexture;
import com.samsungxr.ITouchEvents;
import com.samsungxr.accessibility.SXRAccessibilityTalkBack;
import com.samsungxr.io.SXRCursorController;
import com.samsungxr.io.SXRInputManager;
import com.samsungxr.utility.Log;

import java.util.EnumSet;
import java.util.Locale;

public class Main extends SXRMain {
    private int BACKGROUND_TRANSPARENT = SXRRenderData.SXRRenderingOrder.BACKGROUND + 1;
    private int OPAQUE_GEOMETRY = SXRRenderData.SXRRenderingOrder.BACKGROUND + 1;
    private SXRContext sxrContext;

    private GazeCursorNode cursor;

    private FocusableNode trex;

    private FocusableNode bookObject;
    public static AccessibilityScene accessibilityScene;
    public static AccessibilityManager manager;

    private ITouchEvents mTouchHandler = new TouchHandler();
    private SXRCursorController mController = null;
    private SXRNode pickedObject = null;

    /*
     * Handles initializing the selected controller:
     * - add listener for touch events coming from the controller
     * - attach the scene object to represent the cursor
     * - set cursor properties
     * If we are using the Gaze controller, it does not generate touch events directly.
     * We need to listen for them from SXRActivity to process them with a gesture detector.
     */
    private SXRInputManager.ICursorControllerSelectListener controllerSelector = new SXRInputManager.ICursorControllerSelectListener()
    {
        public void onCursorControllerSelected(SXRCursorController newController, SXRCursorController oldController)
        {
            mController = newController;
            newController.addPickEventListener(mTouchHandler);
            newController.setCursor(cursor);
            newController.setCursorDepth(8);
        }
    };

    /*
     * Keeps track of the current picked object.
     * The default for SXRPicker is to pick only the closest object.
     * If that is changed, the logic below is incorrect because
     * it is possible for multiple objects to be picked.
     */
    public class TouchHandler extends SXREventListeners.TouchEvents
    {
        public void onEnter(SXRNode sceneObj, SXRPicker.SXRPickedObject hit)
        {
            if (sceneObj instanceof FocusableNode)
            {
                pickedObject = sceneObj;
                ((FocusableNode) pickedObject).setFocus(true);
            }
        }

        public void onExit(SXRNode sceneObj, SXRPicker.SXRPickedObject hit)
        {
            if (sceneObj == pickedObject)
            {
                if (sceneObj instanceof FocusableNode)
                {
                    ((FocusableNode) sceneObj).setFocus(false);
                }
                pickedObject = null;
            }
        }
    }

    @Override
    public void onInit(final SXRContext sxrContext) {

        this.sxrContext = sxrContext;
        AccessibilityTexture.getInstance(sxrContext);
        cursor = GazeCursorNode.getInstance(sxrContext);
        manager = new AccessibilityManager(sxrContext);

        SXRScene scene = sxrContext.getMainScene();
        final ShortcutMenu shortcutMenu = createShortcut();

        accessibilityScene = new AccessibilityScene(sxrContext, sxrContext.getMainScene(), shortcutMenu);
        createPedestalObject();
        createDinossaur();

        scene.addNode(shortcutMenu);
        scene.addNode(createSkybox());
        sxrContext.getInputManager().selectController(controllerSelector);
    }

    private ShortcutMenu createShortcut() {
        ShortcutMenu shortcutMenu = new ShortcutMenu(sxrContext);
        ShortcutMenuItem shortcuteItem = shortcutMenu.getShortcutItems().get(0);
        shortcuteItem.createIcon(AccessibilityTexture.getInstance(sxrContext).getAccessibilityIcon(), TypeItem.ACCESSIBILITY);
        return shortcutMenu;
    }

    private void createPedestalObject() {
        SXRMesh baseMesh = sxrContext.getAssetLoader().loadMesh(new SXRAndroidResource(sxrContext, R.raw.base));
        SXRMesh bookMesh = sxrContext.getAssetLoader().loadMesh(new SXRAndroidResource(sxrContext, R.raw.book));
        SXRTexture bookTexture = sxrContext.getAssetLoader().loadTexture(new SXRAndroidResource(sxrContext, R.drawable.book));
        SXRTexture baseTexture = sxrContext.getAssetLoader().loadTexture(new SXRAndroidResource(sxrContext, R.drawable.base));

        FocusableNode baseObject = new FocusableNode(sxrContext, baseMesh, baseTexture);
        bookObject = new FocusableNode(sxrContext, bookMesh, bookTexture);
        FocusableNode pedestalObject = new FocusableNode(sxrContext);

        baseObject.getTransform().setScale(0.005f, 0.005f, 0.005f);
        bookObject.getTransform().setScale(0.005f, 0.005f, 0.005f);

        baseObject.getTransform().setPosition(0, -1.6f, -2);
        bookObject.getTransform().setPosition(0, -1.6f, -2);

        /*
         * Force the pedestal to be behind the Dinosaur.
         * Because it uses a transparent texture, if the rendering order
         * starts out as GEOMETRY, SXR will convert it to TRANSPARENT for you.
         * This puts the pedestal in front of the dinosaur. We don't want this
         * so we force it to render before the dinosaur by putting it in the background.
         */
        baseObject.getRenderData().setRenderingOrder(OPAQUE_GEOMETRY);
        bookObject.getRenderData().setRenderingOrder(OPAQUE_GEOMETRY);

        pedestalObject.addChildObject(baseObject);
        pedestalObject.addChildObject(bookObject);

        sxrContext.getMainScene().addNode(pedestalObject);
    }

    private SXRNode createSkybox() {
        SXRMesh mesh = sxrContext.getAssetLoader().loadMesh(new SXRAndroidResource(sxrContext, R.raw.environment_walls_mesh));
        SXRTexture texture = sxrContext.getAssetLoader().loadTexture(new SXRAndroidResource(sxrContext, R.drawable.environment_walls_tex_diffuse));
        SXRNode skybox = new SXRNode(sxrContext, mesh, texture);
        skybox.getTransform().rotateByAxisWithPivot(-90, 1, 0, 0, 0, 0, 0);
        skybox.getTransform().setPositionY(-1.6f);
        skybox.getRenderData().setRenderingOrder(SXRRenderData.SXRRenderingOrder.BACKGROUND);

        SXRMesh meshGround = sxrContext.getAssetLoader().loadMesh(new SXRAndroidResource(sxrContext, R.raw.environment_ground_mesh));
        SXRTexture textureGround = sxrContext.getAssetLoader().loadTexture(new SXRAndroidResource(sxrContext, R.drawable.environment_ground_tex_diffuse));
        SXRNode skyboxGround = new SXRNode(sxrContext, meshGround, textureGround);
        skyboxGround.getRenderData().setRenderingOrder(SXRRenderData.SXRRenderingOrder.BACKGROUND);

        SXRMesh meshFx = sxrContext.getAssetLoader().loadMesh(new SXRAndroidResource(sxrContext, R.raw.windows_fx_mesh));
        SXRTexture textureFx = sxrContext.getAssetLoader().loadTexture(new SXRAndroidResource(sxrContext, R.drawable.windows_fx_tex_diffuse));
        SXRNode skyboxFx = new SXRNode(sxrContext, meshFx, textureFx);
        skyboxFx.getRenderData().setRenderingOrder(BACKGROUND_TRANSPARENT);
        skyboxFx.getRenderData().setAlphaBlend(true);
        skybox.addChildObject(skyboxFx);
        skybox.addChildObject(skyboxGround);
        return skybox;
    }

    private void createDinossaur() {

        EnumSet<SXRImportSettings> additionalSettings = EnumSet
                .of(SXRImportSettings.CALCULATE_SMOOTH_NORMALS);

        EnumSet<SXRImportSettings> settings = SXRImportSettings
                .getRecommendedSettingsWith(additionalSettings);

        SXRMesh baseMesh = sxrContext.getAssetLoader().loadMesh(new SXRAndroidResource(sxrContext, R.raw.trex_mesh), settings);
        SXRTexture baseTexture = sxrContext.getAssetLoader().loadTexture(new SXRAndroidResource(sxrContext, R.drawable.trex_tex_diffuse));
        trex = new FocusableNode(sxrContext, baseMesh, baseTexture);
        trex.getTransform().setPosition(0, -0.3f, -1);
        trex.getTransform().setScale(0.2f, 0.2f, 0.2f);
        trex.getTransform().rotateByAxis(-90, 1, 0, 0);
        trex.getTransform().rotateByAxis(90, 0, 1, 0);
        activeTalkBack();
        sxrContext.getMainScene().addNode(trex);
    }

    public void setScene(SXRScene scene)
    {
        mController.setScene(scene);
        sxrContext.setMainScene(scene);
    }

    @Override
    public void onStep() {
    }


    private void activeTalkBack() {
        SXRAccessibilityTalkBack talkBackDinossaur = new SXRAccessibilityTalkBack(Locale.US, sxrContext.getContext(), "Dinossaur");
        trex.setTalkBack(talkBackDinossaur);
        trex.attachComponent(new SXRBoxCollider(sxrContext));
        trex.setOnFocusListener(new OnFocusListener() {

            @Override
            public void lostFocus(FocusableNode object) {
            }

            @Override
            public void inFocus(FocusableNode object) {
            }

            @Override
            public void gainedFocus(FocusableNode object) {
                trex.getTalkBack().speak();
            }
        });

        SXRAccessibilityTalkBack talkBackBook = new SXRAccessibilityTalkBack(Locale.US, sxrContext.getContext(), "Book");
        bookObject.setTalkBack(talkBackBook);
        bookObject.attachComponent(new SXRMeshCollider(sxrContext, true));
        bookObject.setOnFocusListener(new OnFocusListener() {

            @Override
            public void lostFocus(FocusableNode object) {
            }

            @Override
            public void inFocus(FocusableNode object) {
            }

            @Override
            public void gainedFocus(FocusableNode object) {
                bookObject.getTalkBack().speak();
            }
        });
    }

    @Override
    public void onSingleTapUp(MotionEvent e) {
        FocusableController.clickProcess(pickedObject);
    }
}
