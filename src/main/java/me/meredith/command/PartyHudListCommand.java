package me.meredith.command;

import me.meredith.WeaveFks;
import net.weavemc.loader.api.command.Command;
import org.jetbrains.annotations.NotNull;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;

import java.util.List;

public class PartyHudListCommand extends Command {
    public PartyHudListCommand() {
        super("squad");
    }

    @Override
    public void handle(@NotNull String[] args) {
        if (args.length < 1) {
            return;
        }

        String subCommand = args[0];

        if ("list".equalsIgnoreCase(subCommand)) {
            WeaveFks weavefks = WeaveFks.getInstance();
            Minecraft mc = Minecraft.getMinecraft();

            List<String> partyMembers = weavefks.getPartyMembers();

            if (partyMembers.isEmpty()) {
                String output = "\u00A77Current Squad: \n\u00A7cNo members in the squad.";
                mc.thePlayer.addChatComponentMessage(new ChatComponentText(output));
                return;
            }

            StringBuilder output = new StringBuilder("\u00A77Current Squad:\n");

            for (String member : partyMembers) {
                output.append("\u00A7c").append(member).append("\n");
            }

            mc.thePlayer.addChatComponentMessage(new ChatComponentText(output.toString()));
        }
    }
}
