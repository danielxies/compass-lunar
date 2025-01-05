package me.meredith.command;

import me.meredith.WeaveFks;
import net.weavemc.loader.api.command.Command;
import org.jetbrains.annotations.NotNull;

public class ResetFinalsCommand extends Command {
    public ResetFinalsCommand() {
        super("resetfinals");
    }

    @Override
    public void handle(@NotNull String[] args) {
        WeaveFks weavefks = WeaveFks.getInstance();

        weavefks.getChatMessageParser().reset();

        try {
            weavefks.addChatComponentText("Reset finals");
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}