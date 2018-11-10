package com.example.org.sxrsdkapplication;

import android.os.Bundle;

import com.samsungxr.SXRActivity;
import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRMain;
import com.samsungxr.SXRNode;
import com.samsungxr.SXRTexture;

public class MainActivity extends SXRActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /**
         * Set Main Scene
         * It will be displayed when app starts
         */
        setMain(new Main());
    }

    private final class Main extends SXRMain {

        @Override
        public void onInit(SXRContext sxrContext) throws Throwable {

            //Load texture
            SXRTexture texture = sxrContext.getAssetLoader().loadTexture(new SXRAndroidResource(sxrContext, R.drawable.__default_splash_screen__));

            //Create a rectangle with the texture we just loaded
            SXRNode quad = new SXRNode(sxrContext, 4, 2, texture);
            quad.getTransform().setPosition(0, 0, -3);

            //Add rectangle to the scene
            sxrContext.getMainScene().addNode(quad);
        }

        @Override
        public SplashMode getSplashMode() {
            return SplashMode.NONE;
        }

        @Override
        public void onStep() {
            //Add update logic here
        }
    }
}
