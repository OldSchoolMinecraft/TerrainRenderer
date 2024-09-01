package me.modman.tr;

public class BlockColor
{
    public static float[] getColor(int blockID, int data, int height)
    {
        // Base colors for different blocks
        float[] baseColor = switch (blockID) {
            case 1 -> new float[]{0.5f, 0.5f, 0.5f}; // Gray for Stone
            case 2 -> new float[]{0.49f, 0.78f, 0.25f}; // Green for Grass
            case 3 -> new float[]{0.65f, 0.16f, 0.16f}; // Brown for Dirt
            case 4 -> new float[]{0.5f, 0.5f, 0.5f}; // Gray for Cobblestone
            case 5 -> new float[]{0.57f, 0.48f, 0.28f}; // Oak plank
            case 7 -> new float[]{0.25f, 0.25f, 0.25f}; // Black for Bedrock
            case 8, 9 -> new float[]{0.0f, 0.0f, 1.0f}; // Blue for Water
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

// Enhance depth effect: Adjust color based on height
        float depthFactor = 0.4f + (height / 255.0f) * 0.6f; // Higher range for more contrast
        float shadowFactor = 1.0f - (height / 255.0f) * 0.3f; // Adds shadow effect based on height

        // Apply brightness and shadow to the base color
        return new float[]{
                Math.min(baseColor[0] * depthFactor * shadowFactor, 1.0f),
                Math.min(baseColor[1] * depthFactor * shadowFactor, 1.0f),
                Math.min(baseColor[2] * depthFactor * shadowFactor, 1.0f)
        };
//        return baseColor;
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
}

