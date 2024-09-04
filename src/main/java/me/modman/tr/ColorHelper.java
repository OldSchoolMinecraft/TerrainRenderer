package me.modman.tr;

public class ColorHelper
{
    private boolean linearInterpolation;
    private boolean specularLightSim;
    private boolean noise;
    private boolean sine;
    private int blockID;
    private int data;
    private int x, z;
    private int height;
    private long initMS;

    public ColorHelper(int blockID, int data, int x, int z, int height)
    {
        this.blockID = blockID;
        this.data = data;
        this.x = x;
        this.z = z;
        this.height = height;
    }

    public ColorHelper linearInterpolation()
    {
        linearInterpolation = true;
        return this;
    }

    public ColorHelper specularLight()
    {
        specularLightSim = true;
        return this;
    }

    public ColorHelper sine(long initMS)
    {
        this.initMS = initMS;
        sine = true;
        return this;
    }

    public ColorHelper noise()
    {
        noise = true;
        return this;
    }

    private float[] getWaterColorWithNoise(float[] baseWaterColor, int x, int z)
    {
        float noise = generateNoise(x, z);
        float noiseFactor = 0.05f * noise;

        return new float[]{
                Math.min(baseWaterColor[0] + noiseFactor, 1.0f),
                Math.min(baseWaterColor[1] + noiseFactor, 1.0f),
                Math.min(baseWaterColor[2] + noiseFactor, 1.0f)
        };
    }

    private float[] getWaterColorWithReflection(float[] baseWaterColor, int x, int z)
    {

        // Calculate reflection effect based on position
        float reflectionIntensity = (float) (Math.cos((x + z) * 0.1) * 0.1); // Vary by position
        float highlight = 0.1f; // Reflection strength

        return new float[]{
                Math.min(baseWaterColor[0] + reflectionIntensity * highlight, 1.0f),
                Math.min(baseWaterColor[1] + reflectionIntensity * highlight, 1.0f),
                Math.min(baseWaterColor[2] + reflectionIntensity * highlight, 1.0f)
        };
    }

    private float[] getWaterColorWithDirFlow(float[] baseWaterColor, int x, int z)
    {
        // Simulate flow by varying the color along a sine wave pattern
        float flowVariation = 0.05f * (float) Math.sin((x * 0.1f) + (z * 0.1f)); // Adjust the coefficients for more/less variation

        return new float[]{
                Math.min(baseWaterColor[0] + flowVariation, 1.0f),
                Math.min(baseWaterColor[1] + flowVariation, 1.0f),
                Math.min(baseWaterColor[2] + flowVariation, 1.0f)
        };
    }

    private float[] getWaterWithTimeSine(float[] baseWaterColor, int x, int z, float time)
    {
        // Time-based sine wave to animate the water
        float animationFactor = 0.05f * (float) Math.sin((x * 0.1f) + (z * 0.1f) + time);

        return new float[]{
                Math.min(baseWaterColor[0] + animationFactor, 1.0f),
                Math.min(baseWaterColor[1] + animationFactor, 1.0f),
                Math.min(baseWaterColor[2] + animationFactor, 1.0f)
        };
    }

    public float[] getFinalColor()
    {
        float[] baseColor = switch (blockID)
        {
            case 1 -> new float[]{0.5f, 0.5f, 0.5f}; // Gray for Stone
            case 2 -> new float[]{0.49f, 0.78f, 0.25f}; // Green for Grass
            case 3 -> new float[]{0.65f, 0.16f, 0.16f}; // Brown for Dirt
            case 4 -> new float[]{0.5f, 0.5f, 0.5f}; // Gray for Cobblestone
            case 5 -> new float[]{0.57f, 0.48f, 0.28f}; // Oak plank
            case 7 -> new float[]{0.25f, 0.25f, 0.25f}; // Black for Bedrock
            case 8, 9 -> pickWaterColor(new float[]{0.0f, 0.0f, 1.0f}); // Blue for Water
            case 10, 11 -> new float[]{1.0f, 0.5f, 0.0f}; // Orange for Lava
            case 12 -> new float[]{0.94f, 0.9f, 0.55f}; // Tan for Sand
            case 13 -> new float[]{0.75f, 0.75f, 0.75f}; // Light Grey for Gravel
            case 17 -> new float[]{0.58f, 0.45f, 0.24f}; // Log
            case 18 -> new float[]{0.41f, 0.54f, 0.09f}; // Green for Leaves
            case 35 -> getWoolColor(data); // Dynamic wool color
            case 24 -> new float[]{0.94f, 0.9f, 0.55f}; // Tan for Sandstone
            case 43 -> new float[]{0.66f, 0.66f, 0.66f}; // double slab block
            case 44 -> getSlabColor(data); // Dynamic slab color
            case 57 -> new float[]{0.67f, 0.88f, 0.93f}; // Blue for Diamond block
            case 79 -> new float[]{0.45f, 0.60f, 0.81f}; // Ice
            case 82 -> new float[]{0.46f, 0.47f, 0.51f}; // Clay
            default -> new float[]{1.0f, 1.0f, 1.0f}; // White for Unknown
        };

        if (linearInterpolation)
            return interpolate(baseColor, 60, 80);
        else return baseColor;
    }

