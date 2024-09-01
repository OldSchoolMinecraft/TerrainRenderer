#version 330 core

in vec3 fragColor;
in vec2 texCoord;

out vec4 color;

void main() {
    // Sample the heightmap texture
    //float height = texture(u_HeightmapTexture, texCoord).r; // Red channel holds the height data

    // Use the height to modify the color or shading
    //float depthFactor = height; // Use height as a depth factor for coloring
    color = vec4(fragColor, 1.0);
}
