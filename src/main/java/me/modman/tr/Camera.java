package me.modman.tr;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Camera {
    private static float xOffset = 0.0f;
    private static float yOffset = 0.0f;
    private static float zoom = 1.0f;

    // Sensitivity multipliers
    private static float panSpeed = 0.003f;   // Reduce this to slow down panning
    private static float zoomSpeed = 0.05f; // Reduce this to slow down zooming

    private static final Matrix4f viewMatrix = new Matrix4f();
    private static final Matrix4f projectionMatrix = new Matrix4f();

    public static void update() {
        viewMatrix.identity();
        viewMatrix.translate(-xOffset, -yOffset, 0);
        viewMatrix.scale(zoom);
    }

    public static void pan(float deltaX, float deltaY) {
        // Apply panning speed
        xOffset -= deltaX * panSpeed;
        yOffset += deltaY * panSpeed;
        update();
    }

    public static void zoom(float deltaZoom) {
        // Apply zoom speed
        zoom += deltaZoom * zoomSpeed;

        // Prevent zoom from becoming negative or zero
        if (zoom < 0.01f) {
            zoom = 0.01f;
        }
        update();
    }

    public static Matrix4f getViewMatrix() {
        return viewMatrix;
    }

    public static Matrix4f getProjectionMatrix() {
        return projectionMatrix;
    }

    public static float getXOffset() {
        return xOffset;
    }

    public static float getYOffset() {
        return yOffset;
    }

    public static Vector3f getPosition()
    {
        return new Vector3f(xOffset, 0, yOffset);
    }

    public static float getZoom() {
        return zoom;
    }

    // Optionally, methods to set speed factors dynamically
    public static void setPanSpeed(float speed) {
        panSpeed = speed;
    }

    public static void setZoomSpeed(float speed) {
        zoomSpeed = speed;
    }
}
