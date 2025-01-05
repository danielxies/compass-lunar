package me.meredith.command;

import me.meredith.WeaveFks;
import net.weavemc.loader.api.command.Command;
import org.jetbrains.annotations.NotNull;

public class CompassScaleCommand extends Command {
    public CompassScaleCommand() {
        super("compassscale");
    }

    @Override
    public void handle(@NotNull String[] args) {
        WeaveFks weavefks = WeaveFks.getInstance();

        if (args.length != 1) {
            return;
        }

        double scalePercentage;

        try {
            scalePercentage = Double.parseDouble(args[0]);
        } catch (NumberFormatException exception) {
            exception.printStackTrace();
            return;
        }

        if (scalePercentage < 1) {
            return;
        }

        weavefks.getConfig().compassHudScale = scalePercentage;
        weavefks.saveConfig();

        try {
            String output = "Set compass HUD scale to " + scalePercentage + "%";
            weavefks.addChatComponentText(output);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
