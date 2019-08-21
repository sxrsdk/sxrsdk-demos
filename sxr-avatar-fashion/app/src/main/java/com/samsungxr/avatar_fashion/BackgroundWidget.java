package com.samsungxr.avatar_fashion;

import com.samsungxr.SXRContext;
import com.samsungxr.SXRRenderData;
import com.samsungxr.SXRRenderPass;
import com.samsungxr.widgetlib.log.Log;
import com.samsungxr.widgetlib.widget.Widget;
import com.samsungxr.widgetlib.widget.properties.JSONHelpers;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.samsungxr.utility.Log.tag;
import static com.samsungxr.widgetlib.widget.properties.JSONHelpers.getString;
class BackgroundWidget extends Widget {
    private final static String TAG = tag(BackgroundWidget.class);

    private enum Properties_level_ext {
        thumbnail
    }

    private List<String> mThumbnailsList = new ArrayList<>();

    BackgroundWidget(final SXRContext sxrContext) {
        super(sxrContext);
        setRenderingOrder(SXRRenderData.SXRRenderingOrder.BACKGROUND - 1);
        setCullFace(SXRRenderPass.SXRCullFaceEnum.None);
        JSONObject metadata = getObjectMetadata();
        JSONArray levels = JSONHelpers.optJSONArray(metadata, Properties.levels);
        for (int index = 0; index < levels.length(); ++index) {
            try {
                String resIdStr = getString(levels.getJSONObject(index),
                        BackgroundWidget.Properties_level_ext.thumbnail);
                mThumbnailsList.add(resIdStr);
            } catch (Exception e) {
                Log.e(TAG, e, "Could not create background at %d", index);
            }
        }

        ArrayList<SXRRenderData> rdata = getNode().
                getAllComponents(SXRRenderData.getComponentType());
        for (SXRRenderData r : rdata) {
            r.disableLight();
        }
        setTouchable(false);
        setFocusEnabled(false);
    }

    List<String> getThumbnailsList() {
        return mThumbnailsList;
    }
}
