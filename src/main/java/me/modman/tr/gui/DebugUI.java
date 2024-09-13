package me.modman.tr.gui;

import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;
import me.modman.tr.Main;
import me.modman.tr.chunk.ChunkManager;

public class DebugUI extends SimpleGUI
{
    public DebugUI()
    {
        super("Debug");
    }

    @Override
    public void build()
    {
//        ImGui.begin(title);
        ImGui.setNextWindowPos(5, 5);
        ImGui.setNextWindowSize(150, 50);
        ImGui.begin(title, ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoScrollbar | ImGuiWindowFlags.NoSavedSettings | ImGuiWindowFlags.NoInputs);
//        ImGui.setNextWindowBgAlpha();
        ImGui.text("FPS: " + Main.getFPS());
        ImGui.text("Loaded Chunks: " + ChunkManager.getLoadedChunks().size());
        ImGui.end();
    }
}
