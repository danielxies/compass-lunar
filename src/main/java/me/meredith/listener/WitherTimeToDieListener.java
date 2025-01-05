package me.meredith.listener;

import net.weavemc.loader.api.event.RenderGameOverlayEvent;
import net.weavemc.loader.api.event.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import me.meredith.WeaveFks;
import me.meredith.events.MegaWallsGameEvent;

import static org.lwjgl.opengl.GL11.*;

public class WitherTimeToDieListener {
    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Post event) {
        render();
    }

    public void render() {
        // if (withersDeadCount != 3) {
        //     return;
        // }
        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 0, 0);
        GlStateManager.scale(0.5, 0.5, 0.5);
        Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow("Number of Withers Dead: " + WeaveFks.getInstance().getChatMessageParser().getWithersDeadCount(), 5, 200, 0xFFFFFF);
        GlStateManager.popMatrix();
    }
    @SubscribeEvent
    public void onMegawallsGameEvent(MegaWallsGameEvent event) {
        if (event.getType() == MegaWallsGameEvent.EventType.BLUE_WITHER_DEAD) {
          WeaveFks.getInstance().addChatComponentText("[WEAVEFKS DEBUG]: Blue Wither Dead");
        } else if (event.getType() == MegaWallsGameEvent.EventType.GREEN_WITHER_DEAD) {
          WeaveFks.getInstance().addChatComponentText("[WEAVEFKS DEBUG]: Green Wither Dead");
        } else if (event.getType() == MegaWallsGameEvent.EventType.RED_WITHER_DEAD) {
          WeaveFks.getInstance().addChatComponentText("[WEAVEFKS DEBUG]: Red Wither Dead");
        } else if (event.getType() == MegaWallsGameEvent.EventType.YELLOW_WITHER_DEAD) {
          WeaveFks.getInstance().addChatComponentText("[WEAVEFKS DEBUG]: Yellow Wither Dead");
        }
    }
}
