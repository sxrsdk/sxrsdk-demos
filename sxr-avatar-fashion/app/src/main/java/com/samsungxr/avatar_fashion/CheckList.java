package com.samsungxr.avatar_fashion;

import android.graphics.Color;

import com.samsungxr.SXRContext;
import com.samsungxr.widgetlib.main.WidgetLib;
import com.samsungxr.widgetlib.widget.Widget;
import com.samsungxr.widgetlib.widget.basic.Checkable;
import com.samsungxr.widgetlib.widget.basic.RadioButton;
import com.samsungxr.widgetlib.widget.basic.TextWidget;
import com.samsungxr.widgetlib.widget.compound.CheckableGroup;
import com.samsungxr.widgetlib.widget.layout.Layout;
import com.samsungxr.widgetlib.widget.layout.OrientedLayout;
import com.samsungxr.widgetlib.widget.layout.basic.LinearLayout;
import org.json.JSONObject;

import java.util.List;

import static com.samsungxr.utility.Log.tag;
import static com.samsungxr.widgetlib.widget.properties.JSONHelpers.optFloat;
import static com.samsungxr.widgetlib.widget.properties.JSONHelpers.optJSONObject;

public class CheckList extends Widget {
    private final CheckableGroup mItemsGroup;

    private static final int BG_COLOR = Color.LTGRAY;
    private static final float LIST_PADDING = 0.3f;
    private static final String TAG = tag(CheckList.class);

    interface Action {
        void enable();
        void disable();
    }

    enum Properties {label}

    public static  class Item extends RadioButton {
        private Action action;

        enum Properties {padding}

        public Item(SXRContext context, String label, JSONObject properties, Action act) {
            super(context, properties);

            setText(label);
            action = act;
            float padding = optFloat(properties, Properties.padding, LIST_PADDING);
            getDefaultLayout().setDividerPadding(padding, Layout.Axis.X);
            getDefaultLayout().setOffset(-0.1f, Layout.Axis.Z);

            setTexture(WidgetLib.getTextureHelper().getSolidColorTexture(BG_COLOR));
        }

        public void toggle() {
            this.setChecked(!this.isChecked());
        }

        public Action getAction() {
            return action;
        }
    }

    public CheckList(SXRContext context, String listLabel, List<Item> items) {
        super(context, 0, 0);
        JSONObject properties = WidgetLib.getPropertyManager().getInstanceProperties(getClass(), TAG);
        JSONObject labelProperties = optJSONObject(properties, Properties.label);
        float padding = optFloat(properties, Item.Properties.padding, LIST_PADDING);

        mItemsGroup = new CheckableGroup(context, 0, 0);
        mItemsGroup.setAllowMultiCheck(true);
        mItemsGroup.getDefaultLayout().setDividerPadding(padding, Layout.Axis.Y);

        for (Item item: items) {
            mItemsGroup.addChild(item);
        }
        mItemsGroup.check(items.get(0));

        OrientedLayout mainScreenLayout = new LinearLayout();
        mainScreenLayout.setOrientation(OrientedLayout.Orientation.VERTICAL);
        mainScreenLayout.setDividerPadding(padding, Layout.Axis.Y);

        applyLayout(mainScreenLayout);

        if (listLabel != null) {
            TextWidget mLabel = new TextWidget(context, labelProperties);
            mLabel.setText(listLabel);
            addChild(mLabel);
        }
        addChild(mItemsGroup);
    }


    public synchronized void disable() {
        mItemsGroup.removeOnCheckChangedListener(mInternalOnFilterChangedListener);
    }

    public synchronized void enable() {
        mItemsGroup.addOnCheckChangedListener(mInternalOnFilterChangedListener);
        mItemsGroup.check(0);
    }

    public synchronized void checkAll() {
        for (int i = 0; i < mItemsGroup.getCheckableCount(); i++) {
            mItemsGroup.check(i);
        }
    }

    private CheckableGroup.OnCheckChangedListener mInternalOnFilterChangedListener =
            new CheckableGroup.OnCheckChangedListener() {
                @Override
                public <T extends Widget & Checkable>
                void onCheckChanged(CheckableGroup group, T checkedWidget, int checkableIndex) {
                    boolean checked = checkedWidget.isChecked();

                    Action action = ((Item)checkedWidget).getAction();
                    if (checked) {
                        action.enable();
                    } else {
                        action.disable();
                    }
                }
            };
}
