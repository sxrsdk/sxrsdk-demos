package com.samsungxr.avatar_fashion;

import java.util.ArrayList;
import java.util.List;

import com.samsungxr.SXRActivity;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRDirectLight;
import com.samsungxr.SXRMain;
import com.samsungxr.SXRScene;
import com.samsungxr.animation.SXRAvatar;
import com.samsungxr.mixedreality.IAnchorEvents;
import com.samsungxr.mixedreality.SXRAnchor;
import com.samsungxr.mixedreality.SXRLightEstimate;
import com.samsungxr.mixedreality.SXRMixedReality;
import com.samsungxr.mixedreality.SXRTrackingState;
import com.samsungxr.widgetlib.content_scene.ContentSceneController;
import com.samsungxr.widgetlib.log.Log;
import com.samsungxr.widgetlib.main.MainScene;
import com.samsungxr.widgetlib.main.WidgetLib;
import org.json.JSONObject;

import com.samsungxr.widgetlib.widget.Widget;

import static com.samsungxr.utility.Log.tag;
import static com.samsungxr.widgetlib.widget.properties.JSONHelpers.optBoolean;

public class AvatarFashionMain extends SXRMain
{
    private static final String TAG = tag(AvatarFashionMain.class);

    private SXRContext      mContext;
    private SXRActivity     mActivity;

    // widgetlib stuff
    private WidgetLib mWidgetLib;
    private MainScene mMainScene;
    private ContentSceneController mContentSceneController;
    private BackgroundWidget mBackgroundWidget;

    // content scenes
    private AvatarsListContentScene mAvatarsList;
    private BackgroundListContentScene mBackgroundList;
    private AvatarViewer mAvatarTracker;

    private SXRMixedReality   mMixedReality;
    private SXRDirectLight    mSceneLight;

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
        try {
            // widget lib init
            mWidgetLib = WidgetLib.init(sxrContext, "app_metadata.json");
            mContentSceneController = WidgetLib.getContentSceneController();

            mMainScene = WidgetLib.getMainScene();
            mMainScene.adjustClippingDistanceForAllCameras();

            mContext.addResetOnRestartHandler(WidgetLib.getSimpleAnimationTracker().clearTracker);
//            Log.enableSubsystem(INPUT, true);

            // create content scenes
            mBackgroundWidget = new BackgroundWidget(mContext);
            mAvatarsList = new AvatarsListContentScene(mContext, mActivity, mSettingsButtonTouchListener);
            mBackgroundList = new BackgroundListContentScene(mContext, mActivity,
                    mBackgroundWidget, mHomeButtonTouchListener);

            mAvatarTracker = new AvatarViewer(mContext, mActivity, mHomeButtonTouchListener);

            // load avatars list
            AvatarReader reader = new AvatarReader(sxrContext);
            Log.d(TAG, "AvatarDataLoaderBasic");
            AvatarListDataLoader loader = new AvatarListDataLoader(sxrContext,
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

    void startAvatarTracker(AvatarReader.Location location, String name) {
        try {
            mAvatarTracker.setAvatar(location, name);
        } catch (Exception e) {
            Log.w(TAG, "Model loading issue for %s %s %s", e, location, name);
        }
    }

    void enableARAnchor(Widget avatarAnchor) {
        // AR initialisation

        if (avatarAnchor == null) {
            return;
        }


        mMainScene.removeNode(mBackgroundWidget);

        SXRScene sxrScene = mContext.getMainScene();
        SceneUtils utility = new SceneUtils();
        if (mMixedReality == null)
        {
            mMixedReality = new SXRMixedReality(sxrScene, false);
            mMixedReality.resume();
            mMixedReality.getEventReceiver().addListener(new PlaneEventsListener(mContext,
                                                                                 utility,
                                                                                 mMixedReality,
                                                                                 avatarAnchor));
            mMixedReality.getEventReceiver().addListener(new IAnchorEvents()
            {
                @Override
                public void onAnchorStateChange(SXRAnchor SXRAnchor, SXRTrackingState SXRTrackingState)
                {
                    SXRAnchor.setEnable(SXRTrackingState == SXRTrackingState.TRACKING);
                }
            });
        }
        if (mSceneLight != null)
        {
            mSceneLight = utility.makeSceneLight(mContext);
            sxrScene.getMainCameraRig().getHeadTransformObject()
                    .addChildObject(mSceneLight.getOwnerObject());
        }
    }

    class AvatarListDataLoader extends AvatarDataLoader {
        private final int mAvatarCount;

        AvatarListDataLoader(final SXRContext sxrContext,
                              final String[] avatarsListInt,
                              final String[] avatarsListExt) {
            super(sxrContext, avatarsListInt, avatarsListExt);
            Log.d(TAG, "AvatarListDataLoader %d, %d",
                    avatarsListInt != null ? avatarsListInt.length : 0,
                    avatarsListExt != null ? avatarsListExt.length : 0);

            mAvatarCount = (avatarsListInt != null ? avatarsListInt.length : 0) +
                    (avatarsListExt != null ? avatarsListExt.length : 0);
        }

        protected List<Avatar> mAvatars = new ArrayList<>();

        @Override
        protected void onLoaded(final SXRAvatar avatar, AvatarReader.Location location, String avatarName) {
            super.onLoaded(avatar, location, avatarName);
            Avatar a = new Avatar(sxrContext, mActivity, location, avatarName, avatar, false);
            a.setName(avatarName);

            mAvatars.add(a);
            a.scale();
            if (mAvatars.size() == mAvatarCount) {
                WidgetLib.getMainThread().runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        mMainScene.addNode(mBackgroundWidget);
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

    private final Runnable defaultBackAction = new Runnable() {
        @Override
        public void run() {
            mContentSceneController.goBack();
        }
    };


    // AR part
    @Override
    public void onStep() {
        if ((mMixedReality != null) && (mSceneLight != null))
        {
            SXRLightEstimate lightEstimate = mMixedReality.getLightEstimate();
            if (lightEstimate != null)
            {
                float light = lightEstimate.getPixelIntensity() * 1.5f;
                mSceneLight.setAmbientIntensity(light, light, light, 1);
                mSceneLight.setDiffuseIntensity(light, light, light, 1);
                mSceneLight.setSpecularIntensity(light, light, light, 1);
            }
        }
    }
}
