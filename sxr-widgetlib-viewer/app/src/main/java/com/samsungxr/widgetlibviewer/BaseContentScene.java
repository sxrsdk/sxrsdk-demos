package com.samsungxr.widgetlibviewer;

import com.samsungxr.SXRContext;
import com.samsungxr.widgetlib.content_scene.ScrollableContentScene;
import com.samsungxr.widgetlib.main.MainScene;
import com.samsungxr.widgetlib.main.WidgetLib;
import com.samsungxr.widgetlib.widget.GroupWidget;
import com.samsungxr.widgetlib.widget.Widget;
import com.samsungxr.widgetlib.widget.custom.ControlBar;
import com.samsungxr.widgetlib.widget.layout.Layout;
import com.samsungxr.widgetlib.widget.layout.OrientedLayout;
import com.samsungxr.widgetlib.widget.layout.basic.LinearLayout;
import org.json.JSONObject;

import static com.samsungxr.utility.Log.tag;
import static com.samsungxr.widgetlib.widget.properties.JSONHelpers.optFloat;
import static com.samsungxr.widgetlib.widget.properties.JSONHelpers.optJSONObject;

public abstract class BaseContentScene extends ScrollableContentScene {
    public BaseContentScene(SXRContext sxrContext) {
        mGvrContext = sxrContext;
        JSONObject properties = WidgetLib.getPropertyManager().getInstanceProperties(getClass(), TAG);
        final float padding = optFloat(properties, Properties.padding, CONTROL_BAR_PADDING);

        mMainWidget = new GroupWidget(sxrContext, 0, 0);
        mMainWidget.setName("MainWidget < " + TAG + " >");

        LinearLayout mainLayout = new LinearLayout();
        mainLayout.setOrientation(OrientedLayout.Orientation.VERTICAL);
        mainLayout.setDividerPadding(padding, Layout.Axis.Y);
        mMainWidget.applyLayout(mainLayout);
        mMainScene = WidgetLib.getMainScene();

        JSONObject controlBarProperties = optJSONObject(properties, Properties.control_bar);
        mControlBar = controlBarProperties != null ?
                new ControlBar(sxrContext, controlBarProperties):
                new ControlBar(sxrContext);
    }

    abstract protected Widget createContent();

    protected void setContentWidget(Widget content) {
        if (mContent != null) {
            mMainWidget.removeChild(mContent);
        }

        mContent = content;
        if (mContent != null) {
            mMainWidget.addChild(mContent, 0);
        }
    }

    protected enum Properties {padding, control_bar}

    @Override
    public String getName() {
        return TAG;
    }

    @Override
    public void show() {
        if (mFirstShow) {
            setContentWidget(createContent());
            mFirstShow = false;
        }
        mMainWidget.addChild(mControlBar);
        mMainScene.addSceneObject(mMainWidget);
    }

    @Override
    public void hide() {
        mMainWidget.removeChild(mControlBar);
        mMainScene.removeSceneObject(mMainWidget);
    }

    @Override
    public void onSystemDialogRemoved() {

    }

    @Override
    public void onSystemDialogPosted() {

    }

    private static float CONTROL_BAR_PADDING = 1.5f;

    protected final SXRContext mGvrContext;
    private Widget mContent;
    private GroupWidget mMainWidget;
    protected ControlBar mControlBar;
    protected MainScene mMainScene;
    protected boolean mFirstShow = true;

    private static final String TAG = tag(BaseContentScene.class);
}

