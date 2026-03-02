package dev.aegis.client.mixin;

import dev.aegis.client.Aegis;
import dev.aegis.client.event.events.PacketEvent;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public class MixinClientConnection {

    @Inject(method = "send(Lnet/minecraft/network/packet/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void onSendPacket(Packet<?> packet, CallbackInfo ci) {
        if (Aegis.getInstance() == null) return;

        PacketEvent event = new PacketEvent(packet, PacketEvent.Direction.SEND);
        Aegis.getInstance().getEventBus().post(event);

        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "handlePacket", at = @At("HEAD"), cancellable = true)
    private static void onReceivePacket(Packet<?> packet, PacketListener listener, CallbackInfo ci) {
        if (Aegis.getInstance() == null) return;

        PacketEvent event = new PacketEvent(packet, PacketEvent.Direction.RECEIVE);
        Aegis.getInstance().getEventBus().post(event);

        if (event.isCancelled()) {
            ci.cancel();
        }
    }
}
