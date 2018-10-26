package com.samsungxr.sxrshadowssample;

import com.samsungxr.SXRActivity;

import android.os.Bundle;
import android.view.MotionEvent;

public class ShadowsActivity extends SXRActivity {
    ShadowsMain main = new ShadowsMain();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMain(main, "sxr.xml");
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        main.onTouchEvent(event);
        return super.onTouchEvent(event);
    }
}
