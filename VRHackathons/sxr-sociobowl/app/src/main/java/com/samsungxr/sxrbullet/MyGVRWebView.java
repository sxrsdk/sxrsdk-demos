package com.samsungxr.sxrbullet;

import android.graphics.Canvas;
import android.view.View;

import com.samsungxr.SXRActivity;
import com.samsungxr.scene_objects.SXRViewSceneObject;
import com.samsungxr.scene_objects.view.SXRView;

import android.graphics.Canvas;
import android.view.View;
import android.webkit.WebView;

import com.samsungxr.SXRActivity;
import com.samsungxr.scene_objects.SXRViewSceneObject;
import com.samsungxr.scene_objects.view.SXRView;

import android.graphics.Canvas;
import android.view.View;
import android.webkit.WebView;
import com.samsungxr.SXRActivity;
import com.samsungxr.scene_objects.SXRViewSceneObject;
import com.samsungxr.scene_objects.view.SXRView;

public class MySXRWebView extends MyWebView implements SXRView {
    private SXRViewSceneObject mSceneObject = null;

    public MySXRWebView(SXRActivity context) {
        super(context);
        context.registerView(this);
    }

    public void draw(Canvas canvas) {
        if(this.mSceneObject != null) {
            Canvas attachedCanvas = this.mSceneObject.lockCanvas();
            attachedCanvas.scale((float)attachedCanvas.getWidth() / (float)canvas.getWidth(), (float)attachedCanvas.getHeight() / (float)canvas.getHeight());
            attachedCanvas.translate((float)(-this.getScrollX()), (float)(-this.getScrollY()));
            super.draw(attachedCanvas);
            this.mSceneObject.unlockCanvasAndPost(attachedCanvas);
        }
    }

    public void setSceneObject(SXRViewSceneObject sceneObject) {
        this.mSceneObject = sceneObject;
    }

    public View getView() {
        return this;
    }
}
