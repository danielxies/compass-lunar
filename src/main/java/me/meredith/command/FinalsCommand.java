package me.meredith.command;

import me.meredith.WeaveFks;
import net.weavemc.loader.api.command.Command;
import org.jetbrains.annotations.NotNull;

public class FinalsCommand extends Command {
    public FinalsCommand() {
        super("finals");
    }

    @Override
    public void handle(@NotNull String[] args) {
        WeaveFks weavefks = WeaveFks.getInstance();

        try {
            int blueFinals = weavefks.getChatMessageParser().getBlue()
                    .values()
                    .stream()
                    .reduce(0, Integer::sum);

            int greenFinals = weavefks.getChatMessageParser().getGreen()
                    .values()
                    .stream()
                    .reduce(0, Integer::sum);

            int redFinals = weavefks.getChatMessageParser().getRed()
                    .values()
                    .stream()
                    .reduce(0, Integer::sum);

            int yellowFinals = weavefks.getChatMessageParser().getYellow()
                    .values()
                    .stream()
                    .reduce(0, Integer::sum);

            String finals = weavefks.getChatMessageParser().getBluePrefix() + "Blue: " + "\u00A7f" + blueFinals + "\n"
                    + weavefks.getChatMessageParser().getGreenPrefix() + "Green: " + "\u00A7f" + greenFinals + "\n"
                    + weavefks.getChatMessageParser().getRedPrefix() + "Red: " + "\u00A7f" + redFinals + "\n"
                    + weavefks.getChatMessageParser().getYellowPrefix() + "Yellow: " + "\u00A7f" + yellowFinals;

            weavefks.addChatComponentText(finals);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
