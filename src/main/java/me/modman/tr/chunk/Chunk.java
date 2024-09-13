package me.modman.tr.chunk;

import me.modman.tr.util.ColorHelper;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL30;

import java.nio.FloatBuffer;

public class Chunk
{
    private static final int CHUNK_SIZE = 16;
    private final int VERTICES_PER_QUAD = 6; // 6 vertices per quad (2 triangles, 3 vertices per triangle)
    private final int FLOATS_PER_VERTEX = 5; // x, y, r, g, b
    private Block[] chunkData;
    private FloatBuffer vertexBuffer; // Store the vertex buffer for this chunk
    private int vboID;
    private int chunkX, chunkZ;

    public Chunk(int chunkX, int chunkZ, Block[] chunkData)
    {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.chunkData = chunkData;
        this.vertexBuffer = BufferUtils.createFloatBuffer(CHUNK_SIZE * CHUNK_SIZE * VERTICES_PER_QUAD * (FLOATS_PER_VERTEX + 1));
        this.vboID = GL30.glGenBuffers();
        updateMesh(); // Generate the mesh when the chunk is created
    }

    public int getVboID()
    {
        return vboID;
    }

    public Block[] getChunkData()
    {
        return chunkData;
    }

    public Block getBlockAt(int x, int z)
    {
        return chunkData[x + z * CHUNK_SIZE];
    }

    public FloatBuffer getVertexBuffer()
    {
        return vertexBuffer;
    }

    // Update the vertex buffer (e.g., when chunk data changes)
    public void updateMesh()
    {
        vertexBuffer.clear(); // Clear the existing data

        float baseBlockSize = 1.0f / CHUNK_SIZE;

        ColorHelper colorHelper = new ColorHelper();

        for (int z = 0; z < CHUNK_SIZE; z++)
        {
            for (int x = 0; x < CHUNK_SIZE; x++)
            {
                int blockIndex = x + z * CHUNK_SIZE;
                Block block = chunkData[blockIndex];
                byte blockID = block.getID();
                byte blockData = block.getData();
                byte blockHeight = block.getHeight();
                float[] color = colorHelper.set(blockID, blockData, x * CHUNK_SIZE, z * CHUNK_SIZE, blockHeight)
                        .noise().linearInterpolation().specularLight().getFinalColor();

                float blockX = x * baseBlockSize;
                float blockY = z * baseBlockSize;
                float blockEndX = blockX + baseBlockSize;
                float blockEndY = blockY + baseBlockSize;

                // Fill vertex buffer directly
                addQuadToBuffer(vertexBuffer, blockX, blockY, blockEndX, blockEndY, color, blockID);
            }
        }

        vertexBuffer.flip(); // Prepare the buffer for reading

        // Bind and update the VBO data
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, vboID);
        GL30.glBufferData(GL30.GL_ARRAY_BUFFER, vertexBuffer, GL30.GL_DYNAMIC_DRAW);
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, 0); // Unbind VBO
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

    public int getChunkX()
    {
        return chunkX;
    }

    public int getChunkZ()
    {
        return chunkZ;
    }
}
