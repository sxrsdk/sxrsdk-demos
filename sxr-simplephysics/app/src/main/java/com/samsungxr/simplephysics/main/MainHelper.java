package com.samsungxr.simplephysics.main;

import android.graphics.Color;
import android.view.Gravity;

import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRDirectLight;
import com.samsungxr.SXRMaterial;
import com.samsungxr.SXRMesh;
import com.samsungxr.SXRMeshCollider;
import com.samsungxr.SXRPointLight;
import com.samsungxr.SXRRenderData;
import com.samsungxr.SXRSceneObject;
import com.samsungxr.SXRSphereCollider;
import com.samsungxr.SXRTexture;
import com.samsungxr.simplephysics.R;
import com.samsungxr.physics.SXRCollisionMatrix;
import com.samsungxr.physics.SXRRigidBody;
import com.samsungxr.scene_objects.SXRCubeSceneObject;
import com.samsungxr.scene_objects.SXRTextViewSceneObject;

import java.io.IOException;
import java.util.List;

/**
 * Created by ragner on 11/9/16.
 */
public class MainHelper {
    private static final float CYLINDER_MASS = 0.3f;
    private static final float BALL_MASS = 0.5f;
    private static final int COLLISION_GROUP_INFINITY_GROUND = 0;
    private static final int COLLISION_GROUP_GROUND = 1;
    private static final int COLLISION_GROUP_BALL = 3;
    public static final int COLLISION_GROUP_CYLINDER = 2;

    public static SXRCollisionMatrix collisionMatrix;

    static {
        collisionMatrix = new SXRCollisionMatrix();

        collisionMatrix.setCollisionFilterMask(COLLISION_GROUP_INFINITY_GROUND, (short) 0x0);

        collisionMatrix.enableCollision(COLLISION_GROUP_CYLINDER, COLLISION_GROUP_GROUND);
        collisionMatrix.enableCollision(COLLISION_GROUP_BALL, COLLISION_GROUP_GROUND);
        collisionMatrix.enableCollision(COLLISION_GROUP_BALL, COLLISION_GROUP_CYLINDER);
    }

    public static SXRSceneObject createPointLight(SXRContext context, float x, float y, float z) {
        SXRSceneObject lightObject = new SXRSceneObject(context);
        SXRPointLight light = new SXRPointLight(context);

       float ambientIntensity = 0.5f;
       float diffuseIntensity = 1.0f;

        light.setAmbientIntensity(1.0f * ambientIntensity, 0.95f * ambientIntensity, 0.83f * ambientIntensity, 0.0f);
        light.setDiffuseIntensity(1.0f * diffuseIntensity, 0.95f * diffuseIntensity, 0.83f * diffuseIntensity, 0.0f);
        light.setSpecularIntensity(0.0f, 0.0f, 0.0f, 0.0f);

        //lightObject.getTransform().setScale(1.0f, 1.0f, 5.0f);
        lightObject.getTransform().setPosition(x, y, z);
        lightObject.attachLight(light);
        return lightObject;
    }

   public static SXRSceneObject createDirectLight(SXRContext context, float x, float y, float z) {
        SXRSceneObject lightObject = new SXRSceneObject(context);
        SXRDirectLight light = new SXRDirectLight(context);

        light.setCastShadow(true);

       float ambientIntensity = 0.1f;
       float diffuseIntensity = 1.0f;

        light.setAmbientIntensity(1.0f * ambientIntensity, 0.95f * ambientIntensity, 0.83f * ambientIntensity, 0.0f);
        light.setDiffuseIntensity(1.0f * diffuseIntensity, 0.95f * diffuseIntensity, 0.83f * diffuseIntensity, 0.0f);
        light.setSpecularIntensity(0.0f, 0.0f, 0.0f, 0.0f);

        lightObject.getTransform().setPosition(x, y, z);
        lightObject.attachLight(light);
        return lightObject;
    }

