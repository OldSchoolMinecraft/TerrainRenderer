#version 330 core

in vec3 fragColor; // Color passed from vertex shader

out vec4 color;    // Final color output

void main() {
    color = vec4(fragColor, 1.0); // Set the final color with alpha value 1.0
}