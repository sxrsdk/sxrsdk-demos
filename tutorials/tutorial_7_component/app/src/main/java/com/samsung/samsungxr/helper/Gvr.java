package com.samsung.gearvrf.helper;

import android.graphics.Color;

import com.example.org.sxrfapplication.R;

import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRMesh;
import com.samsungxr.SXRPicker;
import com.samsungxr.SXRRenderData;
import com.samsungxr.SXRNode;
import com.samsungxr.SXRTexture;
import com.samsungxr.IEvents;
import com.samsungxr.io.SXRInputManager;
import com.samsungxr.nodes.SXRCubeNode;
import com.samsungxr.utility.Log;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class Gvr {
    private static SXRContext s_Context = null;
    private static String TAG = "SXR Helper";

    public static void init(SXRContext context){
        s_Context = context;
    }

    /***************************************
     * Scene Objects
     ***************************************/

    public static SXRCubeNode createCube(){
        if (s_Context == null) {
            Log.e(TAG, "SXRContext is not initialized");
            return null;
        }

        SXRCubeNode cube = new SXRCubeNode(s_Context);
        cube.getRenderData().getMaterial().setColor(Color.WHITE);
        return cube;
    }

    public static SXRNode createMesh(int meshID, int textureID) {
        if (s_Context == null) {
            Log.e(TAG, "SXRContext is not initialized");
            return null;
        }

        SXRMesh mesh = s_Context.getAssetLoader().loadMesh(new SXRAndroidResource(s_Context, meshID));
        SXRTexture texture = s_Context.getAssetLoader().loadTexture(new SXRAndroidResource(s_Context, textureID));
        SXRNode sceneObject = new SXRNode(s_Context, mesh, texture);

        return sceneObject;
    }

    public static SXRNode createQuad(float width, float height, int textureID){
        SXRNode quad = new SXRNode(s_Context,
                s_Context.createQuad(width, height),
                s_Context.getAssetLoader().loadTexture(new SXRAndroidResource(s_Context, textureID)));

        return quad;
    }

    /***************************************
     * Utils
     ***************************************/

    static Matrix4f reverseMatrix(SXRNode object, Matrix4f worldMat){
        Matrix4f mat = new Matrix4f();
        Matrix4f newMat = new Matrix4f(worldMat);

        object.getTransform().getLocalModelMatrix4f().invert(mat);
        newMat.mul(mat);

        return newMat;
    }

    public static Vector3f getWorldDirection(SXRNode object) {
        Matrix4f tmp = object.getTransform().getModelMatrix4f();

        Vector3f dir = new Vector3f(-tmp.m20(), -tmp.m21(), -tmp.m22());

        return dir;
    }

    public static Quaternionf getWorldRotation(SXRNode object) {
        Matrix4f tmp = object.getTransform().getModelMatrix4f();

        Quaternionf rot = new Quaternionf();
        tmp.getNormalizedRotation(rot);

        return rot;
    }

    public static Vector3f getWorldPosition(SXRNode object) {
        Matrix4f tmp = object.getTransform().getModelMatrix4f();

        Vector3f pos = tmp.getTranslation(new Vector3f());

        return pos;
    }
}
