package me.meredith.mixins;

import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.client.network.NetworkPlayerInfo;


import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.Shadow;


@Mixin(GuiPlayerTabOverlay.class)
public abstract class GuiPlayerTabOverlayMixin {
    private ScoreObjective p_175247_1_;
    private String p_175247_3_;

  private static EnumChatFormatting getHPColor(float maxHealthPoints, float healthPoints) {
        if (healthPoints > maxHealthPoints) {
            return EnumChatFormatting.GREEN;
        } else if (healthPoints > maxHealthPoints * 3f / 4f) {
            return EnumChatFormatting.GREEN;
        } else if (healthPoints > maxHealthPoints / 2f) {
            return EnumChatFormatting.YELLOW;
        } else if (healthPoints > maxHealthPoints / 4f) {
            return EnumChatFormatting.RED;
        } else {
            return EnumChatFormatting.DARK_RED;
        }
    }

   private static EnumChatFormatting getColoredHP(int healthPoints) { 
    final float maxHealthPoints;
    maxHealthPoints = Minecraft.getMinecraft().thePlayer.getMaxHealth();
    return getHPColor(maxHealthPoints, healthPoints);
   }

    @ModifyVariable(method = "drawScoreboardValues", at = @At(value = "HEAD"), argsOnly = true)
    private ScoreObjective setter(ScoreObjective p_175247_1_) {
        this.p_175247_1_ = p_175247_1_;
        return p_175247_1_;
    }
    
    @ModifyVariable(method = "drawScoreboardValues", at = @At(value = "HEAD"), argsOnly = true)
    private String setter(String p_175247_3_) {
        this.p_175247_3_ = p_175247_3_;
        return p_175247_3_;
    }

    @Redirect(method = "drawScoreboardValues", at = @At(value = "FIELD", target = "Lnet/minecraft/util/EnumChatFormatting;YELLOW:Lnet/minecraft/util/EnumChatFormatting;", opcode = Opcodes.GETSTATIC))
    private EnumChatFormatting redirectDrawScoreboardValues() {
        return getColoredHP(this.p_175247_1_.getScoreboard().getValueFromObjective(this.p_175247_3_, this.p_175247_1_).getScorePoints());
    }

}


