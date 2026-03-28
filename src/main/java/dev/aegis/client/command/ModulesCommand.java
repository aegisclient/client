package dev.aegis.client.command;

import dev.aegis.client.Aegis;
import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import dev.aegis.client.util.ChatHelper;

import java.util.List;

public class ModulesCommand extends Command {

    public ModulesCommand() {
        super("modules", "Shows all modules with descriptions and keybinds", "modules [category]");
    }

    @Override
    public void execute(String[] args) {
        if (args.length > 0) {
            // Show modules for a specific category
            String catName = args[0];
            Category target = null;
            for (Category cat : Category.values()) {
                if (cat.getDisplayName().equalsIgnoreCase(catName)) {
                    target = cat;
                    break;
                }
            }
            if (target == null) {
                ChatHelper.error("Unknown category: " + catName);
                ChatHelper.info("Categories: Combat, Movement, Render, Player, World, Exploit, Fun, Misc, Premium");
                return;
            }
            showCategory(target);
        } else {
            // Show summary of all categories
            ChatHelper.info("\u00a76\u00a7l=== Aegis Modules ===");
            ChatHelper.info("");
            for (Category cat : Category.values()) {
                List<Module> mods = Aegis.getInstance().getModuleManager().getModulesByCategory(cat);
                if (mods.isEmpty()) continue;
                ChatHelper.info("\u00a7e" + cat.getDisplayName() + " \u00a77(" + mods.size() + " modules)");
            }
            ChatHelper.info("");
            ChatHelper.info("\u00a77Use \u00a7e.modules <category>\u00a77 for details");
            ChatHelper.info("");
            ChatHelper.info("\u00a76\u00a7l=== Quick Reference ===");
            ChatHelper.info("\u00a7eRight Shift \u00a77- Open ClickGUI");
            ChatHelper.info("\u00a7eT \u00a77- Teleport (look at block, press T)");
            ChatHelper.info("\u00a7eF \u00a77- Flight (toggle, use Space/Shift to go up/down)");
            ChatHelper.info("\u00a7eR \u00a77- KillAura (auto-attacks nearby entities)");
            ChatHelper.info("\u00a7eV \u00a77- Speed (faster movement)");
            ChatHelper.info("\u00a7eG \u00a77- Scaffold (auto-places blocks under you)");
            ChatHelper.info("\u00a7eJ \u00a77- Jesus (walk on water)");
            ChatHelper.info("\u00a7eX \u00a77- X-Ray (see ores through blocks)");
            ChatHelper.info("\u00a7eB \u00a77- Fullbright (see in the dark)");
            ChatHelper.info("\u00a7eN \u00a77- Nuker (break blocks around you)");
            ChatHelper.info("");
            ChatHelper.info("\u00a77Hover modules in ClickGUI for descriptions.");
            ChatHelper.info("\u00a77Use \u00a7e.bind <module> <key>\u00a77 to rebind.");
        }
    }

    private void showCategory(Category cat) {
        List<Module> mods = Aegis.getInstance().getModuleManager().getModulesByCategory(cat);
        ChatHelper.info("\u00a76\u00a7l=== " + cat.getDisplayName() + " (" + mods.size() + ") ===");
        for (Module mod : mods) {
            String status = mod.isEnabled() ? "\u00a7a[ON]" : "\u00a7c[OFF]";
            String bind = "";
            if (mod.getKeyBind() != 0) {
                String keyName = org.lwjgl.glfw.GLFW.glfwGetKeyName(mod.getKeyBind(), 0);
                if (keyName != null) bind = " \u00a7d[" + keyName.toUpperCase() + "]";
            }
            ChatHelper.info(status + " \u00a7f" + mod.getName() + bind + " \u00a77- " + mod.getDescription());
        }
    }
}
