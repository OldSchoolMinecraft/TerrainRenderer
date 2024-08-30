package me.modman.tr;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;

import java.awt.*;
import java.nio.DoubleBuffer;

public class Main {

    private static long window;
    private static final int CHUNK_SIZE = 16;
    private static byte[] chunkData;
    private static int chunkX = 0;
    private static int chunkZ = 0;

    private static float lastMouseX;
    private static float lastMouseY;
    private static boolean isDragging = false;

    public static long getWindowID() {
        return window;
    }

    public static void main(String[] args) {
        // Initialize GLFW
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        // Set GLFW window hints
        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);

        // Create the window
        window = GLFW.glfwCreateWindow(800, 600, "OSM Terrain Renderer", MemoryUtil.NULL, MemoryUtil.NULL);
        if (window == MemoryUtil.NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        GLFW.glfwMakeContextCurrent(window);
        GLFW.glfwSwapInterval(1); // Enable v-sync
        GLFW.glfwShowWindow(window);

        // Initialize OpenGL
        GL.createCapabilities();
        GL11.glViewport(0, 0, 800, 600);

        // Enable depth testing
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthFunc(GL11.GL_LEQUAL);

        // Set up key callback
        GLFW.glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (action == GLFW.GLFW_PRESS) {
                // Handle key press
            }
        });

        // Set up mouse callback
        GLFW.glfwSetCursorPosCallback(window, (window, xpos, ypos) -> {
            if (isDragging) {
                float deltaX = (float) (xpos - lastMouseX);
                float deltaY = (float) (ypos - lastMouseY);
                Camera.pan(deltaX, deltaY);
            }
            lastMouseX = (float) xpos;
            lastMouseY = (float) ypos;
        });

        GLFW.glfwSetMouseButtonCallback(window, (window, button, action, mods) -> {
            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                if (action == GLFW.GLFW_PRESS) {
                    isDragging = true;
                    double[] mousePos = getCursorPos(window);
                    lastMouseX = (float) mousePos[0];
                    lastMouseY = (float) mousePos[1];
                } else if (action == GLFW.GLFW_RELEASE) {
                    isDragging = false;
                }
            }
        });

        GLFW.glfwSetScrollCallback(window, (window, xoffset, yoffset) -> {
            System.out.println("Scroll input: xoffset=" + xoffset + ", yoffset=" + yoffset);
            Camera.zoom((float) yoffset);
            System.out.println("New zoom: " + Camera.getZoom());
            updateOrthoProjection();
        });

        ChunkRenderer.init();

        Camera.zoom(0.0f);
        updateOrthoProjection();

        // Main loop
        while (!GLFW.glfwWindowShouldClose(window)) {
            // Poll events
            GLFW.glfwPollEvents();

            // Clear the screen - Clear both color and depth buffers
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

            // Update camera and projection
            Camera.update();
            updateOrthoProjection();

            // Load and render chunks
            ChunkManager.loadVisibleChunks(Camera.getXOffset(), Camera.getYOffset());
            ChunkManager.renderChunks();

            // Swap buffers
            GLFW.glfwSwapBuffers(window);
        }


        // Clean up
        GLFW.glfwDestroyWindow(window);
        GLFW.glfwTerminate();
    }

    private static void updateOrthoProjection() {
        float zoom = Camera.getZoom();

        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();

        // Adjust the orthographic projection based on zoom
        float left = 0 / zoom;
        float right = 800 / zoom;
        float bottom = 600 / zoom;
        float top = 0 / zoom;

        GL11.glOrtho(left, right, bottom, top, -1, 1);

        GL11.glMatrixMode(GL11.GL_MODELVIEW);
    }

    public static double[] getCursorPos(long windowID) {
        DoubleBuffer posX = BufferUtils.createDoubleBuffer(1);
        DoubleBuffer posY = BufferUtils.createDoubleBuffer(1);
        GLFW.glfwGetCursorPos(windowID, posX, posY);
        return new double[] { posX.get(0), posY.get(0) };
    }
}
