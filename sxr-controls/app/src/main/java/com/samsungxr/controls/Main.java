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

package com.samsungxr.controls;

import android.view.MotionEvent;

import com.samsungxr.IApplicationEvents;
import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRCameraRig;
import com.samsungxr.SXRContext;
import com.samsungxr.SXREventListeners;
import com.samsungxr.SXRMain;
import com.samsungxr.SXRMesh;
import com.samsungxr.SXRNode;
import com.samsungxr.SXRRenderPass.SXRCullFaceEnum;
import com.samsungxr.SXRScene;
import com.samsungxr.SXRShaderId;
import com.samsungxr.SXRTexture;
import com.samsungxr.SXRTextureParameters;
import com.samsungxr.SXRTextureParameters.TextureFilterType;
import com.samsungxr.SXRTextureParameters.TextureWrapType;
import com.samsungxr.controls.anim.ActionWormAnimation;
import com.samsungxr.controls.anim.ColorWorm;
import com.samsungxr.controls.anim.StarBoxNode;
import com.samsungxr.controls.cursor.ControlGazeController;
import com.samsungxr.controls.focus.ControlNodeBehavior;
import com.samsungxr.controls.gamepad.GamepadObject;
import com.samsungxr.controls.input.GamepadInput;
import com.samsungxr.controls.input.TouchPadInput;
import com.samsungxr.controls.menu.MenuBox;
import com.samsungxr.controls.model.Apple;
import com.samsungxr.controls.model.touchpad.TouchPad;
import com.samsungxr.controls.shaders.TileShader;
import com.samsungxr.controls.util.Constants;
import com.samsungxr.controls.util.RenderingOrder;

public class Main extends SXRMain {

    private SXRContext mSXRContext;
    private SXRScene scene;

    public static Worm worm;
    private SXRNode skybox, surroundings, sun, ground, fence;
    private Clouds clouds;
    private float GROUND_Y_POSITION = -1;
    private float SKYBOX_SIZE = 1;
    private float SUN_ANGLE_POSITION = 30;
    private float SUN_Y_POSITION = 10;
    private float CLOUDS_DISTANCE = 15;
    private float SCENE_SIZE = 0.75f;
    private float SCENE_Y = -1.0f;
    private float GROUND_SIZE = 55;
    private float SUN_SIZE = 25;
    private int NUMBER_OF_CLOUDS = 8;
    private float GROUND_TILES = 20;

    private GamepadObject gamepadObject;
    private MenuBox menu;

    TouchPad touchpad;

    private Apple apple;
    public static ActionWormAnimation animationColor;
    private static StarBoxNode starBox;
    private IApplicationEvents activityTouchHandler = new SXREventListeners.ApplicationEvents()
    {
        public void dispatchTouchEvent(MotionEvent event)
        {
            mSXRContext.getActivity().onTouchEvent(event);
        }
    };

    @Override
    public void onInit(SXRContext sxrContext) {

        // save context for possible use in onStep(), even though that's empty
        // in this sample
        mSXRContext = sxrContext;

        scene = sxrContext.getMainScene();
        //mSXRContext.getActivity().getEventReceiver().addListener(activityTouchHandler);
        // set background color
        SXRCameraRig mainCameraRig = scene.getMainCameraRig();
        mainCameraRig.getTransform().setPositionY(0);
        createSkybox();
        createClouds();
        createGround();
        createGazeCursor();

        createSun();
        createSurroundings();
        createWorm();
        createFence();
        createMenu();
        createGamepad3D();
        for (int i = 0; i < Constants.NUMBER_OF_APPLES; i++) {
            createApple();
        }

        createTouchPad3D();

        createStar();
        enableAnimationWorm();
    }

    public static void animWormReset(){
        animationColor.resetAnimationState();
    }

    public static void animationColor(com.samsungxr.controls.util.ColorControls.Color color){

        ColorWorm.lastColor = worm.getColor();
        ColorWorm.currentColor = color;
        animationColor.showPlayButton();
    }

    public static void enableAnimationStar(){
        starBox.showPlayButton();
    }

    public void createStar(){

        SXRNode object = new SXRNode(mSXRContext);

        starBox = new StarBoxNode(mSXRContext);

        starBox.getTransform().setPosition(0, .4f, 8.5f);
        starBox.getTransform().rotateByAxisWithPivot(125, 0, 1, 0, 0, 0, 0);
        starBox.setName("star");
        object.addChildObject(starBox);

        scene.addNode(object);
    }

    public void enableAnimationWorm(){

        SXRNode wormParent = worm.getWormParentation();

        animationColor = new ActionWormAnimation(mSXRContext);

        SXRNode object = new SXRNode(mSXRContext);
        object.addChildObject(animationColor);

        wormParent.addChildObject(object);
    }

