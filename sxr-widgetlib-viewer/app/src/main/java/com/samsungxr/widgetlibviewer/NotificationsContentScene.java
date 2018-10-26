package com.samsungxr.widgetlibviewer;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.drawable.Icon;

import com.samsungxr.SXRContext;
import com.samsungxr.SXRMesh;
import com.samsungxr.SXRRenderData;
import com.samsungxr.widgetlib.adapter.BaseAdapter;
import com.samsungxr.widgetlib.log.Log;
import com.samsungxr.widgetlib.main.SXRBitmapTexture;
import com.samsungxr.widgetlib.main.WidgetLib;
import com.samsungxr.widgetlib.widget.GroupWidget;
import com.samsungxr.widgetlib.widget.ListWidget;
import com.samsungxr.widgetlib.widget.Widget;
import com.samsungxr.widgetlib.widget.basic.LightTextWidget;
import com.samsungxr.widgetlib.widget.basic.TextParams;
import com.samsungxr.widgetlib.widget.layout.Layout;
import com.samsungxr.widgetlib.widget.layout.OrientedLayout;
import com.samsungxr.widgetlib.widget.layout.basic.LinearLayout;
import com.samsungxr.widgetlib.widget.properties.JSONHelpers;
import com.samsungxr.widgetlibviewer.SXRWidgetViewer.R;
import org.json.JSONObject;

import static com.samsungxr.utility.Log.tag;
import static com.samsungxr.widgetlib.widget.properties.JSONHelpers.getFloat;
import static com.samsungxr.widgetlib.widget.properties.JSONHelpers.getJSONObject;
import static com.samsungxr.widgetlib.widget.properties.JSONHelpers.optPointF;
public class NotificationsContentScene extends BaseContentScene {
    NotificationsContentScene(SXRContext sxrContext, Widget.OnTouchListener homeListener) {
        super(sxrContext);
        mControlBar.addControlListener("Home", homeListener);
    }

    private enum Properties {
        list, panel, padding
    }

    @Override
    protected Widget createContent() {
        JSONObject properties = WidgetLib.getPropertyManager().getInstanceProperties(getClass(), TAG);
        JSONObject panelProperties = getJSONObject(properties, Properties.panel);

        GroupWidget notificationPanel = new GroupWidget(mGvrContext, panelProperties);
        notificationPanel.setTexture(WidgetLib.getTextureHelper().getSolidColorTexture(Color.RED));
        // main screen layout
        final float padding = JSONHelpers.getFloat(properties, Properties.padding);

        LinearLayout notificationPanelLayout = new LinearLayout();
        notificationPanelLayout.setOrientation(OrientedLayout.Orientation.VERTICAL);
        notificationPanelLayout.setGravity(LinearLayout.Gravity.TOP);
        notificationPanelLayout.enableOuterPadding(true);
        notificationPanelLayout.setDividerPadding(padding, Layout.Axis.Y);
        notificationPanelLayout.setOffset(-0.01f, Layout.Axis.Z);

        //for loading notification_panel_background.fbx, its model is actually the child of the root node.
        //Because of this hierarchy thing, we have to group label and list as children of notificationPanel.
        //Then place notificationPanel as a sibling of the model
        notificationPanel.applyLayout(notificationPanelLayout);

        LinearLayout listLayout = new LinearLayout();
        listLayout.setOrientation(OrientedLayout.Orientation.VERTICAL);
        listLayout.setGravity(LinearLayout.Gravity.TOP);
        listLayout.setDividerPadding(padding, Layout.Axis.Y);
        listLayout.setOffset(-0.01f, Layout.Axis.Z);
        listLayout.enableClipping(true);
        listLayout.enableOuterPadding(true);

        //create List
        JSONObject listProperties = getJSONObject(properties, Properties.list);
        ListWidget list = new ListWidget(mGvrContext, listProperties);
        list.setAdapter(new NotificationAdapter(mGvrContext));
        list.applyLayout(listLayout);
        list.enableClipRegion();
        list.enableTransitionAnimation(true);

        notificationPanel.addChild(list);

        return notificationPanel;
    }

