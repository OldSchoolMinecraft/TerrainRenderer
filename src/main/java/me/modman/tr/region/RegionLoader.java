package me.modman.tr.region;

import me.modman.tr.chunk.Block;
import me.modman.tr.chunk.Chunk;

import java.io.*;
import java.util.zip.GZIPInputStream;

public class RegionLoader
{
    private static final int CHUNK_SIZE = 16;
    private static final int REGION_SIZE = 16;
    private final File dataFolder;

    public RegionLoader(File dataFolder)
    {
        this.dataFolder = dataFolder;
    }

    public Chunk loadChunk(int chunkX, int chunkZ)
    {
        int regionX = (chunkX >> 5);
        int regionZ = (chunkZ >> 5);

        String regionFileName = "region." + regionX + "." + regionZ + ".dat";
        File regionFile = new File(dataFolder, regionFileName);

        if (!regionFile.exists())
        {
            System.out.println("Region file does not exist: " + regionX + "," + regionZ);
            return null;
        }

        try (GZIPInputStream gis = new GZIPInputStream(new FileInputStream(regionFile)); DataInputStream dis = new DataInputStream(gis))
        {
            // Calculate the starting position of the chunk in the region file
            int chunkOffsetX = (chunkX % REGION_SIZE) * CHUNK_SIZE;
            int chunkOffsetZ = (chunkZ % REGION_SIZE) * CHUNK_SIZE;
            int chunkStartIndex = (chunkOffsetX * REGION_SIZE * CHUNK_SIZE * 3) + (chunkOffsetZ * CHUNK_SIZE * 3);

            // Skip to the desired chunk's data position in the region file
            dis.skipBytes(chunkStartIndex);

            // Allocate chunkData buffer
            Block[] chunkData = new Block[CHUNK_SIZE * CHUNK_SIZE];

            for (int x = 0; x < CHUNK_SIZE; x++)
            {
                for (int z = 0; z < CHUNK_SIZE; z++)
                {
                    int index = (x + z * CHUNK_SIZE);

                    byte height = dis.readByte();
                    byte blockTypeCode = dis.readByte();
                    byte data = dis.readByte();

                    chunkData[index] = new Block(blockTypeCode, data, height);
                }
            }

            System.out.println("Loaded chunk " + chunkX + "," + chunkZ + " from region " + regionX + "," + regionZ);

            return new Chunk(chunkX, chunkZ, chunkData);
        } catch (IOException e) {
            System.err.println("Error while loading chunk (" + chunkX + "," + chunkZ + "): " + e.getMessage());
        }

        return null;
    }
}
