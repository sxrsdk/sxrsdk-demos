package com.example.org.gvrfapplication;

import android.os.Bundle;

import com.samsungxr.SXRActivity;

public class MainActivity extends SXRActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /**
         * Set Main Scene
         * It will be displayed when app starts
         */
        setMain(new MainScene(), "gvr.xml");
    }
}
