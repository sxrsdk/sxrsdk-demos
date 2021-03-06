package com.samsungxr.bondage;

import android.content.res.AssetFileDescriptor;

import com.samsungxr.SXRBehavior;
import com.samsungxr.SXRContext;
import java.io.IOException;
import android.media.SoundPool;

public class SoundEffect extends SXRBehavior
{
    private SoundPool           mPlayer;
    private int                 mSoundID;
    private boolean             mLoop;
    private float               mVolume = 1.0f;
    static private long TYPE_SOUND_EFFECT = newComponentType(SoundEffect.class);

    public SoundEffect(SXRContext ctx, final SoundPool soundPool, String soundFile, boolean loop) throws IOException
    {
        super(ctx);
        mType = TYPE_SOUND_EFFECT;
        mLoop = loop;
        mPlayer = soundPool;
        AssetFileDescriptor descriptor = ctx.getContext().getAssets().openFd(soundFile);
        mSoundID = mPlayer.load(descriptor, 1);
        descriptor.close();
     }

    static public long getComponentType() { return TYPE_SOUND_EFFECT; }

    public void setVolume(float v)
    {
        mVolume = v;
    }
    public float getVolume() { return mVolume; }

    public void setLooping(boolean loop) { mLoop = loop; }
    public boolean isLooping() { return mLoop; }

    public void play()
    {
        if ((mPlayer != null) && (mSoundID != 0))
        {
            int loop = mLoop ? 1 : 0;
            mPlayer.play(mSoundID, mVolume, mVolume, 1, loop, 1);
        }
    }

    public void stop()
    {
        if ((mPlayer != null) && (mSoundID != 0))
        {
            mPlayer.stop(mSoundID);
        }
    }

}
