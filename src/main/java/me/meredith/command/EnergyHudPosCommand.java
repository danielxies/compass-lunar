package me.meredith.command;

import me.meredith.WeaveFks;
import net.weavemc.loader.api.command.Command;
import org.jetbrains.annotations.NotNull;

public class EnergyHudPosCommand extends Command {
    public EnergyHudPosCommand() {
        super("energyhudpos");
    }

    @Override
    public void handle(@NotNull String[] args) {
        WeaveFks weavefks = WeaveFks.getInstance();

        if (args.length != 2) {
            return;
        }

        int x;
        int y;

        try {
            x = Integer.parseInt(args[0]);
            y = Integer.parseInt(args[1]);
        } catch (NumberFormatException exception) {
            exception.printStackTrace();
            return;
        }

        if (x < 0 || y < 0) {
            return;
        }

        weavefks.getConfig().energyHudX = x;
        weavefks.getConfig().energyHudY = y;

        weavefks.saveConfig();

        try {
            String output = "Set energy HUD position to X: " + x + ", Y: " + y;
            weavefks.addChatComponentText(output);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
