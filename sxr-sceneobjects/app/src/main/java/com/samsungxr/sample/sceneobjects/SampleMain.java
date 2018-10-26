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

package com.samsungxr.sample.sceneobjects;

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.Gravity;
import android.webkit.WebView;

import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRMain;
import com.samsungxr.SXRMaterial;
import com.samsungxr.SXRScene;
import com.samsungxr.SXRNode;
import com.samsungxr.SXRTexture;
import com.samsungxr.nodes.SXRCameraNode;
import com.samsungxr.nodes.SXRConeNode;
import com.samsungxr.nodes.SXRCubeNode;
import com.samsungxr.nodes.SXRCylinderNode;
import com.samsungxr.nodes.SXRSphereNode;
import com.samsungxr.nodes.SXRTextViewNode;
import com.samsungxr.nodes.SXRVideoNode;
import com.samsungxr.nodes.SXRVideoNode.SXRVideoType;
import com.samsungxr.nodes.SXRViewNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SampleMain extends SXRMain {
    private static final String TAG = SampleMain.class.getSimpleName();
    private List<SXRNode> objectList = new ArrayList<SXRNode>();

    private int currentObject = 0;
    private NodeActivity mActivity;

    SampleMain(NodeActivity activity) {
        mActivity = activity;
    }

    @Override
    public void onInit(SXRContext sxrContext) throws IOException {

        SXRScene scene = sxrContext.getMainScene();

        // load texture asynchronously
        SXRTexture futureTexture = sxrContext
                .getAssetLoader().loadTexture(new SXRAndroidResource(sxrContext,
                        R.drawable.gearvr_logo));
        SXRTexture futureTextureTop = sxrContext
                .getAssetLoader().loadTexture(new SXRAndroidResource(sxrContext,
                        R.drawable.top));
        SXRTexture futureTextureBottom = sxrContext
                .getAssetLoader().loadTexture(new SXRAndroidResource(sxrContext,
                        R.drawable.bottom));
        ArrayList<SXRTexture> futureTextureList = new ArrayList<SXRTexture>(
                3);
        futureTextureList.add(futureTextureTop);
        futureTextureList.add(futureTexture);
        futureTextureList.add(futureTextureBottom);

        // setup material
        SXRMaterial material = new SXRMaterial(sxrContext);
        material.setMainTexture(futureTexture);

        // create a scene object (this constructor creates a rectangular scene
        // object that uses the standard 'unlit' shader)
        SXRNode quadObject = new SXRNode(sxrContext, 4.0f, 2.0f);
        SXRCubeNode cubeObject = new SXRCubeNode(sxrContext,
                true, material);
        SXRSphereNode sphereObject = new SXRSphereNode(
                sxrContext, true, material);
        SXRCylinderNode cylinderObject = new SXRCylinderNode(
                sxrContext, true, material);
        SXRConeNode coneObject = new SXRConeNode(sxrContext,
                true, material);
        SXRViewNode webViewObject = createWebViewObject(sxrContext);
        SXRCameraNode cameraObject = null;
        try {
            cameraObject = new SXRCameraNode(sxrContext, 3.6f, 2.0f);
            cameraObject.setUpCameraForVrMode(1); // set up 60 fps camera preview.
        } catch (SXRCameraNode.SXRCameraAccessException e) {
            // Cannot open camera
            Log.e(TAG, "Cannot open the camera",e);
        }

        SXRVideoNode videoObject = createVideoObject(sxrContext);
        SXRTextViewNode textViewNode = new SXRTextViewNode(sxrContext, "Hello World!");
        textViewNode.setGravity(Gravity.CENTER);
        textViewNode.setTextSize(12);
        objectList.add(quadObject);
        objectList.add(cubeObject);
        objectList.add(sphereObject);
        objectList.add(cylinderObject);
        objectList.add(coneObject);
        objectList.add(webViewObject);
        if(cameraObject != null) {
            objectList.add(cameraObject);
        }
        objectList.add(videoObject);
        objectList.add(textViewNode);

        // turn all objects off, except the first one
        int listSize = objectList.size();
        for (int i = 1; i < listSize; i++) {
            objectList.get(i).setEnable(false);
        }

        quadObject.getRenderData().setMaterial(material);

        // set the scene object positions
        quadObject.getTransform().setPosition(0.0f, 0.0f, -3.0f);
        cubeObject.getTransform().setPosition(0.0f, -1.0f, -3.0f);
        cylinderObject.getTransform().setPosition(0.0f, 0.0f, -3.0f);
        coneObject.getTransform().setPosition(0.0f, 0.0f, -3.0f);
        sphereObject.getTransform().setPosition(0.0f, -1.0f, -3.0f);
        if (cameraObject != null)
            cameraObject.getTransform().setPosition(0.0f, 0.0f, -4.0f);
        videoObject.getTransform().setPosition(0.0f, 0.0f, -4.0f);
        textViewNode.getTransform().setPosition(0.0f, 0.0f, -2.0f);

        // add the scene objects to the scene graph.
        // deal differently with camera scene object: we want it to move
        // with the camera.
        for (SXRNode object : objectList) {
            if (object instanceof SXRCameraNode) {
                scene.getMainCameraRig().addChildObject(object);
            } else {
                scene.addNode(object);
            }
        }
    }

    private SXRVideoNode createVideoObject(SXRContext sxrContext) throws IOException {
        final AssetFileDescriptor afd = sxrContext.getActivity().getAssets().openFd("tron.mp4");
        final MediaPlayer mediaPlayer = new MediaPlayer();
        mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
        mediaPlayer.prepare();
        SXRVideoNode video = new SXRVideoNode(sxrContext, 8.0f,
                4.0f, mediaPlayer, SXRVideoType.MONO);
        video.setName("video");
        return video;
    }

    private SXRViewNode createWebViewObject(SXRContext sxrContext) {
        WebView webView = mActivity.getWebView();
        SXRViewNode webObject = new SXRViewNode(sxrContext,
                webView, 8.0f, 4.0f);
        webObject.setName("web view object");
        webObject.getRenderData().getMaterial().setOpacity(1.0f);
        webObject.getTransform().setPosition(0.0f, 0.0f, -4.0f);

        return webObject;
    }

    public void onPause() {
        if (objectList.isEmpty()) {
            return;
        }

        SXRNode object = objectList.get(currentObject);
        if (object instanceof SXRVideoNode) {
            SXRVideoNode video = (SXRVideoNode) object;
            video.getMediaPlayer().pause();
        }
    }

    public void onTap() {
        SXRNode object = objectList.get(currentObject);
        object.setEnable(false);
        if (object instanceof SXRVideoNode) {
            SXRVideoNode video = (SXRVideoNode) object;
            video.getMediaPlayer().pause();
        }

        currentObject++;
        int totalObjects = objectList.size();
        if (currentObject >= totalObjects) {
            currentObject = 0;
        }

        object = objectList.get(currentObject);
        if (object instanceof SXRVideoNode) {
            SXRVideoNode video = (SXRVideoNode) object;
            video.getMediaPlayer().start();
        }

        object.setEnable(true);
    }
}
