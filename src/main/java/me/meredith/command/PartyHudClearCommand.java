package me.meredith.command;

import me.meredith.WeaveFks;
import net.weavemc.loader.api.command.Command;
import org.jetbrains.annotations.NotNull;

public class PartyHudClearCommand extends Command {
    public PartyHudClearCommand() {
        super("squad");
    }

    @Override
    public void handle(@NotNull String[] args) {
        if (args.length == 0 || !"clear".equalsIgnoreCase(args[0])) {
            return; // No subcommand provided or incorrect subcommand
        }

        WeaveFks weavefks = WeaveFks.getInstance();
        weavefks.clearPartyMembers();
        weavefks.addSelfToPartyMembers();

        try {
            String output = "\u00A77Cleared all players from the squad HUD";
            weavefks.addChatComponentText(output);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
