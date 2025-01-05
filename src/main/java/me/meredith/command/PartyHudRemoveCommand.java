package me.meredith.command;

import me.meredith.WeaveFks;
import net.weavemc.loader.api.command.Command;
import org.jetbrains.annotations.NotNull;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;

import java.util.List;
import java.util.Locale;

public class PartyHudRemoveCommand extends Command {
    public PartyHudRemoveCommand() {
        super("squad");
    }

    @Override
    public void handle(@NotNull String[] args) {
        if (args.length < 2) {
            return;
        }

        String subCommand = args[0];
        String inputName = args[1].toLowerCase(Locale.ROOT);

        if ("remove".equalsIgnoreCase(subCommand)) {
            WeaveFks weavefks = WeaveFks.getInstance();
            Minecraft mc = Minecraft.getMinecraft();

            EntityPlayer playerToRemove = null;
            List<EntityPlayer> players = mc.theWorld.playerEntities;

            for (EntityPlayer player : players) {
                if (player.getName().equalsIgnoreCase(inputName)) {
                    playerToRemove = player;
                    break;
                }
            }

            if (playerToRemove == null) {
                String output = "\u00A77Player \u00A7c" + args[1] + "\u00A77 is not in the squad!";
                mc.thePlayer.addChatComponentMessage(new ChatComponentText(output));
                return;
            }

            String playerName = playerToRemove.getName();
            if (!weavefks.isPartyMember(playerName)) {
                String output = "\u00A77Player \u00A7c" + playerName + "\u00A77 is not in the squad!";
                mc.thePlayer.addChatComponentMessage(new ChatComponentText(output));
                return;
            }

            weavefks.removePartyMember(playerName);

            try {
                String output = "\u00A77Removed \u00A7c" + playerName + "\u00A77 from the squad HUD.";
                mc.thePlayer.addChatComponentMessage(new ChatComponentText(output));
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }
}
