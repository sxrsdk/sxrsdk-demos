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

package com.samsungxr.immersivepedia.model;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;

import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRMeshCollider;
import com.samsungxr.SXRScene;
import com.samsungxr.SXRSceneObject;
import com.samsungxr.animation.SXROpacityAnimation;
import com.samsungxr.animation.SXRRotationByAxisAnimation;
import com.samsungxr.immersivepedia.R;
import com.samsungxr.immersivepedia.dinosaur.Dinosaur;
import com.samsungxr.immersivepedia.dinosaur.DinosaurFactory;
import com.samsungxr.immersivepedia.focus.OnGestureListener;
import com.samsungxr.immersivepedia.props.Totem;
import com.samsungxr.immersivepedia.props.TotemEventListener;
import com.samsungxr.immersivepedia.util.AudioClip;
import com.samsungxr.immersivepedia.util.RenderingOrderApplication;
import com.samsungxr.scene_objects.SXRTextViewSceneObject;
import com.samsungxr.scene_objects.SXRTextViewSceneObject.IntervalFrequency;

import java.io.IOException;

public class TextDinosaurGroup extends SXRSceneObject implements TotemEventListener, OnGestureListener {

    private final float DESCRIPTION_HEIGHT = 3f;
    private final float DESCRIPTION_WIDTH = 16f;
    private final float TITLE_HEIGHT = 1f;
    private final float TITLE_WIDTH = 6f;
    private Dinosaur ankylosaurus;
    private SXRContext sxrContext;
    private SXRScene scene;
    private static final float TEXT_ANIMATION_TIME = 0.2f;
    private boolean isOpen;
    private SXRTextViewSceneObject title;
    private SXRTextViewSceneObject description;

    public TextDinosaurGroup(SXRContext sxrContext, SXRScene scene) throws IOException {
        super(sxrContext);

        this.sxrContext = sxrContext;
        this.scene = scene;
        createTotem();
        createDinosaur();
    }

    private void createDinosaur() {
        ankylosaurus = DinosaurFactory.getInstance(getSXRContext()).getAnkylosaurus();
        ankylosaurus.getTransform().setRotationByAxis(-90, 1, 0, 0);
        ankylosaurus.attachCollider(new SXRMeshCollider(getSXRContext(), true));
        ankylosaurus.setOnGestureListener(this);
        ankylosaurus.setName("ankylosaurus");
        addChildObject(ankylosaurus);
    }

    private void createTotem() {
        Totem totem = new Totem(this.sxrContext,
                this.sxrContext.getAssetLoader().loadTexture(new SXRAndroidResource(sxrContext,
                        R.drawable.totem_tex_diffuse)));

        totem.getTransform().setPosition(0f, 0f, 0f);
        totem.setName("totem_ankylosaurus");
        totem.setTotemEventListener(this);
        scene.addSceneObject(totem);
        totem.getTransform().setPosition(-.3f, 0f, -5.0f);
        totem.getTransform().rotateByAxis(180.0f, 0f, 1f, 0f);
        totem.getTransform().setScale(1f, 1f, 1f);
        totem.setText(sxrContext.getActivity().getResources().getString(R.string.text_totem));
        totem.getTransform().rotateByAxisWithPivot(
                DinosaurFactory.ANKYLOSAURUS_ANGLE_AROUND_CAMERA - 35.0f, 0f, 1f, 0f, 0f, 0f, 0f);
    }

    @Override
    public void onFinishLoadingTotem(Totem totem) {
        sxrContext.runOnGlThread(new Runnable() {
            @Override
            public void run() {
                AudioClip.getInstance(getSXRContext().getContext()).playSound(AudioClip.getUITextAppearSoundID(), 1.0f, 1.0f);
                show();
            }
        });
    }

    @Override
    public boolean shouldTotemAppear(Totem totem) {
        return !isOpen;
    }

    @Override
    public void onSwipeUp() {
    }

    @Override
    public void onSwipeDown() {
    }

    @Override
    public void onSwipeForward() {
        AudioClip.getInstance(getSXRContext().getContext()).playSound(AudioClip.getUIRotateSoundID(), 1.0f, 1.0f);
        new SXRRotationByAxisAnimation(ankylosaurus, 4f, 45, 0, 1, 0).start(sxrContext.getAnimationEngine());
    }

    @Override
    public void onSwipeBack() {
        AudioClip.getInstance(getSXRContext().getContext()).playSound(AudioClip.getUIRotateSoundID(), 1.0f, 1.0f);
        new SXRRotationByAxisAnimation(ankylosaurus, 4f, -45, 0, 1, 0).start(sxrContext.getAnimationEngine());
    }

    @Override
    public void onSwipeIgnore() {
    }

    public void closeAction() {
        AudioClip.getInstance(getSXRContext().getContext()).playSound(AudioClip.getUITextDisappearSoundID(), 1.0f, 1.0f);
        new SXROpacityAnimation(description, TEXT_ANIMATION_TIME, 0f).start(getSXRContext().getAnimationEngine());
        new SXROpacityAnimation(title, TEXT_ANIMATION_TIME, 0f).start(getSXRContext().getAnimationEngine());
        isOpen = false;
    }

    private void show() {
        if (title == null) {
            createDinosaurTitle();
        }
        if (description == null) {
            createDinosaurDescription();
        }
        new SXROpacityAnimation(description, TEXT_ANIMATION_TIME, 1f).start(getSXRContext().getAnimationEngine());
        new SXROpacityAnimation(title, TEXT_ANIMATION_TIME, 1f).start(getSXRContext().getAnimationEngine());
        isOpen = true;
    }

    private void createDinosaurTitle() {
        Resources resources = sxrContext.getActivity().getResources();
        String stringTitle = getSXRContext().getContext().getString(R.string.ankylosaurus_title);
        Bitmap titleBitmap = BitmapFactory.decodeResource(resources, R.drawable.title_background);
        BitmapDrawable background = new BitmapDrawable(resources, titleBitmap);
        title = new SXRTextViewSceneObject(sxrContext, TITLE_WIDTH, TITLE_HEIGHT, stringTitle);
        title.setRefreshFrequency(IntervalFrequency.LOW);
        title.setTextColor(Color.BLACK);
        title.setBackGround(background);
        title.getTransform().setScale(0.3f, 0.3f, 0.3f);
        title.setTextSize(16);
        title.setGravity(android.view.Gravity.CENTER_HORIZONTAL);
        title.getTransform().setPosition(-2f, 2.6f, 3f);
        addChildObject(title);
        title.getRenderData().getMaterial().setOpacity(0);
    }

    private void createDinosaurDescription() {
        description = new SXRTextViewSceneObject(getSXRContext(), DESCRIPTION_WIDTH, DESCRIPTION_HEIGHT,
                getSXRContext().getContext().getString(R.string.ankylosaurus_text));
        description.setGravity(Gravity.LEFT);
        description.setTextColor(Color.BLACK);
        description.getTransform().setPositionY(2f);
        description.getRenderData().setRenderingOrder(RenderingOrderApplication.TEXT_BACKGROUND);
        description.setTextSize(5);
        description.setBackgroundColor(Color.WHITE);
        description.getTransform().setScale(0.3f, 0.3f, 0.3f);
        description.getTransform().setPosition(-.3f, 1.7f, 3f);
        addChildObject(description);
        description.getRenderData().getMaterial().setOpacity(0);
    }

    public boolean isOpen() {
        return isOpen;
    }

}
