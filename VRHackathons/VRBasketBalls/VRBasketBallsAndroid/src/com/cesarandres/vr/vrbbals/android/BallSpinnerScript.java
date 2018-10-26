package com.cesarandres.vr.vrbbals.android;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;

import com.samsungxr.FutureWrapper;
import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRCameraRig;
import com.samsungxr.SXRContext;
import com.samsungxr.SXREyePointeeHolder;
import com.samsungxr.SXRMaterial;
import com.samsungxr.SXRMesh;
import com.samsungxr.SXRPicker;
import com.samsungxr.SXRScene;
import com.samsungxr.SXRNode;
import com.samsungxr.SXRScript;
import com.samsungxr.SXRTexture;
import com.samsungxr.animation.SXRAnimationEngine;
import com.samsungxr.nodes.SXRTextViewNode;
import com.samsungxr.nodes.SXRTextViewNode.IntervalFrequency;

import android.graphics.Color;

import com.cesarandres.vr.vrbbals.android.MainActivity.COMMANDS;

public class BallSpinnerScript extends SXRScript {

	private static int GAME_OVER_REVOLUTION_DURATION = 3;
	private static int STARTING_BALLS = 3;
	private static final float CUBE_WIDTH = 200.0f;
	private static float LOOKAT_COLOR_MASK_R = 1.0f;
	private static float LOOKAT_COLOR_MASK_G = 0.6f;
	private static float LOOKAT_COLOR_MASK_B = 0.6f;

	private MainActivity core;

	private SXRTextViewNode textPanel;
	private SXRContext context;
	private SXRScene scene;
	private SXRNode root;
	private SXRCameraRig mainCamera;
	private SXRAnimationEngine animationEngine;

	private boolean connected = false;

	private ConcurrentLinkedQueue<COMMANDS> commandQueue;
	private Queue<BasketBall> ballPool;
	private StringBuilder messageBuilder;
	private COMMANDS lastCommand;
	private boolean lost;
	private int frameCounter;
	private int lastIndex;
	private int score;

	public BallSpinnerScript(MainActivity core) {
		this.core = core;
	}

	@Override
	public void onInit(SXRContext ctx) throws Throwable {

		context = ctx;
		scene = ctx.getMainScene();
		scene.setFrustumCulling(true);

		float r = 5f / 255f;
		float g = 5f / 255f;
		float b = 55f / 255f;

		FutureWrapper<SXRMesh> futureQuadMesh = new FutureWrapper<SXRMesh>(
				ctx.createQuad(CUBE_WIDTH, CUBE_WIDTH));

		Future<SXRTexture> futureCubemapTexture = ctx
				.loadFutureCubemapTexture(new SXRAndroidResource(ctx,
						R.raw.beach));

		SXRMaterial cubemapMaterial = new SXRMaterial(ctx,
				SXRMaterial.SXRShaderType.Cubemap.ID);
		cubemapMaterial.setMainTexture(futureCubemapTexture);

		// surrounding cube
		SXRNode frontFace = new SXRNode(ctx, futureQuadMesh,
				futureCubemapTexture);
		frontFace.getRenderData().setMaterial(cubemapMaterial);
		frontFace.setName("front");
		scene.addNode(frontFace);
		frontFace.getTransform().setPosition(0.0f, 0.0f, -CUBE_WIDTH * 0.5f);

		SXRNode backFace = new SXRNode(ctx, futureQuadMesh,
				futureCubemapTexture);
		backFace.getRenderData().setMaterial(cubemapMaterial);
		backFace.setName("back");
		scene.addNode(backFace);
		backFace.getTransform().setPosition(0.0f, 0.0f, CUBE_WIDTH * 0.5f);
		backFace.getTransform().rotateByAxis(180.0f, 0.0f, 1.0f, 0.0f);

		SXRNode leftFace = new SXRNode(ctx, futureQuadMesh,
				futureCubemapTexture);
		leftFace.getRenderData().setMaterial(cubemapMaterial);
		leftFace.setName("left");
		scene.addNode(leftFace);
		leftFace.getTransform().setPosition(-CUBE_WIDTH * 0.5f, 0.0f, 0.0f);
		leftFace.getTransform().rotateByAxis(90.0f, 0.0f, 1.0f, 0.0f);

		SXRNode rightFace = new SXRNode(ctx, futureQuadMesh,
				futureCubemapTexture);
		rightFace.getRenderData().setMaterial(cubemapMaterial);
		rightFace.setName("right");
		scene.addNode(rightFace);
		rightFace.getTransform().setPosition(CUBE_WIDTH * 0.5f, 0.0f, 0.0f);
		rightFace.getTransform().rotateByAxis(-90.0f, 0.0f, 1.0f, 0.0f);

		SXRNode topFace = new SXRNode(ctx, futureQuadMesh,
				futureCubemapTexture);
		topFace.getRenderData().setMaterial(cubemapMaterial);
		topFace.setName("top");
		scene.addNode(topFace);
		topFace.getTransform().setPosition(0.0f, CUBE_WIDTH * 0.5f, 0.0f);
		topFace.getTransform().rotateByAxis(90.0f, 1.0f, 0.0f, 0.0f);

		SXRNode bottomFace = new SXRNode(ctx, futureQuadMesh,
				futureCubemapTexture);
		bottomFace.getRenderData().setMaterial(cubemapMaterial);
		bottomFace.setName("bottom");
		scene.addNode(bottomFace);
		bottomFace.getTransform().setPosition(0.0f, -CUBE_WIDTH * 0.5f, 0.0f);
		bottomFace.getTransform().rotateByAxis(-90.0f, 1.0f, 0.0f, 0.0f);

		animationEngine = context.getAnimationEngine();

		// head-tracking pointer
		SXRTexture pTexture = ctx.loadTexture(new SXRAndroidResource(ctx,
				"headtrackingpointer.png"));
		SXRNode headTracker = new SXRNode(ctx, 0.05f, 0.05f,
				pTexture);

		headTracker.getTransform().setPosition(0.0f, 0.0f, -1.0f);
		headTracker.getRenderData().setDepthTest(false);
		headTracker.getRenderData().setRenderingOrder(100000);
		mainCamera = scene.getMainCameraRig();
		mainCamera.addChildObject(headTracker);

		mainCamera.getLeftCamera().setBackgroundColor(r, g, b, 1.0f);
		mainCamera.getRightCamera().setBackgroundColor(r, g, b, 1.0f);
		mainCamera.getTransform().setPosition(0f, 0f, 0f);

		root = new SXRNode(ctx);
		scene.addNode(root);

		textPanel = new SXRTextViewNode(context, 7, 4, "");

		// set the scene object position
		textPanel.setTextColor(Color.GREEN);
		textPanel.setTextSize(textPanel.getTextSize() * 0.55f);
		textPanel.setRefreshFrequency(IntervalFrequency.MEDIUM);
		// add the scene object to the scene graph
		scene.addNode(textPanel);
		textPanel.getTransform().setPositionZ(-1.5f);
		textPanel.getTransform().setPositionY(-2.5f);
		textPanel.getTransform().setPositionX(1.5f);
		textPanel.getTransform().setRotationByAxis(-45, 1, 0, 0);

		messageBuilder = new StringBuilder();
		commandQueue = new ConcurrentLinkedQueue<COMMANDS>();
		ballPool = new LinkedList<>();

		clearValues();
	}

