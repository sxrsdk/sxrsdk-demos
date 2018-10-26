package com.example.org.sxrfapplication;

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;

import com.samsungxr.SXRContext;
import com.samsungxr.SXRMesh;
import com.samsungxr.SXRNode;
import com.samsungxr.nodes.SXRSphereNode;
import com.samsungxr.nodes.SXRVideoNode;
import com.samsungxr.nodes.SXRVideoNodePlayer;

import java.io.IOException;

public class SXRVideoPlayerObject extends SXRNode{

    private final SXRVideoNodePlayer<?> mPlayer;
    private final MediaPlayer mMediaPlayer;

    public SXRVideoPlayerObject(SXRContext sxrContext) {
        super(sxrContext);

        SXRSphereNode sphere = new SXRSphereNode(sxrContext, 72, 144, false);
        SXRMesh mesh = sphere.getRenderData().getMesh();

        mMediaPlayer = new MediaPlayer();
        mPlayer = SXRVideoNode.makePlayerInstance(mMediaPlayer);

        SXRVideoNode video = new SXRVideoNode(sxrContext, mesh, mPlayer, SXRVideoNode.SXRVideoType.MONO);
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
