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
//            if (!isCoordinateInViewport(chunkX, chunkZ, Camera.getProjectionMatrix(), Camera.getViewMatrix(), Main.getWindowWidth(), Main.getWindowHeight())) continue;
            chunkRenderCount++;
            byte[] chunkData = entry.getValue();
            chunkRenderer.renderChunk(chunkData, CHUNK_SIZE, chunkX, chunkZ);
        }
//        System.out.println("Rendered " + chunkRenderCount + " visible chunks");
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

    public static boolean isCoordinateInViewport(float x, float z, Matrix4f projectionMatrix, Matrix4f viewMatrix, int windowWidth, int windowHeight)
    {
        // Combine view and projection matrices
        Matrix4f mvpMatrix = new Matrix4f(projectionMatrix).mul(viewMatrix);

        // Transform world coordinates to clip space
        Vector4f clipSpace = new Vector4f(x, 0.0f, z, 1.0f).mul(mvpMatrix);

        // Perform perspective divide to get normalized device coordinates (NDC)
        if (clipSpace.w != 0.0f)
        {
            clipSpace.x /= clipSpace.w;
            clipSpace.y /= clipSpace.w;
            clipSpace.z /= clipSpace.w;
        } else
        {
            return false; // Avoid division by zero
        }

        // Convert NDC to window coordinates
        float ndcX = (clipSpace.x + 1.0f) * 0.5f * windowWidth;
        float ndcY = (1.0f - clipSpace.y) * 0.5f * windowHeight; // Note that y is inverted

        // Check if coordinates are within the viewport
        return ndcX >= 0 && ndcX <= windowWidth && ndcY >= 0 && ndcY <= windowHeight;
    }
}
