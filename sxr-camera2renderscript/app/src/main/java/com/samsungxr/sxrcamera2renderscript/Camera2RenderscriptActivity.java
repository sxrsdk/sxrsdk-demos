package com.samsungxr.sxrcamera2renderscript;

import com.samsungxr.SXRActivity;
import android.os.Bundle;

public class Camera2RenderscriptActivity extends SXRActivity 
{
	private Camera2RenderscriptManager mManger;
	
	@Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        mManger = new Camera2RenderscriptManager(this);
        setMain(mManger, "sxr.xml");
    }
	
    @Override
    protected void onPause() {
        super.onPause();
        mManger.onPause();
        finish();
    }
}
