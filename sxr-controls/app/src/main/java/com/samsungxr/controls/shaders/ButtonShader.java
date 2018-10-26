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

package com.samsungxr.controls.shaders;

import android.content.Context;

import com.samsungxr.SXRContext;
import com.samsungxr.SXRShader;
import com.samsungxr.SXRShaderData;
import com.samsungxr.controls.R;
import com.samsungxr.utility.TextFile;

public class ButtonShader extends SXRShader{

    public static final String STATE1_BACKGROUND_TEXTURE = "state1Background";
    public static final String STATE1_TEXT_TEXTURE = "state1Text";
    public static final String STATE2_BACKGROUND_TEXTURE = "state2Background";
    public static final String STATE2_TEXT_TEXTURE = "state2Text";
    public static final String STATE3_BACKGROUND_TEXTURE = "state3Background";
    public static final String STATE3_TEXT_TEXTURE = "state3Text";
    public static final String TEXTURE_SWITCH = "textureSwitch";


    public ButtonShader(SXRContext sxrContext) {
        super("float textureSwitch float u_opacity",
              "sampler2D state1Text sampler2D state2Text sampler2D state3Text sampler2D state1Background sampler2D state2Background  sampler2D state3Background",
              "float3 a_position, float3 a_normal, float2 a_texcoord", GLSLESVersion.VULKAN);
        Context context = sxrContext.getContext();
        setSegment("FragmentTemplate", TextFile.readTextFile(context, R.raw.buttonshader_fragment));
        setSegment("VertexTemplate", TextFile.readTextFile(context,R.raw.buttonshader_vertex));
    }

    protected void setMaterialDefaults(SXRShaderData material) {
        material.setFloat(TEXTURE_SWITCH, 0.0f);
        material.setFloat("u_opacity", 1.0f);
    }
}