    static class NotificationContentWidget extends GroupWidget {
        private enum Properties {content_with_big_icon}
        private enum ContentProperties {nameNtime, title, text}
        private ResizableLightTextWidget mTitle;
        private ResizableLightTextWidget mText;

        private final DisplayParams mBigIconParams;

        NotificationContentWidget(SXRContext context) {
            super(context);

            JSONObject widgetProperties = getObjectMetadata();

            LinearLayout layout = new LinearLayout();
            layout.setOrientation(OrientedLayout.Orientation.VERTICAL);
            layout.setOffset(-0.01f, Layout.Axis.Z);
            //layout.setGravity(LinearLayout.Gravity.CENTER);
            applyLayout(layout);

            JSONObject bigIconContentProperties = getJSONObject(widgetProperties, Properties.content_with_big_icon);

            mBigIconParams = new DisplayParams(context, bigIconContentProperties);

            //mAppNameNTime = new NotificationAppNameNTimeWidget(context);
            mTitle = new ResizableLightTextWidget(context, 1.0f, 1.0f);
            //mText = new ResizableLightTextWidget(context, 1.0f, 1.0f);


            //mAppNameNTime.setTouchable(false);
            mTitle.setTouchable(false);
            //mText.setTouchable(false);
            //mAppNameNTime.setFocusEnabled(false);
            mTitle.setFocusEnabled(false);
            //mText.setFocusEnabled(false);

            //addChild(mAppNameNTime);
            addChild(mTitle);
            //addChild(mText);
        }

        public void loadNotificationInfo() {

            meshUpdate(mBigIconParams);
            //mAppNameNTime.loadNotificationInfo(notificationInfo);
            //mText.setText(notificationInfo.text);

            //Svetlana +++++
            //set mTitle rendering order to TRANSPARENT
            mTitle.setText("NotificationTitle");
            mTitle.setRenderingOrder(SXRRenderData.SXRRenderingOrder.TRANSPARENT);
            //Svetlana -----
      //      mText.setRenderingOrder(SXRRenderData.SXRRenderingOrder.TRANSPARENT);
        }

        private void meshUpdate(DisplayParams displayParams) {
           mTitle.updateMesh(displayParams.title.mesh);
//            mText.updateMesh(displayParams.text.mesh);
            TextParams params = mTitle.getTextParams();
            params.setFromJSON(getSXRContext().getActivity(), displayParams.title.properties);
           mTitle.setTextParams(params);

//            params = mText.getTextParams();
//            params.setFromJSON(getSXRContext().getActivity(), displayParams.text.properties);
  //          mText.setTextParams(params);
        }

        class ResizableLightTextWidget extends LightTextWidget {
            public ResizableLightTextWidget(SXRContext context, float width, float height) {
                super(context, width, height);
            }
            protected void updateMesh(SXRMesh mesh) {
                setMesh(mesh);
            }
        }


        private static class DisplayParams {
            static class Item {
                final SXRMesh mesh;
                final JSONObject properties;
                final String name;

                Item(SXRContext context, JSONObject properties, String name) {
                    this.properties = properties;
                    this.name = name;

                    PointF size = optPointF(properties, Widget.Properties.size);
                    Log.d(TAG, name + ": (w,h) = " + size.x + "," + size.y);
                    mesh = context.createQuad(size.x, size.y);
                }
            }

            final Item title;
            final Item text;

            DisplayParams(SXRContext context, JSONObject properties) {
                JSONObject titleProperties = getJSONObject(properties, ContentProperties.title);
                JSONObject textProperties = getJSONObject(properties, ContentProperties.text);

                title = new Item(context, titleProperties, "title");
                text = new Item(context, textProperties, "text");
            }
        }
    }

    public class NotificationAdapter extends BaseAdapter {
        private SXRContext mGvrContext;

        public NotificationAdapter(SXRContext sxrContext) {
            mGvrContext = sxrContext;
        }

        @Override
        public int getCount() {
            return 1;
        }

        @Override
        public String getItem(int position) {
            return "NotificationItem";
        }

