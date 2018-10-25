package com.samsungxr.simplephysics.main;

import android.os.Bundle;

import com.samsungxr.SXRActivity;

public class MainActivity extends SXRActivity
{
    private MainScript main;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        main = new MainScript();
        setMain(main, "gvr.xml");

        enableGestureDetector();
    }
}
