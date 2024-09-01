#version 330 core

layout(location = 0) in vec2 a_Position;
layout(location = 1) in vec3 a_Color;

out vec3 fragColor;

uniform mat4 u_ProjectionMatrix;
uniform mat4 u_ViewMatrix;

void main()
{
    vec4 worldPosition = vec4(a_Position, 0.0, 1.0);
    vec4 viewPosition = u_ViewMatrix * worldPosition;
    gl_Position = u_ProjectionMatrix * viewPosition;
    fragColor = a_Color;
}
