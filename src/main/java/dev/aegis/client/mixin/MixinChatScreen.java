package dev.aegis.client.mixin;

import dev.aegis.client.Aegis;
import net.minecraft.client.gui.screen.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public class MixinChatScreen {

    @Inject(method = "sendMessage", at = @At("HEAD"), cancellable = true)
    private void onSendMessage(String message, boolean addToHistory, CallbackInfoReturnable<Boolean> cir) {
        if (Aegis.getInstance() != null && Aegis.getInstance().getCommandManager() != null) {
            if (Aegis.getInstance().getCommandManager().handleChat(message)) {
                cir.setReturnValue(true);
            }
        }
    }
}
