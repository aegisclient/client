package dev.aegis.client.gui;

import dev.aegis.client.Aegis;
import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClickGui extends Screen {

    private final Map<Category, Boolean> categoryOpen = new HashMap<>();
    private final Map<Category, Integer> categoryScroll = new HashMap<>();
    private int panelWidth = 120;
    private int panelHeight = 18;
    private int moduleHeight = 16;

    // drag state
    private boolean dragging = false;
    private int dragOffsetX, dragOffsetY;
    private Category dragCategory = null;
    private final Map<Category, int[]> categoryPositions = new HashMap<>();

    public ClickGui() {
        super(Text.literal("Aegis"));

        int x = 20;
        for (Category cat : Category.values()) {
            categoryOpen.put(cat, true);
            categoryScroll.put(cat, 0);
            categoryPositions.put(cat, new int[]{x, 30});
            x += panelWidth + 10;
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // dim background slightly
        renderBackground(context, mouseX, mouseY, delta);

        for (Category category : Category.values()) {
            renderPanel(context, category, mouseX, mouseY);
        }

        // draw title
        context.drawCenteredTextWithShadow(
                textRenderer,
                "\u00a76\u00a7lAEGIS \u00a77v" + Aegis.VERSION,
                width / 2, 8, 0xFFFFFF
        );
    }

    private void renderPanel(DrawContext context, Category category, int mouseX, int mouseY) {
        int[] pos = categoryPositions.get(category);
        int x = pos[0];
        int y = pos[1];

        // panel header
        int headerColor = 0xCC1a1a2e;
        context.fill(x, y, x + panelWidth, y + panelHeight, headerColor);
        context.drawCenteredTextWithShadow(
                textRenderer,
                "\u00a7l" + category.getDisplayName(),
                x + panelWidth / 2, y + 5, 0xe67e22
        );

        if (!categoryOpen.get(category)) return;

        List<Module> modules = Aegis.getInstance().getModuleManager().getModulesByCategory(category);
        int yOffset = y + panelHeight;

        for (Module mod : modules) {
            boolean hovered = mouseX >= x && mouseX <= x + panelWidth
                    && mouseY >= yOffset && mouseY <= yOffset + moduleHeight;

            int bgColor = mod.isEnabled() ? 0xBB16213e : 0x992c2c3e;
            if (hovered) bgColor = 0xCC0f3460;

            context.fill(x, yOffset, x + panelWidth, yOffset + moduleHeight, bgColor);

            String modName = mod.getName();
            int textColor = mod.isEnabled() ? 0x2ecc71 : 0xbdc3c7;
            context.drawTextWithShadow(textRenderer, modName, x + 4, yOffset + 4, textColor);

            // keybind display
            if (mod.getKeyBind() != 0) {
                String bind = "[" + org.lwjgl.glfw.GLFW.glfwGetKeyName(mod.getKeyBind(), 0) + "]";
                if (bind.equals("[null]")) bind = "";
                int bindWidth = textRenderer.getWidth(bind);
                context.drawTextWithShadow(textRenderer, bind, x + panelWidth - bindWidth - 4, yOffset + 4, 0x7f8c8d);
            }

            yOffset += moduleHeight;
        }

        // bottom border
        context.fill(x, yOffset, x + panelWidth, yOffset + 2, 0xCCe67e22);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (Category category : Category.values()) {
            int[] pos = categoryPositions.get(category);
            int x = pos[0];
            int y = pos[1];

            // click on header
            if (mouseX >= x && mouseX <= x + panelWidth && mouseY >= y && mouseY <= y + panelHeight) {
                if (button == 0) {
                    dragging = true;
                    dragCategory = category;
                    dragOffsetX = (int) mouseX - x;
                    dragOffsetY = (int) mouseY - y;
                } else if (button == 1) {
                    categoryOpen.put(category, !categoryOpen.get(category));
                }
                return true;
            }

            // click on modules
            if (categoryOpen.get(category)) {
                List<Module> modules = Aegis.getInstance().getModuleManager().getModulesByCategory(category);
                int yOffset = y + panelHeight;

                for (Module mod : modules) {
                    if (mouseX >= x && mouseX <= x + panelWidth
                            && mouseY >= yOffset && mouseY <= yOffset + moduleHeight) {
                        if (button == 0) {
                            mod.toggle();
                        }
                        return true;
                    }
                    yOffset += moduleHeight;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        dragging = false;
        dragCategory = null;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (dragging && dragCategory != null) {
            int[] pos = categoryPositions.get(dragCategory);
            pos[0] = (int) mouseX - dragOffsetX;
            pos[1] = (int) mouseY - dragOffsetY;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
