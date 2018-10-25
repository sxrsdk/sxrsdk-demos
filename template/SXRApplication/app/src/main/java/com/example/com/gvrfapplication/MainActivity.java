package com.example.org.gvrfapplication;

import android.os.Bundle;

import com.samsungxr.SXRActivity;
import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRMain;
import com.samsungxr.SXRSceneObject;
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
        public void onInit(SXRContext gvrContext) throws Throwable {

            //Load texture
            SXRTexture texture = gvrContext.getAssetLoader().loadTexture(new SXRAndroidResource(gvrContext, R.drawable.__default_splash_screen__));

            //Create a rectangle with the texture we just loaded
            SXRSceneObject quad = new SXRSceneObject(gvrContext, 4, 2, texture);
            quad.getTransform().setPosition(0, 0, -3);

            //Add rectangle to the scene
            gvrContext.getMainScene().addSceneObject(quad);
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
