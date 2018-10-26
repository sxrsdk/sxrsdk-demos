package com.samsungxr.sxrbullet;

import com.samsungxr.SXRActivity;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;

public class BulletSampleActivity extends SXRActivity {

    private BulletSampleMain main = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        main = new BulletSampleMain();
        setMain(main, "sxr.xml");
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            main.touchEvent();
        }

        return super.onTouchEvent(event);
    }

}
