package com.samsungxr.avatar_fashion;

import android.content.Context;
import android.content.SharedPreferences;

import com.samsungxr.SXRActivity;
import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRMaterial;
import com.samsungxr.SXRRenderData;
import com.samsungxr.SXRNode;
import com.samsungxr.animation.SXRAnimation;
import com.samsungxr.animation.SXRAnimator;
import com.samsungxr.animation.SXRAvatar;
import com.samsungxr.animation.SXRRepeatMode;
import com.samsungxr.widgetlib.log.Log;
import com.samsungxr.widgetlib.main.WidgetLib;
import com.samsungxr.widgetlib.widget.GroupWidget;
import com.samsungxr.widgetlib.widget.TransformCache;
import com.samsungxr.widgetlib.widget.Widget;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.samsungxr.utility.Log.tag;

public class Avatar extends GroupWidget {
    ArrayList<SXRMaterial> mOriginalMaterial;

    private static final String TAG = tag(Avatar.class);
    static final float MIN_RADIUS = 15f;
    static final float MAX_RADIUS = 50f;

    private TransformCache mCache = new TransformCache();
    private SXRAvatar mAvatar;
    private String mAnimMap;
    private String mAvatarName;
    SXRActivity mActivity;

    static final float SCALE_FACTOR = 0.25f;
    AvatarReader.Location mLocation;
    private boolean mAnimEnabled;
    private SXRAnimator blendAnim = null;
    private int countAnim =0;


    public Avatar(SXRContext context, SXRActivity activity, AvatarReader.Location location, String avatarName,
                  SXRAvatar avatar, boolean animate) {
        super(context, avatar.getModel());
        mActivity = activity;
        mAvatarName = avatarName;
        mAnimEnabled = animate;
        mLocation = location;
        mAvatar = avatar;
        mAvatar.getEventReceiver().addListener(mAvatarListener);
        Log.d(TAG, "Avatar avatarName: %s location: %s", mAvatarName, mLocation);

        setName(mAvatarName);

        mCache.save(this);

        setViewPortWidth(MAX_RADIUS);
        setViewPortHeight(MAX_RADIUS);
        setViewPortDepth(MAX_RADIUS);
        enableClipRegion();
        saveRenderData();
        setTouchable(true);
        setFocusEnabled(true);
        setChildrenFollowFocus(true);
        setChildrenFollowInput(true);
    }


    Widget.OnTouchListener homeButtonTouchListener = new Widget.OnTouchListener() {
        @Override
        public boolean onTouch(Widget widget, final float[] coords) {
            Log.d(TAG, "Home touched!");
            WidgetLib.getContentSceneController().goBack();
            return true;
        }
    };

    AvatarReader.Location getLocation() {
        return mLocation;
    }

    void scale() {
        scale(this, getScale());
    }

    private void scale(GroupWidget w, float scale) {
        w.setScale(scale);
        for (Widget c : w.getChildren()) {
            Log.d(TAG, "SCALE %s", c);
            if (c instanceof GroupWidget) {
                scale(((GroupWidget) c), scale);
            }
        }
    }

    private String getAnimMap() {
        if (mAnimMap == null) {
            AvatarReader reader = new AvatarReader(getSXRContext());
            mAnimMap = reader.getMap(mLocation, mAvatarName);
        }
        return mAnimMap;
    }

    private SXRAvatar.IAvatarEvents mAvatarListener = new SXRAvatar.IAvatarEvents() {
        @Override
        public void onAvatarLoaded(final SXRAvatar avatar, final SXRNode avatarRoot, String filePath, String errors) {
            Log.d(TAG, "onAvatarLoaded , errors " + filePath + "," + errors);
        }

        @Override
        public void onModelLoaded(SXRAvatar avatar, SXRNode avatarRoot, String filePath, String errors) {
        }

        @Override
        public void onAnimationLoaded(SXRAvatar avatar, SXRAnimator animation, String filePath, String errors) {
            if(countAnim==0)
            {
                blendAnim = animation;
            }
            else
            {
                blendAnim.addAnimation(animation.getAnimation(0));
                blendAnim.addAnimation(animation.getAnimation(1));
            }
            countAnim++;
            Log.i("printAnimation"," name "+animation.getName());
            /*
            Log.d(TAG, "onAnimationLoaded , %s, errors = %s", filePath, errors);
            if (errors != null) {
                Log.w(TAG, "Animation loading fail!");
                return;
            }
            animation.setName(filePath);
            animation.setRepeatMode(SXRRepeatMode.ONCE);
            animation.setSpeed(1f);
            if (mAnimEnabled) {
                if (!avatar.isRunning()) {
                    avatar.startAll(SXRRepeatMode.REPEATED);
                } else {
                    avatar.start(animation.getName());
                }
            }
            Log.d(TAG, "onAnimationLoaded count = %d , loaded = %d", mAnimCount, avatar.getAnimationCount());
*/

///////////////////////////blending Animations////////////////////////



            if(countAnim==2)
            {
                blendAnim.sendAvatar(avatar.getModel(),avatar, getAnimMap());
                String map = getAnimMap();
                blendAnim.setRepeatMode(SXRRepeatMode.REPEATED);
                blendAnim.setRepeatCount(-1);
                // // interpolationAnim.setSpeed(0.9f);
                blendAnim.start(1);
                // interpolationAnim.start();

            }


///////////////////////////blending Animations////////////////////////








//            if (mAnimCount == avatar.getAnimationCount()) {
//                if (mAvatarViewer.getAvatar() == Avatar.this) {
//                    mAvatarViewer.createAnimationList();
//                }
//            }
        }

        @Override
        public void onAnimationFinished(SXRAvatar avatar, SXRAnimator animator, SXRAnimation animation) {
        }

        @Override
        public void onAnimationStarted(SXRAvatar avatar, SXRAnimator animator) {
        }
    };