        public Icon getLargeIcon() {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public Widget getView(int position, Widget convertView, GroupWidget parent) {
            NotificationItemGroupWidget itemGroupWidget= new
                    NotificationItemGroupWidget(mGvrContext, getLargeIcon());

            return itemGroupWidget;
        }
    }

    static class NotificationItemWidget extends GroupWidget {
        private static final String TAG = NotificationItemWidget.class.getSimpleName();
        private NotificationContentWidget mContentWidget;
        private Widget mLargeIcon;
        Icon largeIcon;

        private enum Properties {large_icon}

        public NotificationItemWidget(SXRContext context, Icon icon) {
            super(context);
            largeIcon = icon;
            //NotificationItemWidget has two children, mLargeIcon (in Geometry rendering order), and mContentWidget
            JSONObject properties = getObjectMetadata();
            Log.d(TAG, "NotificationItemWidget(): properties: %s", properties);
            LinearLayout layout = new LinearLayout();
            layout.setGravity(LinearLayout.Gravity.CENTER);
            layout.setOrientation(OrientedLayout.Orientation.HORIZONTAL);
            layout.setOffset(-0.01f, Layout.Axis.Z);
            applyLayout(layout);

            mContentWidget = new NotificationContentWidget(context);
            mContentWidget.setTouchable(false);
            mContentWidget.setFocusEnabled(false);

            JSONObject largeIconProperties = getJSONObject(properties, Properties.large_icon);
            mLargeIcon = new Widget(getSXRContext(), largeIconProperties);
            mLargeIcon.setTouchable(false);
            mLargeIcon.setFocusEnabled(false);
            mLargeIcon.setRenderingOrder(SXRRenderData.SXRRenderingOrder.GEOMETRY);

            addChild(mContentWidget);
            addChild(mLargeIcon);

            setTexture(WidgetLib.getTextureHelper().getBitmapTexture(R.drawable.notification_item));
            setRenderingOrder(SXRRenderData.SXRRenderingOrder.GEOMETRY);
            loadNotificationInfo();
        }

        public void loadNotificationInfo() {
            mContentWidget.loadNotificationInfo();
            if (largeIcon != null) {
                SXRBitmapTexture texture = WidgetLib.getTextureHelper().getBitmapTexture(R.drawable.vr_app);
                mLargeIcon.setTexture(texture);
                mLargeIcon.setVisibility(Visibility.VISIBLE);
            } else {
                mLargeIcon.setVisibility(Visibility.PLACEHOLDER);
            }
        }
    }

    static class NotificationItemGroupWidget extends GroupWidget {
        private final NotificationItemWidget mItemWidget;
        private final CancelButton mCancelButton;

        private enum Properties {
            cancel
        }
        public NotificationItemGroupWidget(SXRContext context, Icon icon) {
            super(context);
            setName("NotificationItemGroupWidget");

            JSONObject properties = getObjectMetadata();

            JSONObject cancelProperties = getJSONObject(properties, Properties.cancel);
            Log.d(TAG, "cancel button: Properties: %s" + cancelProperties);

            LinearLayout layout = new LinearLayout();
            layout.setGravity(LinearLayout.Gravity.TOP);
            layout.setOrientation(OrientedLayout.Orientation.HORIZONTAL);
            layout.setDividerPadding(0.25f, Layout.Axis.X);
            layout.setOffset(-0.01f, Layout.Axis.Z);
            applyLayout(layout);

            mItemWidget = new NotificationItemWidget(context, icon);

            mCancelButton = new CancelButton(context, cancelProperties);
            mCancelButton.setTouchable(true);

            addChild(mItemWidget);
            addChild(mCancelButton);
        }

        private class CancelButton extends Widget {
            String key;

            public CancelButton(SXRContext context, float width, float height) {
                super(context, width, height);
                key = "Cancel";
            }

            public CancelButton(SXRContext context, JSONObject properties) {
                super(context, properties);
            }

            public String getkey() {
                return key;
            }
        }
    }

    @Override
    public String getName() {
        return TAG;
    }

    @Override
    public void onProximityChange(boolean onProximity) {
        if (onProximity) {
        }
    }

    private static final String TAG = tag(NotificationsContentScene.class);

}
