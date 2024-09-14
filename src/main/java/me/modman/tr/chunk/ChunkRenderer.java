package me.modman.tr.chunk;

import me.modman.tr.*;
import me.modman.tr.reis.BlockColor;
import me.modman.tr.reis.Environment;
import me.modman.tr.reis.PixelColor;
import me.modman.tr.reis.TintType;
import me.modman.tr.util.Camera;
import me.modman.tr.util.ColorHelper;
import me.modman.tr.util.PixelColorRedux;
import me.modman.tr.util.ShaderUtils;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL30;

import java.nio.FloatBuffer;

public class ChunkRenderer
{
    private int vaoId;
    private int vboId;
    private int defaultShaderProgramID;
    private int waterShaderProgramID;
    private final int VERTICES_PER_QUAD = 6; // 6 vertices per quad (2 triangles, 3 vertices per triangle)
    private final int FLOATS_PER_VERTEX = 5; // x, y, r, g, b
    private long initMS;
    private float[] lightBrightnessTable = this.generateLightBrightnessTable(2.0f / 16.0f);

    public void init()
    {
        initMS = System.currentTimeMillis();

        // Create VAO
        vaoId = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vaoId);

        // Create VBO
        vboId = GL30.glGenBuffers();
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, vboId);
        GL30.glBufferData(GL30.GL_ARRAY_BUFFER, 16 * 16 * VERTICES_PER_QUAD * (FLOATS_PER_VERTEX + 1) * Float.BYTES, GL30.GL_DYNAMIC_DRAW);

        // Define the structure of our vertex data
        GL30.glVertexAttribPointer(0, 2, GL30.GL_FLOAT, false, (FLOATS_PER_VERTEX + 1) * Float.BYTES, 0); // Position (x, y)
        GL30.glVertexAttribPointer(1, 3, GL30.GL_FLOAT, false, (FLOATS_PER_VERTEX + 1) * Float.BYTES, 2 * Float.BYTES); // Color (r, g, b)
        GL30.glVertexAttribPointer(2, 1, GL30.GL_FLOAT, false, (FLOATS_PER_VERTEX + 1) * Float.BYTES, 5 * Float.BYTES); // block ID
        GL30.glEnableVertexAttribArray(0);
        GL30.glEnableVertexAttribArray(1);
        GL30.glEnableVertexAttribArray(2);

        // Unbind VAO
        GL30.glBindVertexArray(0);

        // Create Shader Program
        defaultShaderProgramID = ShaderUtils.createShaderProgram("vertexShader.glsl", "fragmentShader.glsl");
        if (defaultShaderProgramID == -1) {
            System.err.println("Failed to create shader program");
        } else {
            System.out.println("Shader program created successfully, ID: " + defaultShaderProgramID);
        }
    }

    public int getShaderProgramID()
    {
        return defaultShaderProgramID;
    }

    public void renderChunk(Chunk chunk, int chunkSize, int chunkX, int chunkZ)
    {
        if (chunk == null) return; // no data
        Block[] chunkData = chunk.getChunkData();
        if (chunkData == null)
            return; // No data to render

        // Get the zoom factor (if needed for scaling purposes)
        float zoom = Camera.getZoom();

        float aspectRatio = (float) Main.getWindowWidth() / Main.getWindowHeight();

        // Calculate the block size relative to the zoom level
        float baseBlockSize = 1.0f / 16.0f; // Base block size assuming a unit size per chunk (normalized)
        float blockSize = (baseBlockSize * zoom);

        // Calculate the chunk's world position
        float chunkWorldX = chunkX * chunkSize * blockSize;
        float chunkWorldZ = chunkZ * chunkSize * blockSize;

        // Prepare the vertex data array
        float[] vertexData = new float[16 * 16 * VERTICES_PER_QUAD * (FLOATS_PER_VERTEX + 1)];
        int index = 0;

        for (int z = 0; z < 16; z++)
        {
            for (int x = 0; x < 16; x++)
            {
                int blockIndex = x + z * 16; // Calculate index for the block in chunkData
                byte blockID = chunkData[blockIndex].getID();
                byte blockData = chunkData[blockIndex].getData();
                byte blockHeight = chunkData[blockIndex].getHeight();
//                float[] color = BlockColor.getColor(blockID, blockData, blockHeight); // Get the color for this block type
                float[] color = new ColorHelper(blockID, blockData, x * chunkSize, z * chunkSize, blockHeight).noise().linearInterpolation().specularLight().getFinalColor();

                // Calculate the block's position in the world
                float blockX = (chunkWorldX + (x * blockSize)) / aspectRatio;
                float blockY = (chunkWorldZ + (z * blockSize));

                // Ensure that the block is drawn as a square
                float blockEndX = blockX + (blockSize * aspectRatio);
                float blockEndY = blockY + blockSize;

                float height = (float) blockHeight / 255f;

                // Two triangles for each quad
                // Triangle 1
                vertexData[index++] = blockX;
                vertexData[index++] = blockY;
                vertexData[index++] = color[0];
                vertexData[index++] = color[1];
                vertexData[index++] = color[2];
                vertexData[index++] = blockID;

                vertexData[index++] = blockEndX;
                vertexData[index++] = blockY;
                vertexData[index++] = color[0];
                vertexData[index++] = color[1];
                vertexData[index++] = color[2];
                vertexData[index++] = blockID;

                vertexData[index++] = blockX;
                vertexData[index++] = blockEndY;
                vertexData[index++] = color[0];
                vertexData[index++] = color[1];
                vertexData[index++] = color[2];
                vertexData[index++] = blockID;

                // Triangle 2
                vertexData[index++] = blockEndX;
                vertexData[index++] = blockY;
                vertexData[index++] = color[0];
                vertexData[index++] = color[1];
                vertexData[index++] = color[2];
                vertexData[index++] = blockID;

                vertexData[index++] = blockEndX;
                vertexData[index++] = blockEndY;
                vertexData[index++] = color[0];
                vertexData[index++] = color[1];
                vertexData[index++] = color[2];
                vertexData[index++] = blockID;

                vertexData[index++] = blockX;
                vertexData[index++] = blockEndY;
                vertexData[index++] = color[0];
                vertexData[index++] = color[1];
                vertexData[index++] = color[2];
                vertexData[index++] = blockID;
            }
        }

//        int error = GL30.glGetError();
//        if (error != GL30.GL_NO_ERROR) System.err.println("OpenGL Error before buffer update: " + error);

        // Bind VAO and upload vertex data to VBO
        GL30.glBindVertexArray(vaoId);
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, vboId);