	private void clearValues() {
		commandQueue.clear();
		for (BasketBall ball : ballPool) {
			root.removeChildObject(ball.getVrObject());
		}
		ballPool.clear();
		lastCommand = COMMANDS.NONE;
		frameCounter = 0;
		lastIndex = 0;
		score = 0;
		lost = false;
		initBusObjectPool();
	}

	@Override
	public void onStep() {

		frameCounter++;

		if (frameCounter % 50 == 0) {
			for (BasketBall vel : ballPool) {
				if (vel.duration >= GAME_OVER_REVOLUTION_DURATION) {
					lost = true;
					break;
				}
				if (connected) {
					vel.duration *= 1.1;
				} else {
					vel.duration *= 1.1;
				}
				vel.counterClockwise(vel.getVrObject(), vel.duration);
				if (!lost) {
					score += ((GAME_OVER_REVOLUTION_DURATION * 10) - (vel.duration * 10f)) / 5;
				}
			}
		}

		COMMANDS command = this.commandQueue.poll();
		if (command != null) {
			this.lastCommand = command;
		} else {
			command = COMMANDS.NONE;
		}

		if (lost) {
			messageBuilder
					.append("GameOver\nScore: " + Integer.toString(score));
		} else {
			messageBuilder.append("Score: " + Integer.toString(score));
		}
		messageBuilder.append("\nLast Command: " + lastCommand.toString());
		messageBuilder.append("\nServer Status: "
				+ (connected ? "Connected" : "Disconnected"));

		textPanel.setText(messageBuilder.toString());
		messageBuilder.setLength(0);

		switch (command) {
		case LEFT:
			for (SXREyePointeeHolder eph : SXRPicker.pickScene(context
					.getMainScene())) {
				for (BasketBall vel : ballPool) {
					if (eph.getOwnerObject().equals(vel.getVrObject())) {
						if (vel.duration >= 1) {
							if (connected) {
								vel.duration *= 0.075;
							} else {
								vel.duration *= 0.75;
							}
							vel.counterClockwise(vel.getVrObject(),
									vel.duration);
						}
						break;
					}
				}
			}
			break;
		case UP:
			createBall();
			break;
		case RESET:
			clearValues();
			break;
		case CONNECTED:
			connected = true;
			break;
		case DISCONNECTED:
			connected = false;
			break;
		case NONE:
			break;
		}

		if (!lost && ((frameCounter + 1) % 350) == 0) {
			createBall();
		}

		for (BasketBall vel : ballPool) {
			vel.getVrObject().getRenderData().getMaterial()
					.setColor(1.0f, 1.0f, 1.0f);
		}

		for (SXREyePointeeHolder eph : SXRPicker.pickScene(context
				.getMainScene())) {
			eph.getOwnerObject()
					.getRenderData()
					.getMaterial()
					.setColor(LOOKAT_COLOR_MASK_R, LOOKAT_COLOR_MASK_G,
							LOOKAT_COLOR_MASK_B);
			break;
		}
	}

	private void initBusObjectPool() {
		for (int i = 0; i < STARTING_BALLS; i++) {
			createBall();
		}
	}

	void createBall() {
		BasketBall ball = new BasketBall();
		ball.init(context, animationEngine, lastIndex);
		ballPool.add(ball);
		root.addChildObject(ball.getVrObject());
		ball.counterClockwise(ball.getVrObject(), ball.duration);
		if (lastIndex <= 0) {
			lastIndex--;
		}
		lastIndex *= -1;
	}

	public void handleLongPress() {
		commandQueue.add(COMMANDS.RESET);
	}

	public void setCommand(COMMANDS command) {
		if (COMMANDS.DISCONNECTED == command && !this.connected) {
			// We may want to use this for something later.
		} else if (COMMANDS.CONNECTED == command && this.connected) {
			// We may want to use this for something later too.
		} else {
			commandQueue.add(command);
		}
	}
}
