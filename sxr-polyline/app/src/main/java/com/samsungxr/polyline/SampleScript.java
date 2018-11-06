/* Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.samsungxr.polyline;

import android.opengl.GLES20;

import com.samsungxr.SXRContext;
import com.samsungxr.SXRMain;
import com.samsungxr.SXRMaterial;
import com.samsungxr.SXRMesh;
import com.samsungxr.SXRNode;
import com.samsungxr.SXRRenderData;
import com.samsungxr.SXRScene;
import com.samsungxr.nodes.SXRSphereNode;

public class SampleScript extends SXRMain {

    private SXRContext mSXRContext = null;

    @Override
    public void onInit(SXRContext sxrContext) {
        SXRScene scene = sxrContext.getMainScene();
        scene.setBackgroundColor(1.0f, 1.0f, 0, 1.0f);

        float Z = -4;
        float[] L = { -2.5f, 1, Z, -2.5f, -1, Z, -1.5f, -1, Z };
        float[] I = { -1, 1, Z, -1, -1, Z };
        float[] N = { 0, -1, Z, 0, 1, Z, 1, -1, Z, 1, 1, Z };
        float[] E = { 2.5f, 1, Z, 1.5f, 1, Z, 1.5f, -1, Z,
                      2.5f, -1, Z, 2.5f, 0, Z, 1.5f, 0, Z,
                      1.5f, 1, Z, 1.5f, -1, Z};
    	SXRMaterial redMaterial = new SXRMaterial(sxrContext, SXRMaterial.SXRShaderType.Phong.ID);
        SXRMaterial blueMaterial = new SXRMaterial(sxrContext, SXRMaterial.SXRShaderType.Phong.ID);
        SXRMesh mesh = new SXRMesh(sxrContext);
        SXRRenderData rd = new SXRRenderData(sxrContext);

        redMaterial.setDiffuseColor(1, 0, 0, 1);
        redMaterial.setLineWidth(4.0f);
        blueMaterial.setDiffuseColor(0, 0, 1, 0.5f);
        blueMaterial.setLineWidth(8.0f);
        
        SXRNode Lobj = new SXRNode(sxrContext);
        mesh.setVertices(L);
    	rd.setMesh(mesh);
    	rd.setDrawMode(GLES20.GL_LINE_STRIP);
    	rd.setMaterial(redMaterial);
    	Lobj.attachRenderData(rd);
    	scene.addNode(Lobj);

        SXRNode Iobj = new SXRNode(sxrContext);
    	mesh = new SXRMesh(sxrContext);
    	rd = new SXRRenderData(sxrContext);
    	mesh.setVertices(I);
        rd.setMesh(mesh);
        rd.setDrawMode(GLES20.GL_LINES);
        rd.setMaterial(redMaterial);
        Iobj.attachRenderData(rd);
        scene.addNode(Iobj);

        SXRNode Nobj = new SXRNode(sxrContext);
        mesh = new SXRMesh(sxrContext);
        mesh.setVertices(N);
        rd = new SXRRenderData(sxrContext);
        rd.setMesh(mesh);
        rd.setDrawMode(GLES20.GL_LINE_STRIP);
        rd.setMaterial(redMaterial);
        Nobj.attachRenderData(rd);
        scene.addNode(Nobj);
        
        SXRNode Eobj = new SXRNode(sxrContext);
        mesh = new SXRMesh(sxrContext);
        mesh.setVertices(E);
        rd = new SXRRenderData(sxrContext);
        rd.setMesh(mesh);
        rd.setDrawMode(GLES20.GL_LINES);
        rd.setMaterial(blueMaterial);
        rd.setAlphaBlend(true);
        Eobj.attachRenderData(rd);
        scene.addNode(Eobj);
        
        SXRNode sphere = new SXRSphereNode(sxrContext);
        rd = sphere.getRenderData();
        rd.setAlphaBlend(true);
        rd.setMaterial(blueMaterial);
        sphere.getTransform().setPositionZ(Z);
        scene.addNode(sphere);
     }
}
