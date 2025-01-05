package me.meredith.listener;

import net.weavemc.loader.api.event.RenderGameOverlayEvent;
import net.weavemc.loader.api.event.SubscribeEvent;
import me.meredith.WeaveFks;

public class RenderGameOverlayListener {
    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Post event) {
        WeaveFks weavefks = WeaveFks.getInstance();

        if (weavefks.isDisplayEnergyHud()) {
            weavefks.getEnergyHudRenderer().render();
        }
        
        weavefks.getRegenerationTimerRenderer().render();
        weavefks.getAltFinalsCounterRenderer().render();
    }
}
