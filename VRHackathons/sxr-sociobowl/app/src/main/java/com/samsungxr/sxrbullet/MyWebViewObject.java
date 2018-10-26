package com.samsungxr.sxrbullet;

import android.graphics.Canvas;
import android.graphics.SurfaceTexture;
import android.util.Log;
import android.view.Surface;
import android.webkit.WebView;

import com.samsungxr.SXRContext;
import com.samsungxr.SXRDrawFrameListener;
import com.samsungxr.SXRExternalTexture;
import com.samsungxr.SXRMaterial;
import com.samsungxr.SXRMesh;
import com.samsungxr.SXRNode;
import com.samsungxr.SXRTexture;


import com.samsungxr.SXRContext;
import com.samsungxr.SXRDrawFrameListener;
import com.samsungxr.SXRExternalTexture;
import com.samsungxr.SXRMaterial;
import com.samsungxr.SXRMaterial.SXRShaderType;
import com.samsungxr.SXRMesh;
import com.samsungxr.SXRNode;
import com.samsungxr.SXRTexture;

import android.graphics.Canvas;
import android.graphics.SurfaceTexture;
import android.util.Log;
import android.view.Surface;
import android.webkit.WebView;

/**
 * {@linkplain SXRNode Scene object} that shows a web page, using the
 * Android {@link WebView}.
 */
public class MyWebViewObject extends SXRNode implements
        SXRDrawFrameListener {

    private static final String TAG = "MyWebViewObject";

    private int REFRESH_INTERVAL = 30; // # of frames
    private int mCount = 0;
    private boolean paused = false;

    private final Surface mSurface;
    private final SurfaceTexture mSurfaceTexture;
    private final WebView mWebView;

    /**
     * Shows a web page on a {@linkplain SXRNode scene object} with an
     * arbitrarily complex geometry.
     *
     * @param sxrContext
     *            current {@link SXRContext}
     * @param mesh
     *            a {@link SXRMesh} - see
     *            {@link SXRContext#loadMesh(com.samsungxr.SXRAndroidResource)}
     *            and {@link SXRContext#createQuad(float, float)}
     * @param webView
     *            an Android {@link WebView}
     */
    public MyWebViewObject(SXRContext sxrContext, SXRMesh mesh,
                           WebView webView) {
        super(sxrContext, mesh);

        mWebView = webView;
        sxrContext.registerDrawFrameListener(this);

        SXRTexture texture = new SXRExternalTexture(sxrContext);
        SXRMaterial material = new SXRMaterial(sxrContext, SXRMaterial.SXRShaderType.OES.ID);
        material.setMainTexture(texture);
        getRenderData().setMaterial(material);

        mSurfaceTexture = new SurfaceTexture(texture.getId());
        mSurface = new Surface(mSurfaceTexture);
        mSurfaceTexture.setDefaultBufferSize(mWebView.getWidth(),
                mWebView.getHeight());
    }

    /**
     * Shows a web page in a 2D, rectangular {@linkplain SXRNode scene
     * object.}
     *
     * @param sxrContext
     *            current {@link SXRContext}
     * @param width
     *            the rectangle's width
     * @param height
     *            the rectangle's height
     * @param webView
     *            a {@link WebView}
     */
    public MyWebViewObject(SXRContext sxrContext, float width,
                           float height, WebView webView) {
        this(sxrContext, sxrContext.createQuad(width, height), webView);
    }

    public void setRefreshInterval(int interval) {
        REFRESH_INTERVAL = interval;
    }

    public void pauseRender() {
        paused = true;
    }

    public void resumeRender() {
        paused = false;
    }

    public boolean isRenderPaused() {
        return paused;
    }

    public boolean toggleRenderPaused() {
        paused = !paused;
        return paused;
    }

    @Override
    public void onDrawFrame(float frameTime) {
        if (paused)
            return;

        if (++mCount > REFRESH_INTERVAL) {
            refresh();
            mCount = 0;
        }
    }

    /** Draws the {@link WebView} onto {@link #mSurfaceTexture} */
    public void refresh() {
        try {
            Canvas canvas = mSurface.lockCanvas(null);
            mWebView.draw(canvas);
            mSurface.unlockCanvasAndPost(canvas);
        } catch (Surface.OutOfResourcesException t) {
            Log.e(TAG, "lockCanvas failed");
        }
        mSurfaceTexture.updateTexImage();
    }
}