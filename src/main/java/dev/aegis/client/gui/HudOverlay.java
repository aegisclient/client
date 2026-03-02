package dev.aegis.client.gui;

import dev.aegis.client.Aegis;
import dev.aegis.client.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class HudOverlay {

    private final MinecraftClient mc = MinecraftClient.getInstance();

    public void render(DrawContext context) {
        if (mc.player == null || mc.options.hudHidden) return;

        TextRenderer tr = mc.textRenderer;

        // draw watermark
        String watermark = "\u00a76Aegis \u00a77v" + Aegis.VERSION;
        context.drawTextWithShadow(tr, watermark, 4, 4, 0xFFFFFF);

        // draw active modules list (arraylist) on the right side
        List<Module> enabledMods = Aegis.getInstance().getModuleManager().getEnabledModules()
                .stream()
                .sorted(Comparator.comparingInt((Module m) -> tr.getWidth(m.getName())).reversed())
                .collect(Collectors.toList());

        int screenWidth = mc.getWindow().getScaledWidth();
        int y = 4;

        for (int i = 0; i < enabledMods.size(); i++) {
            Module mod = enabledMods.get(i);
            String name = mod.getName();
            int nameWidth = tr.getWidth(name);
            int x = screenWidth - nameWidth - 4;

            // background bar
            int barColor = getModuleColor(i);
            context.fill(x - 2, y - 1, screenWidth, y + 10, 0x90000000);
            context.fill(screenWidth - 2, y - 1, screenWidth, y + 10, barColor);

            context.drawTextWithShadow(tr, name, x, y, barColor);
            y += 11;
        }

        // coordinates display at bottom left
        if (mc.player != null) {
            String coords = String.format("\u00a77XYZ: \u00a7f%.1f \u00a77/ \u00a7f%.1f \u00a77/ \u00a7f%.1f",
                    mc.player.getX(), mc.player.getY(), mc.player.getZ());
            int screenHeight = mc.getWindow().getScaledHeight();
            context.drawTextWithShadow(tr, coords, 4, screenHeight - 14, 0xFFFFFF);

            // fps display
            String fps = "\u00a77FPS: \u00a7f" + mc.getCurrentFps();
            context.drawTextWithShadow(tr, fps, 4, screenHeight - 26, 0xFFFFFF);
        }
    }

    private int getModuleColor(int index) {
        // cycling rainbow-ish color scheme (orange -> gold -> green -> blue)
        float hue = (System.currentTimeMillis() % 5000) / 5000.0f + index * 0.05f;
        hue %= 1.0f;
        return java.awt.Color.HSBtoRGB(hue, 0.7f, 1.0f);
    }
}
