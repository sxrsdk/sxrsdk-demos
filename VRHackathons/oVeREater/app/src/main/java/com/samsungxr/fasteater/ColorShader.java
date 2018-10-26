package com.gearvrf.fasteater;

import com.samsungxr.SXRContext;
import com.samsungxr.SXRMaterialMap;
import com.samsungxr.SXRMaterialShaderManager;
import com.samsungxr.SXRShaderTemplate;
import com.samsungxr.R;
import com.samsungxr.utility.TextFile;

import android.content.Context;

import com.samsungxr.SXRCustomMaterialShaderId;


/**
 * Copied from sxr-eyepicking demo app
 */
public class ColorShader extends SXRShaderTemplate {

    private static final String VERTEX_SHADER = "in vec4 a_position;\n"
            + "uniform mat4 u_mvp;\n"
            + "void main() {\n"
            + "  gl_Position = u_mvp * a_position;\n"
            + "}\n";

    private static final String FRAGMENT_SHADER = "precision mediump float;\n"
            + "uniform vec4 u_color;\n"
            + "out vec4 fragColor;\n"
            + "void main() {\n"
            + "  fragColor = u_color;\n"
            + "}\n";

    public ColorShader(SXRContext sxrContext)
    {
        super("float4 u_color");
        setSegment("FragmentTemplate", FRAGMENT_SHADER);
        setSegment("VertexTemplate", VERTEX_SHADER);
    }


}
