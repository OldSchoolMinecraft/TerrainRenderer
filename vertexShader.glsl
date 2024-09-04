#version 330 core

layout(location = 0) in vec2 a_Position;
layout(location = 1) in vec3 a_Color;
layout(location = 2) in float a_BlockID;

uniform mat4 u_ProjectionMatrix;
uniform mat4 u_ViewMatrix;
uniform vec2 u_ChunkOffset;

out vec3 fragColor;
out float blockID;
out vec2 chunkOffset;

void main()
{
    vec4 worldPos = vec4(a_Position, 0.0, 1.0);
    worldPos.x = -worldPos.x;
    worldPos.z = -worldPos.z;
    gl_Position = u_ProjectionMatrix * u_ViewMatrix * worldPos;

    fragColor = a_Color;
    blockID = a_BlockID;
}
