package com.samsungxr.arpet;

import android.opengl.GLES30;
import android.util.Log;

import com.samsungxr.SXRMaterial;
import com.samsungxr.SXRMesh;
import com.samsungxr.SXRNode;
import com.samsungxr.SXRRenderData;
import com.samsungxr.SXRShaderId;
import com.samsungxr.arpet.shaders.SXRPointCloudShader;
import com.samsungxr.mixedreality.IMixedReality;
import com.samsungxr.mixedreality.IMixedRealityEvents;
import com.samsungxr.mixedreality.SXRPointCloud;
import com.samsungxr.nodes.SXRCubeNode;

public class PointCloudHandler implements IMixedRealityEvents {
    IMixedReality mMixedReality;
    SXRPointCloud mOldPointCloud;
    SXRNode mPointCloudNode;
    PetContext mPetContext;

    public PointCloudHandler(PetContext petContext) {
        mPetContext = petContext;

        SXRMaterial mat = new SXRMaterial(petContext.getSXRContext(),
                new SXRShaderId(SXRPointCloudShader.class));
        mat.setVec3("u_color", 0.94f,0.61f,1f);

        SXRRenderData renderData = new SXRRenderData(petContext.getSXRContext());
        renderData.setDrawMode(GLES30.GL_POINTS);
        renderData.setMaterial(mat);

        mPointCloudNode = new SXRNode(petContext.getSXRContext());
        mPointCloudNode.attachComponent(renderData);
    }

    public void addOnScene() {
        mPetContext.getMainScene().addNode(mPointCloudNode);
    }

    public void removeFromScene() {
        mPetContext.getMainScene().removeNode(mPointCloudNode);
    }

    @Override
    public void onMixedRealityStart(IMixedReality mixedReality) {
        mMixedReality = mixedReality;
        addOnScene();
    }

    @Override
    public void onMixedRealityStop(IMixedReality iMixedReality) {

    }

    @Override
    public void onMixedRealityUpdate(IMixedReality iMixedReality) {
        SXRPointCloud newPointCloud = mMixedReality.acquirePointCloud();
        if (mOldPointCloud != newPointCloud) {
            SXRMesh mesh = new SXRMesh(mPetContext.getSXRContext());
            mesh.setVertices(newPointCloud.getPoints());
            mPointCloudNode.getRenderData().setMesh(mesh);

            mOldPointCloud = newPointCloud;
            newPointCloud.release();
        }
    }
}
