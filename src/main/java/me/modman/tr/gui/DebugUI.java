package me.modman.tr.gui;

import imgui.ImGui;

public class DebugUI extends SimpleGUI
{
    public DebugUI()
    {
        super("Debug");
    }

    @Override
    public void build()
    {
        ImGui.begin(title);
        ImGui.text("This is a test!");
        ImGui.button("Useless Button");
        ImGui.checkbox("Useless Checkbox", false);
        ImGui.end();
    }
}
