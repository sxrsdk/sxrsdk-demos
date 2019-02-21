package com.samsungxr.avatardemo;

import android.graphics.Color;
import android.util.Log;

import com.samsungxr.SXRActivity;
import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRCameraRig;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRDirectLight;
import com.samsungxr.SXRMain;
import com.samsungxr.SXRMaterial;
import com.samsungxr.SXRNode;
import com.samsungxr.SXRScene;
import com.samsungxr.SXRSpotLight;
import com.samsungxr.SXRTexture;
import com.samsungxr.animation.SXRAnimation;
import com.samsungxr.animation.SXRAnimator;
import com.samsungxr.animation.SXRAvatar;
import com.samsungxr.animation.SXRRepeatMode;
import com.samsungxr.nodes.SXRSphereNode;

import java.io.IOException;
import java.io.InputStream;

public class AvatarMain extends SXRMain {
    private final String mModelPath = "YBot/ybot.fbx";
    private final String[] mAnimationPaths =  { "YBot/Zombie_Stand_Up_mixamo.com.bvh", "YBot/Football_Hike_mixamo.com.bvh" };
    private final String mBoneMapPath = "animation/mixamo/mixamo_map.txt";
    private static final String TAG = "AVATAR";
    private SXRContext mContext;
    private SXRScene mScene;
    private SXRActivity mActivity;
    private int mNumAnimsLoaded = 0;
    private String mBoneMap;

    public AvatarMain(SXRActivity activity) {
        mActivity = activity;
    }

    private SXRAvatar.IAvatarEvents mAvatarListener = new SXRAvatar.IAvatarEvents() {
        @Override
        public void onAvatarLoaded(final SXRAvatar avatar, final SXRNode avatarRoot, String filePath, String errors)
        {
            if (avatarRoot.getParent() == null)
            {
                mScene.addNode(avatarRoot);
                loadNextAnimation(avatar, mBoneMap);
                mContext.runOnGlThreadPostRender(1, new Runnable()
                {
                    public void run()
                    {
                        SXRNode.BoundingVolume bv = avatarRoot.getBoundingVolume();
                        float sf = 1.0f / bv.radius;
                        avatarRoot.getTransform().setScale(sf, sf, sf);
                        bv = avatarRoot.getBoundingVolume();
                        avatarRoot.getTransform().setPosition(-bv.center.x, -bv.minCorner.y, -bv.center.z - bv.radius);
                        mContext.runOnTheFrameworkThread(new Runnable()
                        {
                            public void run() {
                                loadNextAnimation(avatar, mBoneMap);
                            }
                        });
                    }
                });
            }
        }

        @Override
        public void onAnimationLoaded(SXRAvatar avatar, SXRAnimator animation, String filePath, String errors)
        {
            animation.setRepeatMode(SXRRepeatMode.ONCE);
            animation.setSpeed(1f);
            avatar.setBlend(1);
            ++mNumAnimsLoaded;
            if (!avatar.isRunning())
            {
                avatar.startAll(SXRRepeatMode.REPEATED);
            }
            else
            {
                avatar.start(animation.getName());
            }
            loadNextAnimation(avatar, mBoneMap);
        }

        public void onModelLoaded(SXRAvatar avatar, final SXRNode avatarRoot, String filePath, String errors) { }

        public void onAnimationFinished(SXRAvatar avatar, SXRAnimator animator) { }

        public void onAnimationStarted(SXRAvatar avatar, SXRAnimator animator) { }
    };


    @Override
    public void onInit(SXRContext ctx)
    {
        mContext = ctx;
        mScene = ctx.getMainScene();

        SXRCameraRig rig = mScene.getMainCameraRig();
        rig.getOwnerObject().getTransform().setPositionY(1.0f);
        rig.setNearClippingDistance(0.1f);
        rig.setFarClippingDistance(50);
        mScene.addNode(makeEnvironment(ctx, mScene));

        SXRAvatar avatar = new SXRAvatar(ctx, "YBot");
        avatar.getEventReceiver().addListener(mAvatarListener);
        mBoneMap = readFile(mBoneMapPath);
        try
        {
            avatar.loadModel(new SXRAndroidResource(ctx, mModelPath));
        }
        catch (IOException e) {
            e.printStackTrace();
            mActivity.finish();
            mActivity = null;
        }
        ctx.getInputManager().selectController();
    }

