package com.samsungxr.avatar_fashion;

import com.samsungxr.SXRActivity;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRNode;
import com.samsungxr.animation.SXRAnimator;
import com.samsungxr.animation.SXRAvatar;
import com.samsungxr.widgetlib.content_scene.ContentSceneController;
import com.samsungxr.widgetlib.log.Log;
import com.samsungxr.widgetlib.main.WidgetLib;
import com.samsungxr.widgetlib.widget.GroupWidget;
import com.samsungxr.widgetlib.widget.Widget;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.samsungxr.utility.Log.tag;
import static com.samsungxr.widgetlib.widget.properties.JSONHelpers.optJSONObject;

public class AvatarViewer extends BaseContentScene {

    AvatarViewer(SXRContext sxrContext, SXRActivity activity, Widget.OnTouchListener homeListener) {
        super(sxrContext, activity);
        mControlBar.addControlListener("Home", homeListener);
    }

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

            mContent.addChild(mAvatar);
            ((AvatarFashionActivity)mActivity).enableARAnchor(mContent);

            final ContentSceneController contentSceneController = WidgetLib.getContentSceneController();

            contentSceneController.goTo(AvatarViewer.this);
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

        mContent = new AnchorBox(mSxrContext);
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

    class AnchorBox extends GroupWidget {
        AnchorBox(SXRContext sxrContext) {
            super(sxrContext);
        }
    }

    @Override
    protected Widget createContent() {
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

    //
    // Animation picker - it is not used in ARAVATAR
    //

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


    private Avatar mAvatar;
    private CheckList mAnimList;
    private GroupWidget mContent;

    private static final String TAG = tag(AvatarViewer.class);

}
