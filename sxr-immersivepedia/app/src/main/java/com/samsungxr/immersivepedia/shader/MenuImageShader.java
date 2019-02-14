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

package com.samsungxr.immersivepedia.shader;

import android.content.Context;

import com.samsungxr.SXRContext;
import com.samsungxr.SXRShaderData;
import com.samsungxr.SXRShaderTemplate;
import com.samsungxr.immersivepedia.R;
import com.samsungxr.utility.TextFile;

public class MenuImageShader extends SXRShaderTemplate {

    public static final String STATE1_TEXTURE = "state1";
    public static final String STATE2_TEXTURE = "state2";
    public static final String TEXTURE_SWITCH = "textureSwitch";

    public MenuImageShader(SXRContext sxrContext) {
        super("float textureSwitch float u_opacity", "sampler2D state1 sampler2D state2 ", "float3 a_position, float2 a_texcoord", GLSLESVersion.VULKAN);

        Context context = sxrContext.getContext();
        setSegment("FragmentTemplate", TextFile.readTextFile(context, R.raw.menu_image_shader_fragment));
        setSegment("VertexTemplate", TextFile.readTextFile(context,R.raw.menu_image_shader_vertex));

    }

    protected void setMaterialDefaults(SXRShaderData material)
    {
        material.setFloat("textureSwitch", 1);
        material.setFloat("u_opacity", 1);
    }
}