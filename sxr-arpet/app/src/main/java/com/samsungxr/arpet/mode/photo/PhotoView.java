/*
 * Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.samsungxr.arpet.mode.photo;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.samsungxr.arpet.R;
import com.samsungxr.arpet.view.BaseView;
import com.samsungxr.arpet.view.IViewController;

public class PhotoView extends BaseView implements IPhotoView {

    private View mCancelButton;
    private View mFacebookButton, mWhatsAppButton, mInstagramButton, mTwitterButton;
    private ImageView mPhoto;
    private View mPhotoTarget;
    private LinearLayout mPhotoContent;
    private View mFlashView;

    public PhotoView(View view, IViewController controller) {
        super(view, controller);
        this.mCancelButton = view.findViewById(R.id.cancel_photo);
        this.mFacebookButton = view.findViewById(R.id.button_facebook);
        this.mWhatsAppButton = view.findViewById(R.id.button_whatsapp);
        this.mInstagramButton = view.findViewById(R.id.button_instagram);
        this.mTwitterButton = view.findViewById(R.id.button_twitter);
        this.mPhoto = view.findViewById(R.id.image_photo);
        this.mPhotoTarget = view.findViewById(R.id.photo_target);
        this.mPhotoContent = view.findViewById(R.id.photo_content);
        this.mFlashView = view.findViewById(R.id.view_flash);
    }

    @Override
    public void setOnCancelClickListener(View.OnClickListener listener) {
        mCancelButton.setOnClickListener(listener);
    }

    @Override
    public void setOnActionsShareClickListener(View.OnClickListener listener) {
        mFacebookButton.setOnClickListener(listener);
        mWhatsAppButton.setOnClickListener(listener);
        mInstagramButton.setOnClickListener(listener);
        mTwitterButton.setOnClickListener(listener);
    }

    @Override
    public void setPhotoBitmap(Bitmap bitmap) {
        mPhotoTarget.post(() -> {
            takeFlash(this::animatePhoto);
            mPhoto.setScaleX(mPhoto.getScaleX() * 1.3f);
            mPhoto.setScaleY(mPhoto.getScaleY() * 1.3f);
            mPhoto.setImageBitmap(bitmap);
        });
    }

    private void takeFlash(Runnable onFlashEnds) {
        mFlashView.setAlpha(1);
        mFlashView.animate().alpha(0)
                .setDuration(600)
                .withEndAction(onFlashEnds);
    }

    private void animatePhoto() {

        float x1 = mPhoto.getX(), y1 = mPhoto.getY();
        float w1 = mPhoto.getWidth(), h1 = mPhoto.getHeight();
        float x2 = mPhotoContent.getX(), y2 = mPhotoContent.getY();
        float w2 = mPhotoContent.getWidth(), h2 = mPhotoContent.getHeight();

        mPhotoTarget.postDelayed(() -> mPhoto.animate()
                .setDuration(400)
                .rotation(-6)
                .scaleX(mPhotoTarget.getWidth() / (1f * mPhoto.getWidth()))
                .scaleY(mPhotoTarget.getHeight() / (1f * mPhoto.getHeight()))
                .translationXBy(x2 - x1 - w1 * (1 - w2 / w1) / 2)
                .translationYBy(y2 - y1 - h1 * (1 - h2 / h1) / 2), 600);
    }

}
