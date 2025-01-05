package me.meredith.bond;

import me.meredith.WeaveFks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.player.EntityPlayer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BondRenderer {
    private final WeaveFks weavefks;
    private final Map<String, Integer> squadHealth = new HashMap<>();
    private static final int LOW_HP_THRESHOLD = 15;

    public BondRenderer(WeaveFks weavefks) {
        this.weavefks = weavefks;
    }

    public void updateSquadHealth() {
        List<String> currentSquad = weavefks.getPartyMembers();

        if (currentSquad.isEmpty()) {
            System.out.println("No party members found.");
            return;
        }

        for (String playerName : currentSquad) {
            EntityPlayer player = Minecraft.getMinecraft().theWorld.getPlayerEntityByName(playerName);
            if (player != null && !playerName.equals(Minecraft.getMinecraft().thePlayer.getName())) {
                if (!squadHealth.containsKey(playerName)) {
                    System.out.println(playerName + " added to squad hud. Current Squad: " + String.join(", ", currentSquad));
                }
                squadHealth.put(playerName, (int) player.getHealth());
            }
        }

        // Check if any players are no longer in the party and remove them from squadHealth
        squadHealth.keySet().removeIf(playerName -> !currentSquad.contains(playerName));
    }

    public void render() {
        try {
            Minecraft mc = Minecraft.getMinecraft();
            boolean inGameHasFocus = mc.inGameHasFocus;
            GameSettings gameSettings = mc.gameSettings;
            boolean showDebugInfo = gameSettings.showDebugInfo;

            // Always show the HUD, regardless of focus and debug info
            updateSquadHealth();

            FontRenderer fontRenderer = mc.fontRendererObj;
            String message = "no one is low";

            for (Map.Entry<String, Integer> entry : squadHealth.entrySet()) {
                String playerName = entry.getKey();
                int health = entry.getValue();

                if (health < LOW_HP_THRESHOLD) {
                    message = playerName + " is LOW!!!";
                    break;
                }
            }

            int x = 200;
            int y = 200;

            GlStateManager.pushMatrix();
            GlStateManager.scale(4.0F, 4.0F, 4.0F); // Adjusted to default scale

            fontRenderer.drawStringWithShadow(message, x, y, 0xFFFFFF);

            GlStateManager.popMatrix();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
