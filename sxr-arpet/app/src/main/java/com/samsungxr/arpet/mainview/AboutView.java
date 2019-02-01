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

package com.samsungxr.arpet.mainview;

import android.view.View;

import com.samsungxr.arpet.R;
import com.samsungxr.arpet.view.BaseView;
import com.samsungxr.arpet.view.IViewController;

public class AboutView extends BaseView implements IAboutView {

    private View mBackButton;

    public AboutView(View view, IViewController controller) {
        super(view, controller);
        this.mBackButton = view.findViewById(R.id.button_back);
    }

    @Override
    public void setBackClickListener(View.OnClickListener listener) {
        mBackButton.setOnClickListener(listener);
    }


}
