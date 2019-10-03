package com.samsungxr.simplephysics.main;

import android.graphics.Color;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.MotionEvent;

import com.samsungxr.SXRCameraRig;
import com.samsungxr.SXRComponent;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRMain;
import com.samsungxr.SXRMaterial;
import com.samsungxr.SXRScene;
import com.samsungxr.SXRNode;
import com.samsungxr.SXRShader;
import com.samsungxr.SXRTransform;
import com.samsungxr.io.SXRControllerType;
import com.samsungxr.io.SXRCursorController;
import com.samsungxr.io.SXRInputManager;
import com.samsungxr.io.SXRTouchPadGestureListener;
import com.samsungxr.physics.SXRRigidBody;
import com.samsungxr.physics.SXRWorld;
import com.samsungxr.nodes.SXRSphereNode;
import com.samsungxr.nodes.SXRTextViewNode;
import com.samsungxr.simplephysics.R;
import com.samsungxr.simplephysics.entity.Countdown;
import com.samsungxr.simplephysics.util.MathUtils;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.io.IOException;

public class MainScript extends SXRMain implements SXRNode.ComponentVisitor {

    private final int MAX_BALLS = 40;
    private int SCORE_OFFSET = -50;
    private SXRScene mScene;
    private SXRCameraRig mCamera;
    private int mTimeTicker = 0;
    private int mScore = 0;
    private int mNumBalls = 0;
    private int mNumCylinders = 0;
    private SXRTextViewNode mScoreLabel;
    private SXRTextViewNode mBallsLabel;
    private SXRTextViewNode mEndGameLabel;
    private Countdown mCountDown;
    private SXRCursorController mController;
    private SXRNode mCursor;
    private SXRNode mBallProto;
    private SXRNode mCurrentBall = null;
    private SXRWorld mWorld = null;


    private SXRCursorController.IControllerEvent mControllerThrowHandler = new SXRCursorController.IControllerEvent()
    {
        private Vector3f mStartDrag = new Vector3f(0, 0, 0);
        private Vector3f mEndDrag = new Vector3f(0, 0, 0);
        private Vector3f mTempDir = new Vector3f();

        public void onEvent(SXRCursorController controller, boolean touched)
        {
            MotionEvent event = controller.getMotionEvent();
            if (event == null)
            {
                return;
            }
            int action = event.getAction();
            if ((mCurrentBall == null) && (action == MotionEvent.ACTION_DOWN))
            {
                mCurrentBall = newBall();
                SXRRigidBody rigidBody = (SXRRigidBody) mCurrentBall.getComponent(SXRRigidBody.getComponentType());
                rigidBody.setEnable(false);
                controller.getCursor().addChildObject(mCurrentBall);
                controller.getPicker().getWorldPickRay(mStartDrag, mTempDir);
            }
            else if ((event.getAction() == MotionEvent.ACTION_UP) && (mCurrentBall != null))
            {
                float dt = SystemClock.uptimeMillis() - event.getDownTime();
                SXRTransform ballTrans = mCurrentBall.getTransform();
                SXRRigidBody rigidBody = (SXRRigidBody) mCurrentBall.getComponent(SXRRigidBody.getComponentType());
                Matrix4f ballMtx = ballTrans.getModelMatrix4f();

                mCurrentBall.getParent().removeChildObject(mCurrentBall);
                ballTrans.setModelMatrix(ballMtx);
                controller.getPicker().getWorldPickRay(mEndDrag, mTempDir);
                mEndDrag.sub(mStartDrag, mTempDir);
                mTempDir.mul(1000000.0f / dt);
                mScene.addNode(mCurrentBall);
                rigidBody.applyCentralForce(mTempDir.x, mTempDir.y, mTempDir.z * 4.0f);
                rigidBody.setEnable(true);
                mCurrentBall = null;
            }
        }
    };

