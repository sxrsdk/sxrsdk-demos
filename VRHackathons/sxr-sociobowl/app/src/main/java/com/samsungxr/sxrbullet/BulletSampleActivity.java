package com.samsungxr.sxrbullet;


import com.samsungxr.SXRActivity;
import com.samsungxr.scene_objects.view.SXRView;
import com.samsungxr.utility.Log;
import com.samsungxr.sxrbullet.VRTouchPadGestureDetector;
import com.samsungxr.sxrbullet.VRTouchPadGestureDetector.OnTouchPadGestureListener;
import com.samsungxr.sxrbullet.VRTouchPadGestureDetector.SwipeDirection;

import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class BulletSampleActivity extends SXRActivity implements
        OnTouchPadGestureListener {
    private long lastDownTime;
    BulletSampleMain viewManager;
    private long mLatestTap = 0;
    private static final int TAP_INTERVAL = 300;
    private VRTouchPadGestureDetector mDetector = null;

    // webview
    private MySXRWebView[] webViews = new MySXRWebView[3];
    String url = "https://soundcloud.com/ge-gras/goodbye-hello";
    String url2 = "https://twitter.com/SamsungMobile";
    String url3 = "http://dev.quasi.co/live";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewManager = new BulletSampleMain(this);
        mDetector = new VRTouchPadGestureDetector(this);


        webViews[0] = createWebView(url);
        webViews[1] = createWebView(url2);
        webViews[2] = createWebView(url3);

        setMain(viewManager, "sxr.xml");
    }

    private MySXRWebView createWebView(String _url) {
        MySXRWebView webView = new MySXRWebView(this);

        webView.setInitialScale(300);
        int w = 1024, h = 1024;
        webView.measure(w, h);
        webView.layout(0, 0, w, h);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        webSettings.setGeolocationEnabled(true);

        webSettings.setMediaPlaybackRequiresUserGesture(false);
        webSettings.setNeedInitialFocus(false);

        webView.loadUrl( _url );

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });

        /*webView.setWebChromeClient(new WebChromeClient() {

        });*/

        webView.addJavascriptInterface(new WebAppInterface(this), "APP");

        return webView;
    }

    public SXRView getWebView(int i) {
        return webViews[i];
    }


    /*
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            lastDownTime = event.getDownTime();
        }

        if (event.getActionMasked() == MotionEvent.ACTION_UP) {
            if (event.getEventTime() - lastDownTime < 200) {
                viewManager.onTap();
            }
        }
        return true;
    }
*/
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onSingleTap(MotionEvent e) {
        Log.v("", "onSingleTap");
        if (System.currentTimeMillis() > mLatestTap + TAP_INTERVAL) {
            mLatestTap = System.currentTimeMillis();
            viewManager.onRealTap();
        }
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onSwipe(MotionEvent e, SwipeDirection swipeDirection,
                           float velocityX, float velocityY) {
        Log.e("", "onSwipe");

        if (swipeDirection.equals(SwipeDirection.Forward)) {
            viewManager.onSwipe(velocityX);
        } else if (swipeDirection.equals(SwipeDirection.Down)) {
            viewManager.onSwipe2(-1);
        } else if (swipeDirection.equals(SwipeDirection.Up)) {
            viewManager.onSwipe2(1);
        }
        return false;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (action == KeyEvent.ACTION_DOWN) {
                    viewManager.moveLeft();
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (action == KeyEvent.ACTION_DOWN) {
                    viewManager.moveRight();
                }
                return true;
            case KeyEvent.KEYCODE_BACK:
                if (action == KeyEvent.ACTION_DOWN) {
                    viewManager.onTap();
                }
                return true;
            default:
                return super.dispatchKeyEvent(event);
        }
    }

    class WebAppInterface {
        Context mContext;

        WebAppInterface(Context c) {
            mContext = c;
        }

        @JavascriptInterface
        public void changeTurn(String turn) {

        }
    }

}
