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

package com.samsungxr.vuforiasample;


import com.samsungxr.SXRContext;
import com.samsungxr.SXRShader;
import com.samsungxr.SXRShaderData;


public class ModelShader extends SXRShader{

    public static final String TEXTURE_KEY = "texSampler2D";
    public static final String MVP_KEY = "modelViewProjectionMatrix";

    private static final String VERTEX_SHADER = "" //
            + "in vec3 a_position;\n"
            + "in vec4 a_normal;\n"
            + "in vec2 a_texcoord;\n"
            + "out vec2 v_tex_coord;\n"
            + "@MATERIAL_UNIFORMS\n"
            + "void main() {\n"
            + "  v_tex_coord = a_texcoord.xy;\n"
            + "  gl_Position = modelViewProjectionMatrix * vec4(a_position,1.0);\n"
            + "}\n";

    private static final String FRAGMENT_SHADER = "" //
            + "precision highp float;\n"
            + "in vec2 v_tex_coord; \n"
            + "uniform sampler2D texSampler2D;\n"
            + "out vec4 outColor; \n"
            + "void main() {\n"
            + "  outColor = texture(texSampler2D, v_tex_coord);\n"
            + "}\n";

    public ModelShader(SXRContext sxrContext) {
        super("mat4 modelViewProjectionMatrix", "sampler2D texSampler2D ", "float3 a_position, float4 a_normal, float2 a_tex_coord", GLSLESVersion.VULKAN);

        setSegment("FragmentTemplate", FRAGMENT_SHADER);
        setSegment("VertexTemplate", VERTEX_SHADER);

    }

    protected void setMaterialDefaults(SXRShaderData material)
    {
        material.setMat4("modelViewProjectionMatrix", 1,1,1,1, 1,1,1,1, 1,1,1,1, 1,1,1,1);
    }
}