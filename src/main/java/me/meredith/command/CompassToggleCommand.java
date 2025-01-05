package me.meredith.command;

import me.meredith.WeaveFks;
import net.weavemc.loader.api.command.Command;
import org.jetbrains.annotations.NotNull;

public class CompassToggleCommand extends Command {
    public CompassToggleCommand() {
        super("compasstoggle");
    }

    @Override
    public void handle(@NotNull String[] args) {
        WeaveFks weavefks = WeaveFks.getInstance();

        boolean currentState = weavefks.getConfig().compassHudAlwaysOn;
        weavefks.getConfig().compassHudAlwaysOn = !currentState;  // Toggle the state
        weavefks.saveConfig();

        try {
            String output = "Compass HUD is now " + (weavefks.getConfig().compassHudAlwaysOn ? "ON" : "OFF");
            weavefks.addChatComponentText(output);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
