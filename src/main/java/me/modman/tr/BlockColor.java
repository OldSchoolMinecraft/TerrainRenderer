package me.modman.tr;

public class BlockColor {
    public static float[] getColor(int blockID) {
        return switch (blockID) {
            case 1 -> new float[]{0.5f, 0.5f, 0.5f}; // Gray for Stone
            case 2 -> new float[]{0.49f, 0.78f, 0.25f}; // Green for Grass
            case 3 -> new float[]{0.65f, 0.16f, 0.16f}; // Brown for Dirt
            case 4 -> new float[]{0.5f, 0.5f, 0.5f}; // Gray for Cobblestone
            case 7 -> new float[]{0.25f, 0.25f, 0.25f}; // Black for Bedrock
            case 8, 9 -> new float[]{0.0f, 0.0f, 1.0f}; // Blue for Water
            case 10, 11 -> new float[]{1.0f, 0.5f, 0.0f}; // Orange for Lava
            case 12 -> new float[]{0.94f, 0.9f, 0.55f}; // Tan for Sand
            case 13 -> new float[]{0.75f, 0.75f, 0.75f}; // Light Grey for Gravel
            case 17, 18 -> new float[]{0.41f, 0.54f, 0.09f}; // Green for Leaves
            case 35 -> new float[]{1.0f, 1.0f, 1.0f}; // White for Wool
            case 24, 43 -> new float[]{0.94f, 0.9f, 0.55f}; // Tan for Sandstone
            default -> new float[]{1.0f, 1.0f, 1.0f}; // White for Unknown
        };
    }

}

