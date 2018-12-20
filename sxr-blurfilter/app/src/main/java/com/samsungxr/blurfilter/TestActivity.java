package com.samsungxr.blurfilter;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.samsungxr.SXRActivity;

public final class TestActivity extends SXRActivity implements ActivityCompat.OnRequestPermissionsResultCallback {
    private final TestMain main = new TestMain();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final int camera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        final int readPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (PackageManager.PERMISSION_GRANTED != camera || PackageManager.PERMISSION_GRANTED != readPermission) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
                    REQUEST_PERMISSIONS);
        } else {
            setMain(main);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (0 == grantResults.length) {
            return;
        }
        switch (requestCode) {
            case REQUEST_PERMISSIONS:
            {
                if (PackageManager.PERMISSION_GRANTED != grantResults[1]) {
                    Log.e("TestActivity", "Camera permission not granted");
                    finish();
                }
                break;
            }
        }

        setMain(main);
    }

    private final static int REQUEST_PERMISSIONS = 1;
}
