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

package com.samsungxr.widgetlibviewer;

import com.samsungxr.SXRContext;
import com.samsungxr.SXRMain;
import com.samsungxr.SXRNode;
import com.samsungxr.SXRRenderData;
import com.samsungxr.SXRRenderPass;
import com.samsungxr.widgetlib.content_scene.ContentSceneController;
import com.samsungxr.widgetlib.log.Log;
import com.samsungxr.widgetlib.main.MainScene;
import com.samsungxr.widgetlib.main.WidgetLib;
import com.samsungxr.widgetlib.widget.Widget;
import com.samsungxr.widgetlib.widget.properties.JSONHelpers;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.samsungxr.utility.Log.tag;
import static com.samsungxr.widgetlib.widget.properties.JSONHelpers.getString;

public class ViewerMain extends SXRMain {
    private static final String TAG = tag(ViewerMain.class);

    private WidgetLib mWidgetLib;
    private final SXRWidgetLibViewer mActivity;
    private MainScene mMainScene;
    private SXRContext mSXRContext;
    private ContentSceneController mContentSceneController;
    private BackgroundWidget mBackgroundWidget;
    private Lights mLight;

    private ModelsListContentScene mModelsList;
    private BackgroundListContentScene mBackgroundList;
    private NotificationsContentScene mNotificationList;

    ViewerMain(SXRWidgetLibViewer activity) {
        mActivity = activity;
    }

    @Override
    public SplashMode getSplashMode() {
        return SplashMode.MANUAL;
    }

    static class BackgroundWidget extends Widget {
        private enum Properties_level_ext {
            thumbnail
        }

        private List<String> mThumbnailsList = new ArrayList<>();

        BackgroundWidget(final SXRContext sxrContext) {
            super(sxrContext);
            setRenderingOrder(SXRRenderData.SXRRenderingOrder.BACKGROUND - 1);
            setCullFace(SXRRenderPass.SXRCullFaceEnum.None);
            JSONObject metadata = getObjectMetadata();
            JSONArray levels = JSONHelpers.optJSONArray(metadata, Properties.levels);
            for (int index = 0; index < levels.length(); ++index) {
                try {
                    String resIdStr = getString(levels.getJSONObject(index), Properties_level_ext.thumbnail);
                    mThumbnailsList.add(resIdStr);
                } catch (Exception e) {
                    Log.e(TAG, e, "Could not create background at %d", index);
                }
            }

            ArrayList<SXRRenderData> rdata = getNode().getAllComponents(SXRRenderData.getComponentType());
            for (SXRRenderData r : rdata) {
                r.disableLight();
            }
            setTouchable(false);
            setFocusEnabled(false);
        }

        List<String> getThumbnailsList() {
            return mThumbnailsList;
        }
    }

    // WidgetLib members
    @Override
    public void onInit(final SXRContext sxrContext) {
        mSXRContext = sxrContext;
        try {
            SXRNode widgetRoot = new SXRNode(sxrContext);
            sxrContext.getMainScene().addNode(widgetRoot);
            mWidgetLib = WidgetLib.init(widgetRoot, "app_metadata.json");
            mContentSceneController = WidgetLib.getContentSceneController();

            mMainScene = WidgetLib.getMainScene();
            mMainScene.adjustClippingDistanceForAllCameras();

            // initialize lights
            mLight = new Lights();
            mLight.loadLights(sxrContext);
            mLight.getLightScene().getTransform().setPosition(0, 10, 0);
            mLight.getLightScene().getTransform().rotateByAxis(-90, 1, 0, 0);
            mMainScene.addNode(mLight.getLightScene());
        } catch (Exception e) {
            Log.e(TAG, "Could not initialize Widget library");
            e.printStackTrace();
            Log.e(TAG, e, "onCreate()");
            mActivity.finish();
            return;
        }

        SXRContext.addResetOnRestartHandler(WidgetLib.getSimpleAnimationTracker().clearTracker);

        mBackgroundWidget = new BackgroundWidget(mSXRContext);

        mModelsList = new ModelsListContentScene(sxrContext, mSettingsButtonTouchListener);

        mNotificationList = new NotificationsContentScene(mSXRContext, mHomeButtonTouchListener);

        WidgetLib.getMainThread().runOnMainThread(new Runnable() {
            @Override
            public void run() {
                mMainScene.addNode(mBackgroundWidget);
                mContentSceneController.goTo(mModelsList);
//                mContentSceneController.goTo(mNotificationList);
                closeSplashScreen();
            }
        });

        mBackgroundList = new BackgroundListContentScene(mSXRContext,
                mBackgroundWidget, mHomeButtonTouchListener);
        WidgetLib.getTouchManager().setDefaultRightClickAction(defaultBackAction);

        Log.init(sxrContext.getContext(), true);
//        Log.enableSubsystem(Log.SUBSYSTEM.FOCUS, true);
//        Log.enableSubsystem(Log.SUBSYSTEM.WIDGET, true);
//        Log.enableSubsystem(Log.SUBSYSTEM.INPUT, true);
    }


    private Widget.OnTouchListener mSettingsButtonTouchListener = new Widget.OnTouchListener() {
        @Override
        public boolean onTouch(Widget widget, final float[] coords) {
            mContentSceneController.goTo(mBackgroundList);
            return true;
        }
    };

    private Widget.OnTouchListener mHomeButtonTouchListener = new Widget.OnTouchListener() {
        @Override
        public boolean onTouch(Widget widget, final float[] coords) {
            mContentSceneController.goBack();
            return true;
        }
    };


    @Override
    public void onStep() {
        FPSCounter.tick();
    }


    private final Runnable defaultBackAction = new Runnable() {
        @Override
        public void run() {
            mContentSceneController.goBack();
        }
    };
}
