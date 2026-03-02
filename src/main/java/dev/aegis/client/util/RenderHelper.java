package dev.aegis.client.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class RenderHelper {

    public static void drawBox(MatrixStack matrices, Box box, float r, float g, float b, float a) {
        MinecraftClient mc = MinecraftClient.getInstance();
        Vec3d cam = mc.gameRenderer.getCamera().getPos();

        matrices.push();
        matrices.translate(-cam.x, -cam.y, -cam.z);

        Matrix4f model = matrices.peek().getPositionMatrix();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();

        buffer.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

        // draw all 12 edges
        drawEdge(buffer, model, box.minX, box.minY, box.minZ, box.maxX, box.minY, box.minZ, r, g, b, a);
        drawEdge(buffer, model, box.maxX, box.minY, box.minZ, box.maxX, box.minY, box.maxZ, r, g, b, a);
        drawEdge(buffer, model, box.maxX, box.minY, box.maxZ, box.minX, box.minY, box.maxZ, r, g, b, a);
        drawEdge(buffer, model, box.minX, box.minY, box.maxZ, box.minX, box.minY, box.minZ, r, g, b, a);

        drawEdge(buffer, model, box.minX, box.maxY, box.minZ, box.maxX, box.maxY, box.minZ, r, g, b, a);
        drawEdge(buffer, model, box.maxX, box.maxY, box.minZ, box.maxX, box.maxY, box.maxZ, r, g, b, a);
        drawEdge(buffer, model, box.maxX, box.maxY, box.maxZ, box.minX, box.maxY, box.maxZ, r, g, b, a);
        drawEdge(buffer, model, box.minX, box.maxY, box.maxZ, box.minX, box.maxY, box.minZ, r, g, b, a);

        drawEdge(buffer, model, box.minX, box.minY, box.minZ, box.minX, box.maxY, box.minZ, r, g, b, a);
        drawEdge(buffer, model, box.maxX, box.minY, box.minZ, box.maxX, box.maxY, box.minZ, r, g, b, a);
        drawEdge(buffer, model, box.maxX, box.minY, box.maxZ, box.maxX, box.maxY, box.maxZ, r, g, b, a);
        drawEdge(buffer, model, box.minX, box.minY, box.maxZ, box.minX, box.maxY, box.maxZ, r, g, b, a);

        tessellator.draw();

        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        matrices.pop();
    }

    private static void drawEdge(BufferBuilder buffer, Matrix4f matrix,
                                  double x1, double y1, double z1,
                                  double x2, double y2, double z2,
                                  float r, float g, float b, float a) {
        buffer.vertex(matrix, (float) x1, (float) y1, (float) z1).color(r, g, b, a).next();
        buffer.vertex(matrix, (float) x2, (float) y2, (float) z2).color(r, g, b, a).next();
    }

    public static void drawLine(MatrixStack matrices, Vec3d start, Vec3d end,
                                 float r, float g, float b, float a) {
        MinecraftClient mc = MinecraftClient.getInstance();
        Vec3d cam = mc.gameRenderer.getCamera().getPos();

        matrices.push();
        matrices.translate(-cam.x, -cam.y, -cam.z);

        Matrix4f model = matrices.peek().getPositionMatrix();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.lineWidth(1.5f);

        buffer.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        buffer.vertex(model, (float) start.x, (float) start.y, (float) start.z).color(r, g, b, a).next();
        buffer.vertex(model, (float) end.x, (float) end.y, (float) end.z).color(r, g, b, a).next();
        tessellator.draw();

        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        matrices.pop();
    }
}
