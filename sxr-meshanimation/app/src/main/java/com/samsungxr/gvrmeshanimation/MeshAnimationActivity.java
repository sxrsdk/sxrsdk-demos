package com.samsungxr.gvrmeshanimation;

import com.samsungxr.SXRActivity;

import android.os.Bundle;

public class MeshAnimationActivity extends SXRActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setMain(new MeshAnimationMain(this), "gvr.xml");
    }
}
