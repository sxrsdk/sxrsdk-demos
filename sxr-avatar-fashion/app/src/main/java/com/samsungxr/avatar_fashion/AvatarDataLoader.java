package com.samsungxr.avatar_fashion;

import com.samsungxr.SXRContext;
import com.samsungxr.SXRNode;
import com.samsungxr.animation.SXRAnimation;
import com.samsungxr.animation.SXRAnimator;
import com.samsungxr.animation.SXRAvatar;
import com.samsungxr.widgetlib.log.Log;

import static com.samsungxr.utility.Log.tag;

public class AvatarDataLoader {
    protected SXRContext sxrContext;
    protected String[] avatarsListInt;
    protected String[] avatarsListExt;

    private final static String TAG = tag(AvatarDataLoader.class);


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
        loadModels(AvatarReader.Location.assets, avatarsListInt);
        loadModels(AvatarReader.Location.sdcard, avatarsListExt);
    }

    private void loadModels(AvatarReader.Location loc, String[] avatarList) {
        if (avatarList != null) {
            Log.d(TAG, "loadAvatarData %d", avatarList.length);
            AvatarReader reader = new AvatarReader(sxrContext);

            for (final String avatarName : avatarList) {
                SXRAvatar sxrAvatar = new SXRAvatar(sxrContext, avatarName);
                sxrAvatar.getEventReceiver().addListener(
                        createListener(avatarName, loc));
                sxrAvatar.loadModel(Avatar.getResource(sxrContext, loc,
                        reader.getModel(loc, avatarName)));
            }
        } else {
            Log.d(TAG, "loadModels empty list!");
        }
    }
}
