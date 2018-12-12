package com.samsungxr.avatar_fashion;

import com.samsungxr.SXRContext;
import com.samsungxr.widgetlib.adapter.BaseAdapter;
import com.samsungxr.widgetlib.log.Log;
import com.samsungxr.widgetlib.widget.GroupWidget;
import com.samsungxr.widgetlib.widget.Widget;

import java.util.List;

import static com.samsungxr.utility.Log.tag;

public class AvatarAdapter extends BaseAdapter {
    protected float mSizeQuad = 4;
    private final List<Avatar> mAvatars;
    private final SXRContext mSxrContext;
    private final String TAG = tag(AvatarAdapter.class);

    public AvatarAdapter(SXRContext sxrContext,
                        final List<Avatar> avatars) {
        mSxrContext = sxrContext;
        Log.d(TAG, "CTOR(): items: %d", avatars.size());
        mAvatars = avatars;
    }

    @Override
    public int getCount() {
        final int size = mAvatars.size();
        Log.d(TAG, "getCount(): %d", size);
        return size;
    }

    @Override
    public Avatar getItem(int position) {
        return mAvatars.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

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
        Widget avatarBox = getItem(position);
        avatarBox.setTouchable(true);
        avatarBox.setFocusEnabled(true);
        avatarBox.setChildrenFollowFocus(true);
        avatarBox.setChildrenFollowInput(true);

        return avatarBox;
    }
}
