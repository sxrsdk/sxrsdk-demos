package com.samsungxr.arpet.animations;

import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRHybridObject;
import com.samsungxr.SXRMaterial;
import com.samsungxr.SXRMesh;
import com.samsungxr.SXRNode;
import com.samsungxr.SXRRenderData;
import com.samsungxr.SXRShaderId;
import com.samsungxr.SXRTexture;
import com.samsungxr.animation.SXRAnimation;
import com.samsungxr.arpet.R;
import com.samsungxr.arpet.shaders.SXRDustyShader;

public class DustyAnimation extends SXRAnimation {
    private final SXRContext mContext;
    private final SXRNode mDustyObject;
    private final SXRMaterial mDustyMaterial;
    public DustyAnimation(SXRContext context, float duration) {
        super(null, duration);
        mContext = context;
        mDustyObject = createDustyObject(context);
        mDustyMaterial = mDustyObject.getRenderData().getMaterial();
    }

    public void setDustySize(float size) {
        mDustyObject.getTransform().setScale(size, size, 1);
    }

    public void setDustyPosition(float x, float y, float z) {
        mDustyObject.getTransform().setPosition(x, y, z);
    }

    @Override
    public void onStart() {
        super.onStart();

        if (mDustyObject.getParent() == null) {
            mContext.getMainScene().addNode(mDustyObject);
        }
    }

    @Override
    protected void onFinish() {
        super.onFinish();

        if (mDustyObject.getParent() != null) {
            mContext.getMainScene().removeNode(mDustyObject);
        }
    }

    public void animate(float timeInSec) {
        mDustyMaterial.setFloat("u_ratio", 1f - (timeInSec/getDuration()));
        mDustyMaterial.setFloat("u_opacity", 1f - (timeInSec/getDuration()));
    }

    private static SXRNode createDustyObject(SXRContext context) {
        final SXRTexture tex = context.getAssetLoader().loadTexture(
                new SXRAndroidResource(context, R.drawable.smoke));
        final SXRMesh mesh = SXRMesh.createQuad(context,
                "float3 a_position float2 a_texcoord", 1.0f, 1.0f);
        final SXRMaterial material = new SXRMaterial(context, new SXRShaderId(SXRDustyShader.class));
        final SXRNode dustyObj = new SXRNode(context, mesh, material);
        final SXRRenderData renderData = dustyObj.getRenderData();

        material.setMainTexture(tex);
        renderData.setAlphaBlend(true);
        dustyObj.getTransform().setRotationByAxis(-90f, 1f, 0f, 0f);

        return dustyObj;
    }
}
