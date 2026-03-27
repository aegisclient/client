package dev.aegis.client.command;

import dev.aegis.client.Aegis;
import dev.aegis.client.util.ChatHelper;

public class HelpCommand extends Command {

    public HelpCommand() {
        super("help", "Shows all available commands", "help");
    }

    @Override
    public void execute(String[] args) {
        ChatHelper.info("\u00a76=== Aegis Commands ===");
        String prefix = Aegis.getInstance().getCommandManager().getPrefix();
        for (Command cmd : Aegis.getInstance().getCommandManager().getCommands()) {
            ChatHelper.info("\u00a7e" + prefix + cmd.getSyntax() + " \u00a77- " + cmd.getDescription());
        }
    }
}
