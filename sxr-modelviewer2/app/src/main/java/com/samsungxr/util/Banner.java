package com.samsungxr.util;


import android.view.Gravity;

import com.samsungxr.SXRContext;
import com.samsungxr.SXRRenderData;
import com.samsungxr.SXRSceneObject;
import com.samsungxr.scene_objects.SXRTextViewSceneObject;

public class Banner {
    SXRTextViewSceneObject message;

    public Banner(SXRContext context, String text, int size, int color, float posX, float posY, float posZ) {
        message = new SXRTextViewSceneObject(context, text);
        message.setGravity(Gravity.CENTER);
        message.setTextSize(size);
        message.setTextColor(color);
        message.getTransform().setPosition(posX, posY, posZ);
        message.getRenderData().setRenderingOrder(SXRRenderData.SXRRenderingOrder.TRANSPARENT);
    }

    public SXRTextViewSceneObject getBanner() {
        return message;
    }
}
