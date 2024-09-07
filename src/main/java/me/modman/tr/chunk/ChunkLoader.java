package me.modman.tr.chunk;

import com.google.common.io.LittleEndianDataInputStream;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class ChunkLoader {

    private static byte[] chunkData; // This will hold the chunk data

    public static Chunk loadChunk(int chunkX, int chunkZ, int chunkSize) {
        File chunkFile = new File("chunks/chunk." + chunkX + "." + chunkZ + ".dat");
        if (!chunkFile.exists()) {
//            System.out.println("Chunk file does not exist: " + chunkX + "," + chunkZ);
            return null;
        }

        try (LittleEndianDataInputStream dis = new LittleEndianDataInputStream(Files.newInputStream(chunkFile.toPath())))
        {
            // Read chunk coordinates
            int startX = dis.readInt();
            int startZ = dis.readInt();

            if (startX != chunkX * chunkSize || startZ != chunkZ * chunkSize) {
                System.err.println("Chunk coordinates do not match. Expected: " + chunkX + "," + chunkZ + " but Received: " + startX + "," + startZ);
                return null;
            }

            // Allocate chunkData buffer
            Block[] chunkData = new Block[chunkSize * chunkSize]; // 5 bytes per block (1 height + 4 bytes for block type)

            for (int x = 0; x < chunkSize; x++) {
                for (int z = 0; z < chunkSize; z++) {
                    int index = (x + z * chunkSize);

                    byte height = dis.readByte();
                    byte blockTypeCode = dis.readByte();
                    byte data = dis.readByte();

                    chunkData[index] = new Block(blockTypeCode, data, height);
                }
            }

            System.out.println("Loaded chunk " + chunkX + "," + chunkZ);

            return new Chunk(chunkData);
        } catch (IOException e) {
            System.err.println("Error while loading chunk (" + chunkX + "," + chunkZ + "): " + e.getMessage());
        }

        return null;
    }
}


