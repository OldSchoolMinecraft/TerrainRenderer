#version 330 core

in vec3 fragColor;
out vec4 color;

void main()
{
    float depthFactor = 1.0; //gl_FragCoord.z / gl_FragCoord.w; // or some other depth factor based on height
    color = vec4(fragColor * depthFactor, 1.0);
}
