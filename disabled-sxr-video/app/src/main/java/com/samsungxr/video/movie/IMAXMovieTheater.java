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

package com.samsungxr.video.movie;

import android.media.MediaPlayer;

import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRExternalTexture;
import com.samsungxr.SXRMaterial;
import com.samsungxr.SXRMesh;
import com.samsungxr.SXRRenderData;
import com.samsungxr.SXRRenderPass;
import com.samsungxr.SXRSceneObject;
import com.samsungxr.SXRShaderId;
import com.samsungxr.SXRTexture;
import com.samsungxr.scene_objects.SXRVideoSceneObject;
import com.samsungxr.video.shaders.AdditiveShader;
import com.samsungxr.video.shaders.RadiosityShader;

import java.io.IOException;

public class IMAXMovieTheater extends MovieTheater {

    SXRSceneObject background = null;
    SXRSceneObject additive = null;
    SXRSceneObject screen = null;

    private float mFadeWeight = 0.0f;
    private float mFadeTarget = 1.0f;

    public IMAXMovieTheater(SXRContext context, MediaPlayer player,
                            SXRExternalTexture screenTexture) {
        super(context);
        try {
            // background
            SXRMesh backgroundMesh = context.getAssetLoader().loadMesh(
                    new SXRAndroidResource(context, "imax/cinema.obj"));
            SXRTexture backgroundLightOffTexture = context.getAssetLoader().loadTexture(
                    new SXRAndroidResource(context, "imax/cinema_light_off.png"));
            SXRTexture backgroundLightOnTexture = context.getAssetLoader().loadTexture(
                    new SXRAndroidResource(context, "imax/cinema_light_on.png"));
            SXRMesh backgroundRadiosity = context.getAssetLoader().loadMesh(new SXRAndroidResource(context, "imax/radiosity1.obj"));
            backgroundMesh.setNormals(backgroundRadiosity.getVertices());
            background = new SXRSceneObject(context, backgroundMesh, backgroundLightOffTexture);
            background.getRenderData().setCullFace(SXRRenderPass.SXRCullFaceEnum.None);
            SXRMesh additiveMesh = context.getAssetLoader().loadMesh(
                    new SXRAndroidResource(context, "imax/additive.obj"));
            SXRTexture additiveTexture = context.getAssetLoader().loadTexture(
                    new SXRAndroidResource(context, "imax/additive.png"));
            SXRMesh additiveRadiosity = context.getAssetLoader().loadMesh(new SXRAndroidResource(context, "imax/radiosity2.obj"));
            additiveMesh.setNormals(additiveRadiosity.getVertices());
            additive = new SXRSceneObject(context, additiveMesh, additiveTexture);
            additive.getRenderData().setCullFace(SXRRenderPass.SXRCullFaceEnum.None);
            additive.getRenderData().setRenderingOrder(2500);

            // radiosity
            RadiosityShader radiosityShader = new RadiosityShader(context);

            background.getRenderData().setMaterial(new SXRMaterial(context, new SXRShaderId(RadiosityShader.class)));
            background.getRenderData().getMaterial().setTexture(
                    RadiosityShader.TEXTURE_OFF_KEY, backgroundLightOffTexture);
            background.getRenderData().getMaterial().setTexture(
                    RadiosityShader.TEXTURE_ON_KEY, backgroundLightOnTexture);
            background.getRenderData().getMaterial().setTexture(
                    RadiosityShader.SCREEN_KEY, screenTexture);
            AdditiveShader additiveShader = new AdditiveShader(context);
            additive.getRenderData().setMaterial(new SXRMaterial(context, new SXRShaderId(AdditiveShader.class)));
            additive.getRenderData().getMaterial().setTexture(AdditiveShader.TEXTURE_KEY, additiveTexture);
            // screen
            SXRMesh screenMesh = context.getAssetLoader().loadMesh(new SXRAndroidResource(
                    context, "imax/screen.obj"));
            screen = new SXRVideoSceneObject(context, screenMesh, player,
                    screenTexture, SXRVideoSceneObject.SXRVideoType.MONO);
            screen.getRenderData().setCullFace(SXRRenderPass.SXRCullFaceEnum.None);
            this.addChildObject(background);
            this.addChildObject(additive);
            this.addChildObject(screen);
            this.getTransform().setPosition(3.353f, -0.401f, 0.000003f);
            this.getTransform().rotateByAxisWithPivot(90.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void hideCinemaTheater() {
        background.getRenderData().setRenderMask(0);
        additive.getRenderData().setRenderMask(0);
        screen.getRenderData().setRenderMask(0);
    }

    @Override
    public void showCinemaTheater() {
        mFadeWeight = 0.0f;
        background.getRenderData().setRenderMask(SXRRenderData.SXRRenderMaskBit.Left
                | SXRRenderData.SXRRenderMaskBit.Right);
        additive.getRenderData().setRenderMask(SXRRenderData.SXRRenderMaskBit.Left
                | SXRRenderData.SXRRenderMaskBit.Right);
        screen.getRenderData().setRenderMask(SXRRenderData.SXRRenderMaskBit.Left
                | SXRRenderData.SXRRenderMaskBit.Right);
    }

    @Override
    public void switchOnLights() {
        background.getRenderData().getMaterial().setMainTexture(
                background.getRenderData().getMaterial().getTexture(RadiosityShader.TEXTURE_ON_KEY));
    }

    @Override
    public void switchOffLights() {
        background.getRenderData().getMaterial().setMainTexture(
                background.getRenderData().getMaterial().getTexture(RadiosityShader.TEXTURE_OFF_KEY));
    }

    @Override
    public void switchToImax() {
        // Nothing to do as Imax mode is not available
    }

    @Override
    public void setShaderValues() {
        mFadeWeight += 0.01f * (mFadeTarget - mFadeWeight);
        background.getRenderData().getMaterial()
                .setFloat(RadiosityShader.WEIGHT_KEY, 0.1f);
        background.getRenderData().getMaterial()
                .setFloat(RadiosityShader.FADE_KEY, mFadeWeight);
        background.getRenderData().getMaterial()
                .setFloat(RadiosityShader.LIGHT_KEY, 1.0f);
        additive.getRenderData().getMaterial()
                .setFloat(AdditiveShader.WEIGHT_KEY, 0.1f);
        additive.getRenderData().getMaterial()
                .setFloat(AdditiveShader.FADE_KEY, mFadeWeight);
    }
}
