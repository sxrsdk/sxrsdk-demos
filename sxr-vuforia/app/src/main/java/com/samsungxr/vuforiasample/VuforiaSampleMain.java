package com.samsungxr.vuforiasample;

import java.io.IOException;

import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRDrawFrameListener;
import com.samsungxr.SXRMaterial;
import com.samsungxr.SXRMaterial.SXRShaderType;
import com.samsungxr.SXRRenderData.SXRRenderingOrder;
import com.samsungxr.SXRMesh;
import com.samsungxr.SXRRenderData;
import com.samsungxr.SXRRenderPass;
import com.samsungxr.SXRRenderTexture;
import com.samsungxr.SXRScene;
import com.samsungxr.SXRSceneObject;
import com.samsungxr.SXRMain;
import com.samsungxr.SXRShaderId;
import com.samsungxr.SXRTexture;

import com.vuforia.GLTextureData;
import com.vuforia.GLTextureUnit;
import com.vuforia.ImageTarget;
import com.vuforia.Matrix44F;
import com.vuforia.Renderer;
import com.vuforia.State;
import com.vuforia.Tool;
import com.vuforia.Trackable;
import com.vuforia.TrackableResult;
import com.vuforia.VideoBackgroundTextureInfo;
import com.vuforia.samples.SampleApplication.SampleApplicationSession;

import android.opengl.Matrix;
import android.util.Log;

public class VuforiaSampleMain extends SXRMain {

    private static final String TAG = "sxr-vuforia";

    private SXRContext sxrContext = null;
    private SXRSceneObject teapot = null;
    private SXRSceneObject passThroughObject = null;
    private Renderer mRenderer = null;
    SampleApplicationSession vuforiaAppSession = null;

    static final int VUFORIA_CAMERA_WIDTH = 1024;
    static final int VUFORIA_CAMERA_HEIGHT = 1024;

    private volatile boolean init = false;

    private SXRScene mainScene;

    private float[] vuforiaMVMatrix;
    private float[] totalMVMatrix;

    private boolean teapotVisible = false;
    boolean isReady = false;

    //ModelShader modelShader = null;

    boolean isPassThroughVisible = false;

    SXRTexture passThroughTexture;

    @Override
    public void onInit(SXRContext sxrContext) {
        this.sxrContext = sxrContext;
        mainScene = sxrContext.getMainScene();
        mainScene.getMainCameraRig().setFarClippingDistance(20000);

        createTeaPotObject();

        vuforiaMVMatrix = new float[16];
        totalMVMatrix = new float[16];

        initRendering();

        init = true;
    }

    private void initRendering() {
        mRenderer = Renderer.getInstance();
    }

    @Override
    public void onStep() {
        if (!isReady)
            return;

        updateObjectPose();
    }

    @Override
    public SplashMode getSplashMode() {
        return SplashMode.NONE;
    }

    void onVuforiaInitialized() {
        sxrContext.runOnGlThread(new Runnable() {
            @Override
            public void run() {
                createCameraPassThrough();
            }
        });
    }

    public boolean isInit() {
        return init;
    }

    private void createCameraPassThrough() {
        passThroughObject = new SXRSceneObject(sxrContext, 1.0f, 1.0f);

        passThroughObject.getTransform().setPosition(0.0f, 0.0f, -100.0f);
        passThroughObject.getTransform().setScaleX(200f);
        passThroughObject.getTransform().setScaleY(200f);

        passThroughTexture = new SXRRenderTexture(sxrContext,
                VUFORIA_CAMERA_WIDTH, VUFORIA_CAMERA_HEIGHT);

        mTextureUnit = new GLTextureUnit(0);
        GLTextureData textureData = new GLTextureData(passThroughTexture.getId());
        final boolean result = Renderer.getInstance().setVideoBackgroundTexture(textureData);
        if (!result) {
            Log.e(TAG, "Vuforia's setVideoBackgroundTexture failed");
            sxrContext.getActivity().finish();
            return;
        }

        sxrContext.registerDrawFrameListener(new SXRDrawFrameListener() {
            @Override
            public void onDrawFrame(float frameTime) {
                Renderer.getInstance().begin();
                Renderer.getInstance().updateVideoBackgroundTexture(mTextureUnit);

                if (!isPassThroughVisible) {

                    VideoBackgroundTextureInfo texInfo = Renderer.getInstance()
                            .getVideoBackgroundTextureInfo();

                    if ((texInfo.getImageSize().getData()[0] == 0)
                            || (texInfo.getImageSize().getData()[1] == 0)) {
                        Renderer.getInstance().end();
                        return;
                    }

                    // These calculate a slope for the texture coords
                    float uRatio = ((float) texInfo.getImageSize().getData()[0] / (float) texInfo
                            .getTextureSize().getData()[0]);
                    float vRatio = ((float) texInfo.getImageSize().getData()[1] / (float) texInfo
                            .getTextureSize().getData()[1]);

                    SXRRenderData renderData = passThroughObject.getRenderData();
                    SXRMaterial material = new SXRMaterial(sxrContext, SXRShaderType.Texture.ID);

                    material.setMainTexture(passThroughTexture);
                    renderData.setMaterial(material);
                    //material.setShaderType(SXRShaderType.Texture.ID);

                    float[] texCoords = { 0.0f, 0.0f, 0.0f, vRatio, uRatio, 0.0f, uRatio, vRatio };
                    SXRMesh mesh = renderData.getMesh();
                    mesh.setTexCoords(texCoords);
                    renderData.setMesh(mesh);
                    renderData.setDepthTest(false);

                    mainScene.getMainCameraRig().addChildObject(passThroughObject);
                    isPassThroughVisible = true;
                }

                Renderer.getInstance().end();
            }
        });
    }

