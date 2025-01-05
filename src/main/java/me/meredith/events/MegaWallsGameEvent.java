package me.meredith.events;
import net.weavemc.loader.api.event.*;

public class MegaWallsGameEvent extends Event {
    public enum EventType {
      BLUE_WITHER_DEAD,
      GREEN_WITHER_DEAD,
      RED_WITHER_DEAD,
      YELLOW_WITHER_DEAD
    }

    private final EventType type;

    public MegaWallsGameEvent(EventType type) {
        this.type = type;
    }

    public EventType getType() {
        return type;
    }
}