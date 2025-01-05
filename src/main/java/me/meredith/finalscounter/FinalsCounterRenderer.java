package me.meredith.finalscounter;

import me.meredith.WeaveFks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.settings.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors; 


public class FinalsCounterRenderer {
    private final WeaveFks weavefks;
    private final Map<String, String> teamStrings = new LinkedHashMap<>();

    public FinalsCounterRenderer(WeaveFks weavefks) {
        this.weavefks = weavefks;
    }

    public void update() {
        // Put the strings into a LinkedHashMap to maintain the order of insertion
        LinkedHashMap<String, Map<String, Integer>> teamData = new LinkedHashMap<>();
        teamData.put("B", weavefks.getChatMessageParser().getBlue());
        teamData.put("G", weavefks.getChatMessageParser().getGreen());
        teamData.put("R", weavefks.getChatMessageParser().getRed());
        teamData.put("Y", weavefks.getChatMessageParser().getYellow());

        LinkedHashMap<String, Map<String, Integer>> sortedTeams = sortByValues(teamData);

        // Update teamStrings based on the sorted order
        teamStrings.clear();
        for (Map.Entry<String, Map<String, Integer>> entry : sortedTeams.entrySet()) {
            String teamName = entry.getKey();
            String teamString = printTeam(teamName, getPrefix(teamName), entry.getValue());
            teamStrings.put(teamName, teamString);
        }
    }

    private static LinkedHashMap<String, Map<String, Integer>> sortByValues(Map<String, Map<String, Integer>> map) {
        List<Map.Entry<String, Map<String, Integer>>> entries = new LinkedList<>(map.entrySet());

        Collections.sort(entries, Comparator.comparing(entry ->
                entry.getValue()
                        .values()
                        .stream()
                        .mapToInt(Integer::intValue)
                        .sum(),
                Comparator.reverseOrder()));

        LinkedHashMap<String, Map<String, Integer>> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<String, Map<String, Integer>> entry : entries) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }

    public void render() {
        try {
            Minecraft mc = Minecraft.getMinecraft();
            // weavefks.addChatComponentText("in render function");
            boolean inGameHasFocus = mc.inGameHasFocus;

            GameSettings gameSettings = mc.gameSettings;
            boolean showDebugInfo = gameSettings.showDebugInfo;

            if (weavefks.getConfig().displayFinalsCounter
                    && inGameHasFocus
                    && !showDebugInfo) {
                float x = weavefks.getConfig().finalsCounterX;
                float y = weavefks.getConfig().finalsCounterY;

                double scale = weavefks.getConfig().finalsCounterScale / 100.0;

                x /= scale;
                y /= scale;

                GlStateManager.pushMatrix();

                GlStateManager.scale(scale, scale, 1.0);

                FontRenderer fontRenderer = mc.fontRendererObj;

                // Iterate over the sorted team order
                for (Map.Entry<String, String> entry : teamStrings.entrySet()) {
                    String teamName = entry.getKey();
                    String teamString = entry.getValue();
                    fontRenderer.drawStringWithShadow(teamString, x, y, 0xFFFFFF);
                    // System.out.println("teamName: " + teamName + ", teamString: " + teamString + ", x: " + x + ", y: " + y);
                    y += 10; // Adjust the spacing between teams as needed
                }

                GlStateManager.popMatrix();
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private String printTeam(String team, String prefix, Map<String, Integer> players) {
        int finals = players
                .values()
                .stream()
                .reduce(0, Integer::sum);

        Map.Entry<String, Integer> highestFinalsPlayer = players
                .entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .orElse(new AbstractMap.SimpleImmutableEntry<>("", 0));

        if (finals == 0) {
            return prefix + team + " " + "\u00A7f" + finals;
        }

        return prefix + team + " "
                + "\u00A7f"
                + finals + ": "
                + highestFinalsPlayer.getKey()
                + " (" + highestFinalsPlayer.getValue() + ")";
    }

    private String getPrefix(String teamName) {
        switch (teamName) {
            case "B":
                return weavefks.getChatMessageParser().getBluePrefix();
            case "G":
                return weavefks.getChatMessageParser().getGreenPrefix();
            case "R":
                return weavefks.getChatMessageParser().getRedPrefix();
            case "Y":
                return weavefks.getChatMessageParser().getYellowPrefix();
            default:
                return "";
        }
    }
}