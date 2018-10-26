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
import com.samsungxr.SXRNode;
import com.samsungxr.SXRShaderId;
import com.samsungxr.SXRTexture;
import com.samsungxr.nodes.SXRVideoNode;
import com.samsungxr.video.shaders.RadiosityShader;

import java.io.IOException;

public class MultiplexMovieTheater extends MovieTheater {

    SXRNode background = null;
    SXRNode screen = null;

    private boolean mIsImax = false;
    private float mTransitionWeight = 0.0f;
    private float mTransitionTarget = 0.0f;
    private float mFadeWeight = 0.0f;
    private float mFadeTarget = 1.0f;

    public MultiplexMovieTheater(SXRContext context, MediaPlayer player,
                                 SXRExternalTexture screenTexture) {
        super(context);
        try {
            // background
            SXRMesh backgroundMesh = context.getAssetLoader().loadMesh(
                    new SXRAndroidResource(context, "multiplex/theater_background.obj"));
            SXRTexture backgroundLightOffTexture = context.getAssetLoader().loadTexture(
                    new SXRAndroidResource(context, "multiplex/theater_background_light_off.jpg"));
            SXRTexture backgroundLightOnTexture = context.getAssetLoader().loadTexture(
                    new SXRAndroidResource(context, "multiplex/theater_background_light_on.jpg"));
            background = new SXRNode(context, backgroundMesh, backgroundLightOffTexture);
            background.setName("background");
            background.getRenderData().setCullFace(SXRRenderPass.SXRCullFaceEnum.None);
            // radiosity
            RadiosityShader radiosityShader = new RadiosityShader(context);
           // background.getRenderData().getMaterial().set(radiosityShader.getShaderId());
            background.getRenderData().setMaterial(new SXRMaterial(context, new SXRShaderId(RadiosityShader.class)));
            background.getRenderData().getMaterial().setTexture(
                    RadiosityShader.TEXTURE_OFF_KEY, backgroundLightOffTexture);
            background.getRenderData().getMaterial().setTexture(
                    RadiosityShader.TEXTURE_ON_KEY, backgroundLightOnTexture);
            background.getRenderData().getMaterial().setTexture(
                    RadiosityShader.SCREEN_KEY, screenTexture);
            // screen
            SXRMesh screenMesh = context.getAssetLoader().loadMesh(new SXRAndroidResource(
                    context, "multiplex/screen.obj"));
            screen = new SXRVideoNode(context, screenMesh, player,
                    screenTexture, SXRVideoNode.SXRVideoType.MONO);
            screen.getRenderData().setCullFace(SXRRenderPass.SXRCullFaceEnum.None);
            this.addChildObject(background);
            this.addChildObject(screen);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void hideCinemaTheater() {
        background.getRenderData().setRenderMask(0);
        screen.getRenderData().setRenderMask(0);
    }

    @Override
    public void showCinemaTheater() {
        mFadeWeight = 0.0f;
        background.getRenderData().setRenderMask(SXRRenderData.SXRRenderMaskBit.Left
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
        if (mIsImax) {
            mIsImax = false;
            mTransitionTarget = 0.0f;
        } else {
            mIsImax = true;
            mTransitionTarget = 1.0f;
        }
    }

    @Override
    public void setShaderValues() {
        mTransitionWeight += 0.1f * (mTransitionTarget - mTransitionWeight);
        mFadeWeight += 0.01f * (mFadeTarget - mFadeWeight);
        background.getRenderData().getMaterial()
                .setFloat(RadiosityShader.WEIGHT_KEY, 0.1f);
        background.getRenderData().getMaterial()
                .setFloat(RadiosityShader.FADE_KEY, mFadeWeight);
        background.getRenderData().getMaterial()
                .setFloat(RadiosityShader.LIGHT_KEY, 2.0f);
        float scale = 1.0f + mTransitionWeight;
        if (scale >= 1.0f) {
            background.getTransform().setScale(scale, scale, 1.0f);
            screen.getTransform().setScale(scale, scale, 1.0f);
        }
    }
}
