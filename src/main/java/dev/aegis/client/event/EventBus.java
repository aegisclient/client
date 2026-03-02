package dev.aegis.client.event;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class EventBus {

    private final List<Object> subscribers = new CopyOnWriteArrayList<>();

    public void subscribe(Object listener) {
        if (!subscribers.contains(listener)) {
            subscribers.add(listener);
        }
    }

    public void unsubscribe(Object listener) {
        subscribers.remove(listener);
    }

    public void post(Event event) {
        for (Object sub : subscribers) {
            if (sub instanceof EventListener) {
                ((EventListener) sub).onEvent(event);
            }
        }
    }

    public List<Object> getSubscribers() {
        return subscribers;
    }
}
