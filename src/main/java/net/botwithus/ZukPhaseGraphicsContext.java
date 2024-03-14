package net.botwithus;

import net.botwithus.rs3.imgui.ImGui;
import net.botwithus.rs3.imgui.ImGuiWindowFlag;
import net.botwithus.rs3.script.ScriptConsole;
import net.botwithus.rs3.script.ScriptGraphicsContext;

public class ZukPhaseGraphicsContext extends ScriptGraphicsContext {

    private ZukPhaseTick script;

    public ZukPhaseGraphicsContext(ScriptConsole scriptConsole, ZukPhaseTick script) {
        super(scriptConsole);
        this.script = script;
    }

    @Override
    public void drawSettings() {
        if (ImGui.Begin("Zuk Phase", ImGuiWindowFlag.None.getValue())) {
            if (ImGui.BeginTabBar("My bar", ImGuiWindowFlag.None.getValue())) {
                if (ImGui.BeginTabItem("Settings", ImGuiWindowFlag.None.getValue())) {
                    ImGui.Text("Welcome to Zuk Phase");
                    ImGui.Text("My scripts state is: " + script.getBotState());
                    if (ImGui.Button("Start")) {
                        //button has been clicked
                        script.setBotState(ZukPhaseTick.BotState.SKILLING);
                    }
                    ImGui.SameLine();
                    if (ImGui.Button("Stop")) {
                        //has been clicked
                        script.setBotState(ZukPhaseTick.BotState.IDLE);
                    }
                    ImGui.EndTabItem();
                }
                if (ImGui.BeginTabItem("Stats", ImGuiWindowFlag.None.getValue())) {
                    //script.setSomeBool(ImGui.Checkbox("Options to check", script.isSomeBool()));
                    ImGui.EndTabItem();
                }
                ImGui.EndTabBar();
            }
            ImGui.End();
        }

    }

    @Override
    public void drawOverlay() {
        super.drawOverlay();
    }
}