//        error = GL30.glGetError();
//        if (error != GL30.GL_NO_ERROR) System.err.println("OpenGL Error before buffer update: " + error);

        GL30.glBufferSubData(GL30.GL_ARRAY_BUFFER, 0, vertexData);

//        error = GL30.glGetError();
//        if (error != GL30.GL_NO_ERROR) System.err.println("OpenGL Error before buffer update: " + error);

        // Use shader program and set uniform matrices
        GL30.glUseProgram(defaultShaderProgramID);
        setShaderUniforms(); // Function to set the camera matrices in the shader

        // Generate a unique offset for the chunk
        float chunkOffsetX = (float) (chunkX * 0.1); // Adjust the multiplier for the desired effect
        float chunkOffsetY = (float) (chunkZ * 0.1); // Adjust the multiplier for the desired effect

        int chunkOffsetMatrixLocation = GL30.glGetUniformLocation(defaultShaderProgramID, "u_ChunkOffset");
        GL30.glUniform2f(chunkOffsetMatrixLocation, chunkOffsetX, chunkOffsetY);
        int chunkSeedMatrixLocation = GL30.glGetUniformLocation(defaultShaderProgramID, "u_ChunkSeed");
        GL30.glUniform1f(chunkSeedMatrixLocation, chunkX * 1000f * chunkZ);

