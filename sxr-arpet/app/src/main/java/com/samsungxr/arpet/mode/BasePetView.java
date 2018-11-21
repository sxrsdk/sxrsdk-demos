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

package com.samsungxr.arpet.mode;

import com.samsungxr.SXRNode;
import com.samsungxr.SXRScene;

import com.samsungxr.arpet.PetContext;

public abstract class BasePetView extends SXRNode implements IPetView {
    protected final PetContext mPetContext;
    protected ILoadEvents mLoadListener;

    public BasePetView(PetContext petContext) {
        super(petContext.getSXRContext());
        mPetContext = petContext;
    }

    @Override
    public void show(SXRScene mainScene) {
        onShow(mainScene);
    }

    @Override
    public void hide(SXRScene mainScene) {
        onHide(mainScene);
    }

    @Override
    public void load(ILoadEvents listener) {
        mLoadListener = listener;
    }

    @Override
    public void unload() {
        mLoadListener = null;
    }

    protected abstract void onShow(SXRScene mainScene);

    protected abstract void onHide(SXRScene mainScene);
}
