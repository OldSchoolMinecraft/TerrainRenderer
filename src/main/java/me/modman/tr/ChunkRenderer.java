package me.modman.tr;

import org.lwjgl.opengl.GL30;

public class ChunkRenderer
{
    private int vaoId;
    private int vboId;
    private int shaderProgramId;
    private final int VERTICES_PER_QUAD = 6; // 6 vertices per quad (2 triangles, 3 vertices per triangle)
    private final int FLOATS_PER_VERTEX = 5; // x, y, r, g, b

    public void init()
    {
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
        GL30.glVertexAttribPointer(2, 1, GL30.GL_FLOAT, false, (FLOATS_PER_VERTEX + 1) * Float.BYTES, 5 * Float.BYTES); // Height
        GL30.glEnableVertexAttribArray(0);
        GL30.glEnableVertexAttribArray(1);
        GL30.glEnableVertexAttribArray(2);

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

    public int getShaderProgramID()
    {
        return shaderProgramId;
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
                float[] color = BlockColor.getColor(blockID, blockData, blockHeight); // Get the color for this block type

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
                vertexData[index++] = height;

                vertexData[index++] = blockEndX;
                vertexData[index++] = blockY;
                vertexData[index++] = color[0];
                vertexData[index++] = color[1];
                vertexData[index++] = color[2];
                vertexData[index++] = height;

                vertexData[index++] = blockX;
                vertexData[index++] = blockEndY;
                vertexData[index++] = color[0];
                vertexData[index++] = color[1];
                vertexData[index++] = color[2];
                vertexData[index++] = height;

                // Triangle 2
                vertexData[index++] = blockEndX;
                vertexData[index++] = blockY;
                vertexData[index++] = color[0];
                vertexData[index++] = color[1];
                vertexData[index++] = color[2];
                vertexData[index++] = height;

                vertexData[index++] = blockEndX;
                vertexData[index++] = blockEndY;
                vertexData[index++] = color[0];
                vertexData[index++] = color[1];
                vertexData[index++] = color[2];
                vertexData[index++] = height;

                vertexData[index++] = blockX;
                vertexData[index++] = blockEndY;
                vertexData[index++] = color[0];
                vertexData[index++] = color[1];
                vertexData[index++] = color[2];
                vertexData[index++] = height;
            }
        }

        int heightMapTextureID = ShaderUtils.generateHeightMapTexture(chunk);
        GL30.glActiveTexture(GL30.GL_TEXTURE0);
        GL30.glBindTexture(GL30.GL_TEXTURE_2D, heightMapTextureID);

        // Bind VAO and upload vertex data to VBO
        GL30.glBindVertexArray(vaoId);
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, vboId);

        int error = GL30.glGetError();
        if (error != GL30.GL_NO_ERROR) System.err.println("OpenGL Error before buffer update: " + error);

        GL30.glBufferSubData(GL30.GL_ARRAY_BUFFER, 0, vertexData);

        error = GL30.glGetError();
        if (error != GL30.GL_NO_ERROR) System.err.println("OpenGL Error before buffer update: " + error);

        // Use shader program and set uniform matrices
        GL30.glUseProgram(shaderProgramId);
        setShaderUniforms(); // Function to set the camera matrices in the shader

        int textureUniformLocation = GL30.glGetUniformLocation(shaderProgramId, "u_HeightmapTexture");
        GL30.glUniform1i(textureUniformLocation, 0); // Texture unit 0

        error = GL30.glGetError();
        if (error != GL30.GL_NO_ERROR) System.err.println("OpenGL Error before buffer update: " + error);

        GL30.glDrawArrays(GL30.GL_TRIANGLES, 0, 16 * 16 * VERTICES_PER_QUAD);

        error = GL30.glGetError();
        if (error != GL30.GL_NO_ERROR) System.err.println("OpenGL Error before buffer update: " + error);

        GL30.glUseProgram(0);

        // Unbind VAO
        GL30.glBindVertexArray(0);
    }

    private void setShaderUniforms()
    {
        int viewMatrixLocation = GL30.glGetUniformLocation(shaderProgramId, "u_ViewMatrix");
        int projectionMatrixLocation = GL30.glGetUniformLocation(shaderProgramId, "u_ProjectionMatrix");

        // Set the matrices from the Camera
        GL30.glUniformMatrix4fv(viewMatrixLocation, false, Camera.getViewMatrix().get(new float[16]));
        GL30.glUniformMatrix4fv(projectionMatrixLocation, false, Camera.getProjectionMatrix().get(new float[16]));
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
        GL30.glUseProgram(shaderProgramId);
        GL30.glDrawArrays(GL30.GL_TRIANGLES, 0, 6);
        GL30.glUseProgram(0);

        // Unbind VAO
        GL30.glBindVertexArray(0);
    }
}
