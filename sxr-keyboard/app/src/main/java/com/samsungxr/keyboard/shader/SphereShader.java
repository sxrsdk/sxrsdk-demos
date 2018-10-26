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

package com.samsungxr.keyboard.shader;

import android.content.Context;

import com.samsungxr.SXRContext;
import com.samsungxr.SXRShader;
import com.samsungxr.SXRShaderData;
import com.samsungxr.SXRShaderTemplate;
import com.samsungxr.keyboard.R;
import com.samsungxr.utility.TextFile;

public class SphereShader extends SXRShader{

    public static final String LIGHT_KEY = "u_light";
    public static final String EYE_KEY = "u_eye";
    public static final String TRANSITION_COLOR = "trans_color";
    public static final String TEXTURE_KEY = "texture_t";
    public static final String SECUNDARY_TEXTURE_KEY = "second_texture";
    public static final String ANIM_TEXTURE = "animTexture";
    public static final String BLUR_INTENSITY = "blur";
    public static final String HDRI_TEXTURE_KEY = "hdri_texture";

    public SphereShader(SXRContext sxrContext) {
        super("float3 u_eye, float3 u_light, float3 trans_color, float animTexture, float blur, float u_radius", "sampler2D texture_t sampler2D second_texture sampler2D HDRI_texture",
                "float4 a_position, float2 a_texcoord float3 a_normal", SXRShader.GLSLESVersion.VULKAN);
        Context context = sxrContext.getContext();
        setSegment("FragmentTemplate", TextFile.readTextFile(context, R.raw.sphereshader_fragment));
        setSegment("VertexTemplate", TextFile.readTextFile(context, R.raw.sphereshader_vertex));

    }

    protected void setMaterialDefaults(SXRShaderData material)
    {
        material.setVec3("u_eye", 0, 0, 0);
        material.setVec3("u_light", 1, 1, 1);
        material.setVec3("trans_color", 1, 1, 1);
        material.setFloat("blur", 1);
        material.setFloat("animTexture", 1);
        material.setFloat("u_radius", 1);
    }

}
