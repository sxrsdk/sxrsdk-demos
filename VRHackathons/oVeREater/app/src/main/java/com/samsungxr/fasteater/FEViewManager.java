package com.gearvrf.fasteater;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.Gravity;
import android.view.MotionEvent;

import com.samsungxr.FutureWrapper;
import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRBitmapTexture;
import com.samsungxr.SXRCollider;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRMaterial;
import com.samsungxr.SXRMesh;
import com.samsungxr.SXRMeshCollider;
import com.samsungxr.SXRRenderData;
import com.samsungxr.SXRRenderData.SXRRenderingOrder;
import com.samsungxr.SXRScene;
import com.samsungxr.SXRNode;
import com.samsungxr.SXRScript;
import com.samsungxr.SXRTexture;
import com.samsungxr.SXRTransform;
import com.samsungxr.ZipLoader;
import com.samsungxr.animation.SXRAnimation;
import com.samsungxr.animation.SXRAnimationEngine;
import com.samsungxr.animation.SXRPositionAnimation;
import com.samsungxr.animation.SXRRepeatMode;
import com.samsungxr.animation.SXRRotationByAxisAnimation;
import com.samsungxr.animation.SXRRotationByAxisWithPivotAnimation;
import com.samsungxr.animation.SXRScaleAnimation;
import com.samsungxr.nodes.SXRTextViewNode;
import com.samsungxr.nodes.SXRTextViewNode.IntervalFrequency;
import com.samsungxr.utility.Log;
import org.siprop.bullet.Bullet;
import org.siprop.bullet.Geometry;
import org.siprop.bullet.MotionState;
import org.siprop.bullet.RigidBody;
import org.siprop.bullet.Transform;
import org.siprop.bullet.shape.BoxShape;
import org.siprop.bullet.shape.StaticPlaneShape;
import org.siprop.bullet.util.Point3;
import org.siprop.bullet.util.Vector3;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Future;

public class FEViewManager extends SXRScript {
	private static final String TAG = Log.tag(FEViewManager.class);
	private SXRAnimationEngine mAnimationEngine;
	private SXRScene mMainScene;
	private SXRContext mSXRContext;
	private SXRNode mainNode, headTracker, astronautMeshObject;
	private SXRTextViewNode textMessageObject, scoreTextMessageObject, livesTextMessageObject, tapTOStart;
	private SXRNode burger;
	private List<FlyingItem> mObjects = new ArrayList<FlyingItem>();
    private Bullet mBullet = null;
    private static final float OBJECT_MASS = 0.5f;
    private RigidBody boxBody;
    private Boolean gameStart = false;
    private Map<RigidBody, SXRNode> rigidBodiesSceneMap = new HashMap<RigidBody, SXRNode>();
    private Timer timer;
    private GameStateMachine gameState;
    private SXRNode homeButton, pauseButton, timerButton;
    private Player ovrEater;
    private Boolean isBGAudioOnce = false;

	private SXRNode asyncNode(SXRContext context, String meshName, String textureName)
			throws IOException {
		return new SXRNode(context, //
				new SXRAndroidResource(context, meshName), new SXRAndroidResource(context, textureName));
	}

	@Override
	public void onInit(SXRContext sxrContext) throws IOException, InterruptedException {
        gameState = new GameStateMachine();
		mSXRContext = sxrContext;
		mAnimationEngine = mSXRContext.getAnimationEngine();
		mMainScene = mSXRContext.getNextMainScene();
		mMainScene.setFrustumCulling(true);

        loadGameScene(mSXRContext, mMainScene);
	}

    @Override
    public SXRTexture getSplashTexture(SXRContext sxrContext) {
        Bitmap bitmap = BitmapFactory.decodeResource(
                sxrContext.getContext().getResources(),
                R.drawable.boot_screen);
        //Bitmap scaledBitmap = bitmap.createScaledBitmap(bitmap, 2, 2, true);
        return new SXRBitmapTexture(sxrContext, bitmap);
    }