    /*
     * Handles initializing the selected controller:
     * - attach the scene object to represent the4 cursor
     * - set cursor properties
     * If we are using the Gaze controller, it does not generate touch events directly.
     * We need to listen for them from SXRActivity to process them with a gesture detector.
     */
    private SXRInputManager.ICursorControllerSelectListener mControllerSelector = new SXRInputManager.ICursorControllerSelectListener()
    {
        public void onCursorControllerSelected(SXRCursorController newController, SXRCursorController oldController)
        {
            if (oldController != null)
            {
                if (oldController.getControllerType() == SXRControllerType.CONTROLLER)
                {
                    oldController.getEventReceiver().removeListener(mControllerThrowHandler);
                    if ((mCurrentBall != null) && (mCurrentBall.getParent() != null))
                    {
                        mCurrentBall.getParent().removeChildObject(mCurrentBall);
                        mCurrentBall = null;
                    }
                }
            }
            mController = newController;
            if (newController.getControllerType() == SXRControllerType.CONTROLLER)
            {
                newController.getEventReceiver().addListener(mControllerThrowHandler);
            }
            newController.setCursor(mCursor);
        }
    };

    @Override
    public void onInit(SXRContext sxrContext)
    {
        mScene = sxrContext.getMainScene();
        mCamera = mScene.getMainCameraRig();
        mCamera.getTransform().setPosition(0.0f, 6.0f, 20f);

        mCursor = MainHelper.createGaze(sxrContext, 0.0f, 0.0f, 0.0f);
        mBallProto = new SXRSphereNode(sxrContext, true, new SXRMaterial(sxrContext, SXRMaterial.SXRShaderType.Phong.ID));
        initScene(sxrContext, mScene);
        initLabels(sxrContext, mScene);
        mWorld = new SXRWorld(mScene, MainHelper.collisionMatrix);
        mScene.getEventReceiver().addListener(this);
        sxrContext.getInputManager().selectController(mControllerSelector);
        mWorld.setEnable(true);
    }

    private void initScene(SXRContext context, SXRScene scene) {
        final float intensity = 1.0f;
        mScene.setBackgroundColor(1.0f * intensity, 0.956f * intensity, 0.84f * intensity, 1f);

        addLights(context, scene);
        addGround(context, scene);
        addCylinderGroup(context, scene);
    }

    private void addTimer(SXRContext context, SXRScene scene) {
        SXRTextViewNode label = MainHelper.createLabel(context, 6f, 9f, -5f);
        mCountDown = new Countdown(label);

        scene.addNode(label);

        mCountDown.start(context);
    }

    private void initLabels(SXRContext context, SXRScene scene) {
        mEndGameLabel = null;
        mScoreLabel = MainHelper.createLabel(context, 0f, 9f, -5f);
        mBallsLabel = MainHelper.createLabel(context, -6f, 9f, -5f);

        mScoreLabel.setText("Score: 0");
        mBallsLabel.setText("Balls: " + MAX_BALLS );

        scene.addNode(mScoreLabel);
        scene.addNode(mBallsLabel);

        addTimer(context, scene);
    }

    private static void addLights(SXRContext context, SXRScene scene) {
        SXRNode centerLight = MainHelper.createDirectLight(context, 0.0f, 10.0f, 2.0f);
        SXRNode leftPointLight = MainHelper.createPointLight(context, -10.0f, 5.0f, 20.0f);
        SXRNode rightPointLight = MainHelper.createPointLight(context, 10.0f, 5.0f, 20.0f);

        centerLight.getTransform().rotateByAxis(-90, 1, 0, 0);

        scene.addNode(centerLight);
        scene.addNode(leftPointLight);
        scene.addNode(rightPointLight);
    }

    private static void addGround(SXRContext context, SXRScene scene) {
        scene.addNode(MainHelper.createGround(context, 0.0f, 0.0f, 0.0f));
    }

