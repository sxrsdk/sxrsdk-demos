package com.samsungxr.widgetlibviewer;

import com.samsungxr.SXRContext;
import com.samsungxr.SXRRenderData;
import com.samsungxr.widgetlib.adapter.BaseAdapter;
import com.samsungxr.widgetlib.log.Log;
import com.samsungxr.widgetlib.widget.GroupWidget;
import com.samsungxr.widgetlib.widget.Widget;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ModelAdapter extends BaseAdapter {
    protected float mSizeQuad = 4;

    public ModelAdapter(SXRContext gvrContext,
                         final List<String> modelsList) {
        Log.d(TAG, "CTOR(): items: %d", modelsList.size());
        mModelsPath = modelsList;
        mGvrContext = gvrContext;
    }

    @Override
    public int getCount() {
        final int size = mModelsPath.size();
        Log.d(TAG, "getCount(): %d", size);
        return size;
    }

    @Override
    public Model getItem(int position) {
        Model model = null;
        try {
            model = new Model(mGvrContext,
                    mModelsPath.get(position).replaceAll("\\..*", ""),
                    mModelsPath.get(position));
        } catch (IOException e) {
            Log.w(TAG, "No models loaded!");
        }
        return model;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private final List<String> mModelsPath;
    private final SXRContext mGvrContext;
    private final String TAG = ModelAdapter.class.getSimpleName();

    @Override
    public boolean hasUniformViewSize() {
        return true;
    }

    @Override
    public float getUniformWidth() {
        return mSizeQuad;
    }

    @Override
    public float getUniformHeight() {
        return mSizeQuad;
    }

    @Override
    public float getUniformDepth() {
        return mSizeQuad;
    }

    @Override
    public Widget getView(int position,
                          Widget convertView,
                          GroupWidget parent) {
        Widget modelBox = getItem(position);
        return modelBox;
    }
}
