package me.modman.tr;

import org.lwjgl.opengl.GL11;

public class Camera {
    private static float xOffset = 0;
    private static float yOffset = 0;
    private static float zoom = 0.05f;
    private static final float ZOOM_SENSITIVITY = 0.005f;
    private static final float MIN_ZOOM = 0.01f;
    private static final float PAN_SENSITIVITY = 0.003f; // Adjust this value as needed

    public static void update() {
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();
        GL11.glTranslatef(xOffset, yOffset, 0);
        GL11.glScaled(zoom, zoom, 1);
    }

    public static void pan(float deltaX, float deltaY) {
        // Fixed pan speed, independent of zoom
        float panSpeed = PAN_SENSITIVITY;

        // Adjust the x and y offsets
        xOffset -= deltaX * panSpeed / zoom; // Move left/right
        yOffset += deltaY * panSpeed / zoom; // Move up/down
    }

    static double scale = 1.0;

    public static void zoom(float deltaZoom) {
        float newZoom = zoom + (deltaZoom * ZOOM_SENSITIVITY);
        if (newZoom < MIN_ZOOM) newZoom = MIN_ZOOM;
        zoom = newZoom;
    }

    public static float getXOffset() {
        return xOffset;
    }

    public static float getYOffset() {
        return yOffset;
    }

    public static float getZoom() {
        return zoom;
    }
}

