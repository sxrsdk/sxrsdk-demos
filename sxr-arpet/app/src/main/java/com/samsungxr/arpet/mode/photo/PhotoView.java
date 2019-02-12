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
import android.widget.FrameLayout;
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
    private LinearLayout mToast_photo;

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
        this.mToast_photo = view.findViewById(R.id.toast_photo);
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
            mPhotoContent.setScaleX(mPhotoContent.getScaleX() * 1.3f);
            mPhotoContent.setScaleY(mPhotoContent.getScaleY() * 1.3f);
            mPhoto.setImageBitmap(bitmap);
        });
    }

    @Override
    public void showToast() {
        runOnUiThread(() -> mToast_photo.setVisibility(View.VISIBLE));
    }

    @Override
    public void enableButtons() {
        runOnUiThread(() -> {
            mFacebookButton.setClickable(true);
            mWhatsAppButton.setClickable(true);
            mInstagramButton.setClickable(true);
            mTwitterButton.setClickable(true);
        });
    }

    private void takeFlash(Runnable onFlashEnds) {
        mFlashView.setAlpha(1);
        mFlashView.animate().alpha(0)
                .setDuration(600)
                .withEndAction(onFlashEnds);
    }

    private void animatePhoto() {

        int[] photoTargetPos = {0, 0};
        mPhotoTarget.getLocationOnScreen(photoTargetPos);

        float x1 = mPhotoContent.getX(), y1 = mPhotoContent.getY();
        float w1 = mPhotoContent.getWidth(), h1 = mPhotoContent.getHeight();
        float x2 = photoTargetPos[0], y2 = photoTargetPos[1];
        float w2 = mPhotoTarget.getWidth(), h2 = mPhotoTarget.getHeight();

        mPhotoTarget.postDelayed(() -> mPhotoContent.animate()
                .setDuration(400)
                .rotation(-6)
                .scaleX(mPhotoTarget.getWidth() / (1f * mPhotoContent.getWidth()))
                .scaleY(mPhotoTarget.getHeight() / (1f * mPhotoContent.getHeight()))
                .translationXBy(x2 - x1 - w1 * (1 - w2 / w1) / 2)
                .translationYBy(y2 - y1 - h1 * (1 - h2 / h1) / 2), 600);
    }

}
