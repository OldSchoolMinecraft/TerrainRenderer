#version 330 core

in vec3 fragColor;
in float blockID;
in vec2 chunkOffset;

uniform float u_Time;
uniform vec2 u_ScreenResolution;

out vec4 color;

void main() {
    int doWave = 1;
    if (blockID == 8 || blockID == 9) {
        if (doWave != 1) {
            color = vec4(fragColor, 1.0);
            return;
        }
        // Calculate normalized screen coordinates
        vec2 uv = gl_FragCoord.xy / u_ScreenResolution;

        // Pixelation factor (controls the blockiness)
        float pixelation = 50.0; // Adjust for pixel size
        vec2 uvPixelated = floor(uv * pixelation) / pixelation;

        // Diagonal lines pattern
        float lineThickness = 0.22; // Thickness of the diagonal lines
        float diagonal = abs(mod(uvPixelated.x + uvPixelated.y + u_Time * 0.5, 1.0) - 0.5) * 2.0;

        // Smoothstep to soften the lines
        float pattern = smoothstep(lineThickness - 0.01, lineThickness + 0.01, diagonal);

        // Apply the pattern to the color
        float intensity = 0.05;
        vec3 wavedColor = fragColor * (1.0 + pattern * intensity);

        // Set the final color output
        color = vec4(wavedColor, 1.0);
    } else {
        color = vec4(fragColor, 1.0);
    }
}
