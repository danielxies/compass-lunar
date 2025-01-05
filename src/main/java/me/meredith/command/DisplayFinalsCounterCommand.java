package me.meredith.command;
import me.meredith.WeaveFks;
import net.minecraft.client.Minecraft;
import net.weavemc.loader.api.command.Command;
import org.jetbrains.annotations.NotNull;

public class DisplayFinalsCounterCommand extends Command {
  public DisplayFinalsCounterCommand() {
      super("displayfinalscounter");
  }

  @Override
  public void handle(@NotNull String[] args) {
      WeaveFks weavefks = WeaveFks.getInstance();
      weavefks.getConfig().displayFinalsCounter = !weavefks.getConfig().displayFinalsCounter;
      weavefks.saveConfig();
      String output = (weavefks.getConfig().displayFinalsCounter ? "Enabled" : "Disabled") + " finals counter HUD";
      weavefks.addChatComponentText(output);
      // String workingDirectory = System.getProperty("user.home") + "/.weave/mods";
      // weavefks.addChatComponentText("DIR: " + workingDirectory);
  }
}