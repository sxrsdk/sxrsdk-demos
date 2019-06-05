/* Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.samsungxr.aravatar;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.samsungxr.SXRActivity;
import com.samsungxr.utility.Log;

public class AvatarActivity extends SXRActivity
{
    private static final String TAG = "ARAVATAR";
    private AvatarMain mMain;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate");
        mMain = new AvatarMain();
        checkPermissions();
    }

    private void checkPermissions()
    {
        final int camera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        final int readPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (PackageManager.PERMISSION_GRANTED != camera || PackageManager.PERMISSION_GRANTED != readPermission)
        {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
                    REQUEST_PERMISSIONS);
        }
        else
        {
            setMain(mMain);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        if (0 == grantResults.length)
        {
            return;
        }
        switch (requestCode)
        {
            case REQUEST_PERMISSIONS:
            {
                if (PackageManager.PERMISSION_GRANTED != grantResults[1])
                {
                    Log.e(TAG, "Camera permission not granted");
                    finish();
                }
                break;
            }
        }
        setMain(mMain);
    }

    private final static int REQUEST_PERMISSIONS = 1;
}
