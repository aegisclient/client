package dev.aegis.client.mixin;

import dev.aegis.client.Aegis;
import dev.aegis.client.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class MixinLivingEntity {

    @Inject(method = "getStepHeight", at = @At("HEAD"), cancellable = true)
    private void onGetStepHeight(CallbackInfoReturnable<Float> cir) {
        Entity self = (Entity) (Object) this;
        MinecraftClient mc = MinecraftClient.getInstance();

        if (mc.player != null && self == mc.player) {
            if (Aegis.getInstance() != null) {
                Module step = Aegis.getInstance().getModuleManager().getModule("Step");
                if (step != null && step.isEnabled()) {
                    cir.setReturnValue(2.0f);
                }
            }
        }
    }
}
