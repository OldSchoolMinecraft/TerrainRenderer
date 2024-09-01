package me.modman.tr;

import org.joml.Matrix4f;

public class Camera
{
    private static float xOffset = 0;
    private static float yOffset = 0;
    private static float zoom = 1.0f;  // Start with default zoom

    private static final float ZOOM_SENSITIVITY = 0.1f;
    private static final float MIN_ZOOM = 0.1f;
    private static final float PAN_SENSITIVITY = 0.05f;

    public static void pan(float deltaX, float deltaY)
    {
        // Adjust pan direction based on zoom
        xOffset += deltaX * PAN_SENSITIVITY / zoom;
        yOffset -= deltaY * PAN_SENSITIVITY / zoom;  // Inverted Y-axis for OpenGL coordinate system
    }

    public static void zoom(float deltaZoom)
    {
        float newZoom = zoom + (deltaZoom * ZOOM_SENSITIVITY);
        if (newZoom < MIN_ZOOM) newZoom = MIN_ZOOM;
        zoom = newZoom;
    }

    public static float getXOffset()
    {
        return xOffset;
    }

    public static float getYOffset()
    {
        return yOffset;
    }

    public static float getZoom()
    {
        return zoom;
    }

    public static Matrix4f getViewMatrix() {
        Matrix4f viewMatrix = new Matrix4f();
        viewMatrix.translate(-xOffset, -yOffset, 0); // Apply translation based on camera offset
        return viewMatrix;
    }
}