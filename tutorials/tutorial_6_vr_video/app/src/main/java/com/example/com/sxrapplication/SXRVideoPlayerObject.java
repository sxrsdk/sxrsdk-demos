package com.example.org.sxrfapplication;

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;

import com.samsungxr.SXRContext;
import com.samsungxr.SXRMesh;
import com.samsungxr.SXRSceneObject;
import com.samsungxr.scene_objects.SXRSphereSceneObject;
import com.samsungxr.scene_objects.SXRVideoSceneObject;
import com.samsungxr.scene_objects.SXRVideoSceneObjectPlayer;

import java.io.IOException;

public class SXRVideoPlayerObject extends SXRSceneObject{

    private final SXRVideoSceneObjectPlayer<?> mPlayer;
    private final MediaPlayer mMediaPlayer;

    public SXRVideoPlayerObject(SXRContext sxrContext) {
        super(sxrContext);

        SXRSphereSceneObject sphere = new SXRSphereSceneObject(sxrContext, 72, 144, false);
        SXRMesh mesh = sphere.getRenderData().getMesh();

        mMediaPlayer = new MediaPlayer();
        mPlayer = SXRVideoSceneObject.makePlayerInstance(mMediaPlayer);

        SXRVideoSceneObject video = new SXRVideoSceneObject(sxrContext, mesh, mPlayer, SXRVideoSceneObject.SXRVideoType.MONO);
        video.getTransform().setScale(100f, 100f, 100f);

        addChildObject(video);
    }

    public void loadVideo(String fileName) {
        final AssetFileDescriptor afd;
        try {
            afd = this.getSXRContext().getContext().getAssets().openFd(fileName);
            mMediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();
            mMediaPlayer.prepare();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void play() {
        if(mMediaPlayer != null) {
            mMediaPlayer.start();
        }
    }

    public void setLooping(boolean value) {
        mMediaPlayer.setLooping(value);
    }

    public void onPause() {
        mMediaPlayer.pause();
    }

    public void onResume() {
        mMediaPlayer.start();
    }
}
