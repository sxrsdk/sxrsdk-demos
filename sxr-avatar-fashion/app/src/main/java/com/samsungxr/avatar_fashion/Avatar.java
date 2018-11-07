package com.samsungxr.avatar_fashion;

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
    private float mCurrentZoom = 0;

    private static final String TAG = tag(Avatar.class);
    static final float MIN_RADIUS = 4f;
    static final float MAX_RADIUS = 6f;
    static final float MAX_ZOOM = 4f;

    private TransformCache mCache = new TransformCache();
    private SXRAvatar mAvatar;
    private String mAnimMap;
    private String mAvatarName;
    private static AvatarViewer mAvatarViewer;

    static final float SCALE_FACTOR = 0.02f;
    AvatarReader.Location mLocation;

    public Avatar(SXRContext context, AvatarReader.Location location, String avatarName,
                  SXRAvatar avatar, boolean animate) {
        super(context, avatar.getModel());
        mAvatarName = avatarName;
        mAnimEnabled = animate;
        mLocation = location;
        mAvatar = avatar;
        mAvatar.getEventReceiver().addListener(mAvatarListener);
        Log.d(TAG, "Avatar avatarName: %s location: %s", mAvatarName, mLocation);

        setName(mAvatarName.replaceAll("\\..*", ""));

        if (mAvatarViewer == null) {
            mAvatarViewer = new AvatarViewer(context, homeButtonTouchListener);
        }

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

    public void startAvatarViewer() {
        try {
            mAvatarViewer.setAvatar(mLocation, mAvatarName);
        } catch (Exception e) {
            Log.w(TAG, "Model loading issue for %s %s %s", e, mLocation, mAvatarName);
        }
    }

    static class AvatarDataLoader {
        protected  SXRContext sxrContext;
        protected  String[] avatarsListInt;
        protected  String[] avatarsListExt;
        AvatarDataLoader(final SXRContext sxrContext,
                         final String[] avatarsListInt,
                         final String[] avatarsListExt) {
            this.sxrContext = sxrContext;
            this.avatarsListInt = avatarsListInt;
            this.avatarsListExt = avatarsListExt;
        }

        protected void onLoaded(SXRAvatar avatar, AvatarReader.Location location, String avatarName) {
        }

        SXRAvatar.IAvatarEvents createListener(final String avatarName,
                                               final AvatarReader.Location location) {
            return new SXRAvatar.IAvatarEvents() {

                public void onAvatarLoaded(SXRAvatar var1, SXRNode var2, String var3, String var4) {
                    Log.d(TAG, "onAvatarLoaded : %s avatar = %s ", var3, var1.getModel());
                    onLoaded(var1, location, avatarName);
                    var1.getEventReceiver().removeListener(this);
                }

                public void onModelLoaded(SXRAvatar var1, SXRNode var2, String var3, String var4) {
                    Log.d(TAG, "onModelLoaded : %s", var3);
                }

                public void onAnimationLoaded(SXRAvatar var1, SXRAnimator var2, String var3, String var4) {
                }

                public void onAnimationStarted(SXRAvatar var1, SXRAnimator var2) {
                }

                public void onAnimationFinished(SXRAvatar var1, SXRAnimator var2, SXRAnimation var3) {
                }

            };
        }

        public void loadAvatarData() {
            AvatarReader reader = new AvatarReader(sxrContext);

            if (avatarsListInt != null) {
                Log.d(TAG, "loadAvatarDataInt %d", avatarsListInt.length);

                for (final String avatarName : avatarsListInt) {
                    Log.d(TAG, "loadAvatarDataInt %s", avatarName);

                    SXRAvatar sxrAvatar = new SXRAvatar(sxrContext, avatarName);
                    sxrAvatar.getEventReceiver().addListener(
                            createListener(avatarName, AvatarReader.Location.assets));
                    try {
                        sxrAvatar.loadModel(new SXRAndroidResource(sxrContext,
                                reader.getModel(AvatarReader.Location.assets, avatarName)));
                    } catch (IOException e) {
                        Log.d(TAG, "loadAvatarDataInt failed %s", avatarName);
                    }
                }
            }
            if (avatarsListExt != null) {
                Log.d(TAG, "loadAvatarDataExt %d", avatarsListExt.length);

                for (final String avatarName : avatarsListExt) {
                    SXRAvatar sxrAvatar = new SXRAvatar(sxrContext, avatarName);
                    sxrAvatar.getEventReceiver().addListener(
                            createListener(avatarName, AvatarReader.Location.sdcard));
                    try {
                        sxrAvatar.loadModel(new SXRAndroidResource(sxrContext,
                                reader.getModel(AvatarReader.Location.sdcard, avatarName)));
                    } catch (IOException e) {
                        Log.d(TAG, "loadAvatarDataExt failed %s", avatarName);
                    }
                }
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
            getSXRContext().runOnGlThread(new Runnable() {
                public void run() {
                    Log.d(TAG, "onAvatarLoaded , avatar name %s, avatarRoot = %s", avatar.getName(), avatarRoot);
                    setScale(SCALE_FACTOR, SCALE_FACTOR, SCALE_FACTOR);

                    mCache.save(Avatar.this);

                    if (mAnimEnabled) {
                        loadAnimation();
                    }
                }
            });
        }

        @Override
        public void onModelLoaded(SXRAvatar avatar, SXRNode avatarRoot, String filePath, String errors) {
        }

        @Override
        public void onAnimationLoaded(SXRAvatar avatar, SXRAnimator animation, String filePath, String errors) {
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
            if (mAnimCount == avatar.getAnimationCount()) {
                if (mAvatarViewer.getAvatar() == Avatar.this) {
                    mAvatarViewer.createAnimationList();
                }
            }
        }

        @Override
        public void onAnimationFinished(SXRAvatar avatar, SXRAnimator animator, SXRAnimation animation) {
            Log.d(TAG, "onAnimationFinished " + animation);
        }

        @Override
        public void onAnimationStarted(SXRAvatar avatar, SXRAnimator animator) {
            Log.d(TAG, "onAnimationStarted " + animator);
        }
    };

    void scaleModel() {
        SXRNode.BoundingVolume bv = mAvatar.getModel().getBoundingVolume();
        float originalRadius = bv.radius;
        Log.i(TAG, "Radius = %f", originalRadius);

        if (originalRadius > MAX_RADIUS || originalRadius < MIN_RADIUS) {
            float scaleFactor = MAX_RADIUS / originalRadius;
            Log.i(TAG, "ScaleFactor = %f", scaleFactor);
            setScale(scaleFactor, scaleFactor, scaleFactor);
        }
    }

    private int mAnimCount;

    void loadAnimation() {
        getSXRContext().runOnGlThread(new Runnable() {
            @Override
            public void run() {
                try {
                    AvatarReader reader = new AvatarReader(getSXRContext());
                    String[] animations = reader.getAnimations(mLocation, mAvatarName);
                    if (animations != null) {
                        mAnimCount = animations.length;
                        for (String anim: animations) {
                            mAvatar.loadAnimation(
                                    new SXRAndroidResource(getSXRContext(),
                                            reader.getAnimation(mLocation, mAvatarName, anim)),
                                    getAnimMap());
                        }
                    }
                } catch (IOException e) {
                    Log.w(TAG, "No animations found! %s", mAvatarName);
                }
            }
        });
    }

    private boolean mAnimEnabled;

    void enableAnimation() {
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
        }
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

    float getCurrentZoom() {
        return mCurrentZoom;
    }

    public void onZoomOverModel(float zoomBy) {
        float zTransform = (int) ((zoomBy));
        if (zTransform > MAX_ZOOM) {
            zTransform = 0;
            setScaleX(mCache.getScaleX());
            setScaleY(mCache.getScaleY());
            setScaleZ(mCache.getScaleZ());
        } else {
            Log.d(TAG, "Zoom by %f  %f", zTransform, zoomBy);
            float units = mCurrentZoom;
            float scaleFactor = units < zTransform ? zTransform - units : units - zTransform;
            float sf = units < zTransform ? 1.2f : 0.8f;

            for (int i = 0; i < scaleFactor; i++) {
                float x = getScaleX();
                float y = getScaleY();
                float z = getScaleZ();
                setScale(sf * x, sf * y, sf * z);
            }
        }
        mCurrentZoom = zTransform;
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
}