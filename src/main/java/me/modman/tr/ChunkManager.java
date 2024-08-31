package me.modman.tr;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import java.util.HashMap;
import java.util.Map;

public class ChunkManager {

    private static final int CHUNK_SIZE = 16;
    private static final int RENDER_DISTANCE = 10; // How many chunks to render around the camera
    private static Map<String, byte[]> loadedChunks = new HashMap<>();

    public static void loadVisibleChunks(float cameraX, float cameraY) {
        int chunkX = (int) (cameraX / (CHUNK_SIZE * Camera.getZoom()));
        int chunkZ = (int) (cameraY / (CHUNK_SIZE * Camera.getZoom()));

        for (int x = -RENDER_DISTANCE; x <= RENDER_DISTANCE; x++) {
            for (int z = -RENDER_DISTANCE; z <= RENDER_DISTANCE; z++) {
                int currentChunkX = chunkX + x;
                int currentChunkZ = chunkZ + z;
                loadChunk(currentChunkX, currentChunkZ);
            }
        }
    }

    private static void loadChunk(int chunkX, int chunkZ) {
        String key = chunkX + "," + chunkZ;
        if (!loadedChunks.containsKey(key)) {
            loadedChunks.put(key, ChunkLoader.loadChunk(chunkX, chunkZ, CHUNK_SIZE));
        }
    }

    public static void renderChunks() {
        GL30.glPushMatrix();

        for (Map.Entry<String, byte[]> entry : loadedChunks.entrySet()) {
            String[] coords = entry.getKey().split(",");
            int chunkX = Integer.parseInt(coords[0]);
            int chunkZ = Integer.parseInt(coords[1]);
            byte[] chunkData = entry.getValue();
            ChunkRenderer.renderChunk(chunkData, CHUNK_SIZE, chunkX, chunkZ);
        }
        GL30.glPopMatrix();
    }
}
