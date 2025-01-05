package me.meredith.command;

import me.meredith.WeaveFks;
import net.weavemc.loader.api.command.Command;
import org.jetbrains.annotations.NotNull;

public class SetScaleCommand extends Command {
    public SetScaleCommand() {
        super("setscale");
    }

    @Override
    public void handle(@NotNull String[] args) {
        WeaveFks weavefks = WeaveFks.getInstance();

        if (args.length != 1) {
            return;
        }

        double scale;

        try {
            scale = Double.parseDouble(args[0]);
        } catch (NumberFormatException exception) {
            exception.printStackTrace();
            return;
        }

        if (scale <= 0) {
            return;
        }

        weavefks.getConfig().finalsCounterScale = scale;

        weavefks.saveConfig();

        try {
            String output = "Set scale to " + scale + "%";

            weavefks.addChatComponentText(output);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
