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

package com.samsungxr.keyboard.mic;

import com.samsungxr.SXRContext;
import com.samsungxr.SXRSceneObject;
import com.samsungxr.SXRSphereCollider;
import com.samsungxr.keyboard.R;
import com.samsungxr.keyboard.mic.model.MicItem;
import com.samsungxr.keyboard.util.SceneObjectNames;

public class MicGroupHitArea extends SXRSceneObject {

    MicItem mHitArea;

    public MicGroupHitArea(SXRContext gvrContext) {
        super(gvrContext);
        setName(SceneObjectNames.MIC_GROUP_HIT_AREA);

        mHitArea = new MicItem(gvrContext, R.raw.empty);
        this.addChildObject(mHitArea);
        enableHitArea(gvrContext, mHitArea);

    }

    public SXRSceneObject getHitAreaObject() {

        return mHitArea;
    }

    private void enableHitArea(SXRContext gvrContext, MicItem hitArea) {
        attachDefaultEyePointee(hitArea);
    }

    private void attachDefaultEyePointee(SXRSceneObject sceneObject) {
        sceneObject.attachComponent(new SXRSphereCollider(getSXRContext()));
    }

}
