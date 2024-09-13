package me.modman.tr.gui;

import imgui.ImGui;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import me.modman.tr.Main;

public abstract class SimpleGUI
{
    private static final ImGuiImplGl3 imguiGL3 = new ImGuiImplGl3();
    private static final ImGuiImplGlfw imguiGLFW = new ImGuiImplGlfw();
    private static boolean initialized = false;
    protected String title;

    public SimpleGUI(String title)
    {
        this.title = title;
    }

    public void init()
    {
        imguiGL3.init();
        imguiGLFW.init(Main.getWindowID(), false);
        initialized = true;
    }

    public void prepare()
    {
        imguiGLFW.newFrame();
        ImGui.newFrame();
    }

    public abstract void build();

    public void draw()
    {
        ImGui.render();
        imguiGL3.renderDrawData(ImGui.getDrawData());
    }
}
