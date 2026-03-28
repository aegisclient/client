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

    private boolean initialized = false;

    public ClickGui() {
        super(Text.literal("Aegis"));

        // Temporary positions; will be recalculated in init() when we know the screen size
        for (Category cat : Category.values()) {
            categoryOpen.put(cat, true);
            categoryScroll.put(cat, 0);
            categoryPositions.put(cat, new int[]{0, 0});
        }
    }

    @Override
    protected void init() {
        super.init();
        if (!initialized) {
            layoutPanels();
            initialized = true;
        }
    }

    private void layoutPanels() {
        int gap = 8;
        int totalCats = Category.values().length;

        // Calculate how many columns fit on screen
        int cols = Math.min(totalCats, Math.max(1, (width - 20) / (panelWidth + gap)));
        int rows = (int) Math.ceil((double) totalCats / cols);

        // Center the grid horizontally
        int gridWidth = cols * panelWidth + (cols - 1) * gap;
        int startX = (width - gridWidth) / 2;
        int startY = 24;

        // Estimate row height: header + ~12 modules visible
        int rowSpacing = panelHeight + moduleHeight * 12 + 20;

        int idx = 0;
        for (Category cat : Category.values()) {
            int col = idx % cols;
            int row = idx / cols;
            int x = startX + col * (panelWidth + gap);
            int y = startY + row * rowSpacing;
            categoryPositions.put(cat, new int[]{x, y});
            idx++;
        }
    }

    // Tooltip state
    private String hoveredTooltip = null;
    private int tooltipX, tooltipY;

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // dim background slightly
        renderBackground(context, mouseX, mouseY, delta);

        hoveredTooltip = null;

        for (Category category : Category.values()) {
            renderPanel(context, category, mouseX, mouseY);
        }

        // draw title
        context.drawCenteredTextWithShadow(
                textRenderer,
                "\u00a76\u00a7lAEGIS \u00a77v" + Aegis.VERSION,
                width / 2, 8, 0xFFFFFF
        );

        // draw tooltip last (on top of everything)
        if (hoveredTooltip != null) {
            int tw = textRenderer.getWidth(hoveredTooltip);
            int tx = tooltipX + 12;
            int ty = tooltipY - 4;
            // Keep on screen
            if (tx + tw + 6 > width) tx = tooltipX - tw - 12;
            if (ty < 2) ty = 2;
            context.fill(tx - 3, ty - 2, tx + tw + 3, ty + 11, 0xEE101020);
            context.fill(tx - 3, ty - 2, tx + tw + 3, ty - 1, 0xFF3b82f6);
            context.drawTextWithShadow(textRenderer, hoveredTooltip, tx, ty, 0xCCCCCC);
        }
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

            // Set tooltip on hover
            if (hovered && mod.getDescription() != null && !mod.getDescription().isEmpty()) {
                hoveredTooltip = mod.getDescription();
                tooltipX = mouseX;
                tooltipY = mouseY;
            }

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
