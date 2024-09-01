package me.modman.tr;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL30;

import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;

public class ShaderUtils {

    public static int createShaderProgram(String vertexShaderPath, String fragmentShaderPath) {
        // Load and compile the vertex shader
        int vertexShaderId = compileShader(vertexShaderPath, GL30.GL_VERTEX_SHADER);
        // Load and compile the fragment shader
        int fragmentShaderId = compileShader(fragmentShaderPath, GL30.GL_FRAGMENT_SHADER);

        // Create a new shader program
        int programId = GL30.glCreateProgram();
        GL30.glAttachShader(programId, vertexShaderId);
        GL30.glAttachShader(programId, fragmentShaderId);
        GL30.glLinkProgram(programId);

        // Check for linking errors
        if (GL30.glGetProgrami(programId, GL30.GL_LINK_STATUS) == GL30.GL_FALSE) {
            System.err.println("Error: Shader program linking failed.");
            System.err.println(GL30.glGetProgramInfoLog(programId));
            return -1;
        }

        // Clean up shaders (they are no longer needed once linked into the program)
        GL30.glDeleteShader(vertexShaderId);
        GL30.glDeleteShader(fragmentShaderId);

        return programId;
    }

    private static int compileShader(String shaderPath, int shaderType) {
        // Load shader source from file
        String shaderSource = readFile(shaderPath);

        // Create a new shader
        int shaderId = GL30.glCreateShader(shaderType);
        GL30.glShaderSource(shaderId, shaderSource);
        GL30.glCompileShader(shaderId);

        // Check for compilation errors
        if (GL30.glGetShaderi(shaderId, GL30.GL_COMPILE_STATUS) == GL30.GL_FALSE) {
            System.err.println("Error: Shader compilation failed for " + shaderPath);
            System.err.println(GL30.glGetShaderInfoLog(shaderId));
            return -1;
        }

        return shaderId;
    }

    public static int getUniformLocation(int shaderProgramId, String uniformName)
    {
        // Ensure the shader program is currently active
        GL30.glUseProgram(shaderProgramId);

        // Get the uniform location
        int uniformLocation = GL30.glGetUniformLocation(shaderProgramId, uniformName);

        // Check if the location was successfully retrieved
        if (uniformLocation == -1)
            System.err.println("Warning: Uniform '" + uniformName + "' not found in shader program " + shaderProgramId);

        GL30.glUseProgram(0);

        return uniformLocation;
    }

    public static int generateHeightMapTexture(Chunk chunk)
    {
        if (chunk == null)
            throw new IllegalArgumentException("Chunk data cannot be null.");

        int chunkSize = 16;
        // Create a ByteBuffer to hold the height data
        ByteBuffer heightData = BufferUtils.createByteBuffer(chunkSize * chunkSize);

        // Extract height values from the chunk data
        for (int x = 0; x < chunkSize; x++)
        {
            for (int z = 0; z < chunkSize; z++)
            {
                int index = x + z * chunkSize;
                byte height = chunk.getBlockAt(x, z).getHeight(); // Assuming getBlock(index) returns the block at the specified index

                // Store the height value in the buffer
                heightData.put(height);
            }
        }
        heightData.flip(); // Prepare the buffer for reading by OpenGL

        // Generate a texture ID
        int textureID = GL30.glGenTextures();
        GL30.glBindTexture(GL30.GL_TEXTURE_2D, textureID);

        // Set texture parameters for scaling and wrapping
        GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MIN_FILTER, GL30.GL_LINEAR);
        GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MAG_FILTER, GL30.GL_LINEAR);
        GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_WRAP_S, GL30.GL_CLAMP_TO_EDGE);
        GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_WRAP_T, GL30.GL_CLAMP_TO_EDGE);

        // Upload the height data to the GPU as a texture
        GL30.glTexImage2D(GL30.GL_TEXTURE_2D, 0, GL30.GL_R8, chunkSize, chunkSize, 0, GL30.GL_RED, GL30.GL_UNSIGNED_BYTE, heightData);

        // Unbind the texture
        GL30.glBindTexture(GL30.GL_TEXTURE_2D, 0);

        return textureID; // Return the texture ID to use in your shaders
    }

    private static String readFile(String filePath) {
        try {
            return new String(Files.readAllBytes(Paths.get(filePath)));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
