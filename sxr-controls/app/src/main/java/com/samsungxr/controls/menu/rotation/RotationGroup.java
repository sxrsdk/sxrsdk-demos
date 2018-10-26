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

package com.samsungxr.controls.menu.rotation;

import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRSceneObject;
import com.samsungxr.animation.SXRAnimation;
import com.samsungxr.animation.SXROnFinish;
import com.samsungxr.animation.SXRRotationByAxisAnimation;
import com.samsungxr.controls.Main;
import com.samsungxr.controls.R;
import com.samsungxr.controls.anim.StarPreviewInfo;

public class RotationGroup extends SXRSceneObject {
    private static final float SCALE_FACTOR = 0.7f;
    SXRSceneObject star;
    SXRSceneObject base;
    private SXRSceneObject place;

    public RotationGroup(SXRContext sxrContext) {
        super(sxrContext);

        place = new SXRSceneObject(sxrContext);

        addChildObject(place);
        createStar();
        createBase();
        place.getTransform().setScale(SCALE_FACTOR, SCALE_FACTOR, SCALE_FACTOR);
        place.addChildObject(star);
        place.addChildObject(base);

        StarPreviewInfo.putStarReference(place);
    }

    private void createBase() {

        SXRAndroidResource baseTextRes = new SXRAndroidResource(getSXRContext(),
                R.drawable.direction_rotation);

        base = new SXRSceneObject(getSXRContext(), 1, 1, getSXRContext().getAssetLoader().loadTexture(baseTextRes));

        base.getTransform().rotateByAxis(90, 0, 0, 1);
        base.getTransform().rotateByAxis(-90, 1, 0, 0);
    }

    private void createStar() {

        SXRAndroidResource starMeshRes = new SXRAndroidResource(getSXRContext(), R.raw.star);
        SXRAndroidResource starTextRes = new SXRAndroidResource(getSXRContext(),
                R.drawable.star_diffuse);

        star = new SXRSceneObject(getSXRContext(), starMeshRes, starTextRes);
        star.getTransform().setPositionY(0.5f);
    }

    public void rotate(final float angleFactor) {

        SXRRotationByAxisAnimation rotationAnimation = new SXRRotationByAxisAnimation(place, 0.1f, angleFactor, 0, 1, 0);

        rotationAnimation.setOnFinish(new SXROnFinish() {

            @Override
            public void finished(SXRAnimation arg0) {

                Main.enableAnimationStar();

                if(angleFactor > 0){

                    StarPreviewInfo.changeRotationFactor(StarPreviewInfo.Direction.left);
                } else {

                    StarPreviewInfo.changeRotationFactor(StarPreviewInfo.Direction.right);
                }
            }
        });

        rotationAnimation.start(getSXRContext().getAnimationEngine());
    }
}