    private float[] interpolate(float[] baseColor, int minHeight, int maxHeight)
    {
        float normalizedHeight = (float) (height - minHeight) / (maxHeight - minHeight);
        float mappedHeight = normalizedHeight * 255.0f;

        // Enhance depth effect: Adjust color based on height
        float depthFactor = 0.4f + (mappedHeight / 255.0f) * 0.8f; // Higher range for more contrast

        // Apply brightness to the base color
        return new float[]{
                Math.min(baseColor[0] * depthFactor, 1.0f),
                Math.min(baseColor[1] * depthFactor, 1.0f),
                Math.min(baseColor[2] * depthFactor, 1.0f)
        };
    }

    private float[] pickWaterColor(float[] baseColor)
    {
        float[] newColor = baseColor;
        if (specularLightSim) newColor = getWaterColorWithReflection(newColor, x, z);
        if (sine) newColor = getWaterWithTimeSine(newColor, x, z, ((System.currentTimeMillis() - initMS) / 1000.0f));
        if (linearInterpolation) newColor = interpolate(newColor, 60, 80);
        if (noise) newColor = getWaterColorWithNoise(newColor, x, z);
        return newColor;
    }

    private static float[] getSlabColor(int data)
    {
        return switch (data)
        {
            case 0 -> new float[]{0.66f, 0.66f, 0.66f}; // Stone
            case 1 -> new float[]{0.8f, 0.8f, 0.6f}; // Sandstone
            case 2 -> new float[]{0.75f, 0.75f, 0.75f}; // Wooden Slab
            case 3 -> new float[]{0.5f, 0.5f, 0.5f}; // Cobblestone
            default -> new float[]{1.0f, 1.0f, 1.0f}; // Default to White if unknown
        };
    }

    private static float[] getWoolColor(int data)
    {
        return switch (data)
        {
            case 0 -> new float[]{1.0f, 1.0f, 1.0f}; // White
            case 1 -> new float[]{0.94f, 0.68f, 0.68f}; // Orange
            case 2 -> new float[]{0.87f, 0.68f, 0.87f}; // Magenta
            case 3 -> new float[]{0.53f, 0.73f, 0.87f}; // Light Blue
            case 4 -> new float[]{0.94f, 0.87f, 0.68f}; // Yellow
            case 5 -> new float[]{0.68f, 0.87f, 0.68f}; // Lime
            case 6 -> new float[]{0.87f, 0.68f, 0.87f}; // Pink
            case 7 -> new float[]{0.35f, 0.35f, 0.35f}; // Gray
            case 8 -> new float[]{0.68f, 0.68f, 0.68f}; // Light Gray
            case 9 -> new float[]{0.28f, 0.68f, 0.87f}; // Cyan
            case 10 -> new float[]{0.68f, 0.35f, 0.87f}; // Purple
            case 11 -> new float[]{0.35f, 0.35f, 0.87f}; // Blue
            case 12 -> new float[]{0.53f, 0.35f, 0.18f}; // Brown
            case 13 -> new float[]{0.35f, 0.53f, 0.18f}; // Green
            case 14 -> new float[]{0.87f, 0.18f, 0.18f}; // Red
            case 15 -> new float[]{0.18f, 0.18f, 0.18f}; // Black
            default -> new float[]{1.0f, 1.0f, 1.0f}; // Default to White if unknown
        };
    }

    private float generateNoise(int x, int z)
    {
        return (float) (Math.sin(x * 12.9898 + z * 78.233) * 43758.5453 % 1.0);
    }
}
