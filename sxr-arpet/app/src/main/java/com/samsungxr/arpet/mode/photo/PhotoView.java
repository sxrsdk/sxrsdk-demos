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

import com.samsungxr.arpet.R;
import com.samsungxr.arpet.view.BaseView;
import com.samsungxr.arpet.view.IViewController;

public class PhotoView extends BaseView implements IPhotoView {

    private View mCancelButton;
    private View mFacebookButton, mWhatsAppButton, mInstagramButton, mTwitterButton;
    private ImageView mPhoto;

    public PhotoView(View view, IViewController controller) {
        super(view, controller);
        this.mCancelButton = view.findViewById(R.id.cancel_photo);
        this.mFacebookButton = view.findViewById(R.id.button_facebook);
        this.mWhatsAppButton = view.findViewById(R.id.button_whatsapp);
        this.mInstagramButton = view.findViewById(R.id.button_instagram);
        this.mTwitterButton = view.findViewById(R.id.button_twitter);
        this.mPhoto = view.findViewById(R.id.image_photo);
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
        runOnUiThread(() -> mPhoto.setImageBitmap(bitmap));
    }
}
