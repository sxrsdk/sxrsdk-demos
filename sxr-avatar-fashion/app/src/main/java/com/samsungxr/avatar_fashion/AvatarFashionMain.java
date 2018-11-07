package com.samsungxr.avatar_fashion;

import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.List;

import com.samsungxr.SXRActivity;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRDirectLight;
import com.samsungxr.SXRRenderData;
import com.samsungxr.SXRRenderPass;
import com.samsungxr.SXRScene;
import com.samsungxr.SXRMain;
import com.samsungxr.animation.SXRAvatar;
import com.samsungxr.widgetlib.content_scene.ContentSceneController;
import com.samsungxr.widgetlib.log.Log;
import com.samsungxr.widgetlib.main.MainScene;
import com.samsungxr.widgetlib.main.WidgetLib;
import org.json.JSONArray;
import org.json.JSONObject;

import com.samsungxr.widgetlib.widget.Widget;
import com.samsungxr.widgetlib.widget.properties.JSONHelpers;

import static com.samsungxr.utility.Log.tag;
import static com.samsungxr.widgetlib.log.Log.SUBSYSTEM.INPUT;
import static com.samsungxr.widgetlib.widget.properties.JSONHelpers.getString;
import static com.samsungxr.widgetlib.widget.properties.JSONHelpers.optBoolean;

public class AvatarFashionMain extends SXRMain
{
    private static final String TAG = tag(AvatarFashionMain.class);

    private SXRContext      mContext;
    private SXRActivity     mActivity;

    private SXRScene        mScene;

    // widgetlib stuff
    private WidgetLib mWidgetLib;
    private MainScene mMainScene;
    private ContentSceneController mContentSceneController;
    private BackgroundWidget mBackgroundWidget;

    // content scenes
    private AvatarsListContentScene mAvatarsList;
    private BackgroundListContentScene mBackgroundList;


    public AvatarFashionMain(SXRActivity activity) {
        mActivity = activity;
    }

    @Override
    public SplashMode getSplashMode() {
        return SplashMode.MANUAL;
    }

    @Override
    public void onInit(SXRContext sxrContext) {
        mContext = sxrContext;
        mScene = sxrContext.getMainScene();
        mScene.getMainCameraRig().getHeadTransformObject().attachComponent(new SXRDirectLight(mContext));
        try {
            // widget lib init
            mWidgetLib = WidgetLib.init(sxrContext, "app_metadata.json");
            mContentSceneController = WidgetLib.getContentSceneController();

            mMainScene = WidgetLib.getMainScene();
            mMainScene.adjustClippingDistanceForAllCameras();

            SXRContext.addResetOnRestartHandler(WidgetLib.getSimpleAnimationTracker().clearTracker);
//            Log.enableSubsystem(INPUT, true);

            // create background
            mBackgroundWidget = new BackgroundWidget(mContext);

            mAvatarsList = new AvatarsListContentScene(mContext, mSettingsButtonTouchListener);
            mBackgroundList = new BackgroundListContentScene(mContext,
                    mBackgroundWidget, mHomeButtonTouchListener);

            AvatarReader reader = new AvatarReader(sxrContext);

            AvatarDataLoaderBasic loader = new AvatarDataLoaderBasic(sxrContext,
                    reader.getAllAvatars(AvatarReader.Location.assets),
                    reader.getAllAvatars(AvatarReader.Location.sdcard));
            loader.loadAvatarData();

        } catch (Exception e) {
            e.printStackTrace();
            mActivity.finish();
            mActivity = null;
            Log.e(TAG, "One or more assets could not be loaded.");
        }
        sxrContext.getInputManager().selectController();
        WidgetLib.getTouchManager().setDefaultRightClickAction(defaultBackAction);
    }

    class AvatarDataLoaderBasic extends Avatar.AvatarDataLoader {
        private final int mAvatarCount;

        AvatarDataLoaderBasic(final SXRContext sxrContext,
                              final String[] avatarsListInt,
                              final String[] avatarsListExt) {
            super(sxrContext, avatarsListInt, avatarsListExt);
            mAvatarCount = (avatarsListInt != null ? avatarsListInt.length : 0) +
                    (avatarsListExt != null ? avatarsListExt.length : 0);
        }

        protected List<Avatar> mAvatars = new ArrayList<>();

        @Override
        protected void onLoaded(SXRAvatar avatar, AvatarReader.Location location, String avatarName) {
            Avatar a = new Avatar(sxrContext, location, avatarName, avatar, false);
            a.setName(avatarName);
            mAvatars.add(a);
            avatar.getModel().getTransform().setScale(Avatar.SCALE_FACTOR,
                    Avatar.SCALE_FACTOR, Avatar.SCALE_FACTOR);

            if (mAvatars.size() == mAvatarCount) {
                WidgetLib.getMainThread().runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject properties = WidgetLib.getPropertyManager().
                                getInstanceProperties(AvatarsListContentScene.class, TAG);
                        final boolean bgPicker = optBoolean(properties,
                                AvatarsListContentScene.Properties.background_picker, true);

                        if (bgPicker) {
                            mMainScene.addNode(mBackgroundWidget);
                        }
                        mAvatarsList.setAvatarList(mAvatars);

                        mContentSceneController.goTo(mAvatarsList);
                        closeSplashScreen();
                    }
                });
            }

        }
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
                    String resIdStr = getString(levels.getJSONObject(index),
                            Properties_level_ext.thumbnail);
                    mThumbnailsList.add(resIdStr);
                } catch (Exception e) {
                    Log.e(TAG, e, "Could not create background at %d", index);
                }
            }

            ArrayList<SXRRenderData> rdata = getNode().
                    getAllComponents(SXRRenderData.getComponentType());
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

        @Override
    public void onStep() {
    }

    public void onSingleTapUp(MotionEvent event) {
     //   mAvatarsList.startNextAvatar();
    }

    private final Runnable defaultBackAction = new Runnable() {
        @Override
        public void run() {
            mContentSceneController.goBack();
        }
    };

}
