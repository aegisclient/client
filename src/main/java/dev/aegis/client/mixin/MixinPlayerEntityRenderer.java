package dev.aegis.client.mixin;

import dev.aegis.client.Aegis;
import dev.aegis.client.module.Module;
import dev.aegis.client.module.premium.AegisCape;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public class MixinPlayerEntityRenderer {

    @Inject(method = "render(Lnet/minecraft/client/network/AbstractClientPlayerEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At("RETURN"))
    private void onRenderPlayer(AbstractClientPlayerEntity player, float yaw, float tickDelta,
                                MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light,
                                CallbackInfo ci) {
        try {
            if (Aegis.getInstance() == null) return;
            if (Aegis.getInstance().getModuleManager() == null) return;

            // Only render cape on the local player
            if (player != Aegis.mc().player) return;

            Module capeModule = Aegis.getInstance().getModuleManager().getModule("AegisCape");
            if (capeModule == null || !capeModule.isEnabled()) return;
            if (!(capeModule instanceof AegisCape aegisCape)) return;
            if (!aegisCape.isTextureReady()) return;

            Identifier capeTexture = aegisCape.getCapeTextureId();
            if (capeTexture == null) return;

            renderCape(player, tickDelta, matrices, vertexConsumers, light, capeTexture);
        } catch (Exception e) {
            // Silently fail so we don't crash rendering
        }
    }

    private void renderCape(AbstractClientPlayerEntity player, float tickDelta,
                            MatrixStack matrices, VertexConsumerProvider vertexConsumers,
                            int light, Identifier texture) {
        matrices.push();

        // Position cape on the player's back
        matrices.translate(0.0f, 0.0f, 0.125f);

        // Simple physics based on player movement
        double velX = player.getX() - player.prevX;
        double velZ = player.getZ() - player.prevZ;
        float bodyYaw = MathHelper.lerp(tickDelta, player.prevBodyYaw, player.bodyYaw);
        double yawRad = Math.toRadians(bodyYaw);

        // Forward/backward cape sway based on movement direction relative to body
        float forwardMotion = (float) (velX * (-Math.sin(yawRad)) + velZ * Math.cos(yawRad));
        float capeAngle = forwardMotion * 80.0f;
        capeAngle = MathHelper.clamp(capeAngle, -25.0f, 60.0f);

        // Add wind effect
        float windOffset = (float) Math.sin((player.age + tickDelta) * 0.1) * 3.0f;

        // Walking bob
        float walkAnim = MathHelper.lerp(tickDelta, player.prevStrideDistance, player.strideDistance);
        float walkBob = MathHelper.sin(walkAnim * 6.283f) * walkAnim * 8.0f;

        // Sneak adjustment
        if (player.isSneaking()) {
            capeAngle += 25.0f;
            matrices.translate(0.0f, 0.15f, 0.0f);
        }

        float totalAngle = 6.0f + capeAngle + windOffset + walkBob;
        totalAngle = MathHelper.clamp(totalAngle, -30.0f, 90.0f);

        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(totalAngle));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f));

        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getEntitySolid(texture));
        Matrix4f posMatrix = matrices.peek().getPositionMatrix();
        Matrix3f normalMatrix = matrices.peek().getNormalMatrix();

        // Cape quad dimensions
        float halfWidth = 0.35f;
        float capeHeight = 1.0f;

        // UV coordinates for cape area in the 64x32 texture
        float u0 = 1.0f / 64.0f;
        float u1 = 11.0f / 64.0f;
        float v0 = 1.0f / 32.0f;
        float v1 = 17.0f / 32.0f;

        // Front face
        vertex(vertexConsumer, posMatrix, normalMatrix, light, -halfWidth, 0, 0, u1, v0);
        vertex(vertexConsumer, posMatrix, normalMatrix, light, halfWidth, 0, 0, u0, v0);
        vertex(vertexConsumer, posMatrix, normalMatrix, light, halfWidth, -capeHeight, 0, u0, v1);
        vertex(vertexConsumer, posMatrix, normalMatrix, light, -halfWidth, -capeHeight, 0, u1, v1);

        // Back face (so cape is visible from both sides)
        vertex(vertexConsumer, posMatrix, normalMatrix, light, halfWidth, 0, 0.01f, u0, v0);
        vertex(vertexConsumer, posMatrix, normalMatrix, light, -halfWidth, 0, 0.01f, u1, v0);
        vertex(vertexConsumer, posMatrix, normalMatrix, light, -halfWidth, -capeHeight, 0.01f, u1, v1);
        vertex(vertexConsumer, posMatrix, normalMatrix, light, halfWidth, -capeHeight, 0.01f, u0, v1);

        matrices.pop();
    }

    private void vertex(VertexConsumer consumer, Matrix4f posMatrix, Matrix3f normalMatrix,
                        int light, float x, float y, float z, float u, float v) {
        consumer.vertex(posMatrix, x, y, z)
                .color(255, 255, 255, 255)
                .texture(u, v)
                .overlay(OverlayTexture.DEFAULT_UV)
                .light(light)
                .normal(normalMatrix, 0.0f, 0.0f, -1.0f)
                .next();
    }
}
