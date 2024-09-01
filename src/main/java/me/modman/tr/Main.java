package me.modman.tr;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GLUtil;
import org.lwjgl.system.MemoryUtil;

import java.awt.*;
import java.nio.DoubleBuffer;

public class Main {

    private static long window;

    private static float lastMouseX;
    private static float lastMouseY;
    private static boolean isDragging = false;

    private static final int WINDOW_WIDTH = 1280;
    private static final int WINDOW_HEIGHT = 720;

    public static int getWindowWidth()
    {
        return WINDOW_WIDTH;
    }

    public static int getWindowHeight()
    {
        return WINDOW_HEIGHT;
    }

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
        window = GLFW.glfwCreateWindow(WINDOW_WIDTH, WINDOW_HEIGHT, "OSM Terrain Renderer", MemoryUtil.NULL, MemoryUtil.NULL);
        if (window == MemoryUtil.NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        GLFW.glfwMakeContextCurrent(window);
        GLFW.glfwSwapInterval(1); // Enable v-sync
        GLFW.glfwShowWindow(window);

        // Initialize OpenGL
        GL.createCapabilities();
        GL30.glViewport(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);

        // Enable depth testing
        GL30.glEnable(GL30.GL_DEPTH_TEST);
        GL30.glDepthFunc(GL30.GL_LEQUAL);

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
//            System.out.println("Scroll input: xoffset=" + xoffset + ", yoffset=" + yoffset);
            Camera.zoom((float) yoffset);
//            System.out.println("New zoom: " + Camera.getZoom());
            updateOrthoProjection();
        });

        ChunkRenderer.init();

        // Main loop
        while (!GLFW.glfwWindowShouldClose(window)) {
            // Poll events
            GLFW.glfwPollEvents();

            // Clear the screen - Clear both color and depth buffers
            GL30.glClear(GL30.GL_COLOR_BUFFER_BIT | GL30.GL_DEPTH_BUFFER_BIT);

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
        GL30.glMatrixMode(GL30.GL_PROJECTION);
        GL30.glLoadIdentity();

        float zoom = Camera.getZoom();
        int windowWidth = Main.getWindowWidth();
        int windowHeight = Main.getWindowHeight();

        float aspectRatio = (float) windowWidth / windowHeight;
        float zoomFactor = 1.0f / zoom;
        float left = -aspectRatio * zoomFactor;
        float right = aspectRatio * zoomFactor;
        float bottom = -zoomFactor;
        float top = zoomFactor;

        GL30.glOrtho(left, right, bottom, top, -1.0f, 1.0f);

        GL30.glMatrixMode(GL30.GL_MODELVIEW);
    }

    public static double[] getCursorPos(long windowID) {
        DoubleBuffer posX = BufferUtils.createDoubleBuffer(1);
        DoubleBuffer posY = BufferUtils.createDoubleBuffer(1);
        GLFW.glfwGetCursorPos(windowID, posX, posY);
        return new double[] { posX.get(0), posY.get(0) };
    }
}
