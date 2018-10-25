package com.samsungxr.gvrbullet;

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
import com.samsungxr.SXRSceneObject;
import com.samsungxr.SXRTexture;


import com.samsungxr.SXRContext;
import com.samsungxr.SXRDrawFrameListener;
import com.samsungxr.SXRExternalTexture;
import com.samsungxr.SXRMaterial;
import com.samsungxr.SXRMaterial.SXRShaderType;
import com.samsungxr.SXRMesh;
import com.samsungxr.SXRSceneObject;
import com.samsungxr.SXRTexture;

import android.graphics.Canvas;
import android.graphics.SurfaceTexture;
import android.util.Log;
import android.view.Surface;
import android.webkit.WebView;

/**
 * {@linkplain SXRSceneObject Scene object} that shows a web page, using the
 * Android {@link WebView}.
 */
public class MyWebViewObject extends SXRSceneObject implements
        SXRDrawFrameListener {

    private static final String TAG = "MyWebViewObject";

    private int REFRESH_INTERVAL = 30; // # of frames
    private int mCount = 0;
    private boolean paused = false;

    private final Surface mSurface;
    private final SurfaceTexture mSurfaceTexture;
    private final WebView mWebView;

    /**
     * Shows a web page on a {@linkplain SXRSceneObject scene object} with an
     * arbitrarily complex geometry.
     *
     * @param gvrContext
     *            current {@link SXRContext}
     * @param mesh
     *            a {@link SXRMesh} - see
     *            {@link SXRContext#loadMesh(com.samsungxr.SXRAndroidResource)}
     *            and {@link SXRContext#createQuad(float, float)}
     * @param webView
     *            an Android {@link WebView}
     */
    public MyWebViewObject(SXRContext gvrContext, SXRMesh mesh,
                           WebView webView) {
        super(gvrContext, mesh);

        mWebView = webView;
        gvrContext.registerDrawFrameListener(this);

        SXRTexture texture = new SXRExternalTexture(gvrContext);
        SXRMaterial material = new SXRMaterial(gvrContext, SXRMaterial.SXRShaderType.OES.ID);
        material.setMainTexture(texture);
        getRenderData().setMaterial(material);

        mSurfaceTexture = new SurfaceTexture(texture.getId());
        mSurface = new Surface(mSurfaceTexture);
        mSurfaceTexture.setDefaultBufferSize(mWebView.getWidth(),
                mWebView.getHeight());
    }

    /**
     * Shows a web page in a 2D, rectangular {@linkplain SXRSceneObject scene
     * object.}
     *
     * @param gvrContext
     *            current {@link SXRContext}
     * @param width
     *            the rectangle's width
     * @param height
     *            the rectangle's height
     * @param webView
     *            a {@link WebView}
     */
    public MyWebViewObject(SXRContext gvrContext, float width,
                           float height, WebView webView) {
        this(gvrContext, gvrContext.createQuad(width, height), webView);
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