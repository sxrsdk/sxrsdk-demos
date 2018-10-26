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


package com.samsungxr.util;

import com.samsungxr.SXRContext;
import com.samsungxr.SXRShader;
import com.samsungxr.SXRShaderData;

public class OutlineShader extends SXRShader {
    public static final String COLOR_KEY = "u_color";
    public static final String THICKNESS_KEY = "u_thickness";
    private static final String VERTEX_SHADER =
            "precision mediump float;\n"
                    +            "layout(location = 0) in  vec3 a_position;\n"
                    + "layout(location = 1) in vec3 a_normal;\n"
                    + "@MATRIX_UNIFORMS\n"
                    +  "@MATERIAL_UNIFORMS\n"
                    + "void main() {\n"
                    + "  vec4 pos = vec4(a_position.xyz + a_normal * u_thickness, 1.0);\n"
                    + "  gl_Position = u_mvp * pos;\n"
                    + "}\n";

    private static final String FRAGMENT_SHADER =
            "precision mediump float;\n"
                    + "out vec4 outColor;\n"
                    +  "@MATERIAL_UNIFORMS\n"
                    + "void main() {\n"
                    + "  outColor = u_color;\n"
                    + "}\n";

    public OutlineShader(SXRContext sxrcontext) {
        super("float4 u_color; float u_thickness", "", "float3 a_position float3 a_normal", GLSLESVersion.VULKAN);
        setSegment("FragmentTemplate", FRAGMENT_SHADER);
        setSegment("VertexTemplate", VERTEX_SHADER);
    }


    protected void setMaterialDefaults(SXRShaderData material) {
        material.setVec4("u_color", 1, 1, 1, 1);
        material.setFloat("u_thickness", 1);
    }
}

