#version 330 core

layout(location = 0) in vec2 a_Position;
layout(location = 1) in vec3 a_Color;
layout(location = 2) in float a_Height;

uniform mat4 u_ProjectionMatrix;
uniform mat4 u_ViewMatrix;

out vec3 fragColor;
out vec2 texCoord;

void main()
{
    // Transform vertex position
    vec4 worldPos = vec4(a_Position, 0.0, 1.0);
    worldPos.x = -worldPos.x;
    worldPos.z = -worldPos.z;
    gl_Position = u_ProjectionMatrix * u_ViewMatrix * worldPos;

    // Pass color and texture coordinates to fragment shader
    fragColor = a_Color;

    // Calculate the texture coordinates based on the position
    // Assuming a_Position is normalized, adjust if needed
    texCoord = (a_Position + 1.0) * 0.5; // Convert from [-1, 1] to [0, 1]
}
