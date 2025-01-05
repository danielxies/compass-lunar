package me.meredith.finalscounter;

import me.meredith.WeaveFks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.settings.GameSettings;
import java.util.*;
import java.util.stream.Collectors;

public class AltFinalsCounterRenderer {
    private final WeaveFks weavefks;

    public AltFinalsCounterRenderer(WeaveFks weavefks) {
        this.weavefks = weavefks;
    }

    public void render() {
        try {
            Minecraft mc = Minecraft.getMinecraft();
            boolean inGameHasFocus = mc.inGameHasFocus;

            GameSettings gameSettings = mc.gameSettings;
            boolean showDebugInfo = gameSettings.showDebugInfo;

            if (weavefks.getConfig().displayFinalsCounter
                    && weavefks.getConfig().altFinalDisplay
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

                LinkedHashMap<String, Map<String, Integer>> teamData = new LinkedHashMap<>();
                teamData.put("B", weavefks.getChatMessageParser().getBlue());
                teamData.put("G", weavefks.getChatMessageParser().getGreen());
                teamData.put("R", weavefks.getChatMessageParser().getRed());
                teamData.put("Y", weavefks.getChatMessageParser().getYellow());

                LinkedHashMap<String, Map<String, Integer>> sortedTeams = sortByValues(teamData);

                List<String> finalsStrings = sortedTeams.entrySet().stream()
                        .map(entry -> getColorPrefix(entry.getKey()) + String.valueOf(entry.getValue().values().stream().mapToInt(Integer::intValue).sum()))
                        .collect(Collectors.toList());

                String finalsDisplay = String.join(" \u00A7f/ ", finalsStrings);
                fontRenderer.drawStringWithShadow(finalsDisplay, x, y, 0xFFFFFF);

                GlStateManager.popMatrix();
            }
        } catch (Exception exception) {
            exception.printStackTrace();
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

    private String getColorPrefix(String teamName) {
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
                return "\u00A7f"; // Default to white if team is unknown
        }
    }
}
