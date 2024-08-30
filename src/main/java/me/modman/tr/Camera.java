package me.modman.tr;

import org.lwjgl.opengl.GL11;

public class Camera {
    private static float xOffset = 0;
    private static float yOffset = 0;
    private static float zoom = 1.0f;
    private static final float ZOOM_SENSITIVITY = 0.1f;
    private static final float MIN_ZOOM = 0.1f;
    private static final float PAN_SENSITIVITY = 0.1f; // Adjust this value as needed

    public static void update() {
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();
        GL11.glTranslatef(xOffset, yOffset, 0);
        GL11.glScalef(zoom, zoom, 1);
    }

    public static void pan(float deltaX, float deltaY) {
        float panSpeed = PAN_SENSITIVITY / zoom;
        xOffset -= deltaX * panSpeed; // Adjust to match zooming direction
        yOffset += deltaY * panSpeed; // Adjust to match zooming direction
    }

    public static void zoom(float deltaZoom) {
        zoom += deltaZoom * ZOOM_SENSITIVITY;
        if (zoom < MIN_ZOOM) zoom = MIN_ZOOM; // Constrain zoom to minimum value
        ChunkRenderer.setScaleFactor(zoom); // Update scale factor for rendering
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

