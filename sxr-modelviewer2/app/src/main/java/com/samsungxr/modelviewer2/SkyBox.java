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

    private SXRSphereSceneObject loadSkyBoxModel(SXRContext sxrContext, String skyBoxPath, String skyBoxName) {
        SXRSphereSceneObject sphereObject = null;

        // load texture
        SXRTexture texture = null;
        try {
            texture = sxrContext.getAssetLoader().loadTexture(new SXRAndroidResource(sxrContext, skyBoxPath + skyBoxName));
        } catch (Exception e) {
            e.printStackTrace();
        }
        // create a sphere scene object with the specified texture and triangles facing inward (the 'false' argument)
        sphereObject = new SXRSphereSceneObject(sxrContext, false, texture);
        sphereObject.getTransform().setScale(100, 100, 100);
        return sphereObject;
    }

    public SXRSphereSceneObject getSkyBox(SXRContext sxrContext, String skyBoxPath) {
        if (skyBoxModel == null) {
            skyBoxModel = loadSkyBoxModel(sxrContext, skyBoxPath, skyBoxName);
            return skyBoxModel;
        } else {
            return skyBoxModel;
        }
    }

    private SXRSphereSceneObject loadSkyBoxModelFromSD(SXRContext sxrContext, String skyBoxPath, String skyBoxName) {
        SXRSphereSceneObject sphereObject = null;

        // load texture
        SXRTexture texture = null;
        try {
            texture = sxrContext.getAssetLoader().loadTexture(new SXRAndroidResource(skyBoxPath + skyBoxName));

        } catch (Exception e) {
            e.printStackTrace();
        }
        // create a sphere scene object with the specified texture and triangles facing inward (the 'false' argument)
        sphereObject = new SXRSphereSceneObject(sxrContext, false, texture);
        sphereObject.getTransform().setScale(100, 100, 100);
        return sphereObject;
    }

    public SXRSphereSceneObject getSkyBoxFromSD(SXRContext sxrContext, String skyBoxPath) {
        if (skyBoxModel == null) {
            skyBoxModel = loadSkyBoxModelFromSD(sxrContext, skyBoxPath, skyBoxName);
            return skyBoxModel;
        } else {
            return skyBoxModel;
        }
    }


}