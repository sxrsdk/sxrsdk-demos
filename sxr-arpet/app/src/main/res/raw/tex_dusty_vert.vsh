#extension GL_ARB_separate_shader_objects : enable
#extension GL_ARB_shading_language_420pack : enable

uniform float u_ratio;

layout ( location = 0 )in vec3 a_position;
layout ( location = 1 )in vec2 a_texcoord;

@MATRIX_UNIFORMS

layout ( location = 0 ) out vec2 diffuse_coord;
layout ( location = 1 ) out vec2 dusty_coord;

void main()
{
    diffuse_coord = a_texcoord;
    dusty_coord = a_position.st * u_ratio + vec2(0.5, 0.5);

    gl_Position = u_mvp * vec4(a_position, 1);
}