    private void loadGameScene(SXRContext context, SXRScene scene) throws IOException {
        gameState.setStatus(GameStateMachine.GameStatus.STATE_GAME_IN_PROGRESS);
        // load all audio files. TODO: change this to spacial Audio
        AudioClip.getInstance(context.getContext());

        ovrEater = new Player();

        mainNode = new SXRNode(context);
        mMainScene.addNode(mainNode);
        mMainScene.getMainCameraRig().getTransform().setPosition(0.0f, 6.0f, 8.0f);

        SXRMesh mesh = context.loadMesh(new SXRAndroidResource(context,
                "space_sphere.obj"));

        SXRNode leftScreen = new SXRNode(context, mesh,
                context.loadTexture(new SXRAndroidResource(context,
                        "city_domemap_left.png")));
        leftScreen.getTransform().setScale(200,200,200);
        SXRNode rightScreen = new SXRNode(context, mesh,
                context.loadTexture(new SXRAndroidResource(context,
                        "city_domemap_right.png")));
        rightScreen.getTransform().setScale(200,200,200);

        mainNode.addChildObject(leftScreen);
        mainNode.addChildObject(rightScreen);

        tapTOStart = setInfoMessage("Tap to start");
        mainNode.addChildObject(tapTOStart);

    }

    private SXRTextViewNode setInfoMessage(String str)
    {
        SXRTextViewNode textMessageObject = new SXRTextViewNode(mSXRContext, 4, 4, str);
        textMessageObject.setTextColor(Color.YELLOW);
        textMessageObject.setGravity(Gravity.CENTER);
        textMessageObject.setKeepWrapper(true);
        textMessageObject.setTextSize(15);
        textMessageObject.setBackgroundColor(Color.TRANSPARENT);
        textMessageObject.setRefreshFrequency(IntervalFrequency.HIGH);
        textMessageObject.getTransform().setPosition(-2.0f, 6.0f, -6.0f);
        textMessageObject.getTransform().rotateByAxisWithPivot(0, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f);

        SXRRenderData renderData = textMessageObject.getRenderData();
        renderData.setRenderingOrder(SXRRenderingOrder.TRANSPARENT);
        renderData.setDepthTest(false);

        return textMessageObject;
    }

    private SXRTextViewNode makeScoreboard(SXRContext ctx, SXRNode parent)
    {
        SXRTextViewNode scoreBoard = new SXRTextViewNode(ctx, 2.0f, 1.5f, "000");

        SXRRenderData rdata = scoreBoard.getRenderData();
        SXRCollider collider = new SXRMeshCollider(ctx, true);

        collider.setEnable(false);
        scoreBoard.attachComponent(collider);
        scoreBoard.setTextColor(Color.YELLOW);
        scoreBoard.setTextSize(6);
        scoreBoard.setBackgroundColor(Color.argb(0, 0, 0, 0));
        scoreBoard.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        rdata.setDepthTest(false);
        rdata.setAlphaBlend(true);
        rdata.setRenderingOrder(SXRRenderingOrder.OVERLAY);
        parent.addChildObject(scoreBoard);
        return scoreBoard;
    }

    private SXRTextViewNode makeLivesLeft(SXRContext ctx, SXRNode parent)
    {
        SXRTextViewNode livesLeft = new SXRTextViewNode(ctx, 5.3f, 1.5f, "Lives: 3");
        livesLeft.setTextSize(6);
        SXRRenderData rdata = livesLeft.getRenderData();
        SXRCollider collider = new SXRMeshCollider(ctx, true);

        collider.setEnable(false);
        livesLeft.attachComponent(collider);
        livesLeft.setTextColor(Color.YELLOW);
        livesLeft.setBackgroundColor(Color.argb(0, 0, 0, 0));
        livesLeft.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        rdata.setDepthTest(false);
        rdata.setAlphaBlend(true);
        rdata.setRenderingOrder(SXRRenderingOrder.OVERLAY);
        parent.addChildObject(livesLeft);
        return livesLeft;
    }

