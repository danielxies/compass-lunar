package me.meredith.mixins;

import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.util.IChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(GuiNewChat.class)
public abstract class ChatMixin {
    @Inject(method = "printChatMessage", at = @At("HEAD"))
    private void onPrintChatMessage(IChatComponent chatComponent, CallbackInfo ci) {
        // chatComponent.appendText("guru");
        System.out.println("Chat message intercepted!");
    }
}