    float getScale() {
        if (mLocation == AvatarReader.Location.sdcard)
            return 0.35f;
        SXRNode.BoundingVolume bv = mAvatar.getModel().getBoundingVolume();
        float originalRadius = bv.radius;
        Log.i(TAG, "Radius = %f", originalRadius);

        if (originalRadius > MAX_RADIUS || originalRadius < MIN_RADIUS) {
            float scaleFactor = MAX_RADIUS / originalRadius;
            Log.i(TAG, "ScaleFactor = %f", scaleFactor);
            return scaleFactor;
        }
        return 1;
    }

    private int mAnimCount;

    void loadAnimation() {
        AvatarReader reader = new AvatarReader(getSXRContext());
        String[] animations = reader.getAnimations(mLocation, mAvatarName);
        if (animations != null) {
            mAnimCount = animations.length;
            for (String anim : animations) {
                String animPath = reader.getAnimation(mLocation, mAvatarName, anim);
                mAvatar.loadAnimation(getResource(animPath), getAnimMap());
            }
        }
    }

    private SXRAndroidResource getResource(String resource) {
        return getResource(getSXRContext(), mLocation, resource);
    }

    static SXRAndroidResource getResource(SXRContext context, AvatarReader.Location loc, String resource) {
        SXRAndroidResource res = null;
        try {
            switch (loc) {
                case sdcard:
                    res = new SXRAndroidResource(resource);
                    break;
                case assets:
                    res = new SXRAndroidResource(context, resource);
                    break;
            }
        } catch (IOException e) {
            Log.w(TAG, "No resource found! %s", resource);
        }
        return res;
    }

    void enableAnimation() {
        /*
        if (!mAnimEnabled) {
            mAnimEnabled = true;
            List<SXRAnimator> anims = getAnimations();
            if (!anims.isEmpty()) {
                if (!mAvatar.isRunning()) {
                    mAvatar.startAll(SXRRepeatMode.REPEATED);
                } else {
                    mAvatar.start(anims.get(0).getName());
                }
            } else {
                loadAnimation();
            }
        }*/
    }

    void disableAnimation() {
        if (mAnimEnabled) {
            mAnimEnabled = false;
            mAvatar.stop();
            mAvatar.clear();
        }
    }

    @Override
    protected void onAttached() {
        setChildrenFollowFocus(true);
        setChildrenFollowInput(true);
    }

    private void saveRenderData() {
        mOriginalMaterial = new ArrayList<>();
        for (SXRRenderData r : getRenderDatas()) {
            mOriginalMaterial.add(r.getMaterial());
        }
    }

    public List<SXRAnimator> getAnimations() {
        int count = mAvatar.getAnimationCount();
        List<SXRAnimator> anims = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            anims.add(mAvatar.getAnimation(i));
        }
        return anims;
    }

    void addAnimation(String animPath) {
        SXRAnimator anim = mAvatar.findAnimation(animPath);
        Log.d(TAG, "addAnimation %s", animPath);

        if (anim == null) {
            try {
                mAvatar.loadAnimation(
                        new SXRAndroidResource(getSXRContext(), animPath), getAnimMap());
            } catch (IOException e) {

            }
        }
    }

    void removeAnimation(String animName) {
        SXRAnimator anim = mAvatar.findAnimation(animName);
        if (anim != null) {
            Log.d(TAG, "removeAnimation %s", animName);
            mAvatar.removeAnimation(anim);
        }
    }

    private List<SXRRenderData> getRenderDatas() {
        return getNode().getAllComponents(SXRRenderData.getComponentType());
    }

    static class AvatarPreferences {
        static final String AVATAR_PREF = "default_avatar";
        final static String AVATAR_LOCATION = "avatar_location";
        final static String AVATAR_NAME = "avatar_name";

        SharedPreferences mPrefs;

        AvatarPreferences(Context context) {
            mPrefs = context.getSharedPreferences(AVATAR_PREF, 0);
        }

        AvatarReader.Location getLocation() {
            String loc = mPrefs.getString(AVATAR_LOCATION, "assets");
            return AvatarReader.Location.valueOf(loc);
        }

        String getName() {
            return mPrefs.getString(AVATAR_NAME, "Eva");
        }

        void setPrefs(AvatarReader.Location location, String name) {
            SharedPreferences.Editor editor = mPrefs.edit();
            editor.clear();
            editor.putString(AVATAR_NAME, name);
            editor.putString(AVATAR_LOCATION, location.name());
            editor.commit();
        }
    }
}