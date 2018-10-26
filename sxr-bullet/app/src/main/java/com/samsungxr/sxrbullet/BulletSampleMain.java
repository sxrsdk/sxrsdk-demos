package com.samsungxr.sxrbullet;

import android.graphics.Color;
import android.util.Log;

import com.samsungxr.FutureWrapper;
import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRBoxCollider;
import com.samsungxr.SXRCameraRig;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRMain;
import com.samsungxr.SXRMesh;
import com.samsungxr.SXRMeshCollider;
import com.samsungxr.SXRScene;
import com.samsungxr.SXRSceneObject;
import com.samsungxr.SXRSphereCollider;
import com.samsungxr.SXRTexture;
import com.samsungxr.physics.SXRRigidBody;
import com.samsungxr.physics.SXRWorld;
import com.samsungxr.physics.ICollisionEvents;

import java.io.IOException;

public class BulletSampleMain extends SXRMain {

    public class CollisionHandler implements ICollisionEvents {
        private SXRTexture blueObject;

        CollisionHandler() {
            try {
                blueObject = mSXRContext.getAssetLoader().loadTexture(new SXRAndroidResource(mSXRContext, "sphereblue.jpg"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void swapTextures(SXRSceneObject sceneObj0) {
            SXRTexture tmp = sceneObj0.getRenderData().getMaterial().getMainTexture();

            sceneObj0.getRenderData().getMaterial().setMainTexture(blueObject);

            blueObject = tmp;
        }

        public void onEnter(SXRSceneObject sceneObj0, SXRSceneObject sceneObj1, float normal[], float distance) {
            swapTextures(sceneObj0);
        }

       public void onExit(SXRSceneObject sceneObj0, SXRSceneObject sceneObj1, float normal[], float distance) {
            swapTextures(sceneObj0);
        }

    }

    private static final float CUBE_MASS = 0.5f;
    private static final float BALL_MASS = 2.5f;
    private SXRContext mSXRContext = null;
    private SXRRigidBody mSphereRigidBody = null;
    private CollisionHandler mCollisionHandler;
    private SXRScene mScene = null;

    @Override
    public void onInit(SXRContext sxrContext) throws Throwable {
        mSXRContext = sxrContext;
        mCollisionHandler = new CollisionHandler();
        SXRScene scene = mSXRContext.getMainScene();

        SXRCameraRig mainCameraRig = scene.getMainCameraRig();
        mainCameraRig.getLeftCamera().setBackgroundColor(Color.BLACK);
        mainCameraRig.getRightCamera().setBackgroundColor(Color.BLACK);

        mainCameraRig.getTransform().setPosition(0.0f, 6.0f, 0.0f);

        addGroundMesh(scene, 0.0f, 0.4f, 0.0f, 0.0f);

        /*
         * Create Some cubes in Bullet world and hit it with a sphere
         */
        addCube(scene, 0.0f, 1.0f, -9.0f, CUBE_MASS);
        addCube(scene, 0.0f, 1.0f, -10.0f, CUBE_MASS);
        addCube(scene, 0.0f, 1.0f, -11.0f, CUBE_MASS);
        addCube(scene, 1.0f, 1.0f, -9.0f, CUBE_MASS);
        addCube(scene, 1.0f, 1.0f, -10.0f, CUBE_MASS);
        addCube(scene, 1.0f, 1.0f, -11.0f, CUBE_MASS);
        addCube(scene, 2.0f, 1.0f, -9.0f, CUBE_MASS);
        addCube(scene, 2.0f, 1.0f, -10.0f, CUBE_MASS);
        addCube(scene, 2.0f, 1.0f, -11.0f, CUBE_MASS);

        addCube(scene, 0.0f, 2.0f, -9.0f, CUBE_MASS);
        addCube(scene, 0.0f, 2.0f, -10.0f, CUBE_MASS);
        addCube(scene, 0.0f, 2.0f, -11.0f, CUBE_MASS);
        addCube(scene, 1.0f, 2.0f, -9.0f, CUBE_MASS);
        addCube(scene, 1.0f, 2.0f, -10.0f, CUBE_MASS);
        addCube(scene, 1.0f, 2.0f, -11.0f, CUBE_MASS);
        addCube(scene, 2.0f, 2.0f, -9.0f, CUBE_MASS);
        addCube(scene, 2.0f, 2.0f, -10.0f, CUBE_MASS);
        addCube(scene, 2.0f, 2.0f, -11.0f, CUBE_MASS);

        addCube(scene, 0.0f, 3.0f, -9.0f, CUBE_MASS);
        addCube(scene, 0.0f, 3.0f, -10.0f, CUBE_MASS);
        addCube(scene, 0.0f, 3.0f, -11.0f, CUBE_MASS);
        addCube(scene, 1.0f, 3.0f, -9.0f, CUBE_MASS);
        addCube(scene, 1.0f, 3.0f, -10.0f, CUBE_MASS);
        addCube(scene, 1.0f, 3.0f, -11.0f, CUBE_MASS);
        addCube(scene, 2.0f, 3.0f, -9.0f, CUBE_MASS);
        addCube(scene, 2.0f, 3.0f, -10.0f, CUBE_MASS);
        addCube(scene, 2.0f, 3.0f, -11.0f, CUBE_MASS);

        /*
         * Throw a sphere from top
         */
        addSphere(scene, 1.0f, 1.5f, 40.0f, -10.0f, BALL_MASS);

        scene.getRoot().attachComponent(new SXRWorld(sxrContext));

        mScene = scene;
    }

    int randomActions = -1;

    public void touchEvent() {
        SXRWorld world = mSphereRigidBody.getWorld();
        if (randomActions < 0) {
            /*
            0 - Enable/disable world simulation
            1 - Enable/disable body simulation
            2 - Add/Remove scene object
            3 - apply force on the ball
             */
            randomActions = (int) System.currentTimeMillis() % 4;
        }
        switch (randomActions)
        {
            case 0:
            if (world.isEnabled()) {
                world.disable();
            } else {
                world.enable();
                randomActions = -1;
            }
            break;

            case 1:
            if (mSphereRigidBody.isEnabled()) {
                mSphereRigidBody.disable();
            } else {
                mSphereRigidBody.enable();
                randomActions = -1;
            }
            break;

            case 2:
            SXRSceneObject owner = mSphereRigidBody.getOwnerObject();

            if (world != null) {
                mScene.removeSceneObject(owner);
            } else {
                mScene.addSceneObject(owner);
                randomActions = -1;
            }
            break;

            default:
            mSphereRigidBody.applyCentralForce(-20.0f, 900.0f, 0.0f);
            mSphereRigidBody.applyTorque(5.0f, 0.5f, 0.0f);
            randomActions = -1;
        }
    }

    @Override
    public void onStep() {

    }

    private SXRSceneObject quadWithTexture(float width, float height,
                                           String texture) {
        SXRMesh mesh = new SXRMesh(getSXRContext());
        mesh.createQuad(width, height);
        try
        {
            SXRTexture tex = mSXRContext.getAssetLoader().loadTexture(
                                    new SXRAndroidResource(mSXRContext, texture));
            return new SXRSceneObject(mSXRContext, width, height, tex);
            // TODO: Create mesh collider to ground and add SXRCollision component
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private SXRSceneObject meshWithTexture(String mesh, String texture) {
        SXRSceneObject object = null;
        try {
            object = new SXRSceneObject(mSXRContext, new SXRAndroidResource(
                    mSXRContext, mesh), new SXRAndroidResource(mSXRContext,
                    texture));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return object;
    }

    private void addGroundMesh(SXRScene scene, float x, float y, float z, float mass) {
        try {
            SXRTexture texture = mSXRContext.getAssetLoader().loadTexture(new SXRAndroidResource(mSXRContext, "floor.jpg"));
            SXRSceneObject meshObject =  new SXRSceneObject(mSXRContext, 100.0f, 100.0f, texture);

            meshObject.getTransform().setPosition(x, y, z);
            meshObject.getTransform().setRotationByAxis(-90.0f, 1.0f, 0.0f, 0.0f);

            // Collider
            SXRMeshCollider meshCollider = new SXRMeshCollider(mSXRContext, false);
            meshObject.attachCollider(meshCollider);

            // Physics body
            SXRRigidBody body = new SXRRigidBody(mSXRContext);

            body.setRestitution(0.5f);
            body.setFriction(1.0f);

            meshObject.attachComponent(body);

            scene.addSceneObject(meshObject);
        } catch (IOException exception) {
            Log.d("sxr", exception.toString());
        }
    }

    /*
     * Function to add a cube of unit size with mass at the specified position
     * in Bullet physics world and scene graph.
     */
    private void addCube(SXRScene scene, float x, float y, float z, float mass) {

        SXRSceneObject cubeObject = meshWithTexture("cube.obj", "cube.jpg");
        cubeObject.getTransform().setPosition(x, y, z);

        // Collider
        SXRBoxCollider boxCollider = new SXRBoxCollider(mSXRContext);
        boxCollider.setHalfExtents(0.5f, 0.5f, 0.5f);
        cubeObject.attachCollider(boxCollider);

        // Physics body
        SXRRigidBody body = new SXRRigidBody(mSXRContext, mass);

        body.setRestitution(0.5f);
        body.setFriction(1.0f);

        cubeObject.attachComponent(body);

        scene.addSceneObject(cubeObject);
    }

    /*
     * Function to add a sphere of dimension and position specified in the
     * Bullet physics world and scene graph
     */
    private void addSphere(SXRScene scene, float radius, float x, float y,
                           float z, float mass) {

        SXRSceneObject sphereObject = meshWithTexture("sphere.obj",
                "sphere.jpg");
        sphereObject.getTransform().setPosition(x, y, z);

        // Collider
        SXRSphereCollider sphereCollider = new SXRSphereCollider(mSXRContext);
        sphereCollider.setRadius(1.0f);
        sphereObject.attachCollider(sphereCollider);

        // Physics body
        mSphereRigidBody = new SXRRigidBody(mSXRContext, mass);

        mSphereRigidBody.setRestitution(1.5f);
        mSphereRigidBody.setFriction(0.5f);
        sphereObject.getEventReceiver().addListener(mCollisionHandler);

        sphereObject.attachComponent(mSphereRigidBody);

        scene.addSceneObject(sphereObject);
    }

}