    private void addCylinderGroup(SXRContext context, SXRScene scene) {
        final int[] CYLINDER_COLORS = {R.drawable.black, R.drawable.brown,
                R.drawable.green, R.drawable.grey, R.drawable.orange, R.drawable.pink,
                R.drawable.red, R.drawable.yellow, R.drawable.light_blue, R.drawable.light_green,
                R.drawable.dark_blue, R.drawable.cy};

        final int SQUARE_SIZE = 3;
        float offset = 0;
        try {
            for (int y = 0; y < SQUARE_SIZE; y++) {
                for (int x = 0; x < SQUARE_SIZE - y; x++) {
                    for (int z = 0; z < SQUARE_SIZE; z++) {
                        addCylinder(context, scene, (x - (SQUARE_SIZE / 2.0f)) * 2.5f + 1.5f + offset,
                                1f + (y * 1.2f), (z + (SQUARE_SIZE / 2.0f)) * 2.5f - 5f,
                                CYLINDER_COLORS[mNumCylinders++ % CYLINDER_COLORS.length]);
                    }
                }
                offset += 1.25f;
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private static void addCylinder(SXRContext context, SXRScene scene, float x, float y, float z,
                                    int drawable) throws IOException {
        scene.addNode(MainHelper.createCylinder(context, x, y, z, drawable));
    }

    @Override
    public void onSwipe(SXRTouchPadGestureListener.Action action, float velocityX) {

        if (gameStopped() || (mController.getControllerType() == SXRControllerType.CONTROLLER)) {
            return;
        }
        int normal = MathUtils.calculateForce(velocityX);
        float[] forward = MathUtils.calculateRotation( mCamera.getHeadTransform().getRotationPitch(), mCamera.getHeadTransform().getRotationYaw());
        float[] force = {normal * forward[0], normal * forward[1], normal * forward[2]};

        try {
            SXRTransform trans = mCamera.getTransform();
            SXRNode ball = MainHelper.createBall(mBallProto, mScene,
                    5 * forward[0] + trans.getPositionX(),
                    5 * forward[1] + trans.getPositionY(),
                    5 * forward[2] + trans.getPositionZ());

            SXRRigidBody body = (SXRRigidBody) ball.getComponent(SXRRigidBody.getComponentType());
            body.applyCentralForce(force[0], force[1], force[2]);
            mNumBalls++;
            mBallsLabel.setText("Balls: " + (MAX_BALLS - mNumBalls));
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }


    private SXRNode newBall()
    {
        try
        {
            SXRNode ball = MainHelper.createBall(mBallProto, mScene, 0,0,0);
            mScene.removeNode(ball);
            mNumBalls++;
            mBallsLabel.setText("Balls: " + (MAX_BALLS - mNumBalls));
            return ball;
        }
        catch (IOException ioe)
        {
            ioe.printStackTrace();
        }
        return null;
    }

    @Override
    public void onStep() {
        if (gameFinished()) return;

        mTimeTicker = mTimeTicker++ % 120;
        if (mTimeTicker != 0) {
            return;
        }

        if (gameStopped()) {
            // Score
            SCORE_OFFSET = -1;
            mScene.getRoot().forAllComponents(this, SXRRigidBody.getComponentType());

            // Show finished message.
            if (mScore == mNumCylinders) {
                mEndGameLabel = new SXRTextViewNode(getSXRContext(),
                        18, 4f, "Congratulations, you won!");
            } else if (mCountDown.isFinished()){

                mEndGameLabel = new SXRTextViewNode(getSXRContext(),
                        18, 4f, "Time out! Try again.");
            } else {
                mEndGameLabel = new SXRTextViewNode(getSXRContext(),
                        18, 4f, "No shots left! Try again.");
            }

            mEndGameLabel.setTextSize(10);
            mEndGameLabel.setBackgroundColor(Color.DKGRAY);
            mEndGameLabel.setTextColor(Color.WHITE);
            mEndGameLabel.setGravity(Gravity.CENTER);
            mEndGameLabel.getTransform().setPosition(0f, 9f, -4f);

            mScene.addNode(mEndGameLabel);
        } else {
            // Score
            mScene.getRoot().forAllComponents(this, SXRRigidBody.getComponentType());
        }

    }

    @Override
    public boolean visit(SXRComponent sxrComponent) {
        if (sxrComponent.getTransform().getPositionY() < SCORE_OFFSET) {
            mScene.removeNode(sxrComponent.getOwnerObject());
            doScore((SXRRigidBody) sxrComponent);
        }

        return false;
    }

    private void doScore(SXRRigidBody body) {
        if (body.getCollisionGroup() != MainHelper.COLLISION_GROUP_CYLINDER) {
            return;
        }

        mScore ++;
        mScoreLabel.setText("Score: " + mScore);
    }

    private boolean gameFinished() {
        return mEndGameLabel != null;
    }

    private boolean gameStopped() {
        return  mNumBalls == MAX_BALLS || mCountDown.isFinished() || mScore == mNumCylinders;
    }
}
