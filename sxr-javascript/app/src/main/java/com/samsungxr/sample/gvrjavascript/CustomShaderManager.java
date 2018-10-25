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

package com.samsungxr.sample.gvrjavascript;

import android.content.Context;

import com.samsungxr.SXRContext;
import com.samsungxr.SXRShader;
import com.samsungxr.SXRShaderData;
import com.samsungxr.SXRShaderTemplate;
import com.samsungxr.utility.TextFile;

public class CustomShaderManager extends SXRShaderTemplate{
    static final String COLOR_KEY = "u_color";

    public CustomShaderManager(SXRContext gvrContext) {
        super("float4 u_color", "", "float4 a_position", GLSLESVersion.VULKAN);
        Context context = gvrContext.getContext();
        setSegment("FragmentTemplate", TextFile.readTextFile(context, R.raw.fragment));
        setSegment("VertexTemplate", TextFile.readTextFile(context,R.raw.vertex));
    }

    protected void setMaterialDefaults(SXRShaderData material)
    {
        material.setVec4("u_color", 1, 1, 1, 1);
    }
}