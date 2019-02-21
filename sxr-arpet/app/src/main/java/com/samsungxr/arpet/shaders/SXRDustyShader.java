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
package com.samsungxr.arpet.shaders;

import android.content.Context;

import com.samsungxr.SXRContext;
import com.samsungxr.SXRShaderData;
import com.samsungxr.SXRShaderTemplate;
import com.samsungxr.arpet.R;
import com.samsungxr.utility.TextFile;

/**
 * Shader which samples from an external texture.
 * This shader does not use light sources.
 * @<code>
 *    a_position    position vertex attribute
 *    a_texcoord    texture coordinate vertex attribute
 *    u_color       color to modulate texture
 *    u_opacity     opacity
 *    u_texture     external texture
 * </code>
 */
public class SXRDustyShader extends SXRShaderTemplate
{
    public SXRDustyShader(SXRContext sxrContext)
    {
        super("float3 u_color; float u_opacity; float u_ratio",
              "sampler2D u_texture",
              "float3 a_position; float2 a_texcoord", GLSLESVersion.VULKAN);
        Context context = sxrContext.getContext();
        setSegment("FragmentTemplate", TextFile.readTextFile(context, R.raw.tex_dusty_frag));
        setSegment("VertexTemplate", TextFile.readTextFile(context, R.raw.tex_dusty_vert));
    }

    protected void setMaterialDefaults(SXRShaderData material)
    {
        material.setVec3("u_color", 1, 1, 1);
        material.setFloat("u_opacity", 1f);
        material.setFloat("u_ratio", 1f);
    }
}
