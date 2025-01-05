package me.meredith.command;

import net.weavemc.loader.api.command.Command;
import org.jetbrains.annotations.NotNull;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import me.meredith.WeaveFks;

import java.util.List;
import java.util.Locale;
import java.util.ArrayList;
import java.util.Collections;

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

            // Check if player exists in the scoreboard
            boolean playerExists = false;
            String correctPlayerName = null;
            String playerColor = "";

            if (mc.theWorld.getScoreboard() != null) {
                for (Object entry : mc.theWorld.getScoreboard().getScores()) {
                    net.minecraft.scoreboard.Score score = (net.minecraft.scoreboard.Score) entry;
                    String name = score.getPlayerName();
                    
                    // Remove color codes for comparison
                    String cleanName = name.replaceAll("§[0-9a-fk-or]", "");
                    
                    if (cleanName.toLowerCase(Locale.ROOT).equals(inputName)) {
                        playerExists = true;
                        correctPlayerName = cleanName;
                        // Extract color code from the original name
                        playerColor = name.contains("§") ? "§" + name.charAt(name.indexOf("§") + 1) : "";
                        break;
                    }
                }
            }

            if (!playerExists) {
                String output = "\u00A77Player \u00A7c" + args[1] + "\u00A77 does not exist!";
                mc.thePlayer.addChatComponentMessage(new ChatComponentText(output));
                return;
            }

            if (weavefks.isPartyMember(correctPlayerName)) {
                String output = "\u00A77Player \u00A7c" + correctPlayerName + "\u00A77 is already in the squad!";
                mc.thePlayer.addChatComponentMessage(new ChatComponentText(output));
                return;
            }

            // Add the player with their color
            weavefks.addPartyMember(playerColor + correctPlayerName);

            try {
                String output = "\u00A77Added \u00A7c" + correctPlayerName + "\u00A77 to the squad HUD.";
                mc.thePlayer.addChatComponentMessage(new ChatComponentText(output));
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }

    /**
     * This method returns possible completions for the arguments typed so far.
     */
    public List<String> complete(@NotNull String[] args) {
        // First argument: suggest "add"
        if (args.length == 1) {
            return List.of("add");
        }

        // Second argument: if the first arg is "add", try to tab-complete player names
        if (args.length == 2 && "add".equalsIgnoreCase(args[0])) {
            Minecraft mc = Minecraft.getMinecraft();
            List<String> matches = new ArrayList<>();
            String partial = args[1].toLowerCase(Locale.ROOT);
            
            if (mc.theWorld != null && mc.theWorld.getScoreboard() != null) {
                for (Object entry : mc.theWorld.getScoreboard().getScores()) {
                    net.minecraft.scoreboard.Score score = (net.minecraft.scoreboard.Score) entry;
                    String name = score.getPlayerName();
                    // Remove color codes for comparison
                    String cleanName = name.replaceAll("§[0-9a-fk-or]", "");
                    
                    if (cleanName.toLowerCase(Locale.ROOT).startsWith(partial)) {
                        matches.add(cleanName);
                    }
                }
            }
            
            return matches;
        }
        
        // If none of the above apply, return an empty list
        return Collections.emptyList();
    }
}