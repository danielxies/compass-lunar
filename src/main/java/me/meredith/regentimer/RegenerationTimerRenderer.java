package me.meredith.regentimer;

import me.meredith.WeaveFks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.client.settings.GameSettings;

public class RegenerationTimerRenderer {
    private final WeaveFks weavefks;

    public RegenerationTimerRenderer(WeaveFks weavefks) {
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

                PotionEffect regenEffect = mc.thePlayer.getActivePotionEffect(Potion.regeneration);
                if (regenEffect == null || regenEffect.getAmplifier() != 2) {
                    return;
                }

                // Convert ticks to tenths of a second
                float duration = regenEffect.getDuration() / 2.0f / 10.0f;
                String regenText = String.format("\u00A7d%.1fs", duration); // Prepend the pink color code and format the string

                float x = weavefks.getConfig().regenTimerX;
                float y = weavefks.getConfig().regenTimerY;

                double scale = weavefks.getConfig().regenTimerScale / 100.0;

                x /= scale;
                y /= scale;

                GlStateManager.pushMatrix();
                GlStateManager.scale(scale, scale, 1.0);

                FontRenderer fontRenderer = mc.fontRendererObj;
                fontRenderer.drawStringWithShadow(regenText, x, y, 0xFFFFFF); // Using white as default color for text shadow

                GlStateManager.popMatrix();
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
