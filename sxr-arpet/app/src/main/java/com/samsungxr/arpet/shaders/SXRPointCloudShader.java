package com.samsungxr.arpet.shaders;

import com.samsungxr.SXRContext;
import com.samsungxr.SXRShaderData;
import com.samsungxr.SXRShaderTemplate;
import com.samsungxr.arpet.R;
import com.samsungxr.utility.TextFile;

public class SXRPointCloudShader extends SXRShaderTemplate {
    private static String fragTemplate;
    private static String vtxTemplate;

    public SXRPointCloudShader(SXRContext context)
    {
        super("float3 u_color; float u_point_size", "",
                "float3 a_position", GLSLESVersion.VULKAN);

        fragTemplate = TextFile.readTextFile(context.getContext(), R.raw.point_cloud_frag);
        vtxTemplate = TextFile.readTextFile(context.getContext(), R.raw.point_cloud_vert);

        setSegment("VertexTemplate", vtxTemplate);
        setSegment("FragmentTemplate", fragTemplate);
    }

    protected void setMaterialDefaults(SXRShaderData material)
    {
        material.setVec3("u_color", 1, 1, 1);
        material.setFloat("u_point_size", 5f);
    }

}
