package me.modman.tr.chunk;

public class Chunk
{
    private static final int CHUNK_SIZE = 16;
    private Block[] chunkData;

    public Chunk(Block[] chunkData)
    {
        this.chunkData = chunkData;
    }

    public Block[] getChunkData()
    {
        return chunkData;
    }

    public Block getBlockAt(int x, int z)
    {
        return chunkData[x + z * CHUNK_SIZE];
    }
}
