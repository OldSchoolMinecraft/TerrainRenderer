#version 330 core

layout(location = 0) in vec2 position; // Vertex position
layout(location = 1) in vec3 color;    // Vertex color

out vec3 fragColor;

void main() {
    gl_Position = vec4(position, 0.0, 1.0); // Convert 2D to 3D position
    fragColor = color;                      // Pass color to fragment shader
}
