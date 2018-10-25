package com.samsungxr.gvr360Photo;



import com.samsungxr.SXRContext;
import com.samsungxr.SXRCustomMaterialShaderId;
import com.samsungxr.SXRMaterialMap;
import com.samsungxr.SXRMaterialShaderManager;
import com.samsungxr.SXRPhongShader;

public class Outline {
    public static final String COLOR_KEY = "u_color";
    public static final String THICKNESS_KEY = "u_thickness";
    public static final String TEXTURE_KEY = "texture";
    private static final String VERTEX_SHADER =
            "attribute  vec4 a_position;\n"
                    + "attribute vec3 a_normal;\n"
                    + "uniform mat4 u_mvp;\n"
                    //+ "float u_thickness = 2.0f;\n"
                    + "void main() {\n"
                    //+ "  vec4 pos = vec4(a_position.xyz + a_normal * u_thickness, 1.0);\n"
                   // + "  vec4 pos = vec4(a_position.xyz + a_normal, 1.0);\n"
                    + "  gl_Position = u_mvp * a_position;\n"
                    + "}\n";



    private static final String FRAGMENT_SHADER =
             "#extension GL_EXT_frag_depth : require\n"
            +"precision highp float;\n"
                  //  + "uniform sampler2D texture;\n"
                    + "vec4  u_color = vec4(0.0f, 0.0f, 0.5f, 1.0f);\n"
                    + "void main() {\n"
               //     + "float xPos = gl_FragCoord.x/133.33f;\n"
              //      + "float yPos = gl_FragCoord.y/100.0f;\n"
               //     + "vec2 temp = vec2(xPos, yPos);\n"
               //     + "vec4 queriedDepth = texture2D(texture, temp);\n"
              //      + "float depthOfObject = queriedDepth.x;\n"
                  //  + "depthOfObject = depthOfObject/255.0f;\n"
                    //+ "if(depthOfObject > gl_FragDepthEXT){\n"
                //   +"if(queriedDepth.x > 1.0f){\n"
                //    + "  gl_FragColor = queriedDepth;\n"
                //    + "}\n"
                //    + "else\n"
                //    + "gl_FragColor = texture2D(texture, vec2(0,0));\n"
                     + "gl_FragColor = vec4(1,0,0,1);\n"
                    + "}\n";

    private SXRCustomMaterialShaderId mShaderId;
    private SXRMaterialMap mCustomShader = null;

    public Outline(SXRContext gvrcontext) {
        final SXRMaterialShaderManager shaderManager = gvrcontext
                .getMaterialShaderManager();
        mShaderId = shaderManager.addShader(VERTEX_SHADER, FRAGMENT_SHADER);
        mCustomShader = shaderManager.getShaderMap(mShaderId);
       // mCustomShader.addUniformVec4Key("u_color", COLOR_KEY);
       // mCustomShader.addUniformVec3Key("u_light", LIGHT_KEY);
       // mCustomShader.addUniformVec3Key("u_eye", EYE_KEY);
        //mCustomShader.addUniformFloatKey("u_radius", RADIUS_KEY);
       // mCustomShader.addTextureKey("texture", TEXTURE_KEY);
        //mCustomShader.addTextureKey("textureDepth", TEXTURE_KEY1D);


        //super(gvrcontext);
       // setSegment("FragmentTemplate", FRAGMENT_SHADER);
        //setSegment("VertexTemplate", VERTEX_SHADER);
        //mCustomShader.addTextureKey("texture", TEXTURE_KEY);
    }

    public SXRCustomMaterialShaderId getShaderId() {
        return mShaderId;
    }
}
