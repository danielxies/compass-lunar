package me.meredith.command;

import me.meredith.WeaveFks;
import net.weavemc.loader.api.command.Command;
import org.jetbrains.annotations.NotNull;

public class FinalsInTabCommand extends Command {
    public FinalsInTabCommand() {
        super("finalsintab");
    }

    @Override
    public void handle(@NotNull String[] args) {
        WeaveFks weavefks = WeaveFks.getInstance();

        weavefks.getConfig().finalsInTab = !weavefks.getConfig().finalsInTab;
        weavefks.saveConfig();

        try {
            String output = (weavefks.getConfig().finalsInTab ? "Enabled" : "Disabled") + " finals in tab";
            weavefks.addChatComponentText(output);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
