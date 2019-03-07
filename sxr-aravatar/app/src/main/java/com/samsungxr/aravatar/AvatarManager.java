package com.samsungxr.aravatar;

import android.util.Log;

import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRBoxCollider;
import com.samsungxr.SXRComponent;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRNode;
import com.samsungxr.SXRRenderData;
import com.samsungxr.animation.SXRAnimation;
import com.samsungxr.animation.SXRAnimator;
import com.samsungxr.animation.SXRAvatar;
import com.samsungxr.animation.SXRRepeatMode;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class AvatarManager
{
    private static final String TAG = "AVATAR";
    private final String[] YBOT = new String[] { "YBot/YBot.fbx", "YBot/bonemap.txt", "YBot/Football_Hike.bvh", "YBot/Zombie_Stand_Up.bvh" };

    private final String[] EVA = { "Eva/Eva.dae", "Eva/pet_map.txt", "Eva/Walk_Circle.bvh", "Eva/bvhExport_GRAB_BONE.bvh",  };

    private final String[] GYLE = { "Gyle/gyle_skin.fbx", "Gyle/bonemap.txt", "Gyle/idle_Anim.bvh", "Gyle/hiphop_mixamo.com.bvh", "Gyle/running_Dance_mixamo.com.bvh" };

    private final String[] MRCHUNG = new String[] { "Hololab/evp_chung_lowres.fbx" };

    private final String[] AMINA = new String[] { "Hololab/amina_lowres.fbx" };

    private final String[] PAULA = new String[] { "Hololab/paula_lowres.fbx" };

    private final String[] RAJESH = new String[] { "Hololab/rajesh_lowres.fbx" };

    private final String[] MYAVATAR = new String[] { "/sdcard/mymodel.ply" };

    private final List<String[]> mAvatarFiles = new ArrayList<String[]>();
    private final List<SXRAvatar> mAvatars = new ArrayList<SXRAvatar>();
    private int mAvatarIndex = -1;
    private int mNumAnimsLoaded = 0;
    private String mBoneMap = null;
    private SXRContext mContext;
    private SXRAvatar.IAvatarEvents mEventHandler;

    AvatarManager(SXRContext ctx, SXRAvatar.IAvatarEvents handler)
    {
        mContext = ctx;
        if (handler == null)
        {
            handler = mAvatarListener;
        }
        mEventHandler = handler;
        mAvatarFiles.add(0, YBOT);
        mAvatarFiles.add(1, MRCHUNG);
        mAvatarFiles.add(2, GYLE);
        mAvatarFiles.add(3, MYAVATAR);
        mAvatarFiles.add( 4, AMINA);
        mAvatarFiles.add( 5, PAULA);
        mAvatarFiles.add( 6, RAJESH);
        mAvatars.add(0, new SXRAvatar(ctx, "YBOT"));
        mAvatars.add(1, new SXRAvatar(ctx, "MRCHUNG"));
        mAvatars.add(2, new SXRAvatar(ctx, "GYLE"));
        mAvatars.add(3, new SXRAvatar(ctx, "MYAVATAR"));
        mAvatars.add(4, new SXRAvatar(ctx, "AMINA"));
        mAvatars.add(5, new SXRAvatar(ctx, "PAULA"));
        mAvatars.add(6, new SXRAvatar(ctx, "RAJESH"));
    }

    public SXRAvatar selectAvatar(String name)
    {
        for (int i = 0; i < mAvatars.size(); ++i)
        {
            SXRAvatar avatar = mAvatars.get(i);
            if (name.equals(avatar.getName()))
            {
                if (mAvatarIndex == i)
                {
                    return avatar;
                }
                unselectAvatar();
                mAvatarIndex = i;
                mNumAnimsLoaded = avatar.getAnimationCount();
                if ((avatar.getSkeleton() == null) &&
                    (mEventHandler != null))
                {
                    avatar.getEventReceiver().addListener(mEventHandler);
                }
                if (mNumAnimsLoaded == 0)
                {
                    String mapFile = getMapFile();
                    if (mapFile != null)
                    {
                        mBoneMap = readFile(mapFile);
                    }
                    else
                    {
                        mBoneMap = null;
                    }
                }
                return avatar;
            }
        }
        return null;
    }

    private void unselectAvatar()
    {
        if (mAvatarIndex >= 0)
        {
            SXRAvatar avatar = getAvatar();
            avatar.stop(avatar.getName());
            mNumAnimsLoaded = 0;
            mBoneMap = null;
        }
    }

    public SXRAvatar getAvatar()
    {
        return mAvatars.get(mAvatarIndex);
    }

    public String getModelFile()
    {
        return mAvatarFiles.get(mAvatarIndex)[0];
    }

    public String getMapFile()
    {
        String[] files = mAvatarFiles.get(mAvatarIndex);
        if (files.length < 2)
        {
            return null;
        }
        return files[1];
    }

    public String getAnimFile(int animIndex)
    {
        String[] files = mAvatarFiles.get(mAvatarIndex);
        if (animIndex + 2 >= files.length)
        {
            return null;
        }
        return files[2 + animIndex];
    }

    public int getAvatarIndex(SXRAvatar avatar)
    {
        return mAvatars.indexOf(avatar);
    }

    public SXRAvatar getAvatar(String name)
    {
        for (SXRAvatar a : mAvatars)
        {
            if (name.equals(a.getName()))
            {
                return a;
            }
        }
        return null;
    }

    public boolean loadModel()
    {
        SXRAndroidResource res = null;
        SXRAvatar avatar = getAvatar();

        try
        {
            res = new SXRAndroidResource(mContext, getModelFile());
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
            return false;
        }
        avatar.loadModel(res);
        return true;
    }

    public boolean loadNextAnimation()
    {
        String animFile = getAnimFile(mNumAnimsLoaded);
        if (animFile == null)
        {
            return false;
        }
        try
        {
            SXRAndroidResource res = new SXRAndroidResource(mContext, animFile);
            ++mNumAnimsLoaded;
            getAvatar().loadAnimation(res, mBoneMap);
            return true;
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
            Log.e(TAG, "Animation could not be loaded from " + animFile);
            return false;
        }
    }

    private String readFile(String filePath)
    {
        try
        {
            SXRAndroidResource res = new SXRAndroidResource(mContext, filePath);
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

    public  SXRAvatar.IAvatarEvents mAvatarListener = new SXRAvatar.IAvatarEvents()
    {
        @Override
        public void onAvatarLoaded(final SXRAvatar avatar, final SXRNode avatarRoot, String filePath, String errors)
        {
            SXRNode.BoundingVolume bv = avatarRoot.getBoundingVolume();
            if (bv.radius > 0)
            {
                float scale = 0.5f / bv.radius;
                avatarRoot.getTransform().setScale(scale, scale, scale);
                bv = avatarRoot.getBoundingVolume();
            }
            avatarRoot.getTransform().setPosition(-bv.center.x, -bv.center.y, -bv.center.z);
            loadNextAnimation();
        }

        @Override
        public void onAnimationLoaded(SXRAvatar avatar, SXRAnimator animation, String filePath, String errors)
        {
            if (animation == null)
            {
                return;
            }
            animation.setRepeatMode(SXRRepeatMode.ONCE);
            animation.setSpeed(1f);
            if (!avatar.isRunning())
            {
                avatar.startAll(SXRRepeatMode.REPEATED);
            }
            else
            {
                avatar.start(animation.getName());
            }
            loadNextAnimation();
        }

        public void onModelLoaded(SXRAvatar avatar, SXRNode avatarRoot, String filePath, String errors)
        {
            SXRNode.BoundingVolume bv = avatarRoot.getBoundingVolume();
            if (bv.radius > 0)
            {
                float scale = 0.5f / bv.radius;
                avatarRoot.getTransform().setScale(scale, scale, scale);
                bv = avatarRoot.getBoundingVolume();
            }
            avatarRoot.getTransform().setPosition(-bv.center.x, -bv.center.y, -bv.center.z);
        }

        public void onAnimationFinished(SXRAvatar avatar, SXRAnimator animator) { }

        public void onAnimationStarted(SXRAvatar avatar, SXRAnimator animator) { }
    };
}
