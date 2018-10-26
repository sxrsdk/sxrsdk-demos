package com.samsungxr.util;


import android.view.Gravity;

import com.samsungxr.SXRContext;
import com.samsungxr.SXRRenderData;
import com.samsungxr.SXRNode;
import com.samsungxr.nodes.SXRTextViewNode;

public class Banner {
    SXRTextViewNode message;

    public Banner(SXRContext context, String text, int size, int color, float posX, float posY, float posZ) {
        message = new SXRTextViewNode(context, text);
        message.setGravity(Gravity.CENTER);
        message.setTextSize(size);
        message.setTextColor(color);
        message.getTransform().setPosition(posX, posY, posZ);
        message.getRenderData().setRenderingOrder(SXRRenderData.SXRRenderingOrder.TRANSPARENT);
    }

    public SXRTextViewNode getBanner() {
        return message;
    }
}
