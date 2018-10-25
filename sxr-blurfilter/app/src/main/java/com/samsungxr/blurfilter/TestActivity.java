package com.samsungxr.blurfilter;

import com.samsungxr.SXRActivity;

import android.os.Bundle;
import android.view.MotionEvent;

public class TestActivity extends SXRActivity
{
    TestMain main = new TestMain();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setMain(main);
    }
}

