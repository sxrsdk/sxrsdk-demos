package com.samsungxr.blurfilter;

import com.samsungxr.SXRContext;
import com.samsungxr.SXRShader;
import com.samsungxr.SXRShaderData;
import com.samsungxr.SXRShaderTemplate;
import com.samsungxr.utility.TextFile;

public class VertBlurShader extends SXRShader
{
    private static String fragTemplate;
    private static String vtxTemplate;

    public VertBlurShader(SXRContext context)
    {
        super("float u_resolution", "sampler2D u_texture", "float3 a_position float2 a_texcoord", GLSLESVersion.VULKAN);
        fragTemplate = TextFile.readTextFile(context.getContext(), R.raw.gaussianblurvert);
        vtxTemplate = TextFile.readTextFile(context.getContext(), R.raw.pos_tex);

        setSegment("VertexTemplate", vtxTemplate);
        setSegment("FragmentTemplate", fragTemplate);
    }

    protected void setMaterialDefaults(SXRShaderData material)
    {
        material.setFloat("u_resolution", 1);
    }
}