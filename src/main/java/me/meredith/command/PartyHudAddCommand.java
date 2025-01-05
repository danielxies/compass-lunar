package me.meredith.command;

import net.weavemc.loader.api.command.Command;
import org.jetbrains.annotations.NotNull;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import me.meredith.WeaveFks;

import java.util.List;
import java.util.Locale;

public class PartyHudAddCommand extends Command {
    public PartyHudAddCommand() {
        super("squad");
    }

    @Override
    public void handle(@NotNull String[] args) {
        if (args.length < 2) {
            return;
        }

        String subCommand = args[0];
        String inputName = args[1].toLowerCase(Locale.ROOT);

        if ("add".equalsIgnoreCase(subCommand)) {
            WeaveFks weavefks = WeaveFks.getInstance();
            Minecraft mc = Minecraft.getMinecraft();

            EntityPlayer playerToAdd = null;
            List<EntityPlayer> players = mc.theWorld.playerEntities;

            for (EntityPlayer player : players) {
                if (player.getName().equalsIgnoreCase(inputName)) {
                    playerToAdd = player;
                    break;
                }
            }

            if (playerToAdd == null) {
                String output = "\u00A77Player \u00A7c" + args[1] + "\u00A77 does not exist!";
                mc.thePlayer.addChatComponentMessage(new ChatComponentText(output));
                return;
            }

            String playerName = playerToAdd.getName();
            if (weavefks.isPartyMember(playerName)) {
                String output = "\u00A77Player \u00A7c" + playerName + "\u00A77 is already in the squad!";
                mc.thePlayer.addChatComponentMessage(new ChatComponentText(output));
                return;
            }

            weavefks.addPartyMember(playerName);

            try {
                String output = "\u00A77Added \u00A7c" + playerName + "\u00A77 to the squad HUD.";
                mc.thePlayer.addChatComponentMessage(new ChatComponentText(output));
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }
}
