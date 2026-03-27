package dev.aegis.client.command;

import dev.aegis.client.Aegis;
import dev.aegis.client.module.Module;
import dev.aegis.client.util.ChatHelper;

public class ToggleCommand extends Command {

    public ToggleCommand() {
        super("toggle", "Toggles a module on or off", "toggle <module>");
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 1) {
            ChatHelper.error("Usage: .toggle <module>");
            return;
        }

        Module mod = Aegis.getInstance().getModuleManager().getModule(args[0]);
        if (mod == null) {
            ChatHelper.error("Module not found: " + args[0]);
            return;
        }

        mod.toggle();
    }
}
