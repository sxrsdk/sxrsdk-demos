package com.cesarandres.vr.vrbbals.android;

import java.io.IOException;
import java.util.Random;

import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRMesh;
import com.samsungxr.SXRSceneObject;
import com.samsungxr.SXRTexture;
import com.samsungxr.animation.SXRAnimation;
import com.samsungxr.animation.SXRAnimationEngine;
import com.samsungxr.animation.SXRRepeatMode;
import com.samsungxr.animation.SXRRotationByAxisWithPivotAnimation;

public class BasketBall {

	protected SXRSceneObject vrObject;
	protected SXRMesh mesh;
	protected SXRTexture texture;

	protected SXRSceneObject vrpObject;
	protected SXRMesh pmesh;
	protected SXRTexture ptexture;

	protected static Random rnd = new Random();
	protected SXRAnimationEngine mAnimationEngine;

	public float duration;

	public BasketBall() {
		this.duration = 1f;
	}

	public void init(SXRContext vrcontext, SXRAnimationEngine mAnimationEngine,
			int index) {
		try {
			this.mAnimationEngine = mAnimationEngine;
			mesh = vrcontext.loadMesh(new SXRAndroidResource(vrcontext,
					"sphere.obj"));
			texture = vrcontext.loadTexture(new SXRAndroidResource(vrcontext,
					"basketbal.jpg"));
			vrObject = new SXRSceneObject(vrcontext, mesh, texture);
			vrObject.getTransform().setScale(0.2f, 0.2f, 0.2f);

			pmesh = vrcontext.loadMesh(new SXRAndroidResource(vrcontext,
					"cube.obj"));
			ptexture = vrcontext.loadTexture(new SXRAndroidResource(vrcontext,
					"mars_1k_color.jpg"));
			vrpObject = new SXRSceneObject(vrcontext, pmesh, ptexture);
			vrpObject.getTransform().setScale(0.1f, 4f, 0.1f);
			vrpObject.getTransform().setPosition(0f, -4f, 0f);

			vrObject.attachEyePointeeHolder();
			vrObject.addChildObject(vrpObject);

			this.setX((float) index / 2f);
			this.setY(0.5f);
			this.setZ(-1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public float getX() {
		return this.vrObject.getTransform().getPositionX();
	}

	public void setX(float x) {
		this.vrObject.getTransform().setPositionX(x);
	}

	public float getY() {
		return this.vrObject.getTransform().getPositionY();
	}

	public void setY(float y) {
		this.vrObject.getTransform().setPositionY(y);
	}

	public float getZ() {
		return this.vrObject.getTransform().getPositionZ();
	}

	public void setZ(float z) {
		this.vrObject.getTransform().setPositionZ(z);
	}

	public SXRSceneObject getVrObject() {
		return vrObject;
	}

	public void setup(SXRAnimation animation) {
		animation.setRepeatMode(SXRRepeatMode.REPEATED).setRepeatCount(-1);
		mAnimationEngine.start(animation);
	}

	public void counterClockwise(SXRSceneObject object, float duration) {
		setup(new SXRRotationByAxisWithPivotAnimation( //
				object, duration, 360.0f, //
				0.0f, 1.0f, 0.0f, //
				object.getTransform().getPositionX(), object.getTransform()
						.getPositionY(), object.getTransform().getPositionZ()));
	}

	public void clockwise(SXRSceneObject object, float duration) {
		setup(new SXRRotationByAxisWithPivotAnimation( //
				object, duration, -360.0f, //
				0.0f, 1.0f, 0.0f, //
				object.getTransform().getPositionX(), object.getTransform()
						.getPositionY(), object.getTransform().getPositionZ()));
	}
}