    public static SXRSceneObject createGround(SXRContext context, float x, float y, float z) {
        SXRTexture texture = context.getAssetLoader().loadTexture(new SXRAndroidResource(context, R.drawable.orange));
        SXRMaterial material = new SXRMaterial(context, SXRMaterial.SXRShaderType.Phong.ID);

        SXRSceneObject groundObject = new SXRCubeSceneObject(context, true, texture);

        groundObject.getRenderData().setMaterial(material);
        groundObject.getRenderData().getMaterial().setTexture("diffuseTexture", texture);
        groundObject.getRenderData().getMaterial().setMainTexture(texture);
        groundObject.getTransform().setScale(15.0f, 0.5f, 15.0f);
        groundObject.getTransform().setPosition(x, y, z);

        // Collider
        SXRMeshCollider meshCollider = new SXRMeshCollider(context, groundObject.getRenderData().getMesh());
        groundObject.attachCollider(meshCollider);

        // Physics body
        SXRRigidBody body = new SXRRigidBody(context, 0.0f, COLLISION_GROUP_GROUND);
        body.setRestitution(0.5f);
        body.setFriction(1.0f);
        groundObject.attachComponent(body);

        return groundObject;
    }

    public static SXRSceneObject createCylinder(SXRContext context, float x, float y, float z,
                                                 int drawable) throws IOException {
        SXRTexture texture = context.getAssetLoader().loadTexture(new SXRAndroidResource(context, drawable));
        SXRMaterial mtl = new SXRMaterial(context, SXRMaterial.SXRShaderType.Phong.ID);
        SXRMesh mesh = context.getAssetLoader().loadMesh(new SXRAndroidResource(context, "cylinder.fbx"));
        SXRSceneObject cylinderObject = new SXRSceneObject(context, mesh, mtl);

        cylinderObject.getTransform().setPosition(x, y, z);
        cylinderObject.getTransform().setRotationByAxis(90.0f, 1.0f, 0.0f, 0.0f);
        mtl.setTexture("diffuseTexture", texture);

        // Collider
        SXRMeshCollider meshCollider = new SXRMeshCollider(context, false);
        cylinderObject.attachCollider(meshCollider);

        // Physics body
        SXRRigidBody body = new SXRRigidBody(context, CYLINDER_MASS, COLLISION_GROUP_CYLINDER);
        body.setRestitution(0.5f);
        body.setFriction(5.0f);
        cylinderObject.attachComponent(body);

        return cylinderObject;
    }

    public static SXRSceneObject createBall(SXRSceneObject ballProto, float x, float y, float z,
                                            float[] force) throws IOException {
        SXRContext context = ballProto.getSXRContext();
        SXRSceneObject ballGeometry = new SXRSceneObject(context, ballProto.getRenderData().getMesh(), ballProto.getRenderData().getMaterial());
        ballGeometry.getTransform().setScale(0.7f, 0.7f, 0.7f);
        ballGeometry.getTransform().setPosition(x, y, z);

        SXRSphereCollider sphereCollider = new SXRSphereCollider(context);
        sphereCollider.setRadius(1.0f);
        ballGeometry.attachCollider(sphereCollider);

        SXRRigidBody rigidBody = new SXRRigidBody(context, BALL_MASS, COLLISION_GROUP_BALL);
        rigidBody.setRestitution(1.5f);
        rigidBody.setFriction(0.5f);
        rigidBody.applyCentralForce(force[0], force[1], force[2]);
        ballGeometry.attachComponent(rigidBody);
        return ballGeometry;
    }

    public static SXRSceneObject createGaze(SXRContext context, float x, float y, float z) {
        SXRMesh mesh = new SXRMesh(context, "float3 a_position float2 a_texcoord");
        mesh.createQuad(0.1f, 0.1f);
        SXRSceneObject gaze = new SXRSceneObject(context, mesh,
                context.getAssetLoader().loadTexture(new SXRAndroidResource(context, R.drawable.gaze)));

        gaze.getTransform().setPosition(x, y, z);
        gaze.getRenderData().setDepthTest(false);
        gaze.getRenderData().setRenderingOrder(100000);
        gaze.getRenderData().disableLight();

        return gaze;
    }

    public static SXRTextViewSceneObject createLabel(SXRContext context, float x, float y, float z) {
        SXRTextViewSceneObject textObject = new SXRTextViewSceneObject(context, 5f, 2f, "00");
        textObject.setTextColor(Color.BLACK);
        textObject.setGravity(Gravity.CENTER);
        textObject.setTextSize(20);
        textObject.setRefreshFrequency(SXRTextViewSceneObject.IntervalFrequency.LOW);
        textObject.getTransform().setPosition(x, y, z);
        return textObject;
    }
}
