package com.samsungxr.sxrcamera2renderscript;

import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.renderscript.RenderScript;
import android.util.Size;
import android.view.Surface;

import com.samsungxr.SXRActivity;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRDrawFrameListener;
import com.samsungxr.SXRExternalImage;
import com.samsungxr.SXRMain;
import com.samsungxr.SXRMaterial.SXRShaderType;
import com.samsungxr.SXRNode;
import com.samsungxr.SXRScene;
import com.samsungxr.SXRTexture;

import java.util.Arrays;
import java.util.List;

public class Camera2RenderscriptManager extends SXRMain {
	private SXRActivity mActivity;
	private RenderScript mRS;
	private SurfaceTexture mSurfaceTexture;
	private SurfaceTexture mEffectTexture;
	private Camera2Helper mCameraHelper;
	private RenderscriptProcessor mProcessor;
	private Surface mSurfaceInterim;

	public Camera2RenderscriptManager(SXRActivity activity) {
		mActivity = activity;
		mRS = RenderScript.create(mActivity);
	}

	@Override
	public SplashMode getSplashMode() {
		return SplashMode.MANUAL;
	}

	@Override
	public void onInit(SXRContext sxrContext) throws Throwable {
		SXRScene mainScene = sxrContext.getMainScene();

		final SXRExternalImage passThroughImage = new SXRExternalImage(sxrContext);
		final SXRTexture passThroughTexture = new SXRTexture(passThroughImage);
		mSurfaceTexture = new SurfaceTexture(passThroughTexture.getId());

		SXRNode passThroughObject = new SXRNode(sxrContext, sxrContext.createQuad(3.0f, 1.5f), passThroughTexture,SXRShaderType.OES.ID);
		passThroughObject.getTransform().setPositionY(-0.7f);
		passThroughObject.getTransform().setPositionZ(-3.0f);
		mainScene.getMainCameraRig().addChildObject(passThroughObject);

		final SXRExternalImage effectImage = new SXRExternalImage(sxrContext);
		final SXRTexture effectTexture = new SXRTexture(effectImage);
		mEffectTexture = new SurfaceTexture(effectTexture.getId());

		SXRNode effectObject = new SXRNode(sxrContext, sxrContext.createQuad(3.0f, 1.5f), effectTexture,SXRShaderType.OES.ID);
		effectObject.getTransform().setPositionY(0.8f);
		effectObject.getTransform().setPositionZ(-3.0f);

		mainScene.getMainCameraRig().addChildObject(effectObject);
		sxrContext.registerDrawFrameListener(new SXRDrawFrameListener() {
			@Override
			public void onDrawFrame(float v) {
				mSurfaceTexture.updateTexImage();
				mEffectTexture.updateTexImage();
			}
		});

		try {
			mCameraHelper = new Camera2Helper(mActivity, 0);
			Size previewSize = mCameraHelper.setPreferredSize(1920, 1080);

			mSurfaceTexture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
			Surface surface = new Surface(mSurfaceTexture);

			mEffectTexture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
			Surface effectSurface = new Surface(mEffectTexture);

			mProcessor = new RenderscriptProcessor(mRS, previewSize.getWidth(), previewSize.getHeight());
			mProcessor.setOutputSurface(effectSurface);
			mSurfaceInterim = mProcessor.getInputSurface();

			Surface[] surface_array = {surface, mSurfaceInterim};
			List<Surface> surfaces = Arrays.asList(surface_array);
			mCameraHelper.startCapture(surfaces);
		} catch (CameraAccessException e) {
			e.printStackTrace();
			sxrContext.getActivity().finish();
		}

		closeSplashScreen();
	}

	public void onPause() {
		if (mCameraHelper != null) {
			mCameraHelper.closeCamera();
		}
	}
}
