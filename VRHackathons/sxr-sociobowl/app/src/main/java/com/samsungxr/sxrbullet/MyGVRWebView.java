package com.samsungxr.sxrbullet;

import android.graphics.Canvas;
import android.view.View;

import com.samsungxr.SXRActivity;
import com.samsungxr.nodes.SXRViewNode;
import com.samsungxr.nodes.view.SXRView;

import android.graphics.Canvas;
import android.view.View;
import android.webkit.WebView;

import com.samsungxr.SXRActivity;
import com.samsungxr.nodes.SXRViewNode;
import com.samsungxr.nodes.view.SXRView;

import android.graphics.Canvas;
import android.view.View;
import android.webkit.WebView;
import com.samsungxr.SXRActivity;
import com.samsungxr.nodes.SXRViewNode;
import com.samsungxr.nodes.view.SXRView;

public class MySXRWebView extends MyWebView implements SXRView {
    private SXRViewNode mNode = null;

    public MySXRWebView(SXRActivity context) {
        super(context);
        context.registerView(this);
    }

    public void draw(Canvas canvas) {
        if(this.mNode != null) {
            Canvas attachedCanvas = this.mNode.lockCanvas();
            attachedCanvas.scale((float)attachedCanvas.getWidth() / (float)canvas.getWidth(), (float)attachedCanvas.getHeight() / (float)canvas.getHeight());
            attachedCanvas.translate((float)(-this.getScrollX()), (float)(-this.getScrollY()));
            super.draw(attachedCanvas);
            this.mNode.unlockCanvasAndPost(attachedCanvas);
        }
    }

    public void setNode(SXRViewNode sceneObject) {
        this.mNode = sceneObject;
    }

    public View getView() {
        return this;
    }
}
