package me.modman.tr.chunk;

import me.modman.tr.util.Camera;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.awt.Point; // Use Point instead of String for chunk keys
import java.util.HashMap;
import java.util.Map;

public class ChunkManager {

    private static final int CHUNK_SIZE = 16;
    private static final int RENDER_DISTANCE = 10; // How many chunks to render around the camera
    private static final Map<Point, Chunk> loadedChunks = new HashMap<>(); // Use Point for chunk keys

    public static void loadVisibleChunks(float cameraX, float cameraY) {
        // Cache Camera zoom level to avoid repeated calls
        float zoom = Camera.getZoom();
        int chunkX = (int) (cameraX / (CHUNK_SIZE * zoom));
        int chunkZ = (int) (cameraY / (CHUNK_SIZE * zoom));

        // Use Point objects to represent chunk coordinates
        for (int x = -RENDER_DISTANCE; x <= RENDER_DISTANCE; x++) {
            for (int z = -RENDER_DISTANCE; z <= RENDER_DISTANCE; z++) {
                int currentChunkX = chunkX + x;
                int currentChunkZ = chunkZ + z;
                Point key = new Point(currentChunkX, currentChunkZ);

                // Load the chunk if not already loaded
                if (!loadedChunks.containsKey(key)) {
                    loadChunk(currentChunkX, currentChunkZ);
                }
            }
        }
    }

    private static void loadChunk(int chunkX, int chunkZ) {
        Point key = new Point(chunkX, chunkZ);
        if (!loadedChunks.containsKey(key)) {
            loadedChunks.put(key, ChunkLoader.loadChunk(chunkX, chunkZ, CHUNK_SIZE));
        }
    }

    public static void renderChunks(ChunkRenderer chunkRenderer) {
        int chunkRenderCount = 0;

        for (Map.Entry<Point, Chunk> entry : loadedChunks.entrySet()) {
            Point coords = entry.getKey();
            int chunkX = coords.x;
            int chunkZ = coords.y;

            Chunk chunk = entry.getValue();
            if (chunk == null) continue;

            Block[] chunkData = chunk.getChunkData();
            if (chunkData == null) continue;

            chunkRenderCount++;
            chunkRenderer.renderChunk2(chunk, CHUNK_SIZE, chunkX, chunkZ);
        }
    }

    public static boolean isChunkVisible(float chunkX, float chunkZ) {
        Matrix4f viewMatrix = Camera.getViewMatrix();
        Matrix4f projectionMatrix = Camera.getProjectionMatrix();
        Matrix4f mvpMatrix = new Matrix4f(projectionMatrix).mul(viewMatrix);

        Vector4f chunkCenter = new Vector4f(chunkX * CHUNK_SIZE + CHUNK_SIZE / 2.0f, chunkZ * CHUNK_SIZE + CHUNK_SIZE / 2.0f, 0.0f, 1.0f);
        chunkCenter.mul(mvpMatrix);

        return chunkCenter.x >= -1.0f && chunkCenter.x <= 1.0f &&
                chunkCenter.y >= -1.0f && chunkCenter.y <= 1.0f &&
                chunkCenter.z >= -1.0f && chunkCenter.z <= 1.0f;
    }

    public static boolean isCoordinateInViewport(float x, float z, Matrix4f projectionMatrix, Matrix4f viewMatrix, int windowWidth, int windowHeight) {
        // Combine view and projection matrices
        Matrix4f mvpMatrix = new Matrix4f(projectionMatrix).mul(viewMatrix);

        // Transform world coordinates to clip space
        Vector4f clipSpace = new Vector4f(x, 0.0f, z, 1.0f).mul(mvpMatrix);

        // Perform perspective divide to get normalized device coordinates (NDC)
        if (clipSpace.w != 0.0f) {
            clipSpace.x /= clipSpace.w;
            clipSpace.y /= clipSpace.w;
            clipSpace.z /= clipSpace.w;
        } else {
            return false; // Avoid division by zero
        }

        // Convert NDC to window coordinates
        float ndcX = (clipSpace.x + 1.0f) * 0.5f * windowWidth;
        float ndcY = (1.0f - clipSpace.y) * 0.5f * windowHeight; // Note that y is inverted

        // Check if coordinates are within the viewport
        return ndcX >= 0 && ndcX <= windowWidth && ndcY >= 0 && ndcY <= windowHeight;
    }
}
