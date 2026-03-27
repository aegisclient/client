package dev.aegis.client.command;

import dev.aegis.client.Aegis;
import dev.aegis.client.util.ChatHelper;

public class ConfigCommand extends Command {

    public ConfigCommand() {
        super("config", "Save or load module configurations", "config <save/load/list> [name]");
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 1) {
            ChatHelper.error("Usage: .config <save/load/list> [name]");
            return;
        }

        switch (args[0].toLowerCase()) {
            case "save" -> {
                String name = args.length > 1 ? args[1] : "default";
                Aegis.getInstance().getConfigManager().saveConfig(name);
                ChatHelper.info("Saved config: \u00a7e" + name);
            }
            case "load" -> {
                String name = args.length > 1 ? args[1] : "default";
                if (Aegis.getInstance().getConfigManager().loadConfig(name)) {
                    ChatHelper.info("Loaded config: \u00a7e" + name);
                } else {
                    ChatHelper.error("Config not found: " + name);
                }
            }
            case "list" -> {
                var configs = Aegis.getInstance().getConfigManager().listConfigs();
                if (configs.isEmpty()) {
                    ChatHelper.info("No saved configs.");
                } else {
                    ChatHelper.info("\u00a76Configs: \u00a7f" + String.join(", ", configs));
                }
            }
            default -> ChatHelper.error("Usage: .config <save/load/list> [name]");
        }
    }
}
