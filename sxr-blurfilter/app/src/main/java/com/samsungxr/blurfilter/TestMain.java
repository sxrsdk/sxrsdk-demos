package com.samsungxr.blurfilter;

import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRCamera;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRMain;
import com.samsungxr.SXRMaterial;
import com.samsungxr.SXRRenderData;
import com.samsungxr.SXRRenderPass;
import com.samsungxr.SXRRenderTarget;
import com.samsungxr.SXRRenderTexture;
import com.samsungxr.SXRScene;
import com.samsungxr.SXRSceneObject;
import com.samsungxr.SXRShaderData;
import com.samsungxr.SXRShaderId;
import com.samsungxr.SXRSwitch;
import com.samsungxr.SXRTexture;
import com.samsungxr.scene_objects.SXRCameraSceneObject;
import com.samsungxr.utility.Log;


public class TestMain extends SXRMain
{
    private SXRContext mContext;
    private SXRSwitch mSwitch;
    private SXRRenderTexture mRenderTexture;

    public void onInit(SXRContext context)
    {
        mContext = context;
        SXRScene scene = context.getMainScene();
        SXRCameraSceneObject cameraObject = null;

        //
        // Create a camera scene object.
        // This step will fail if your camera cannot be accessed.
        //
        try
        {
            cameraObject = new SXRCameraSceneObject(context, 3.6f, 2.0f);
            cameraObject.setUpCameraForVrMode(1); // set up 60 fps camera preview.
        }
        catch (SXRCameraSceneObject.SXRCameraAccessException e)
        {
            // Cannot open camera
            Log.e("Test", "Cannot open the camera",e);
            context.getActivity().finish();
        }
        //
        // Make a scene object to show the blurred camera view.
        // It is the same size as the camera scene object but will
        // used the blurred camera texture instead of the real one.
        // The texture in it's material will be substituted with
        // the blurred camera texture by "createBlurTexture"
        //
        final SXRMaterial cameraMtl = cameraObject.getRenderData().getMaterial();
        final SXRTexture cameraTex = cameraMtl.getMainTexture();
        final SXRMaterial blurryMtl = createDisplayScene(scene, cameraTex);
        //
        // Continuously render a scene which shows the blurred camera texture on a
        // full-screen quad.
        //
        context.runOnGlThread(new Runnable()
        {
            public void run()
            {
                createBlurTexture(cameraTex, blurryMtl);
            }
        });
    }

    //
    // Creates a blurred version of the camera texture.
    // We make a material which uses the texture from the camera
    // but employs a custom blur shader instead of the OESShader.
    // The quad which will be blurred is added to the scene
    // which renders to the blurred texture - not the main scene.
    //
    public void createBlurTexture(SXRTexture texture, final SXRMaterial material)
    {
        SXRScene blurScene = new SXRScene(mContext);
        SXRSceneObject blurryQuad = createBlurScene(blurScene, texture);
        blurScene.getMainCameraRig().addChildObject(blurryQuad);
        //
        // Set up a texture to render into and a SXRRenderTarget
        // to initiate rendering into the texture every frame.
        //
        mRenderTexture = new SXRRenderTexture(mContext, 1024, 1024);
        SXRRenderTarget renderTarget = new SXRRenderTarget(mRenderTexture, blurScene);
        blurScene.getMainCameraRig().getOwnerObject().attachComponent(renderTarget);
        material.setMainTexture(mRenderTexture);
        renderTarget.setEnable(true);
    }

    /*
     * Create a scene object which is a full screen quad that produces a blurry version
     * of the input texture when rendered. This quad has two render passes, each of which
     * each of which performs a gaussian blur in a single direction.
     */
    public SXRSceneObject createBlurScene(SXRScene scene, SXRTexture texture)
    {
        final SXRSceneObject blurryQuad = new SXRSceneObject(mContext, 2.0f, 2.0f, texture, SXRMaterial.SXRShaderType.OES.ID);
        SXRCamera camera = scene.getMainCameraRig().getCenterCamera();

        SXRMaterial horzBlurMtl = new SXRMaterial(mContext, new SXRShaderId(HorzBlurShader.class));
        horzBlurMtl.setFloat("u_resolution", 1024.0f);
        camera.addPostEffect(horzBlurMtl);

        SXRMaterial vertBlurMtl = new SXRMaterial(mContext, new SXRShaderId(VertBlurShader.class));
        vertBlurMtl.setFloat("u_resolution", 1024.0f);
        camera.addPostEffect(vertBlurMtl);

        blurryQuad.getTransform().setPositionZ(-0.1f);
        return blurryQuad;
    }

    //
    // Make two scene objects to show the normal and blurred camera views.
    // The blurred view uses the blurred camera texture instead of the real one.
    // The texture in it's material will be substituted with
    // the blurred camera texture by "createBlurTexture"
    //
    public SXRMaterial createDisplayScene(SXRScene scene, SXRTexture cameraTex)
    {
        SXRSceneObject normalCamera = new SXRSceneObject(mContext, 3.6f, 2.0f, cameraTex, SXRMaterial.SXRShaderType.OES.ID);
        SXRTexture tempTex = getSXRContext().getAssetLoader().loadTexture(new SXRAndroidResource(getSXRContext(), R.drawable.checker));
        SXRSceneObject blurryCamera = new SXRSceneObject(mContext, 3.6f, 2.0f, tempTex);
        final SXRMaterial blurryMtl = blurryCamera.getRenderData().getMaterial();
        SXRSceneObject cameraRoot = new SXRSceneObject(mContext);

        blurryCamera.getRenderData().setCullFace(SXRRenderPass.SXRCullFaceEnum.None);
        normalCamera.getTransform().setPositionX(-2);
        blurryCamera.getTransform().setPositionX(2);
        cameraRoot.getTransform().setPosition(0.0f, 0.0f, -4.0f);
        cameraRoot.addChildObject(normalCamera);	// index 0 = normal camera
        cameraRoot.addChildObject(blurryCamera);	// index 1 = blurry camera
        scene.getMainCameraRig().addChildObject(cameraRoot);
        return blurryMtl;
    }
}