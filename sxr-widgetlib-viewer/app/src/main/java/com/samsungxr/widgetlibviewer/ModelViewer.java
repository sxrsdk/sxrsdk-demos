package com.samsungxr.widgetlibviewer;

import com.samsungxr.SXRContext;
import com.samsungxr.widgetlib.log.Log;
import com.samsungxr.widgetlib.main.WidgetLib;
import com.samsungxr.widgetlib.widget.GroupWidget;
import com.samsungxr.widgetlib.widget.Widget;
import com.samsungxr.widgetlib.widget.layout.Layout;
import com.samsungxr.widgetlib.widget.layout.OrientedLayout;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.samsungxr.utility.Log.tag;
import static com.samsungxr.widgetlib.widget.properties.JSONHelpers.optFloat;
import static com.samsungxr.widgetlib.widget.properties.JSONHelpers.optJSONObject;

public class ModelViewer extends BaseContentScene {
    enum SHADER {
        Original,
        NoTexture,
        Lines,
        LinesLoop,
        Points
    };

    ModelViewer(SXRContext gvrContext, Widget.OnTouchListener homeListener) {
        super(gvrContext);

        JSONObject properties = WidgetLib.getPropertyManager().getInstanceProperties(getClass(), TAG);
        final float zoom_step = optFloat(properties, Properties.zoom_step, ZOOM_STEP);
        mPadding = optFloat(properties, Properties.padding, PADDING);

        Widget.OnTouchListener zoomListener = new Widget.OnTouchListener() {
            @Override
            public boolean onTouch(Widget widget, float[] floats) {
                if (mModel != null) {
                    mModel.onZoomOverModel(mModel.getCurrentZoom() + zoom_step);
                }
                return true;
            }
        };

        mControlBar.addControlListener("Home", homeListener);
        mControlBar.addControlListener("Zoom", zoomListener);

        JSONObject listProperties = WidgetLib.getPropertyManager().getInstanceProperties(CheckList.class, TAG);
        final JSONObject labelProperties = optJSONObject(listProperties, CheckList.Properties.label);

        List<CheckList.Item> shaders = new ArrayList<>();
        shaders.add(new CheckList.Item(gvrContext, "Original", labelProperties, new ShaderAction(SHADER.Original)));
        shaders.add(new CheckList.Item(gvrContext, "No Texture", labelProperties, new ShaderAction(SHADER.NoTexture)));
        shaders.add(new CheckList.Item(gvrContext, "Lines", labelProperties, new ShaderAction(SHADER.Lines)));
        shaders.add(new CheckList.Item(gvrContext, "Lines_Loop", labelProperties, new ShaderAction(SHADER.LinesLoop)));
        shaders.add(new CheckList.Item(gvrContext, "Points", labelProperties, new ShaderAction(SHADER.Points)));
        mShaderList = new CheckList(gvrContext, "ShaderList", shaders);
    }

    private final static SHADER DEFAULT_SHADER_ID = SHADER.Original;
    private class ShaderAction implements CheckList.Action {
        private final SHADER mShaderId;
        ShaderAction(SHADER shaderId) {
            mShaderId = shaderId;
        }

        @Override
        public void enable() {
            if (mModel != null) {
                Log.d(TAG, "enable ShaderAction for shader [%s]", mShaderId);

                mModel.applyCustomShader(DEFAULT_SHADER_ID);
                mModel.applyCustomShader(mShaderId);
            }
        }

        @Override
        public void disable() {
            if (mModel != null) {
                mModel.applyCustomShader(DEFAULT_SHADER_ID);
            }
        }
    };

    private enum Properties {zoom_step, padding}

    void setModel(Model model) {
        mModel = model;
        mFirstShow = true;
    }

    class ModelBox extends GroupWidget {
        ModelBox(SXRContext gvrContext, Model model) {
            super(gvrContext);
            OrientedLayout layout = new com.samsungxr.widgetlib.widget.layout.basic.LinearLayout();
            getDefaultLayout().setDividerPadding(mPadding, Layout.Axis.X);
            applyLayout(layout);

            addChild(mShaderList);
            addChild(model);
        }
    }

    @Override
    protected Widget createContent() {
        return new ModelBox(mGvrContext, mModel);
    }

    @Override
    public String getName() {
        return TAG;
    }

    @Override
    public void show() {
        if (mModel != null) {
            super.show();
            mModel.enableDisableLight(true);
            mShaderList.enable();
        }
    }

    @Override
    public void hide() {
        if (mModel != null) {
            mModel.enableDisableLight(false);
            mShaderList.disable();
            super.hide();
        }
    }

    @Override
    public void onProximityChange(boolean onProximity) {
        if (onProximity) {
        }
    }

    private Model mModel;
    private static final float ZOOM_STEP = 1;
    private static final float PADDING = 3;
    private final float mPadding;
    private CheckList mShaderList;

    private static final String TAG = tag(ModelViewer.class);
}
