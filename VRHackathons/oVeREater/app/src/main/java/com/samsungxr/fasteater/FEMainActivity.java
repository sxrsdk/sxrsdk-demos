package com.gearvrf.fasteater;

import java.io.IOException;

import com.samsungxr.SXRActivity;
import com.samsungxr.script.SXRScriptManager;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ViewManager;

public class FEMainActivity extends SXRActivity {

	private FEViewManager viewManager;
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        viewManager = new FEViewManager();
        setScript(viewManager, "gvr.xml");
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
    	try {
			viewManager.onTouchEvent(event);
		} catch (IOException e) {
			e.printStackTrace();
		}
        return super.onTouchEvent(event);
    }
}
