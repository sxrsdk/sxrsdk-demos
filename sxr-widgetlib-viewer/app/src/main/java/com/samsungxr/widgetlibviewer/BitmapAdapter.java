package com.samsungxr.widgetlibviewer;

import android.graphics.Bitmap;

import com.samsungxr.SXRContext;
import com.samsungxr.widgetlib.adapter.BaseAdapter;
import com.samsungxr.widgetlib.log.Log;
import com.samsungxr.widgetlib.main.SXRBitmapTexture;
import com.samsungxr.widgetlib.widget.GroupWidget;
import com.samsungxr.widgetlib.widget.Widget;

import java.util.ArrayList;
import java.util.List;

import static com.samsungxr.utility.Threads.spawn;

public class BitmapAdapter extends BaseAdapter {
    protected float widthQuad = 4;
    protected float heightQuad = widthQuad / 2.f;

    public BitmapAdapter(SXRContext sxrContext,
                         final List<? extends BitmapGetter> items) {
        Log.d(TAG, "CTOR(): items: %d", items.size());
        mBitmaps = new ArrayList<Bitmap>();
        mGvrContext = sxrContext;
        final Runnable onBackgroundThread = new Runnable() {
            @Override
            public void run() {
                getBitmaps(items);
                notifyDataSetChanged();
            }
        };
        spawn(onBackgroundThread);
    }

    @Override
    public int getCount() {
        final int size = mBitmaps.size();
        Log.d(TAG, "getCount(): %d", size);
        return size;
    }

    @Override
    public SXRBitmapTexture getItem(int position) {
        SXRBitmapTexture texture = mTextures.get(position);
        if (texture == null) {
            final Bitmap bitmap = mBitmaps.get(position);
            texture = new SXRBitmapTexture(mGvrContext, bitmap);
            mTextures.set(position, texture);

            mBitmaps.set(position, null);
        }
        return texture;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private void getBitmaps(
            final List<? extends BitmapGetter> backgroundsInfo) {
        for (BitmapGetter item : backgroundsInfo) {
            final Bitmap bitmap = item.get();
            if (null == bitmap) {
                Log.e(TAG,
                        "no bitmap object retrieved from the bitmap getter; item"
                                + item);
            }
            mBitmaps.add(bitmap);
            mTextures.add(null); // Prep list for on-demand texture
            // generation
        }
    }

    private final List<Bitmap> mBitmaps;
    private final List<SXRBitmapTexture> mTextures = new ArrayList<SXRBitmapTexture>();
    private final SXRContext mGvrContext;
    private final String TAG = BitmapAdapter.class.getSimpleName();

    @Override
    public Widget getView(int position,
                          Widget convertView,
                          GroupWidget parent) {

        Log.d(TAG, "getView(): %05.2f, %05.2f at %d", widthQuad,
                heightQuad, position);
        final SXRBitmapTexture texture = getItem(position);
        if (convertView != null) {
            convertView.setTexture(texture);
            return convertView;
        }
        Widget quadWidget = new Widget(mGvrContext, widthQuad, heightQuad);
        quadWidget.setTexture(texture);
        return quadWidget;
    }

    public interface BitmapGetter {
        Bitmap get();
    }
}
