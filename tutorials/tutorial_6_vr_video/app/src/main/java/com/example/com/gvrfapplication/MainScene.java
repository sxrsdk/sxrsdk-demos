package com.example.org.gvrfapplication;

import com.samsungxr.SXRContext;
import com.samsungxr.SXRMain;

/**
 * The Main Scene of the App
 */
public class MainScene extends SXRMain {

    private SXRVideoPlayerObject mPlayerObj = null;

    @Override
    public void onInit(SXRContext gvrContext) throws Throwable {

        mPlayerObj = new SXRVideoPlayerObject(gvrContext);
        mPlayerObj.loadVideo("videos_s_3.mp4");
        mPlayerObj.setLooping(true);
        mPlayerObj.play();

        gvrContext.getMainScene().addSceneObject(mPlayerObj);
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
