#version 330 core

layout(location = 0) in vec2 position; // Vertex position
layout(location = 1) in vec3 color;    // Vertex color

out vec3 fragColor; // Color to pass to fragment shader

void main() {
    // Directly use 2D position; z = 0.0 and w = 1.0 are default for 2D rendering
    gl_Position = vec4(position, 0.0, 1.0);
    fragColor = color; // Pass color to fragment shader
}