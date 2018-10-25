package com.samsungxr.modelviewer2;


import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRSceneObject;
import com.samsungxr.SXRTexture;
import com.samsungxr.scene_objects.SXRSphereSceneObject;
import com.samsungxr.utility.Log;

import java.util.ArrayList;
import java.util.concurrent.Future;

public class SkyBox {
    SXRSphereSceneObject skyBoxModel;
    String skyBoxName;

    public SkyBox(String skyBoxName) {
        this.skyBoxName = skyBoxName;
    }

    public String getSkyBoxName(){
        return skyBoxName;
    }

    private SXRSphereSceneObject loadSkyBoxModel(SXRContext gvrContext, String skyBoxPath, String skyBoxName) {
        SXRSphereSceneObject sphereObject = null;

        // load texture
        SXRTexture texture = null;
        try {
            texture = gvrContext.getAssetLoader().loadTexture(new SXRAndroidResource(gvrContext, skyBoxPath + skyBoxName));
        } catch (Exception e) {
            e.printStackTrace();
        }
        // create a sphere scene object with the specified texture and triangles facing inward (the 'false' argument)
        sphereObject = new SXRSphereSceneObject(gvrContext, false, texture);
        sphereObject.getTransform().setScale(100, 100, 100);
        return sphereObject;
    }

    public SXRSphereSceneObject getSkyBox(SXRContext gvrContext, String skyBoxPath) {
        if (skyBoxModel == null) {
            skyBoxModel = loadSkyBoxModel(gvrContext, skyBoxPath, skyBoxName);
            return skyBoxModel;
        } else {
            return skyBoxModel;
        }
    }

    private SXRSphereSceneObject loadSkyBoxModelFromSD(SXRContext gvrContext, String skyBoxPath, String skyBoxName) {
        SXRSphereSceneObject sphereObject = null;

        // load texture
        SXRTexture texture = null;
        try {
            texture = gvrContext.getAssetLoader().loadTexture(new SXRAndroidResource(skyBoxPath + skyBoxName));

        } catch (Exception e) {
            e.printStackTrace();
        }
        // create a sphere scene object with the specified texture and triangles facing inward (the 'false' argument)
        sphereObject = new SXRSphereSceneObject(gvrContext, false, texture);
        sphereObject.getTransform().setScale(100, 100, 100);
        return sphereObject;
    }

    public SXRSphereSceneObject getSkyBoxFromSD(SXRContext gvrContext, String skyBoxPath) {
        if (skyBoxModel == null) {
            skyBoxModel = loadSkyBoxModelFromSD(gvrContext, skyBoxPath, skyBoxName);
            return skyBoxModel;
        } else {
            return skyBoxModel;
        }
    }


}