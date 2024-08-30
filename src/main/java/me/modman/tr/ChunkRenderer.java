package me.modman.tr;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

public class ChunkRenderer {
    private static int vaoId;
    private static int vboId;
    private static int shaderProgramId;
    private static final int VERTICES_PER_QUAD = 4;
    private static final int FLOATS_PER_VERTEX = 5; // x, y, r, g, b
    private static float scaleFactor = 1.0f;

    public static void setScaleFactor(float scale) {
        scaleFactor = scale;
    }


    public static void init() {
        // Create VAO
        vaoId = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vaoId);

        // Create VBO
        vboId = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, 16 * 16 * VERTICES_PER_QUAD * FLOATS_PER_VERTEX * Float.BYTES, GL15.GL_DYNAMIC_DRAW);

        // Define the structure of our vertex data
        GL20.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, FLOATS_PER_VERTEX * Float.BYTES, 0); // Position (x, y)
        GL20.glVertexAttribPointer(1, 3, GL11.GL_FLOAT, false, FLOATS_PER_VERTEX * Float.BYTES, 2 * Float.BYTES); // Color (r, g, b)
        GL20.glEnableVertexAttribArray(0);
        GL20.glEnableVertexAttribArray(1);

        // Unbind VAO
        GL30.glBindVertexArray(0);

        // Create Shader Program
        shaderProgramId = ShaderUtils.createShaderProgram("vertexShader.glsl", "fragmentShader.glsl");
        if (shaderProgramId == -1) {
            System.err.println("Failed to create shader program");
        } else {
            System.out.println("Shader program created successfully, ID: " + shaderProgramId);
        }

    }

    public static void renderChunk(byte[] chunkData, int chunkSize, int chunkX, int chunkZ) {
        if (chunkData == null) {
            return; // No data to render
        }

        // Calculate chunk position based on its coordinates
        int posX = chunkX * chunkSize;
        int posZ = chunkZ * chunkSize;

        // Adjust for camera position
        float adjustedX = posX - Camera.getXOffset();
        float adjustedY = posZ - Camera.getYOffset();

        // Prepare the vertex data
        float[] vertexData = new float[16 * 16 * VERTICES_PER_QUAD * FLOATS_PER_VERTEX];
        int index = 0;
        int blockSize = chunkSize / 16; // Size of each block
//        float blockSize = 0.01f;

        for (int z = 0; z < 16; z++) {
            for (int x = 0; x < 16; x++) {
                int blockIndex = x + z * 16; // Compute index based on chunk size of 16x16
                byte blockID = chunkData[blockIndex];
                float[] color = BlockColor.getColor(blockID);

                float blockX = adjustedX + x * blockSize;
                float blockY = adjustedY + z * blockSize;

                // Apply scale factor consistently
                float scaledBlockSize = blockSize;

                // Vertex 1
                vertexData[index++] = blockX;
                vertexData[index++] = blockY;
                vertexData[index++] = color[0];
                vertexData[index++] = color[1];
                vertexData[index++] = color[2];

                // Vertex 2
                vertexData[index++] = blockX + scaledBlockSize;
                vertexData[index++] = blockY;
                vertexData[index++] = color[0];
                vertexData[index++] = color[1];
                vertexData[index++] = color[2];

                // Vertex 3
                vertexData[index++] = blockX + scaledBlockSize;
                vertexData[index++] = blockY + scaledBlockSize;
                vertexData[index++] = color[0];
                vertexData[index++] = color[1];
                vertexData[index++] = color[2];

                // Vertex 4
                vertexData[index++] = blockX;
                vertexData[index++] = blockY + scaledBlockSize;
                vertexData[index++] = color[0];
                vertexData[index++] = color[1];
                vertexData[index++] = color[2];
            }
        }

        // Bind VAO and upload vertex data to VBO
        GL30.glBindVertexArray(vaoId);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
        GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0, vertexData);

        // Use shader program and draw the quads
        GL20.glUseProgram(shaderProgramId);
        GL11.glDrawArrays(GL11.GL_QUADS, 0, 16 * 16 * VERTICES_PER_QUAD);
        GL20.glUseProgram(0);

        // Unbind VAO
        GL30.glBindVertexArray(0);
    }

}
