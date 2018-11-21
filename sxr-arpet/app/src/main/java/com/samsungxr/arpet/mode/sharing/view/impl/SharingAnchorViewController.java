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

package com.samsungxr.arpet.mode.sharing.view.impl;

import com.samsungxr.arpet.PetContext;
import com.samsungxr.arpet.R;
import com.samsungxr.arpet.mainview.BaseViewController;
import com.samsungxr.arpet.mainview.ConnectionFinishedView;
import com.samsungxr.arpet.mode.sharing.view.IConnectionFoundView;
import com.samsungxr.arpet.mode.sharing.view.IGuestLookingAtTargetView;
import com.samsungxr.arpet.mode.sharing.view.IHostLookingAtTargetView;
import com.samsungxr.arpet.mode.sharing.view.ILetsStartView;
import com.samsungxr.arpet.mode.sharing.view.ISharingErrorView;
import com.samsungxr.arpet.mainview.IConnectionFinishedView;
import com.samsungxr.arpet.mode.sharing.view.IWaitingForGuestView;
import com.samsungxr.arpet.mode.sharing.view.IWaitingForHostView;

public class SharingAnchorViewController extends BaseViewController {

    public SharingAnchorViewController(PetContext petContext) {
        super(petContext);
        registerView(ILetsStartView.class, R.layout.view_lets_start, LetsStartView.class);
        registerView(IWaitingForHostView.class, R.layout.view_waiting_for_host, WaitingForHostView.class);
        registerView(IWaitingForGuestView.class, R.layout.view_waiting_for_guests, WaitingForGuestView.class);
        registerView(IConnectionFoundView.class, R.layout.view_connection_found, ConnectionFoundView.class);
        registerView(IGuestLookingAtTargetView.class, R.layout.view_guest_looking_at_target, GuestLookingAtTargetView.class);
        registerView(IHostLookingAtTargetView.class, R.layout.view_host_looking_at_target, HostLookingAtTargetView.class);
        registerView(IConnectionFinishedView.class, R.layout.view_connection_finished, ConnectionFinishedView.class);
        registerView(ISharingErrorView.class, R.layout.view_sharing_error, SharingErrorView.class);

    }
}
