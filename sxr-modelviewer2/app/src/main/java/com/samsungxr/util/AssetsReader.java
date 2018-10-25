package com.samsungxr.util;


import android.content.res.AssetManager;
import android.content.res.Resources;
import android.util.Log;

import com.samsungxr.SXRActivity;

import java.io.IOException;

public final class AssetsReader {

   public static String[] getAssetsList(SXRActivity activity, String sDirectoryPath) {
        String sAList[] = null;
        try {
            Resources resources = activity.getResources();
            AssetManager assetManager = resources.getAssets();
            sAList = assetManager.list(sDirectoryPath);
            for (int i = 0; i < sAList.length; i++) {
                Log.d("", sAList[i]);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Modelviewer", "Directory " + sDirectoryPath + " not found");
        }
        return sAList;
    }
}
