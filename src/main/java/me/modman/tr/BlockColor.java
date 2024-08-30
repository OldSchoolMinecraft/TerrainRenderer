package me.modman.tr;

public class BlockColor {
    public static float[] getColor(int blockID) {
        switch (blockID) {
            case 1: return new float[]{1.0f, 0.0f, 0.0f}; // Red for block ID 1
            case 2: return new float[]{0.0f, 1.0f, 0.0f}; // Green for block ID 2
            case 3: return new float[]{0.0f, 0.0f, 1.0f}; // Blue for block ID 3
            // Add more cases for different block IDs
            default: return new float[]{1.0f, 1.0f, 1.0f}; // White for unknown IDs
        }
    }
}

