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

package com.samsungxr.arpet.view;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.samsungxr.SXRRenderData;
import com.samsungxr.SXRScene;
import com.samsungxr.nodes.SXRViewNode;

import com.samsungxr.arpet.PetContext;
import com.samsungxr.arpet.R;
import com.samsungxr.arpet.constant.PetConstants;
import com.samsungxr.arpet.mode.BasePetView;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class BaseViewController extends BasePetView implements IViewController {

    private final String TAG = getClass().getSimpleName();
    private Map<Class<? extends IView>, ViewInfo> mViewInfo = new HashMap<>();

    private ViewGroup mViewContent;
    private BaseView mViewModel;
    private DisplayMetrics mDisplayMetrics;

    public BaseViewController(PetContext petContext) {
        this(petContext, R.layout.view_main_content_dimmed);
    }

    public BaseViewController(PetContext petContext, @LayoutRes int viewContentId) {
        super(petContext);

        mDisplayMetrics = new DisplayMetrics();
        petContext.getActivity().getWindowManager().getDefaultDisplay().getRealMetrics(mDisplayMetrics);

        mViewContent = (ViewGroup) View.inflate(petContext.getSXRContext().getContext(), viewContentId, null);
        mViewContent.setLayoutParams(new ViewGroup.LayoutParams(mDisplayMetrics.widthPixels, mDisplayMetrics.heightPixels));

        SXRViewNode viewObject = new SXRViewNode(petContext.getSXRContext(), mViewContent);
        viewObject.getRenderData().setRenderingOrder(SXRRenderData.SXRRenderingOrder.OVERLAY);
        viewObject.setTextureBufferSize(PetConstants.TEXTURE_BUFFER_SIZE);

        addChildObject(viewObject);
    }

    @Override
    public void registerView(Class<? extends IView> viewInterface, int layoutId, Class<? extends BaseView> viewImplementation) {
        mViewInfo.put(viewInterface, new ViewInfo(layoutId, viewImplementation));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends IView> T makeView(@NonNull Class<T> type) {

        ViewInfo viewInfo = mViewInfo.get(type);
        if (viewInfo == null) {
            throw new RuntimeException("View type not registered: " + type.getClass().getSimpleName());
        }

        View view = View.inflate(mPetContext.getSXRContext().getContext(), viewInfo.layoutId, null);
        T viewModel = null;

        try {
            Constructor constructor = viewInfo.viewType.getConstructor(View.class, IViewController.class);
            viewModel = (T) constructor.newInstance(view, this);
        } catch (NoSuchMethodException | IllegalAccessException
                | InstantiationException | InvocationTargetException e) {
            Log.e(TAG, "Error showing view of type " + type.getName(), e);
        }

        return viewModel;
    }

    @Override
    public void showView(IView viewModel) {

        if (!viewModel.getClass().isInstance(mViewModel)) {
            mPetContext.getActivity().runOnUiThread(() -> {

                BaseView vm = (BaseView) viewModel;

                View view = vm.getView();
                view.setLayoutParams(new ViewGroup.LayoutParams(
                        mDisplayMetrics.widthPixels, mDisplayMetrics.heightPixels));

                clearContentView();
                mViewContent.addView(view);
                mViewModel = vm;
                mViewModel.onShown();
                Log.d(TAG, "showView: " + viewModel.getClass().getSimpleName());
            });
        }
    }

    public IView getCurrentView() {
        return mViewModel;
    }

    private void clearContentView() {
        mPetContext.getActivity().runOnUiThread(() -> {
            mViewContent.removeAllViews();
            mViewModel = null;
        });
    }

    @Override
    public void onShow(SXRScene mainScene) {
        mainScene.getMainCameraRig().addChildObject(this);
        mPetContext.runDelayedOnPetThread(() ->
                getTransform().setPosition(0.0f, 0.0f, -0.74f), 100);
    }

    @Override
    public void onHide(SXRScene mainScene) {
        clearContentView();
        mainScene.getMainCameraRig().removeChildObject(this);
    }

    private static class ViewInfo {

        @LayoutRes
        int layoutId;

        Class<? extends IView> viewType;

        ViewInfo(@LayoutRes int layoutId, Class<? extends IView> viewType) {
            this.layoutId = layoutId;
            this.viewType = viewType;
        }
    }
}
