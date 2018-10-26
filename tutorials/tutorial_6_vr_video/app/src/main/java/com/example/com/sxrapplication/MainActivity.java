package com.example.org.sxrfapplication;

import android.os.Bundle;

import com.samsungxr.SXRActivity;

public class MainActivity extends SXRActivity {

    MainScene main;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /**
         * Set Main Scene
         * It will be displayed when app starts
         */
        main = new MainScene();
        setMain(main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        main.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        main.onPause();
    }
}
