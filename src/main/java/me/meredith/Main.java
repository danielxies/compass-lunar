package me.meredith;

import net.weavemc.loader.api.ModInitializer;
import net.weavemc.loader.api.command.CommandBus;
import net.weavemc.loader.api.event.*;
import me.meredith.WeaveFks;
import me.meredith.finalscounter.ChatMessageParser;
import me.meredith.command.*;
import me.meredith.listener.RenderGameOverlayListener;
import me.meredith.listener.PartyHudListener;
import me.meredith.listener.WitherTimeToDieListener;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import org.lwjgl.input.Keyboard;

public class Main implements ModInitializer {
    @Override
    public void preInit() {
        System.out.println("Initializing weavefks!");
        EventBus.subscribe(this);
        
    }
    @SubscribeEvent
    public void onGameStart(StartGameEvent.Post e) {
        WeaveFks weavefks = WeaveFks.getInstance();
        ChatMessageParser chatMessageParser = weavefks.getChatMessageParser();
        EventBus.subscribe(ChatReceivedEvent.class, ce -> {
            chatMessageParser.onChat(ce.getMessage());
        });
        CommandBus.register(new DisplayFinalsCounterCommand());
        CommandBus.register(new FinalsCommand());
        CommandBus.register(new FinalsInTabCommand());
        CommandBus.register(new PartyHudAddCommand());
        CommandBus.register(new PartyHudClearCommand());
        CommandBus.register(new PartyHudDisplayCommand());
        CommandBus.register(new PartyHudListCommand());
        CommandBus.register(new PartyHudPositionCommand());
        CommandBus.register(new PartyHudRemoveCommand());
        CommandBus.register(new PartyHudScaleCommand());
        CommandBus.register(new PlayerFinalsCommand());
        CommandBus.register(new ResetFinalsCommand());
        CommandBus.register(new SayFks());
        CommandBus.register(new SetPosCommand());
        CommandBus.register(new SetScaleCommand());
        CommandBus.register(new RegenTimerPosCommand());
        CommandBus.register(new RegenTimerScaleCommand());
        CommandBus.register(new EnergyHudPosCommand());
        CommandBus.register(new EnergyHudToggleCommand());
        CommandBus.register(new CompassPosCommand());
        CommandBus.register(new CompassScaleCommand());
        CommandBus.register(new CompassToggleCommand());
        weavefks.initialize(System.getProperty("user.home") + "/.weave/mods");
        EventBus.subscribe(new RenderGameOverlayListener());
        EventBus.subscribe(new PartyHudListener());
        // EventBus.subscribe(new CompassRenderer());
        // EventBus.subscribe(new WitherTimeToDieListener());
    }
}
