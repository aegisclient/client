package dev.aegis.client.command;

import dev.aegis.client.Aegis;
import dev.aegis.client.util.ChatHelper;

import java.util.ArrayList;
import java.util.List;

public class CommandManager {

    private final List<Command> commands = new ArrayList<>();
    private String prefix = ".";

    public void init() {
        register(new HelpCommand());
        register(new BindCommand());
        register(new ToggleCommand());
        register(new FriendCommand());
        register(new ConfigCommand());
        register(new VClipCommand());
        register(new HClipCommand());
        register(new PrefixCommand());

        Aegis.LOGGER.info("Registered {} commands", commands.size());
    }

    private void register(Command cmd) {
        commands.add(cmd);
    }

    public boolean handleChat(String message) {
        if (!message.startsWith(prefix)) return false;

        String stripped = message.substring(prefix.length()).trim();
        if (stripped.isEmpty()) return true;

        String[] parts = stripped.split("\\s+");
        String cmdName = parts[0].toLowerCase();
        String[] args = new String[parts.length - 1];
        System.arraycopy(parts, 1, args, 0, args.length);

        for (Command cmd : commands) {
            if (cmd.getName().equalsIgnoreCase(cmdName)) {
                try {
                    cmd.execute(args);
                } catch (Exception e) {
                    ChatHelper.error("Error: " + e.getMessage());
                    ChatHelper.info("Usage: " + prefix + cmd.getSyntax());
                }
                return true;
            }
        }

        ChatHelper.error("Unknown command: " + cmdName + ". Type " + prefix + "help for a list.");
        return true;
    }

    public List<Command> getCommands() { return commands; }
    public String getPrefix() { return prefix; }
    public void setPrefix(String prefix) { this.prefix = prefix; }
}
