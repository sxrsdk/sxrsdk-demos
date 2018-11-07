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

package com.samsungxr.avatar_fashion;

import com.samsungxr.SXRContext;
import com.samsungxr.SXRDirectLight;
import com.samsungxr.SXRMain;
import com.samsungxr.SXRScene;
import com.samsungxr.animation.SXRAvatar;
import com.samsungxr.mixedreality.SXRAnchor;
import com.samsungxr.mixedreality.SXRMixedReality;
import com.samsungxr.mixedreality.SXRTrackingState;
import com.samsungxr.mixedreality.IAnchorEvents;
import com.samsungxr.mixedreality.IPlaneEvents;
import com.samsungxr.widgetlib.log.Log;

public class ARAvatarMain extends SXRMain {
    private static String TAG = "ARAVATAR";
    private SXRContext        mContext;
    private SXRScene          mScene;
    private final SXRAvatar   mAvatar;
    private SXRMixedReality   mMixedReality;
    private SceneUtils        mUtility;
    private IPlaneEvents      mPlaneEventsListener;
    private SXRDirectLight    mSceneLight;

    ARAvatarMain(SXRAvatar avatar) {
        mAvatar = avatar;
    }

    @Override
    public void onInit(SXRContext ctx)
    {
        mContext = ctx;
        mScene = mContext.getMainScene();
        mUtility = new SceneUtils();
        mSceneLight = mUtility.makeSceneLight(ctx);
        mScene.addNode(mSceneLight.getOwnerObject());

        // read avatar default from preferences
        Avatar.AvatarPreferences pref = new Avatar.AvatarPreferences(ctx.getContext());
        final AvatarReader.Location loc = pref.getLocation();
        final String name = pref.getName();

        Log.d(TAG, "Default Avatar: %s, %s", loc.name(), name);

        if (mAvatar == null)
        {
            Log.e(TAG, "Avatar could not be found");
        } else {
            Log.d(TAG, "Avatar : %s , model: %s", mAvatar.getName(), mAvatar.getModel());
        }

        mMixedReality = new SXRMixedReality(mScene, false);

        mPlaneEventsListener = new PlaneEventsListener(mContext, mUtility, mMixedReality, mAvatar);
        mMixedReality.getEventReceiver().addListener(mPlaneEventsListener);
        mMixedReality.getEventReceiver().addListener(mAnchorEventsListener);
        mMixedReality.resume();
    }

    @Override
    public void onStep() {
        if (mMixedReality != null) {
            float light = mMixedReality.getLightEstimate().getPixelIntensity() * 1.5f;
            mSceneLight.setAmbientIntensity(light, light, light, 1);
            mSceneLight.setDiffuseIntensity(light, light, light, 1);
            mSceneLight.setSpecularIntensity(light, light, light, 1);
        }
    }

    private IAnchorEvents mAnchorEventsListener = new IAnchorEvents() {
        @Override
        public void onAnchorStateChange(SXRAnchor SXRAnchor, SXRTrackingState SXRTrackingState)
        {
            SXRAnchor.setEnable(SXRTrackingState == SXRTrackingState.TRACKING);
        }
    };
}
