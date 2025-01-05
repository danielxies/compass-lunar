package me.meredith.command;

import me.meredith.WeaveFks;
import net.weavemc.loader.api.command.Command;
import org.jetbrains.annotations.NotNull;

public class EnergyHudToggleCommand extends Command {
    public EnergyHudToggleCommand() {
        super("energy");
    }

    @Override
    public void handle(@NotNull String[] args) {
        WeaveFks weavefks = WeaveFks.getInstance();

        boolean currentSetting = weavefks.getConfig().displayEnergyHud;
        weavefks.getConfig().displayEnergyHud = !currentSetting;

        weavefks.saveConfig();

        String status = weavefks.getConfig().displayEnergyHud ? "enabled" : "disabled";
        try {
            String output = "Energy HUD " + status;
            weavefks.addChatComponentText(output);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
