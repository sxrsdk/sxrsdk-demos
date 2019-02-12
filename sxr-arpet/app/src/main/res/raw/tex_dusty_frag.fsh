precision highp float;
uniform sampler2D u_texture;

@MATERIAL_UNIFORMS

layout ( location = 0 ) in vec2 diffuse_coord;
layout ( location = 1 ) in vec2 dusty_coord;

out vec4 outColor;

void main()
{
    vec4 diff_color = texture(u_texture, diffuse_coord);
    vec4 color = texture(u_texture, dusty_coord);
    float opacity = diff_color.a * color.a * u_opacity;

    outColor = vec4(color.r * u_color.r * opacity, color.g * u_color.g * opacity, color.b * u_color.b * opacity, opacity);
}
