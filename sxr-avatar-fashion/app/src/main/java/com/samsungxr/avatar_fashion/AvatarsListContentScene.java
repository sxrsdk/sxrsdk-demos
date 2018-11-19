package com.samsungxr.avatar_fashion;

import com.samsungxr.SXRActivity;
import com.samsungxr.SXRContext;
import com.samsungxr.widgetlib.adapter.Adapter;
import com.samsungxr.widgetlib.log.Log;
import com.samsungxr.widgetlib.main.WidgetLib;
import com.samsungxr.widgetlib.widget.ListWidget;
import com.samsungxr.widgetlib.widget.Widget;
import com.samsungxr.widgetlib.widget.custom.PickerWidget;
import com.samsungxr.widgetlib.widget.layout.Layout;
import com.samsungxr.widgetlib.widget.layout.LayoutScroller;
import com.samsungxr.widgetlib.widget.layout.basic.LinearLayout;
import org.json.JSONObject;

import java.util.List;

import static com.samsungxr.utility.Log.tag;
import static com.samsungxr.widgetlib.widget.properties.JSONHelpers.optFloat;

public class AvatarsListContentScene extends BaseContentScene {
    AvatarsListContentScene(SXRContext sxrContext,
                            SXRActivity activity,
                            Widget.OnTouchListener settingsListener) {
        super(sxrContext, activity);
        mControlBar.addControlListener("Settings", settingsListener);
    }

    @Override
    protected Widget createContent() {
        mHorizontalPicker = setupHorizontalPicker();
        return mHorizontalPicker;
    }
    public enum Properties {arch_radius, background_picker}

    @Override
    public String getName() {
        return TAG;
    }

    @Override
    public void show() {
        Log.d(TAG, "Avatar list content scene show!");
        super.show();
        mHorizontalPicker.show();
    }

    @Override
    public void hide() {
        super.hide();
        mHorizontalPicker.hide();
    }

    @Override
    public void onProximityChange(boolean onProximity) {
        if (onProximity) {
            mHorizontalPicker.hide();
            mHorizontalPicker.show();
        }
    }


    List<Avatar> mAvatars;
    void setAvatarList(List<Avatar> avatars) {
        mAvatars = avatars;
    }

    private PickerWidget setupHorizontalPicker() {
        Adapter avatarAdapter = new AvatarAdapter(mSxrContext, mAvatars);

        JSONObject properties = WidgetLib.getPropertyManager().getInstanceProperties(getClass(), TAG);
        final float panelPadding = optFloat(properties, BaseContentScene.Properties.padding, PANEL_PADDING);

        LinearLayout listLayout = new LinearLayout();
        listLayout.setDividerPadding(panelPadding, Layout.Axis.X);
        listLayout.enableClipping(true);

        PickerWidget horizontalPicker = new PickerWidget(mSxrContext, avatarAdapter,0, 0);

        horizontalPicker.setViewPortWidth(Float.POSITIVE_INFINITY);

        horizontalPicker.enableFocusAnimation(true);
        horizontalPicker.enableTransitionAnimation(true);
        horizontalPicker.applyLayout(listLayout);

        ListWidget.OnItemFocusListener focusListener = new ListWidget.OnItemFocusListener() {
            public void onFocus(ListWidget list, boolean focused, int dataIndex) {
                Avatar avatar = (Avatar)(list.getView(dataIndex));
            }
            public void onLongFocus(ListWidget list, int dataIndex) {
                Log.d(TAG, "onLongFocus: dataIndex = %d", dataIndex);
                Avatar avatar = (Avatar)(list.getView(dataIndex));
            }
        };

        horizontalPicker.addOnItemFocusListener(focusListener);

        ListWidget.OnItemTouchListener touchListener = new ListWidget.OnItemTouchListener() {
            @Override
            public boolean onTouch(ListWidget listWidget, int dataIndex) {
                Log.d(TAG, "onTouch: dataIndex = %d", dataIndex);
                Avatar avatar = (Avatar)(listWidget.getView(dataIndex));
                ((AvatarFashionActivity)mActivity).startAvatarTracker(avatar);
                return true;

            }
        };

        horizontalPicker.addOnItemTouchListener(touchListener);

        horizontalPicker.setViewPortHeight(Avatar.MAX_RADIUS);


        horizontalPicker.hide();
        return horizontalPicker;
    }

    private LayoutScroller getLayoutScroller() {
        if (mLayoutScroller == null) {
            mLayoutScroller = new LayoutScroller(mSxrContext.getContext(), mHorizontalPicker);
        }
        return mLayoutScroller;
    }

    @Override
    protected void scrollLeft() {
        Log.d(TAG, "scrollLeft");
        getLayoutScroller().scrollToPrevItem();
    }

    @Override
    protected void scrollRight() {
        Log.d(TAG, "scrollRight");
        getLayoutScroller().scrollToNextItem();
    }
    private static final float PANEL_PADDING = 0.8f;
    private LayoutScroller mLayoutScroller;
    private PickerWidget mHorizontalPicker;

    private static final String TAG = tag(AvatarsListContentScene.class);
}
