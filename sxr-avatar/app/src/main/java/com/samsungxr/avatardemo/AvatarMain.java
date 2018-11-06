package com.samsungxr.avatardemo;

import java.io.IOException;
import java.io.InputStream;

import com.samsungxr.SXRActivity;
import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRCameraRig;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRDirectLight;
import com.samsungxr.SXRScene;
import com.samsungxr.SXRMain;
import com.samsungxr.animation.SXRAnimation;
import com.samsungxr.animation.SXRAnimator;
import com.samsungxr.animation.SXRAvatar;
import com.samsungxr.SXRNode;
import com.samsungxr.animation.SXRRepeatMode;
import com.samsungxr.animation.SXRSkeleton;
import com.samsungxr.animation.keyframe.SXRSkeletonAnimation;

import android.graphics.Color;
import android.util.Log;

public class AvatarMain extends SXRMain
{
    private final String mModelPath = "YBot/ybot.fbx";
    private final String[] mAnimationPaths =  { "animation/mixamo/Ybot_SambaDancing.bvh" };
    private final String mBoneMapPath = "animation/mixamo/bonemap.txt";
    private static final String TAG = "AVATAR";
    private SXRContext      mContext;
    private SXRScene        mScene;
    private SXRAvatar       mAvatar;
    private SXRActivity     mActivity;
    private int             mNumAnimsLoaded = 0;
    private String          mBoneMap;

    public AvatarMain(SXRActivity activity) {
        mActivity = activity;
    }

    private SXRAvatar.IAvatarEvents mAvatarListener = new SXRAvatar.IAvatarEvents()
    {
        @Override
        public void onAvatarLoaded(final SXRAvatar avatar, final SXRNode avatarRoot, String filePath, String errors)
        {
            if (avatarRoot.getParent() == null)
            {
                mContext.runOnGlThread(new Runnable()
                {
                    public void run()
                    {
                        avatar.centerModel(avatarRoot);
                        mScene.addNode(avatarRoot);
                    }
                });
            }
            loadNextAnimation(mAvatar, mBoneMap);
        }

        @Override
        public void onAnimationLoaded(SXRAvatar avatar, SXRAnimator animation, String filePath, String errors)
        {
            animation.setRepeatMode(SXRRepeatMode.ONCE);
            animation.setSpeed(1f);
            ++mNumAnimsLoaded;
            if (!avatar.isRunning())
            {
                avatar.startAll(SXRRepeatMode.REPEATED);
            }
            else
            {
                avatar.start(animation.getName());
            }
            if (mNumAnimsLoaded < mAnimationPaths.length)
            {
                loadNextAnimation(avatar, mBoneMap);
            }
        }

        public void onModelLoaded(SXRAvatar avatar, final SXRNode avatarRoot, String filePath, String errors) { }

        public void onAnimationFinished(SXRAvatar avatar, SXRAnimator animator, SXRAnimation animation) { }

        public void onAnimationStarted(SXRAvatar avatar, SXRAnimator animator) { }
    };


    @Override
    public void onInit(SXRContext sxrContext)
    {
        mContext = sxrContext;
        mScene = sxrContext.getMainScene();
        SXRCameraRig rig = mScene.getMainCameraRig();
        SXRDirectLight topLight = new SXRDirectLight(sxrContext);
        SXRNode topLightObj = new SXRNode(sxrContext);

        topLightObj.attachComponent(topLight);
        topLightObj.getTransform().rotateByAxis(-90, 1, 0, 0);
        mScene.addNode(topLightObj);
        rig.getLeftCamera().setBackgroundColor(Color.LTGRAY);
        rig.getRightCamera().setBackgroundColor(Color.LTGRAY);
        rig.getOwnerObject().attachComponent(new SXRDirectLight(mContext));

        mAvatar = new SXRAvatar(sxrContext, "YBot");
        mAvatar.getEventReceiver().addListener(mAvatarListener);
        mBoneMap = readFile(mBoneMapPath);
        try
        {
            mAvatar.loadModel(new SXRAndroidResource(sxrContext, mModelPath));
        }
        catch (IOException e)
        {
            e.printStackTrace();
            mActivity.finish();
            mActivity = null;
        }
        sxrContext.getInputManager().selectController();
    }

    private void loadNextAnimation(SXRAvatar avatar, String bonemap)
    {
        try
        {
            SXRAndroidResource res = new SXRAndroidResource(mContext, mAnimationPaths[mNumAnimsLoaded]);
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

    @Override
    public void onStep() {
    }

    private String readFile(String filePath)
    {
        try
        {
            SXRAndroidResource res = new SXRAndroidResource(getSXRContext(), filePath);
            InputStream stream = res.getStream();
            byte[] bytes = new byte[stream.available()];
            stream.read(bytes);
            String s = new String(bytes);
            return s;
        }
        catch (IOException ex)
        {
            return null;
        }
    }



}