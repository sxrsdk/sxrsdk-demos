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

import com.samsungxr.SXRContext;
import com.samsungxr.SXRMain;
import com.samsungxr.SXRMaterial;
import com.samsungxr.SXRMesh;
import com.samsungxr.SXRRenderData;
import com.samsungxr.SXRScene;
import com.samsungxr.SXRSceneObject;
import com.samsungxr.scene_objects.SXRSphereSceneObject;

import android.opengl.GLES20;

public class SampleScript extends SXRMain {

    private SXRContext mSXRContext = null;

    @Override
    public void onInit(SXRContext sxrContext) {
        SXRScene scene = sxrContext.getMainScene();
        scene.getMainCameraRig().getLeftCamera().setBackgroundColor(1.0f, 1.0f, 0, 1.0f);
        scene.getMainCameraRig().getRightCamera().setBackgroundColor(1.0f, 1.0f, 0, 1.0f);
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
        
        SXRSceneObject Lobj = new SXRSceneObject(sxrContext);
        mesh.setVertices(L);
    	rd.setMesh(mesh);
    	rd.setDrawMode(GLES20.GL_LINE_STRIP);
    	rd.setMaterial(redMaterial);
    	Lobj.attachRenderData(rd);
    	scene.addSceneObject(Lobj);

        SXRSceneObject Iobj = new SXRSceneObject(sxrContext);
    	mesh = new SXRMesh(sxrContext);
    	rd = new SXRRenderData(sxrContext);
    	mesh.setVertices(I);
        rd.setMesh(mesh);
        rd.setDrawMode(GLES20.GL_LINES);
        rd.setMaterial(redMaterial);
        Iobj.attachRenderData(rd);
        scene.addSceneObject(Iobj);

        SXRSceneObject Nobj = new SXRSceneObject(sxrContext);
        mesh = new SXRMesh(sxrContext);
        mesh.setVertices(N);
        rd = new SXRRenderData(sxrContext);
        rd.setMesh(mesh);
        rd.setDrawMode(GLES20.GL_LINE_STRIP);
        rd.setMaterial(redMaterial);
        Nobj.attachRenderData(rd);
        scene.addSceneObject(Nobj);
        
        SXRSceneObject Eobj = new SXRSceneObject(sxrContext);
        mesh = new SXRMesh(sxrContext);
        mesh.setVertices(E);
        rd = new SXRRenderData(sxrContext);
        rd.setMesh(mesh);
        rd.setDrawMode(GLES20.GL_LINES);
        rd.setMaterial(blueMaterial);
        rd.setAlphaBlend(true);
        Eobj.attachRenderData(rd);
        scene.addSceneObject(Eobj);
        
        SXRSceneObject sphere = new SXRSphereSceneObject(sxrContext);
        rd = sphere.getRenderData();
        rd.setAlphaBlend(true);
        rd.setMaterial(blueMaterial);
        sphere.getTransform().setPositionZ(Z);
        scene.addSceneObject(sphere);
     }

    @Override
    public void onStep() {
    }

}
