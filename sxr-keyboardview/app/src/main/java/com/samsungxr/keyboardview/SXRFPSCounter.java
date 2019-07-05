package com.samsungxr.keyboardview;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.SurfaceTexture;
import android.view.Surface;

import com.samsungxr.SXRContext;
import com.samsungxr.SXRDrawFrameListener;
import com.samsungxr.SXRExternalImage;
import com.samsungxr.SXRMaterial;
import com.samsungxr.SXRNode;
import com.samsungxr.SXRTexture;

public class SXRFPSCounter extends SXRNode {
    private static final int SIZE = 100;
    private final Activity mActivity;
    private Surface mSurface;
    private SurfaceTexture mSurfaceTexture;
    private final Paint mPaint;
    private final SXRDrawFrameListener mDrawFrameListener;
    private float mFramesTime[] = { // Last 60 frames
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    private int mFrameIndex;
    private float mSumFrameTime;
    private int mFps;

    public SXRFPSCounter(final SXRContext sxrContext) {
        super(sxrContext, sxrContext.createQuad(1.0f, 1.0f));

        final SXRExternalImage image = new SXRExternalImage(sxrContext);
        final SXRTexture texture = new SXRTexture(image);

        final SXRMaterial material = new SXRMaterial(sxrContext, SXRMaterial.SXRShaderType.OES.ID);

        mActivity = sxrContext.getActivity();

        mSurfaceTexture = new SurfaceTexture(texture.getId());
        mSurfaceTexture.setDefaultBufferSize(SIZE, SIZE);
        mSurface = new Surface(mSurfaceTexture);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setTextSize(64);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setColor(Color.WHITE);
        mPaint.setAlpha(255);
        mPaint.setFakeBoldText(true);

        material.setMainTexture(texture);
        getRenderData().setMaterial(material);
        getRenderData().setDepthTest(false);
        getRenderData().setRenderingOrder(100000);

        mSumFrameTime = 0;
        mFrameIndex = 0;
        mFps = 0;

        mDrawFrameListener = new SXRDrawFrameListener() {
            @Override
            public void onDrawFrame(float frameTime) {
                mFrameIndex = (mFrameIndex + 1) % mFramesTime.length;

                mSumFrameTime -= mFramesTime[mFrameIndex];
                mSumFrameTime += frameTime;

                mFramesTime[mFrameIndex] = frameTime;
            }
        };

        sxrContext.registerDrawFrameListener(mDrawFrameListener);

        mSurfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
            SXRDrawFrameListener drawFrameListener = new SXRDrawFrameListener() {
                @Override
                public void onDrawFrame(float frameTime) {
                    mSurfaceTexture.updateTexImage();

                    sxrContext.unregisterDrawFrameListener(this);
                }
            };

            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                sxrContext.registerDrawFrameListener(drawFrameListener);
            }
        });

        final Thread drawTask = new Thread(new Runnable() {
            Object lock = new Object();
            @Override
            public void run() {
                int fps;
                synchronized (lock) {
                    while (!mActivity.isFinishing()) {
                        try {
                            lock.wait(1000);

                            fps = (int) (1.0f / (mSumFrameTime / mFramesTime.length));

                            if (fps == mFps)
                                continue;
                            mFps = fps;

                            Canvas canvas = mSurface.lockCanvas(null);

                            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                            if (mFps < 50) {
                                mPaint.setColor(Color.RED);
                            } else if (mFps < 58) {
                                mPaint.setColor(Color.YELLOW);
                            } else {
                                mPaint.setColor(Color.GREEN);
                            }
                            mPaint.setAlpha(180);
                            canvas.drawCircle(SIZE / 2, SIZE / 2, SIZE /2 , mPaint);

                            mPaint.setColor(Color.WHITE);
                            mPaint.setAlpha(255);

                            canvas.drawText(String.valueOf(mFps), SIZE / 2, SIZE / 1.4f, mPaint);

                            mSurface.unlockCanvasAndPost(canvas);

                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });

        drawTask.start();
    }
}
