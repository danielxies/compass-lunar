/* Decompiler 200ms, total 549ms, lines 26 */
package me.meredith.command;

import me.meredith.WeaveFks;
import net.weavemc.loader.api.command.Command;
import org.jetbrains.annotations.NotNull;

public class PartyHudDisplayCommand extends Command {
   public PartyHudDisplayCommand() {
      super("phuddisplay", new String[0]);
   }

   public void handle(@NotNull String[] args) {
      WeaveFks weavefks = WeaveFks.getInstance();
      weavefks.getConfig().displayPartyHUD = !weavefks.getConfig().displayPartyHUD;
      weavefks.saveConfig();

      try {
         String output = (weavefks.getConfig().displayPartyHUD ? "Enabled" : "Disabled") + " Party HUD";
         weavefks.addChatComponentText(output);
      } catch (Exception var4) {
         var4.printStackTrace();
      }

   }
}