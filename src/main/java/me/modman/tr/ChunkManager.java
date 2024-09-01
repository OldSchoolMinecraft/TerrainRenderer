package me.modman.tr;

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

        // Track the chunks that should be loaded this frame
        HashSet<String> newChunks = new HashSet<>();

        // Define the range to load chunks based on the render distance
        for (int x = -RENDER_DISTANCE; x <= RENDER_DISTANCE; x++) {
            for (int z = -RENDER_DISTANCE; z <= RENDER_DISTANCE; z++) {
                int currentChunkX = chunkX + x;
                int currentChunkZ = chunkZ + z;
                String key = currentChunkX + "," + currentChunkZ;

                // Add new chunk to the set
                newChunks.add(key);

                // Load the chunk if not already loaded
                if (!loadedChunks.containsKey(key)) {
                    loadChunk(currentChunkX, currentChunkZ);
                }
            }
        }

        // Remove chunks that are no longer visible
        Iterator<Map.Entry<String, byte[]>> iterator = loadedChunks.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, byte[]> entry = iterator.next();
            String key = entry.getKey();
            if (!newChunks.contains(key)) {
                iterator.remove(); // Remove chunk from the map
            }
        }
    }

    private static void loadChunk(int chunkX, int chunkZ) {
        String key = chunkX + "," + chunkZ;
        if (!loadedChunks.containsKey(key)) {
            loadedChunks.put(key, ChunkLoader.loadChunk(chunkX, chunkZ, CHUNK_SIZE));
        }
    }

    public static void renderChunks()
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
                ChunkRenderer.renderChunk(chunkData, CHUNK_SIZE, chunkX, chunkZ);
            }
        }
        System.out.println("Rendered " + chunkRenderCount + " visible chunks");
        GL30.glPopMatrix();
    }

    public static boolean isChunkVisible(float chunkX, float chunkZ) {
        // Get camera parameters
        float cameraX = Camera.getXOffset();
        float cameraY = Camera.getYOffset();
        float zoom = Camera.getZoom();

        // Calculate viewport boundaries based on zoom
        int windowWidth = Main.getWindowWidth();
        int windowHeight = Main.getWindowHeight();

        // Viewport width and height in world coordinates
        float viewportWidth = windowWidth / zoom;
        float viewportHeight = windowHeight / zoom;

        // Calculate viewport boundaries in world coordinates
        float left = cameraX - viewportWidth / 2;
        float right = cameraX + viewportWidth / 2;
        float bottom = cameraY - viewportHeight / 2;
        float top = cameraY + viewportHeight / 2;

        // Calculate chunk boundaries in world coordinates
        float chunkLeft = chunkX * CHUNK_SIZE;
        float chunkRight = chunkLeft + CHUNK_SIZE;
        float chunkBottom = chunkZ * CHUNK_SIZE;
        float chunkTop = chunkBottom + CHUNK_SIZE;

        // Check if the chunk is within the viewport boundaries
        boolean withinX = chunkRight > left && chunkLeft < right;
        boolean withinY = chunkTop > bottom && chunkBottom < top;

        // Print debug information
        System.out.printf("Camera: (%.2f, %.2f), Zoom: %.2f%n", cameraX, cameraY, zoom);
        System.out.printf("Viewport: (%.2f, %.2f, %.2f, %.2f)%n", left, right, bottom, top);
        System.out.printf("Chunk: (%.2f, %.2f, %.2f, %.2f)%n", chunkLeft, chunkRight, chunkBottom, chunkTop);
        System.out.printf("Visible: %b%n", (withinX && withinY));

        return withinX && withinY;
    }
}
