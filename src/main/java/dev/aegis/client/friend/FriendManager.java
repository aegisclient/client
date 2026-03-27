package dev.aegis.client.friend;

import java.util.ArrayList;
import java.util.List;

public class FriendManager {

    private final List<String> friends = new ArrayList<>();

    public void addFriend(String name) {
        if (!friends.contains(name.toLowerCase())) {
            friends.add(name.toLowerCase());
        }
    }

    public void removeFriend(String name) {
        friends.remove(name.toLowerCase());
    }

    public boolean isFriend(String name) {
        return friends.contains(name.toLowerCase());
    }

    public List<String> getFriends() {
        return friends;
    }

    public void clearFriends() {
        friends.clear();
    }
}
