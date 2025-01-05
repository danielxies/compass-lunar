package me.meredith.energy;

import me.meredith.WeaveFks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.settings.GameSettings;

public class EnergyHudRenderer {
    private final WeaveFks weavefks;

    public EnergyHudRenderer(WeaveFks weavefks) {
        this.weavefks = weavefks;
    }

    public void render() {
        try {
            Minecraft mc = Minecraft.getMinecraft();
            boolean inGameHasFocus = mc.inGameHasFocus;

            GameSettings gameSettings = mc.gameSettings;
            boolean showDebugInfo = gameSettings.showDebugInfo;

            if (inGameHasFocus && !showDebugInfo) {
                if (mc.thePlayer == null) {
                    return;
                }

                int xpLevel = mc.thePlayer.experienceLevel;

                String xpText = String.format("\u00A7b %d", xpLevel); // Prepend the aqua color code and format the string

                float x = weavefks.getConfig().energyHudX;
                float y = weavefks.getConfig().energyHudY;

                double scale = weavefks.getConfig().energyHudScale / 100.0;

                x /= scale;
                y /= scale;

                GlStateManager.pushMatrix();
                GlStateManager.scale(scale, scale, 1.0);

                FontRenderer fontRenderer = mc.fontRendererObj;
                fontRenderer.drawStringWithShadow(xpText, x, y, 0xFFFFFF); // Using white as default color for text shadow

                GlStateManager.popMatrix();
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
