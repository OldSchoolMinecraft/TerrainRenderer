package me.modman.tr;

import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

public class ChunkManager {

    private static final int CHUNK_SIZE = 16;
    private static final int RENDER_DISTANCE = 10; // How many chunks to render around the camera
    private static Map<String, byte[]> loadedChunks = new HashMap<>();

    public static void loadVisibleChunks(float cameraX, float cameraY) {
        // Calculate the chunk coordinates based on the camera position and zoom level
        int chunkX = (int) (cameraX / (CHUNK_SIZE * Camera.getZoom()));
        int chunkZ = (int) (cameraY / (CHUNK_SIZE * Camera.getZoom()));

        // Define the range to load chunks based on the render distance
        for (int x = -(RENDER_DISTANCE); x <= RENDER_DISTANCE; x++) {
            for (int z = -RENDER_DISTANCE; z <= RENDER_DISTANCE; z++) {
                int currentChunkX = chunkX + x;
                int currentChunkZ = chunkZ + z;
                String key = currentChunkX + "," + currentChunkZ;

                // Load the chunk if not already loaded
                if (!loadedChunks.containsKey(key)) {
                    loadChunk(currentChunkX, currentChunkZ);
                }
            }
        }
    }

    private static void loadChunk(int chunkX, int chunkZ) {
        String key = chunkX + "," + chunkZ;
        if (!loadedChunks.containsKey(key)) {
            loadedChunks.put(key, ChunkLoader.loadChunk(chunkX, chunkZ, CHUNK_SIZE));
        }
    }

    public static void renderChunks(ChunkRenderer chunkRenderer)
    {
        GL30.glPushMatrix();

        int chunkRenderCount = 0;
        for (Map.Entry<String, byte[]> entry : loadedChunks.entrySet())
        {
            String[] coords = entry.getKey().split(",");
            int chunkX = Integer.parseInt(coords[0]);
            int chunkZ = Integer.parseInt(coords[1]);
            if (isChunkVisible(chunkX, chunkZ))
            {
                chunkRenderCount++;
                byte[] chunkData = entry.getValue();
                chunkRenderer.renderChunk(chunkData, CHUNK_SIZE, chunkX, chunkZ);
            }
        }
        System.out.println("Rendered " + chunkRenderCount + " visible chunks");
        GL30.glPopMatrix();
    }

    public static boolean isChunkVisible(float chunkX, float chunkZ)
    {
        Matrix4f viewMatrix = Camera.getViewMatrix();
        Matrix4f projectionMatrix = Camera.getProjectionMatrix();
        Matrix4f mvpMatrix = new Matrix4f(projectionMatrix).mul(viewMatrix);

        Vector4f chunkCenter = new Vector4f(chunkX * CHUNK_SIZE + CHUNK_SIZE / 2.0f, chunkZ * CHUNK_SIZE + CHUNK_SIZE / 2.0f, 0.0f, 1.0f);
        chunkCenter.mul(mvpMatrix);

        return chunkCenter.x >= -1.0f && chunkCenter.x <= 1.0f &&
                chunkCenter.y >= -1.0f && chunkCenter.y <= 1.0f &&
                chunkCenter.z >= -1.0f && chunkCenter.z <= 1.0f;
    }
}
