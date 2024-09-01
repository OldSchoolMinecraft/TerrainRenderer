#version 330 core

layout(location = 0) in vec2 aPosition;  // Vertex position
layout(location = 1) in vec3 aColor;     // Vertex color

out vec3 fragColor;                      // Color passed to fragment shader

uniform mat4 u_ProjectionMatrix;         // Projection matrix for transformation

void main()
{
    gl_Position = u_ProjectionMatrix * vec4(aPosition, 0.0, 1.0); // Apply projection matrix
    fragColor = aColor; // Pass the color to the fragment shader
}