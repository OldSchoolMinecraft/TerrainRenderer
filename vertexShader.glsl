#version 330 core

layout(location = 0) in vec2 a_Position;
layout(location = 1) in vec3 a_Color;

uniform sampler2D u_HeightmapTexture;
uniform mat4 u_ProjectionMatrix;
uniform mat4 u_ViewMatrix;

out vec3 fragColor;
out vec2 texCoord;

void main()
{
    vec4 worldPos = vec4(a_Position, 0.0, 1.0);
    worldPos.x = -worldPos.x;
    worldPos.z = -worldPos.z;
    gl_Position = u_ProjectionMatrix * u_ViewMatrix * worldPos;

    // Use block position to determine texture coordinates
    texCoord = (a_Position + 1.0) * 0.5; // Adjust this calculation based on your needs

    float height = texture(u_HeightmapTexture, texCoord).r; // Red channel holds the height data
    fragColor = a_Color;
}