//        error = GL30.glGetError();
//        if (error != GL30.GL_NO_ERROR) System.err.println("OpenGL Error before buffer update: " + error);

        GL30.glDrawArrays(GL30.GL_TRIANGLES, 0, 16 * 16 * VERTICES_PER_QUAD);

//        error = GL30.glGetError();
//        if (error != GL30.GL_NO_ERROR) System.err.println("OpenGL Error before buffer update: " + error);

        GL30.glUseProgram(0);

        // Unbind VAO
        GL30.glBindVertexArray(0);
    }

    private void setShaderUniforms()
    {
        int viewMatrixLocation = GL30.glGetUniformLocation(defaultShaderProgramID, "u_ViewMatrix");
        int projectionMatrixLocation = GL30.glGetUniformLocation(defaultShaderProgramID, "u_ProjectionMatrix");
        int timeMatrixLocation = GL30.glGetUniformLocation(defaultShaderProgramID, "u_Time");
        int resolutionMatrixLocation = GL30.glGetUniformLocation(defaultShaderProgramID, "u_ScreenResolution");

        // Set the matrices from the Camera
        GL30.glUniformMatrix4fv(viewMatrixLocation, false, Camera.getViewMatrix().get(new float[16]));
        GL30.glUniformMatrix4fv(projectionMatrixLocation, false, Camera.getProjectionMatrix().get(new float[16]));
        GL30.glUniform1f(timeMatrixLocation, ((System.currentTimeMillis() - initMS) / 1000.0f));
        GL30.glUniform2f(resolutionMatrixLocation, Main.getWindowWidth(), Main.getWindowHeight());
    }

    public void renderChunk2(Chunk chunk, int chunkSize, int chunkX, int chunkZ)
    {
        if (chunk == null) return;
        Block[] chunkData = chunk.getChunkData();
        if (chunkData == null) return;

        float zoom = Camera.getZoom();
        float aspectRatio = (float) Main.getWindowWidth() / Main.getWindowHeight();
        float baseBlockSize = 1.0f / 16.0f;
        float blockSize = (baseBlockSize * zoom);
        float chunkWorldX = chunkX * chunkSize * blockSize;
        float chunkWorldZ = chunkZ * chunkSize * blockSize;

        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(16 * 16 * VERTICES_PER_QUAD * (FLOATS_PER_VERTEX + 1));

        // Precreate ColorHelper instance if possible
        ColorHelper colorHelper = new ColorHelper();
        PixelColorRedux pixelColor = new PixelColorRedux();

        for (int z = 0; z < 16; z++)
        {
            for (int x = 0; x < 16; x++)
            {
                int blockIndex = x + z * 16;
                Block block = chunkData[blockIndex];
                byte blockID = block.getID();
                byte blockData = block.getData();
                byte blockHeight = block.getHeight();

                BlockColor blockColor = BlockColor.getBlockColor(blockID, blockData);

                // Convert to pixel color
                int r = (int) (blockColor.red * 255);
                int g = (int) (blockColor.green * 255);
                int b = (int) (blockColor.blue * 255);
                int a = (int) (blockColor.alpha * 255);


                compositeColor(chunk, x, blockHeight, z, blockData, pixelColor, blockColor, blockColor.tintType);
//                pixelColor.composite(blockColor.alpha, blockColor.red, blockColor.green, blockColor.blue);

//                float[] color = colorHelper.set(blockID, blockData, x * chunkSize, z * chunkSize, blockHeight)
//                        .noise().linearInterpolation().specularLight().getFinalColor();

                float[] color = new float[] { pixelColor.red, pixelColor.green, pixelColor.blue };

                float blockX = (chunkWorldX + (x * blockSize)) / aspectRatio;
                float blockY = (chunkWorldZ + (z * blockSize));
                float blockEndX = blockX + (blockSize * aspectRatio);
                float blockEndY = blockY + blockSize;

                // Fill vertex buffer directly
                addQuadToBuffer(vertexBuffer, blockX, blockY, blockEndX, blockEndY, color, blockID);
            }
        }

        vertexBuffer.flip();

        GL30.glBindVertexArray(vaoId);
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, vboId);
        GL30.glBufferData(GL30.GL_ARRAY_BUFFER, vertexBuffer, GL30.GL_DYNAMIC_DRAW); // Use GL_STATIC_DRAW or GL_DYNAMIC_DRAW based on needs

        GL30.glUseProgram(defaultShaderProgramID);
        setShaderUniforms();
        setChunkUniforms(chunkX, chunkZ);

        GL30.glDrawArrays(GL30.GL_TRIANGLES, 0, 16 * 16 * VERTICES_PER_QUAD);
        GL30.glUseProgram(0);
        GL30.glBindVertexArray(0);
    }

    private int lightmap = 1;
    private boolean environmentColor = false;
    private void compositeColor(Chunk chunk, int x, int y, int z, int metadata, PixelColorRedux pixel, BlockColor color, TintType tintType) {
        // If color has zero alpha and y is greater than 0, recursively call for the block below
        if (color.alpha == 0.0F && y > 0) {
            this.compositeColor(chunk, x, y - 1, z, metadata, pixel, color, color.tintType);
            return;
        }

        // Determine light value based on the current lightmap setting
        int lightValue = 0;
        switch (this.lightmap) {
            case 1:
                lightValue = (y < 127) ? Math.min(15, 10) : 15;
                break;
            case 2:
                lightValue = (y < 127) ? Math.min(15, 4) : 4;
                break;
            case 3:
                lightValue = 15;
                break;
            default:
                this.lightmap = 0;
        }

        // Get brightness from the light brightness table
        float lightBrightness = this.lightBrightnessTable[lightValue];

        // Apply environment color based on the tint type
        if (this.environmentColor) {
            int level;
            switch (color.tintType.ordinal()) {
                case 1: // Grass tint
                    level = Environment.getEnvironment(chunk, x, z).getGrassColor();
                    pixel.composite(color.alpha, level, lightBrightness * 0.6F);
                    return;
                case 2: // Foliage tint
                    level = Environment.getEnvironment(chunk, x, z).getFoliageColor();
                    pixel.composite(color.alpha, level, lightBrightness * 0.5F);
                    return;
                case 3: // Pine foliage tint
                    level = Environment.getEnvironment(chunk, x, z).getFoliageColorPine();
                    pixel.composite(color.alpha, level, lightBrightness * 0.5F);
                    return;
                case 4: // Birch foliage tint
                    level = Environment.getEnvironment(chunk, x, z).getFoliageColorBirch();
                    pixel.composite(color.alpha, level, lightBrightness * 0.5F);
                    return;
            }
        }

        // Apply special tint types (water and glass)
        if (color.tintType != TintType.WATER || tintType != TintType.WATER) {
            if (color.tintType != TintType.GLASS || tintType != TintType.GLASS) {
                if (color.tintType == TintType.REDSTONE) {
                    float level1 = (float) metadata * 0.06666667F;
                    float r = metadata == 0 ? 0.3F : level1 * 0.6F + 0.4F;
                    float g = Math.max(0.0F, level1 * level1 * 0.7F - 0.5F);
                    float b = 0.0F;
                    float a = color.alpha;
                    pixel.composite(a, r, g, b, lightBrightness);
                } else {
                    pixel.composite(color.alpha, color.red, color.green, color.blue, lightBrightness);
                }
            }
        }

        // Apply additional lighting effect based on the y-coordinate
        float factor = 0.25F;
        double red = (double) (y);
        float blue = (float) Math.log10(Math.abs(red) * 0.125D + 1.0D) * factor;
        if (red >= 0.0D) {
            pixel.red += blue * (1.0F - pixel.red);
            pixel.green += blue * (1.0F - pixel.green);
            pixel.blue += blue * (1.0F - pixel.blue);
        } else {
            pixel.red -= Math.abs(blue) * pixel.red;
            pixel.green -= Math.abs(blue) * pixel.green;
            pixel.blue -= Math.abs(blue) * pixel.blue;
        }
    }

    private float[] generateLightBrightnessTable(float f)
    {
        float[] result = new float[16];

        for (int i = 0; i <= 15; ++i)
        {
            float f1 = 1.0F - (float) i / 15.0F;
            result[i] = (1.0F - f1) / (f1 * 3.0F + 1.0F) * (1.0F - f) + f;
        }

        return result;
    }

    public void renderChunk3(Chunk chunk, int chunkX, int chunkZ)
    {
        if (chunk == null) return;
        FloatBuffer vertexBuffer = chunk.getVertexBuffer();
        if (vertexBuffer == null || vertexBuffer.capacity() == 0) return;

        GL30.glBindVertexArray(vaoId);
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, chunk.getVboID());
//        GL30.glBufferData(GL30.GL_ARRAY_BUFFER, vertexBuffer, GL30.GL_DYNAMIC_DRAW); // Upload the buffer data

        GL30.glUseProgram(defaultShaderProgramID);
        setShaderUniforms();
        setChunkUniforms(chunkX, chunkZ);

        GL30.glDrawArrays(GL30.GL_TRIANGLES, 0, 16 * 16 * VERTICES_PER_QUAD);
        GL30.glUseProgram(0);
        GL30.glBindVertexArray(0);
    }

    private void addQuadToBuffer(FloatBuffer buffer, float x1, float y1, float x2, float y2, float[] color, byte blockID)
    {
        // Add vertex data for two triangles (one quad)
        buffer.put(new float[]{x1, y1, color[0], color[1], color[2], blockID});
        buffer.put(new float[]{x2, y1, color[0], color[1], color[2], blockID});
        buffer.put(new float[]{x1, y2, color[0], color[1], color[2], blockID});

        buffer.put(new float[]{x2, y1, color[0], color[1], color[2], blockID});
        buffer.put(new float[]{x2, y2, color[0], color[1], color[2], blockID});
        buffer.put(new float[]{x1, y2, color[0], color[1], color[2], blockID});
    }

    private void setChunkUniforms(int chunkX, int chunkZ)
    {
        float chunkOffsetX = (float) (chunkX * 0.1);
        float chunkOffsetY = (float) (chunkZ * 0.1);
        int chunkOffsetMatrixLocation = GL30.glGetUniformLocation(defaultShaderProgramID, "u_ChunkOffset");
        GL30.glUniform2f(chunkOffsetMatrixLocation, chunkOffsetX, chunkOffsetY);
        int chunkSeedMatrixLocation = GL30.glGetUniformLocation(defaultShaderProgramID, "u_ChunkSeed");
        GL30.glUniform1f(chunkSeedMatrixLocation, chunkX * 1000f * chunkZ);
    }

    public void renderSimpleSquare(float centerX, float centerY, float size, float[] color) {
        // Calculate the vertices for a square centered at (centerX, centerY) with a given size
        float halfSize = size / 2.0f;
        float x0 = centerX - halfSize;
        float y0 = centerY - halfSize;
        float x1 = centerX + halfSize;
        float y1 = centerY + halfSize;

        // Prepare vertex data
        float[] vertexData = {
                x0, y0, color[0], color[1], color[2], // Bottom-left
                x1, y0, color[0], color[1], color[2], // Bottom-right
                x1, y1, color[0], color[1], color[2], // Top-right
                x0, y1, color[0], color[1], color[2]  // Top-left
        };

        // Bind VAO and upload vertex data to VBO
        GL30.glBindVertexArray(vaoId);
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, vboId);
        GL30.glBufferSubData(GL30.GL_ARRAY_BUFFER, 0, vertexData);

        // Use shader program and draw the square as two triangles
        GL30.glUseProgram(defaultShaderProgramID);
        GL30.glDrawArrays(GL30.GL_TRIANGLES, 0, 6);
        GL30.glUseProgram(0);

        // Unbind VAO
        GL30.glBindVertexArray(0);
    }
}