    @Override
    public SplashMode getSplashMode() {
        return SplashMode.NONE;
    }

    private void createApple() {

        apple = new Apple(mSXRContext);
        apple.setName("apple");
        mSXRContext.getMainScene().addNode(apple);
        apple.setAppleRandomPosition(mSXRContext);
        apple.getTransform().setPositionY(Constants.APPLE_INICIAL_YPOS);
        apple.playAnimation(mSXRContext);
    }

    private void createTouchPad3D() {
        touchpad = new TouchPad(mSXRContext);
        touchpad.getTransform().setPositionZ(-8.5f);
        touchpad.getTransform().setPositionY(0.6f);
        touchpad.getTransform().setScale(0.6f, 0.6f, 0.6f);
        touchpad.getTransform().rotateByAxisWithPivot(90 + 45, 0, 1, 0, 0, 0, 0);
        touchpad.setName("touchpad");
        mSXRContext.getMainScene().addNode(touchpad);
    }

    private void createFence() {

        SXRMesh mesh = mSXRContext.getAssetLoader().loadMesh(
                new SXRAndroidResource(mSXRContext, R.raw.fence));
        SXRTexture texture = mSXRContext.getAssetLoader().loadTexture(
                new SXRAndroidResource(mSXRContext, R.drawable.atlas01));
        fence = new SXRNode(mSXRContext, mesh, texture);
        fence.getTransform().setPositionY(GROUND_Y_POSITION);
        fence.getTransform().setScale(SCENE_SIZE, SCENE_SIZE, SCENE_SIZE);
        fence.getRenderData().setCullFace(SXRCullFaceEnum.None);
        fence.getRenderData().setRenderingOrder(RenderingOrder.FENCE);
        fence.setName("fence");
        scene.addNode(fence);
    }

    private void createWorm() {

        worm = new Worm(mSXRContext);
        worm.enableShadow();
        worm.setName("worm");
        scene.addNode(worm);
    }

    private void createGround() {

        SXRTextureParameters parameters = new SXRTextureParameters(mSXRContext);
        parameters.setWrapSType(TextureWrapType.GL_REPEAT);
        parameters.setWrapTType(TextureWrapType.GL_REPEAT);
        parameters.setAnisotropicValue(16);
        parameters.setMinFilterType(TextureFilterType.GL_LINEAR);
        parameters.setMagFilterType(TextureFilterType.GL_LINEAR);

        SXRMesh mesh = mSXRContext.createQuad(GROUND_SIZE, GROUND_SIZE);
        SXRTexture texture = mSXRContext.getAssetLoader().loadTexture(
                new SXRAndroidResource(mSXRContext, R.drawable.ground_512), parameters);

        ground = new SXRNode(mSXRContext, mesh, texture,
                new SXRShaderId(TileShader.class));
        ground.getTransform().setPositionY(GROUND_Y_POSITION);
        ground.getTransform().setScale(SCENE_SIZE, SCENE_SIZE, SCENE_SIZE);
        ground.getTransform().setRotationByAxis(-45, 0, 0, 1);
        ground.getTransform().setRotationByAxis(-90, 1, 0, 0);
        ground.getRenderData().setRenderingOrder(RenderingOrder.GROUND);

       // ground.getRenderData().getMaterial().setFloat(TileShader.TILE_COUNT, GROUND_TILES);
        ground.getRenderData().getMaterial().setTexture(TileShader.TEXTURE_KEY, texture);
        ground.setName("ground");
        scene.addNode(ground);
    }

    private void createSkybox() {

        SXRMesh mesh = mSXRContext.getAssetLoader().loadMesh(
                new SXRAndroidResource(mSXRContext, R.raw.skybox));
        SXRTexture texture = mSXRContext.getAssetLoader().loadTexture(
                new SXRAndroidResource(mSXRContext, R.drawable.skybox));

        skybox = new SXRNode(mSXRContext, mesh, texture);
        skybox.getTransform().setScale(SKYBOX_SIZE, SKYBOX_SIZE, SKYBOX_SIZE);
        skybox.getRenderData().setRenderingOrder(RenderingOrder.SKYBOX);
        skybox.setName("skybox");
        scene.addNode(skybox);
    }

    private void createClouds() {

        clouds = new Clouds(mSXRContext, CLOUDS_DISTANCE, NUMBER_OF_CLOUDS);
        clouds.setName("clouds");
    }

