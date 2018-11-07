package com.samsungxr.avatar_fashion;

import com.samsungxr.SXRActivity;
import com.samsungxr.SXRContext;
import com.samsungxr.animation.SXRAnimator;
import com.samsungxr.animation.SXRAvatar;
import com.samsungxr.widgetlib.content_scene.ContentSceneController;
import com.samsungxr.widgetlib.log.Log;
import com.samsungxr.widgetlib.main.WidgetLib;
import com.samsungxr.widgetlib.widget.GroupWidget;
import com.samsungxr.widgetlib.widget.Widget;
import com.samsungxr.widgetlib.widget.layout.Layout;
import com.samsungxr.widgetlib.widget.layout.OrientedLayout;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.samsungxr.utility.Log.tag;
import static com.samsungxr.widgetlib.widget.properties.JSONHelpers.optFloat;
import static com.samsungxr.widgetlib.widget.properties.JSONHelpers.optJSONObject;

public class AvatarViewer extends BaseContentScene {

    AvatarViewer(SXRContext sxrContext, SXRActivity activity, Widget.OnTouchListener homeListener) {
        super(sxrContext, activity);

        JSONObject properties = WidgetLib.getPropertyManager().getInstanceProperties(getClass(), TAG);
        final float zoom_step = optFloat(properties, Properties.zoom_step, ZOOM_STEP);
        mPadding = optFloat(properties, Properties.padding, PADDING);

        Widget.OnTouchListener zoomListener = new Widget.OnTouchListener() {
            @Override
            public boolean onTouch(Widget widget, float[] floats) {
                if (mAvatar != null) {
                    mAvatar.onZoomOverModel(mAvatar.getCurrentZoom() + zoom_step);
                }
                return true;
            }
        };

        mControlBar.addControlListener("Home", homeListener);
        mControlBar.addControlListener("Zoom", zoomListener);

    }

    void createAnimationList() {
        if (mAnimList != null) {
            return;
        }
        JSONObject listProperties = WidgetLib.getPropertyManager().getInstanceProperties(CheckList.class, TAG);
        if (listProperties.length() != 0) {
            final JSONObject labelProperties = optJSONObject(listProperties, CheckList.Properties.label);

            Log.d(TAG, "createAnimationList");
            List<CheckList.Item> animations = new ArrayList<>();
            if (mAvatar != null) {
                for (SXRAnimator anim : mAvatar.getAnimations()) {
                    String path = anim.getName();
                    String label = path.substring(path.lastIndexOf("//") + 1, path.length());
                    Log.d(TAG, "Added anim to the list %s %s", path, label);
                    animations.add(new CheckList.Item(mSxrContext, label, labelProperties, new AnimationAction(path)));
                }
            }
            mAnimList = new CheckList(mSxrContext, "Animations", animations);
            mAnimList.checkAll();
            mAnimList.enable();
            mContent.addChild(mAnimList);
        }
    }

    private class AnimationAction implements CheckList.Action {
        private final String mAnimationName;
        AnimationAction(String animationName) {
            mAnimationName = animationName;
        }

        @Override
        public void enable() {
            if (mAvatar != null) {
                Log.d(TAG, "enable Action for animation [%s]", mAnimationName);
                mAvatar.addAnimation(mAnimationName);
            }
        }

        @Override
        public void disable() {
            if (mAvatar != null) {
                Log.d(TAG, "disable Action for animation [%s]", mAnimationName);
                mAvatar.removeAnimation(mAnimationName);
            }
        }
    };


    private enum Properties {zoom_step, padding}

    class AvatarDataLoaderWithAnimation extends AvatarDataLoader {

        AvatarDataLoaderWithAnimation(final SXRContext sxrContext,
                                      final String[] avatarsListInt,
                                      final String[] avatarsListExt) {
            super(sxrContext, avatarsListInt, avatarsListExt);
        }

        @Override
        protected void onLoaded(final SXRAvatar avatar, AvatarReader.Location location, String avatarName) {
            super.onLoaded(avatar, location, avatarName);
            mAvatar = new Avatar(sxrContext, mActivity, location, avatarName, avatar, true);
            mAvatar.setName(avatarName);
            mAvatar.loadAnimation();

            mAvatar.scale();
            ((AvatarFashionActivity)mActivity).setMyMain(new ARAvatarMain(avatar));

//            final ContentSceneController contentSceneController = WidgetLib.getContentSceneController();
//            contentSceneController.goTo(AvatarViewer.this);
        }
    }


    void setAvatar(AvatarReader.Location location, String avatarName) {
        Avatar.AvatarPreferences preferences = new Avatar.AvatarPreferences(mSxrContext.getContext());
        preferences.setPrefs(location, avatarName);

        String[] avatarsList = { avatarName };
        AvatarDataLoaderWithAnimation loader = (location == AvatarReader.Location.assets) ?
                new AvatarDataLoaderWithAnimation(mSxrContext, avatarsList, null) :
                new AvatarDataLoaderWithAnimation(mSxrContext, null, avatarsList);

        loader.loadAvatarData();
        mAnimList = null;
        mFirstShow = true;
    }

    Avatar getAvatar() {
        return mAvatar;
    }

    @Override
    public void show() {
        super.show();
        mAvatar.enableAnimation();
    }


    @Override
    public void hide() {
        super.hide();
        mAvatar.disableAnimation();
    }

    class ModelBox extends GroupWidget {
        ModelBox(SXRContext sxrContext, Avatar model) {
            super(sxrContext);
            OrientedLayout layout = new com.samsungxr.widgetlib.widget.layout.basic.LinearLayout();
            getDefaultLayout().setDividerPadding(mPadding, Layout.Axis.X);
            applyLayout(layout);

            addChild(model);
        }
    }

    @Override
    protected Widget createContent() {
        mContent = new ModelBox(mSxrContext, mAvatar);
        return mContent;
    }

    @Override
    public String getName() {
        return TAG;
    }

    @Override
    public void onProximityChange(boolean onProximity) {
        if (onProximity) {
        }
    }

    private Avatar mAvatar;
    private static final float ZOOM_STEP = 1;
    private static final float PADDING = 3;
    private final float mPadding;
    private CheckList mAnimList;
    private ModelBox mContent;

    private static final String TAG = tag(AvatarViewer.class);
}
