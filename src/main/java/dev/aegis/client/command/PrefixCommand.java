package dev.aegis.client.command;

import dev.aegis.client.Aegis;
import dev.aegis.client.util.ChatHelper;

public class PrefixCommand extends Command {

    public PrefixCommand() {
        super("prefix", "Change the command prefix", "prefix <new prefix>");
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 1) {
            ChatHelper.info("Current prefix: \u00a7e" + Aegis.getInstance().getCommandManager().getPrefix());
            return;
        }

        Aegis.getInstance().getCommandManager().setPrefix(args[0]);
        ChatHelper.info("Command prefix set to: \u00a7e" + args[0]);
    }
}