    private void createTeaPotObject() {
        try {
            //modelShader = new ModelShader(sxrContext);
            SXRMesh teapotMesh = sxrContext.getAssetLoader().loadMesh(
                    new SXRAndroidResource(sxrContext, "teapot.obj"));
            SXRTexture teapotTexture = sxrContext.getAssetLoader().loadTexture(
                    new SXRAndroidResource(sxrContext.getContext(), "teapot_tex1.jpg"));
            teapot = new SXRSceneObject(sxrContext, teapotMesh);

            SXRMaterial material = new SXRMaterial(sxrContext, new SXRShaderId(ModelShader.class));
            material.setTexture(ModelShader.TEXTURE_KEY, teapotTexture);

            teapot.getRenderData().setMaterial(material);

            teapot.getRenderData().setDepthTest(false);
            teapot.getRenderData().setRenderingOrder(SXRRenderingOrder.OVERLAY);
            teapot.getRenderData().setCullFace(SXRRenderPass.SXRCullFaceEnum.None);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showTeapot() {
        if (teapotVisible == false) {
            mainScene.addSceneObject(teapot);
            teapotVisible = true;
        }
    }

    private void hideTeapot() {
        if (teapotVisible) {
            mainScene.removeSceneObject(teapot);
            teapotVisible = false;
        }
    }

    public void updateObjectPose() {
        State state = mRenderer.begin();

        // did we find any trackables this frame?
        int numDetectedMarkers = state.getNumTrackableResults();

        if (numDetectedMarkers == 0) {
            hideTeapot();
            return;
        }

        for (int tIdx = 0; tIdx < numDetectedMarkers; tIdx++) {
            TrackableResult result = state.getTrackableResult(tIdx);
            Trackable trackable = result.getTrackable();

            if (trackable.getId() == 1 || trackable.getId() == 2) {
                Matrix44F modelViewMatrix_Vuforia = Tool.convertPose2GLMatrix(result.getPose());
                vuforiaMVMatrix = modelViewMatrix_Vuforia.getData();

                float scaleFactor = (((ImageTarget) trackable).getSize().getData()[0])/2.0f;
                Matrix.rotateM(vuforiaMVMatrix, 0, 90, 1, 0, 0);
                Matrix.scaleM(vuforiaMVMatrix, 0, scaleFactor, scaleFactor, scaleFactor);

                Matrix.multiplyMM(totalMVMatrix, 0,
                        vuforiaAppSession.getProjectionMatrix().getData(), 0, vuforiaMVMatrix, 0);

                teapot.getRenderData().getMaterial().setMat4(ModelShader.MVP_KEY,
                        totalMVMatrix[0], totalMVMatrix[1], totalMVMatrix[2], totalMVMatrix[3],
                        totalMVMatrix[4], totalMVMatrix[5], totalMVMatrix[6], totalMVMatrix[7],
                        totalMVMatrix[8], totalMVMatrix[9], totalMVMatrix[10], totalMVMatrix[11],
                        totalMVMatrix[12], totalMVMatrix[13], totalMVMatrix[14], totalMVMatrix[15]);
                showTeapot();
                break;
            } else {
                hideTeapot();
            }
        }
    }

    @SuppressWarnings("unused")
    private void showMatrix(String name, float[] matrix) {
        Log.d(TAG, name);
        Log.d(TAG, String.format("%5.2f %5.2f %5.2f %5.2f", matrix[0],
                matrix[4], matrix[8], matrix[12]));
        Log.d(TAG, String.format("%5.2f %5.2f %5.2f %5.2f", matrix[1],
                matrix[5], matrix[9], matrix[13]));
        Log.d(TAG, String.format("%5.2f %5.2f %5.2f %5.2f", matrix[2],
                matrix[6], matrix[10], matrix[14]));
        Log.d(TAG, String.format("%5.2f %5.2f %5.2f %5.2f", matrix[3],
                matrix[7], matrix[11], matrix[15]));
        Log.d(TAG, "\n");
    }

    private GLTextureUnit mTextureUnit;
}
