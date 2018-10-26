package com.example.org.sxrfapplication;

import com.samsungxr.SXRContext;
import com.samsungxr.SXRMain;

/**
 * The Main Scene of the App
 */
public class MainScene extends SXRMain {

    private SXRVideoPlayerObject mPlayerObj = null;

    @Override
    public void onInit(SXRContext sxrContext) throws Throwable {

        mPlayerObj = new SXRVideoPlayerObject(sxrContext);
        mPlayerObj.loadVideo("videos_s_3.mp4");
        mPlayerObj.setLooping(true);
        mPlayerObj.play();

        sxrContext.getMainScene().addNode(mPlayerObj);
    }

    @Override
    public SplashMode getSplashMode() {
        return SplashMode.NONE;
    }

    @Override
    public void onStep() {
        //Add update logic here
    }

    public void onResume() {
        if (mPlayerObj != null)
            mPlayerObj.onResume();
    }

    public void onPause() {
        if (mPlayerObj != null)
            mPlayerObj.onPause();
    }
}