	private SXRNode quadWithTexture(float width, float height, String texture) {
		FutureWrapper<SXRMesh> futureMesh = new FutureWrapper<SXRMesh>(mSXRContext.createQuad(width, height));
		SXRNode object = null;
		try {
			object = new SXRNode(mSXRContext, futureMesh,
					mSXRContext.loadFutureTexture(new SXRAndroidResource(mSXRContext, texture)));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return object;
	}

    private int MAX_THROW = 15;

    private void _throwObject()
    {
        Timer timer = new Timer();
        TimerTask task = new TimerTask()
        {
            public void run() {
                try {
                    int num_throw = Helper.randomNextInt(MAX_THROW);
                    for(int i = 0; i < num_throw; i++) {
                        throwAnObject();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        int THROW_OBJECT_RATE_MIN = 1 * 1000;
        int THROW_OBJECT_RATE_MAX = 4 * 1000;
        int THROW_OBJECT_DELAY_MIN = 1 * 1000;
        int THROW_OBJECT_DELAY_MAX = 4 * 1000;
        timer.scheduleAtFixedRate(task,
                Helper.randomInRange(THROW_OBJECT_DELAY_MIN, THROW_OBJECT_DELAY_MAX),
                Helper.randomInRange(THROW_OBJECT_RATE_MIN, THROW_OBJECT_RATE_MAX));

        //timeElapsed = System.currentTimeMillis();
    }

    private int MIN_GAME_WIDTH = -10;
    private int MAX_GAME_WIDTH = 10;
    private int MIN_GAME_HEIGHT_START = 5;
    private int MAX_GAME_HEIGHT_START = 7;
    private int MIN_GAME_HEIGHT_REACH = 5;
    private int MAX_GAME_HEIGHT_REACH = 7;
    private int MIN_SPEED = 2;
    private int MAX_SPEED = 0;

    private String[][] OverEatObjects = new String[][]{
            { "hotdog.obj", "hotdog.png", "hotdog" },
            { "hamburger.obj", "hamburger.png", "hamburger" },
            { "bomb.obj", "bomb.png", "bomb" },
            { "sodacan.obj", "sodacan.png", "sodacan" }
    };

    public void throwAnObject() throws IOException {
        if(!ovrEater.isDead()) {
            int rand_index = Helper.randomNextInt(OverEatObjects.length);
            SXRNode object = asyncNode(mSXRContext, OverEatObjects[rand_index][0], OverEatObjects[rand_index][1]);
            FlyingItem item = new FlyingItem(OverEatObjects[rand_index][2], object);
            object.getTransform().setPosition(
                    Helper.randomInRangeFloat(MIN_GAME_WIDTH, MAX_GAME_WIDTH),
                    Helper.randomInRangeFloat(MIN_GAME_HEIGHT_START, MAX_GAME_HEIGHT_START),
                    -20);
            mainNode.addChildObject(object);
            mObjects.add(item);

            relativeMotionAnimation(object,
                    Helper.randomInRange(MIN_SPEED, MAX_SPEED),
                    0,
                    0,
                    -(object.getTransform().getPositionZ() - 10));
        }
    }

	@Override
	public void onStep() {
        if(ovrEater.isDead() && gameState.getStatus() == GameStateMachine.GameStatus.STATE_GAME_IN_PROGRESS) {
            playerDead();
        } else if(gameState.getStatus() == GameStateMachine.GameStatus.STATE_GAME_IN_PROGRESS) {
            for (int i = 0; i < mObjects.size(); i++) {
                try {
                    headTracker.getRenderData().getMaterial().setMainTexture(
                            mSXRContext.loadFutureTexture(new SXRAndroidResource(mSXRContext, "mouth_open.png")));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (mObjects.get(i) != null && mObjects.get(i).getNode().getRenderData().getMesh() != null) {
                    if (mObjects.get(i).getNode().isColliding(headTracker)) {
                        //Log.e(TAG, "mObjects.get(i).getName: Penke " + mObjects.get(i).getName() + "score" + ovrEater.getCurrentScore());
                        if (mObjects.get(i).getName().compareTo("bomb") == 0) {
                            animateTextures("explode_.zip", mObjects.get(i).getNode());
                            ovrEater.loseALife();
                            AudioClip.getInstance(mSXRContext.getContext()).
                                    playSound(AudioClip.getUISoundGrenadeID(), 1.0f, 1.0f);
                            Log.e(TAG, "remaining Lives Penke " + ovrEater.getNumLivesRemaining());
                        } else if (mObjects.get(i).getName().compareTo("hamburger") == 0) {
                            animateTextures("splat.zip", mObjects.get(i).getNode());
                            AudioClip.getInstance(mSXRContext.getContext()).
                                    playSound(AudioClip.getUISoundEatID(), 1.0f, 1.0f);
                            ovrEater.incrementScore(50);
                        } else if (mObjects.get(i).getName().compareTo("hotdog") == 0) {
                            ovrEater.incrementScore(30);
                        } else if (mObjects.get(i).getName().compareTo("sodacan") == 0) {
                            AudioClip.getInstance(mSXRContext.getContext()).
                                    playSound(AudioClip.getUISoundDrinkID(), 1.0f, 1.0f);
                            ovrEater.incrementScore(10);
                        }
                        scoreTextMessageObject.setText(String.format("%03d", ovrEater.getCurrentScore()));
                        livesTextMessageObject.setText("Lives: " + ovrEater.getNumLivesRemaining());
                        mainNode.removeChildObject(mObjects.get(i).getNode());
                        mObjects.remove(i);
                        try {
                            headTracker.getRenderData().getMaterial().setMainTexture(
                                    mSXRContext.loadFutureTexture(new SXRAndroidResource(mSXRContext, "mouth_close.png")));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else if (mObjects.get(i).getNode().getTransform().getPositionZ() >
                            mMainScene.getMainCameraRig().getTransform().getPositionZ()) {
                        mainNode.removeChildObject(mObjects.get(i).getNode());
                        mObjects.remove(i);
                    }

                }
            }

            mMainScene.getMainCameraRig()
                    .getTransform()
                    .setPosition(getXLinearDistance(
                            mMainScene.getMainCameraRig().getHeadTransform().getRotationRoll()),
                            mMainScene.getMainCameraRig().getTransform().getPositionY(),
                            mMainScene.getMainCameraRig().getTransform().getPositionZ());
        }
	}

    private void animateTextures(String assetName, SXRNode object) {
        try {
            List<Future<SXRTexture>> loaderTextures = ZipLoader.load(mSXRContext,
                    assetName, new ZipLoader.ZipEntryProcessor<Future<SXRTexture>>() {
                        @Override
                        public Future<SXRTexture> getItem(SXRContext context, SXRAndroidResource
                                resource) {
                            return context.loadFutureTexture(resource);
                        }
                    });

            SXRNode loadingObject = new SXRNode(mSXRContext, 1.0f, 1.0f);

            SXRRenderData renderData = loadingObject.getRenderData();
            SXRMaterial loadingMaterial = new SXRMaterial(mSXRContext);
            renderData.setMaterial(loadingMaterial);
            renderData.setRenderingOrder(SXRRenderingOrder.TRANSPARENT);
            loadingMaterial.setMainTexture(loaderTextures.get(0));
            SXRAnimation animation = new ImageFrameAnimation(loadingMaterial, 1.5f,
                    loaderTextures);
            animation.setRepeatMode(SXRRepeatMode.ONCE);
            animation.setRepeatCount(-1);
            animation.start(mSXRContext.getAnimationEngine());

            loadingObject.getTransform().setPosition(
                    object.getTransform().getPositionX(),
                    object.getTransform().getPositionY(),
                    object.getTransform().getPositionZ()
            );
            mainNode.addChildObject(loadingObject);
        } catch (IOException e) {
            Log.e(TAG, "Error loading animation", e);
        }
    }

    private void playerDead() {
        //stop throwing burgers
        //show score board
        //display click to start again
            /*setInfoMessage(String
                    .format("Score %d", ovrEater.getCurrentScore()));*/
        gameState.setStatus(GameStateMachine.GameStatus.STATE_GAME_END);
        showMouthPointer(false);
        tapTOStart = setInfoMessage("Game Over   " + String
                .format("Score : %d", ovrEater.getCurrentScore()) + "Click Back Button to Play Again");
        mainNode.addChildObject(tapTOStart);
        if(timer != null)
            timer.cancel();
    }

    private void showMouthPointer(Boolean enable) {
        if(enable) {
            // add head-tracking pointer
            try {
                headTracker = new SXRNode(mSXRContext, new FutureWrapper<SXRMesh>(mSXRContext.createQuad(0.5f, 0.5f)),
                        mSXRContext.loadFutureTexture(new SXRAndroidResource(mSXRContext, "mouth_open.png")));
            } catch (IOException e) {
                e.printStackTrace();
            }
            headTracker.getTransform().setPosition(0.0f, 0.0f, -2.0f);
            headTracker.getRenderData().setDepthTest(false);
            headTracker.getRenderData().setRenderingOrder(100000);
            mMainScene.getMainCameraRig().addChildObject(headTracker);
        } else {
            if(headTracker != null)
                mainNode.removeChildObject(headTracker);
        }
    }
	
	private float minLinearX = -12.0f;
	private float maxLinearX = 12.0f;
	private float yawToLinearScale = 0.15f;

	private float getXLinearDistance(float headRotationRoll) {
		float val = headRotationRoll * yawToLinearScale;

		if(val < minLinearX) 		return -minLinearX;
		else if(val > maxLinearX)	return -maxLinearX;
		else						return -val;
	}

	private void run(SXRAnimation animation) {
		animation.setRepeatMode(SXRRepeatMode.REPEATED).setRepeatCount(-1).start(mAnimationEngine);
	}

	private void runOnce(SXRAnimation animation) {
		animation.setRepeatMode(SXRRepeatMode.ONCE).setRepeatCount(-1).start(mAnimationEngine);
	}

	private SXRNode attachedObject = null;
	private float lastX = 0, lastY = 0;
	private boolean isOnClick = false;

	public void onTouchEvent(MotionEvent event) throws IOException {
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			lastX = event.getX();
			lastY = event.getY();
			isOnClick = true;
			break;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			if (isOnClick && (gameState.getStatus() == GameStateMachine.GameStatus.STATE_GAME_END ||
                    gameState.getStatus() == GameStateMachine.GameStatus.STATE_GAME_IN_PROGRESS)) {

                if (gameState.getStatus() == GameStateMachine.GameStatus.STATE_GAME_END) {
                    gameState.setScore(0);
                }

                gameState.setStatus(GameStateMachine.GameStatus.STATE_GAME_IN_PROGRESS);

                if(!isBGAudioOnce) {
                    AudioClip.getInstance(mSXRContext.getContext()).
                            playLoop(AudioClip.getUISoundBGID(), 0.8f, 0.4f);
                    isBGAudioOnce = true;
                }
                if(tapTOStart != null)
                    mainNode.removeChildObject(tapTOStart);

                showMouthPointer(true);
                if (scoreTextMessageObject == null) {
                    scoreTextMessageObject = makeScoreboard(mSXRContext, headTracker);
                }
                scoreTextMessageObject.getTransform().setPosition(-1.2f, 1.2f, -2.2f);
                if (livesTextMessageObject == null) {
                    livesTextMessageObject = makeLivesLeft(mSXRContext, headTracker);
                }
                livesTextMessageObject.getTransform().setPosition(1.2f, 1.2f, -2.2f);
                _throwObject();
			} else if(ovrEater.isDead()) {
                AudioClip.getInstance(mSXRContext.getContext()).
                        stopSound(AudioClip.getUISoundBGID());
                showMouthPointer(false);
                gameState.setStatus(GameStateMachine.GameStatus.STATE_GAME_END);
            }
			break;
		case MotionEvent.ACTION_MOVE:
			break;
		default:
			break;
		}
	}

	private void counterClockwise(SXRNode object, float duration) {
		run(new SXRRotationByAxisWithPivotAnimation(object, duration, 360.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f));
	}

	private void clockwise(SXRNode object, float duration) {
		run(new SXRRotationByAxisWithPivotAnimation(object, duration, -360.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f));
	}

	private void clockwise(SXRTransform transform, float duration) {
		run(new SXRRotationByAxisWithPivotAnimation(transform, duration, -360.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f));
	}

	private void clockwiseOnZ(SXRNode object, float duration) {
		runOnce(new SXRRotationByAxisAnimation(object, duration, -360.0f, 0.0f, 0.0f, 1.0f));
	}

	private void scaleAnimation(SXRNode object, float duration, float x, float y, float z) {
		runOnce(new SXRScaleAnimation(object, duration, x, y, z));
	}

    /*
	private void startSpaceShip(SXRNode object, float duration) {

	}
	*/

	private void relativeMotionAnimation(SXRNode object, float duration, float x, float y, float z) {
		runOnce(new SXRPositionAnimation(object, duration, x, y, z));
	}

	private void attachDefaultEyePointee(SXRNode sceneObject) {
		sceneObject.attachEyePointeeHolder();
	}

}
