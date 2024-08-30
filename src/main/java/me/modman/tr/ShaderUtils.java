package me.modman.tr;

import org.lwjgl.opengl.GL20;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;

public class ShaderUtils {

    public static int createShaderProgram(String vertexShaderPath, String fragmentShaderPath) {
        // Load and compile the vertex shader
        int vertexShaderId = compileShader(vertexShaderPath, GL20.GL_VERTEX_SHADER);
        // Load and compile the fragment shader
        int fragmentShaderId = compileShader(fragmentShaderPath, GL20.GL_FRAGMENT_SHADER);

        // Create a new shader program
        int programId = GL20.glCreateProgram();
        GL20.glAttachShader(programId, vertexShaderId);
        GL20.glAttachShader(programId, fragmentShaderId);
        GL20.glLinkProgram(programId);

        // Check for linking errors
        if (GL20.glGetProgrami(programId, GL20.GL_LINK_STATUS) == GL20.GL_FALSE) {
            System.err.println("Error: Shader program linking failed.");
            System.err.println(GL20.glGetProgramInfoLog(programId));
            return -1;
        }

        // Clean up shaders (they are no longer needed once linked into the program)
        GL20.glDeleteShader(vertexShaderId);
        GL20.glDeleteShader(fragmentShaderId);

        return programId;
    }

    private static int compileShader(String shaderPath, int shaderType) {
        // Load shader source from file
        String shaderSource = readFile(shaderPath);

        // Create a new shader
        int shaderId = GL20.glCreateShader(shaderType);
        GL20.glShaderSource(shaderId, shaderSource);
        GL20.glCompileShader(shaderId);

        // Check for compilation errors
        if (GL20.glGetShaderi(shaderId, GL20.GL_COMPILE_STATUS) == GL20.GL_FALSE) {
            System.err.println("Error: Shader compilation failed for " + shaderPath);
            System.err.println(GL20.glGetShaderInfoLog(shaderId));
            return -1;
        }

        return shaderId;
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
