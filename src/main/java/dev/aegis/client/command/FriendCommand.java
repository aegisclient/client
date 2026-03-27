package dev.aegis.client.command;

import dev.aegis.client.Aegis;
import dev.aegis.client.util.ChatHelper;

public class FriendCommand extends Command {

    public FriendCommand() {
        super("friend", "Manage friends list", "friend <add/remove/list> [name]");
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 1) {
            ChatHelper.error("Usage: .friend <add/remove/list> [name]");
            return;
        }

        switch (args[0].toLowerCase()) {
            case "add" -> {
                if (args.length < 2) {
                    ChatHelper.error("Usage: .friend add <name>");
                    return;
                }
                Aegis.getInstance().getFriendManager().addFriend(args[1]);
                ChatHelper.info("Added \u00a7e" + args[1] + "\u00a7f to friends");
            }
            case "remove", "del" -> {
                if (args.length < 2) {
                    ChatHelper.error("Usage: .friend remove <name>");
                    return;
                }
                Aegis.getInstance().getFriendManager().removeFriend(args[1]);
                ChatHelper.info("Removed \u00a7e" + args[1] + "\u00a7f from friends");
            }
            case "list" -> {
                var friends = Aegis.getInstance().getFriendManager().getFriends();
                if (friends.isEmpty()) {
                    ChatHelper.info("No friends added.");
                } else {
                    ChatHelper.info("\u00a76Friends (" + friends.size() + "): \u00a7f" + String.join(", ", friends));
                }
            }
            default -> ChatHelper.error("Usage: .friend <add/remove/list> [name]");
        }
    }
}