    private SXRNode makeEnvironment(SXRContext ctx, SXRScene scene)
    {
        SXRNode env = new SXRNode(ctx);
        SXRDirectLight topLight = new SXRDirectLight(ctx);
        SXRSpotLight headLight = new SXRSpotLight(ctx);
        SXRNode topLightObj = new SXRNode(ctx);
        SXRCameraRig rig = scene.getMainCameraRig();
        SXRMaterial floorMtl = new SXRMaterial(ctx, SXRMaterial.SXRShaderType.Phong.ID);
        SXRMaterial skyMtl = new SXRMaterial(ctx, SXRMaterial.SXRShaderType.Phong.ID);
        SXRNode skyBox = new SXRSphereNode(ctx, false, skyMtl);
        SXRNode floor = new SXRNode(ctx, 10, 10);

        try
        {
            SXRTexture  floorTex = ctx.getAssetLoader().loadTexture(new SXRAndroidResource(ctx, "checker.png"));
            floorMtl.setMainTexture(floorTex);
            floor.getRenderData().setMaterial(floorMtl);
        }
        catch (IOException ex)
        {
            Log.e(TAG, ex.getMessage());
        }
        headLight.setInnerConeAngle(50.0f);
        headLight.setOuterConeAngle(60.0f);
        floorMtl.setAmbientColor(0.7f, 0.6f, 0.5f, 1);
        floorMtl.setDiffuseColor(0.7f, 0.6f, 0.5f, 1);
        floorMtl.setSpecularColor(1, 1, 0.8f, 1);
        floorMtl.setSpecularExponent(4.0f);
        floor.getTransform().rotateByAxis(-90, 1, 0, 0);
        floor.getRenderData().setCastShadows(false);
        skyBox.getRenderData().setCastShadows(false);
        skyBox.getTransform().setScale(20,  20, 20);
        skyMtl.setAmbientColor(0.1f, 0.25f, 0.25f, 1.0f);
        skyMtl.setDiffuseColor(0.3f, 0.5f, 0.5f, 1.0f);
        skyMtl.setSpecularColor(0, 0, 0, 1);
        skyMtl.setSpecularExponent(0);
        rig.getHeadTransformObject().attachComponent(headLight);
        headLight.setShadowRange(0.1f, 20);
        topLightObj.attachComponent(topLight);
        topLightObj.getTransform().rotateByAxis(-90, 1, 0, 0);
        topLightObj.getTransform().setPosition(0, 2, -1);
        topLight.setShadowRange(0.1f, 50);
        env.addChildObject(topLight);
        env.addChildObject(skyBox);
        env.addChildObject(floor);
        return env;
    }

    private void loadNextAnimation(SXRAvatar avatar, String bonemap)
    {
        if (mNumAnimsLoaded >= mAnimationPaths.length)
        {
            return;
        }
        try
        {
            SXRAndroidResource res =
                new SXRAndroidResource(mContext, mAnimationPaths[mNumAnimsLoaded]);
            avatar.loadAnimation(res, bonemap);
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
            mActivity.finish();
            mActivity = null;
            Log.e(TAG, "Animation could not be loaded from " + mAnimationPaths[mNumAnimsLoaded]);
        }
    }

    private String readFile(String filePath) {
        try {
            SXRAndroidResource res = new SXRAndroidResource(getSXRContext(), filePath);
            InputStream stream = res.getStream();
            byte[] bytes = new byte[stream.available()];
            stream.read(bytes);
            String s = new String(bytes);
            return s;
        } catch (IOException ex) {
            return null;
        }
    }
}