    private void createSurroundings() {

        SXRMesh mesh = mSXRContext.getAssetLoader().loadMesh(
                new SXRAndroidResource(mSXRContext, R.raw.stones));
        SXRTexture texture = mSXRContext.getAssetLoader().loadTexture(
                new SXRAndroidResource(mSXRContext, R.drawable.atlas01));

        surroundings = new SXRNode(mSXRContext, mesh, texture);
        surroundings.getTransform().setScale(SCENE_SIZE, SCENE_SIZE, SCENE_SIZE);
        surroundings.getTransform().setPositionY(SCENE_Y);
        surroundings.getRenderData().setRenderingOrder(RenderingOrder.FLOWERS);
        scene.addNode(surroundings);
        // ground.addChildObject(surroundings);

        mesh = mSXRContext.getAssetLoader().loadMesh(
                new SXRAndroidResource(mSXRContext, R.raw.grass));
        texture = mSXRContext.getAssetLoader().loadTexture(
                new SXRAndroidResource(mSXRContext, R.drawable.atlas01));

        surroundings = new SXRNode(mSXRContext, mesh, texture);
        surroundings.getTransform().setScale(SCENE_SIZE, SCENE_SIZE, SCENE_SIZE);
        surroundings.getTransform().setPositionY(SCENE_Y);
        scene.addNode(surroundings);
        // ground.addChildObject(surroundings);
        surroundings.getRenderData().setRenderingOrder(RenderingOrder.GRASS);

        mesh = mSXRContext.getAssetLoader().loadMesh(
                new SXRAndroidResource(mSXRContext, R.raw.flowers));
        texture = mSXRContext.getAssetLoader().loadTexture(
                new SXRAndroidResource(mSXRContext, R.drawable.atlas01));

        surroundings = new SXRNode(mSXRContext, mesh, texture);
        surroundings.getTransform().setScale(SCENE_SIZE, SCENE_SIZE, SCENE_SIZE);
        surroundings.getTransform().setPositionY(SCENE_Y);
        scene.addNode(surroundings);
        // ground.addChildObject(surroundings);
        surroundings.getRenderData().setRenderingOrder(RenderingOrder.FLOWERS);

        mesh = mSXRContext.getAssetLoader().loadMesh(
                new SXRAndroidResource(mSXRContext, R.raw.wood));
        texture = mSXRContext.getAssetLoader().loadTexture(
                new SXRAndroidResource(mSXRContext, R.drawable.atlas01));
        surroundings = new SXRNode(mSXRContext, mesh, texture);
        surroundings.getTransform().setScale(SCENE_SIZE, SCENE_SIZE, SCENE_SIZE);
        surroundings.getTransform().setPositionY(SCENE_Y);
        surroundings.getRenderData().setCullFace(SXRCullFaceEnum.None);
        scene.addNode(surroundings);
        surroundings.setName("surroundings");
        // ground.addChildObject(surroundings);
        surroundings.getRenderData().setRenderingOrder(RenderingOrder.WOOD);
    }

    private void createSun() {

        SXRMesh mesh = mSXRContext.createQuad(SUN_SIZE, SUN_SIZE);
        SXRTexture texture = mSXRContext.getAssetLoader().loadTexture(
                new SXRAndroidResource(mSXRContext, R.drawable.sun));
        sun = new SXRNode(mSXRContext, mesh, texture);
        sun.getTransform().setRotationByAxis(90, 1, 0, 0);
        sun.getTransform().setPositionY(SUN_Y_POSITION);
        sun.getTransform().rotateByAxisWithPivot(SUN_ANGLE_POSITION, 1, 0, 0, 0, 0, 0);
        sun.getRenderData().setRenderingOrder(RenderingOrder.SUN);
        sun.setName("sun");
        scene.addNode(sun);
    }

    @Override
    public void onStep() {
        worm.chainMove(mSXRContext);

        GamepadInput.process();
        TouchPadInput.process();

        if(touchpad != null)
            touchpad.updateIndicator();

        worm.interactWithDPad();
        worm.animateWormByTouchPad();
        ControlNodeBehavior.process(mSXRContext);

        if (gamepadObject != null) {
            gamepadObject.inputControl();
        }
        worm.checkWormEatingApple(mSXRContext);
    }

    private void createMenu() {

        menu = new MenuBox(mSXRContext);
        menu.setName("menu");
        scene.addNode(menu);
    }

    private void createGazeCursor() {
        ControlGazeController.setupGazeCursor(mSXRContext);
    }

    private void createGamepad3D() {
        gamepadObject = new GamepadObject(mSXRContext);

        gamepadObject.getTransform().setPosition(0, 1.f, -8.5f);
        gamepadObject.getTransform().rotateByAxisWithPivot(225, 0, 1, 0, 0, 0, 0);
        gamepadObject.setName("gamepad");
        scene.addNode(gamepadObject);
    }
}
