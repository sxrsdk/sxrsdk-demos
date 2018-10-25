package com.samsungxr.avatardemo;

import com.samsungxr.SXRActivity;

import android.os.Bundle;
import android.view.MotionEvent;

public class AvatarActivity extends SXRActivity {
    AvatarMain mMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMain = new AvatarMain(this);
        setMain(mMain);
        enableGestureDetector();
    }
}
