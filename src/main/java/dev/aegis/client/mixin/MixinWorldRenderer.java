package dev.aegis.client.mixin;

import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(WorldRenderer.class)
public class MixinWorldRenderer {
    // world render hooks handled via Fabric API events
}
