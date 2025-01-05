package me.meredith;

import com.google.gson.Gson;
import me.meredith.Config;
import me.meredith.finalscounter.AltFinalsCounterRenderer;
import me.meredith.finalscounter.ChatMessageParser;
import me.meredith.finalscounter.FinalsCounterRenderer;
import me.meredith.regentimer.RegenerationTimerRenderer;
import me.meredith.command.RegenTimerPosCommand;
import me.meredith.energy.EnergyHudRenderer;
import me.meredith.bond.BondRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

import net.weavemc.loader.api.event.ChatReceivedEvent;
import net.weavemc.loader.api.event.SubscribeEvent;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.swing.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class WeaveFks {
    public static WeaveFks instance;
    private final ChatMessageParser chatMessageParser = new ChatMessageParser(this);
    private final FinalsCounterRenderer finalsCounterRenderer = new FinalsCounterRenderer(this);
    private final AltFinalsCounterRenderer altFinalsCounterRenderer = new AltFinalsCounterRenderer(this);
    private final RegenerationTimerRenderer regenerationTimerRenderer = new RegenerationTimerRenderer(this);
    private final EnergyHudRenderer energyHudRenderer = new EnergyHudRenderer(this);
    private final BondRenderer bondRenderer = new BondRenderer(this);
    private File configFile;
    private Config config = new Config();
    private final Gson gson = new Gson();
    public static final String MODID = "weavefks";
    public static final String NAME = "Weave Fks Mod";
    public static final String VERSION = "1.0";

    private static List<String> partyMembers = new ArrayList<>();
    
    public WeaveFks() {
        instance = this;
    }

    public String getServerName() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.getCurrentServerData() != null) {
            return mc.getCurrentServerData().serverName;
        }
        return null;
    }

    public static WeaveFks getInstance() {
        if (instance == null) {
            instance = new WeaveFks();
        }
        return instance;
    }

    public boolean isPartyMember(String playerName) {
        for (String member : partyMembers) {
            if (member.equalsIgnoreCase(playerName)) {
                return true;
            }
        }
        return false;
    }

    public boolean initialize(String workingDirectory) {
        System.out.println("WeaveFks.initialize");
        configFile = new File(workingDirectory, "WeaveFks.json");

        if (configFile.exists()) {
            try {
                String jsonConfig = new String(Files.readAllBytes(configFile.toPath()), StandardCharsets.UTF_8);
                config = gson.fromJson(jsonConfig, Config.class);
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        } else {
            saveConfig();
        }

        return true;
    }

    public List<String> getPartyMembers() {
        return new ArrayList<>(partyMembers);
    }

    public void addPartyMember(String playerName) {
        partyMembers.add(playerName);
    }

    public void removePartyMember(String playerName) {
        partyMembers.remove(playerName);
    }

    public void clearPartyMembers() {
        partyMembers.clear();
    }

    public void addSelfToPartyMembers() {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        if (player != null) {
            String playerName = player.getGameProfile().getName();
            if (!partyMembers.contains(playerName)) {
                partyMembers.add(playerName);
            }
        }
    }

    public Collection<NetworkPlayerInfo> getPartyMembersNetworkPlayerInfo(String playerName) {
        Collection<NetworkPlayerInfo> partyMembersInfo = new ArrayList<>();
        
        Collection<NetworkPlayerInfo> playerInfoMap = Minecraft.getMinecraft().getNetHandler().getPlayerInfoMap();
        
        if (playerInfoMap != null) {
            for (NetworkPlayerInfo playerInfo : playerInfoMap) {
                String playerNameInGame = playerInfo.getGameProfile().getName();
                if (playerNameInGame != null && playerNameInGame.equals(playerName)) {
                    partyMembersInfo.add(playerInfo);
                }
            }
        }
        return partyMembersInfo;
    }
    
    public ChatMessageParser getChatMessageParser() {
        return chatMessageParser;
    }

    public FinalsCounterRenderer getFinalsCounterRenderer() {
        return finalsCounterRenderer;
    }

    public RegenerationTimerRenderer getRegenerationTimerRenderer() {
        return regenerationTimerRenderer;
    }

    public EnergyHudRenderer getEnergyHudRenderer() {
        return energyHudRenderer;
    }

    public AltFinalsCounterRenderer getAltFinalsCounterRenderer() {
        return altFinalsCounterRenderer;
    }

    public BondRenderer getBondRenderer() {
        return bondRenderer;
    }

    public Config getConfig() {
        return config;
    }

    public void saveConfig() {
        try {
            if (!configFile.exists()) {
                if (!configFile.createNewFile()) {
                    System.err.println("Failed to create config file!");
                    return;
                }
            }

            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(configFile));
            bufferedWriter.write(gson.toJson(config));
            bufferedWriter.close();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public void addChatComponentText(String text) {
        if (Minecraft.getMinecraft().thePlayer == null) return;
        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(text));
    }

    public void sendMessage(String message) {
        if (Minecraft.getMinecraft().thePlayer == null) return;
        Minecraft.getMinecraft().thePlayer.sendChatMessage(message);
    }

    public boolean isDisplayEnergyHud() {
        return config.displayEnergyHud;
    }

    public void onRender() {
        regenerationTimerRenderer.render();
        finalsCounterRenderer.render();
    }
}
