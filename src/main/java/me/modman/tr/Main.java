package me.modman.tr;

import imgui.ImGui;
import me.modman.tr.chunk.ChunkManager;
import me.modman.tr.chunk.ChunkRenderer;
import me.modman.tr.gui.DebugUI;
import me.modman.tr.gui.SimpleGUI;
import me.modman.tr.util.Camera;
import me.modman.tr.util.ShaderUtils;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;

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

    private static ChunkRenderer chunkRenderer = new ChunkRenderer();
    private static boolean usingGUI;
    private static SimpleGUI currentGUI;

    public static void main(String[] args)
    {
        // Initialize GLFW
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        // Set GLFW window hints
        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE);

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

        chunkRenderer.init();

        // Set up key callback
        GLFWKeyCallbackI keyCallback = (window, key, scancode, action, mods) ->
        {
            if (key == GLFW.GLFW_KEY_GRAVE_ACCENT && action == GLFW.GLFW_RELEASE)
            {
                if (currentGUI instanceof DebugUI)
                {
                    currentGUI = null;
                    usingGUI = false;
                    return;
                }
                currentGUI = new DebugUI();
                currentGUI.init();
                usingGUI = true;
            }
        };
        try (GLFWKeyCallback callback = GLFW.glfwSetKeyCallback(window, keyCallback)) {}

        // Set up mouse callback
        GLFWCursorPosCallbackI cursorPosCallback = (window, xpos, ypos) ->
        {
            if (isDragging)
            {
                float deltaX = (float) (xpos - lastMouseX);
                float deltaY = (float) (ypos - lastMouseY);
                Camera.pan(deltaX, deltaY);
            }
            lastMouseX = (float) xpos;
            lastMouseY = (float) ypos;
        };
        try (GLFWCursorPosCallback callback = GLFW.glfwSetCursorPosCallback(window, cursorPosCallback)) {};

        GLFWMouseButtonCallbackI mouseButtonCallback = (window, button, action, mods) ->
        {
            if (usingGUI) return;
            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT)
            {
                if (action == GLFW.GLFW_PRESS)
                {
                    isDragging = true;
                    double[] mousePos = getCursorPos(window);
                    lastMouseX = (float) mousePos[0];
                    lastMouseY = (float) mousePos[1];
                } else if (action == GLFW.GLFW_RELEASE) isDragging = false;
            }
        };
        try (GLFWMouseButtonCallback callback = GLFW.glfwSetMouseButtonCallback(window, mouseButtonCallback)) {};

        GLFWScrollCallbackI scrollCallback = (window, xoffset, yoffset) ->
        {
            if (!usingGUI) Camera.zoom((float) yoffset);
            updateOrthoProjection();
        };
        try (GLFWScrollCallback callback = GLFW.glfwSetScrollCallback(window, scrollCallback)) {};

        GLFWWindowSizeCallbackI resizeCallback = (window, width, height) -> updateOrthoProjection();
        try (GLFWWindowSizeCallback callback = GLFW.glfwSetWindowSizeCallback(window, resizeCallback)) {};

        ImGui.createContext();
        ImGui.setNextWindowSize(getWindowWidth(), getWindowHeight());
        ImGui.setNextWindowPos(150f, 150f);

        // Main loop
        while (!GLFW.glfwWindowShouldClose(window))
        {
            // Poll events
            GLFW.glfwPollEvents();

            if (currentGUI != null)
            {
                currentGUI.prepare();
                currentGUI.build();
            }

            // Clear the screen - Clear both color and depth buffers
            GL30.glClear(GL30.GL_COLOR_BUFFER_BIT | GL30.GL_DEPTH_BUFFER_BIT);

            // Update camera and projection
            Camera.update();
            updateOrthoProjection();

            // Load and render chunks
            ChunkManager.loadVisibleChunks(Camera.getXOffset(), Camera.getYOffset());
            ChunkManager.renderChunks(chunkRenderer);

            if (currentGUI != null)
                currentGUI.draw();

            // Swap buffers
            GLFW.glfwSwapBuffers(window);
        }

        // Clean up
        GLFW.glfwDestroyWindow(window);
        GLFW.glfwTerminate();
    }

    public static void updateOrthoProjection()
    {
        int shaderProgramId = chunkRenderer.getShaderProgramID();
        int projectionMatrixLocation = ShaderUtils.getUniformLocation(shaderProgramId, "u_ProjectionMatrix");
        int viewMatrixLocation = ShaderUtils.getUniformLocation(shaderProgramId, "u_ViewMatrix");

        float zoom = Camera.getZoom();
        int windowWidth = Main.getWindowWidth();
        int windowHeight = Main.getWindowHeight();

        // Calculate the aspect ratio
        float aspectRatio = (float) windowWidth / windowHeight;
        float zoomFactor = 1.0f / zoom;

        // Set the orthographic projection
        float left = -aspectRatio * zoomFactor;
        float right = aspectRatio * zoomFactor;
        float bottom = -zoomFactor;
        float top = zoomFactor;

        // Create the projection matrix
        Matrix4f projectionMatrix = new Matrix4f().ortho(left, right, bottom, top, -1.0f, 1.0f);
        Matrix4f viewMatrix = Camera.getViewMatrix();

        try (MemoryStack stack = MemoryStack.stackPush())
        {
            FloatBuffer projectionMatrixBuffer = stack.mallocFloat(16);
            FloatBuffer viewMatrixBuffer = stack.mallocFloat(16);
            projectionMatrix.get(projectionMatrixBuffer);
            viewMatrix.get(viewMatrixBuffer);

            GL30.glUseProgram(shaderProgramId);
            GL30.glUniformMatrix4fv(projectionMatrixLocation, false, projectionMatrixBuffer);
            GL30.glUniformMatrix4fv(viewMatrixLocation, false, viewMatrixBuffer);
            GL30.glUseProgram(0);
        }
    }

    public static double[] getCursorPos(long windowID) {
        DoubleBuffer posX = BufferUtils.createDoubleBuffer(1);
        DoubleBuffer posY = BufferUtils.createDoubleBuffer(1);
        GLFW.glfwGetCursorPos(windowID, posX, posY);
        return new double[] { posX.get(0), posY.get(0) };
    }
}
