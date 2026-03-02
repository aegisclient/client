package dev.aegis.client.mixin;

import dev.aegis.client.Aegis;
import dev.aegis.client.module.Module;
import dev.aegis.client.module.movement.Flight;
import dev.aegis.client.module.movement.NoFall;
import dev.aegis.client.module.movement.Speed;
import dev.aegis.client.module.movement.Sprint;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class MixinClientPlayerEntity {

    @Inject(method = "tick", at = @At("HEAD"))
    private void onPlayerTick(CallbackInfo ci) {
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;

        // auto sprint
        Module sprint = Aegis.getInstance().getModuleManager().getModule("Sprint");
        if (sprint != null && sprint.isEnabled()) {
            if (player.input.movementForward > 0 && !player.isSneaking()) {
                player.setSprinting(true);
            }
        }
    }

    @Inject(method = "move", at = @At("HEAD"), cancellable = true)
    private void onMove(MovementType type, Vec3d movement, CallbackInfo ci) {
        // flight override handled in module tick to keep it simpler
    }
}
