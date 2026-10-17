package org.polyfrost.oneclient.common;

import org.polyfrost.oneclient.api.event.EventBus;
import org.polyfrost.oneclient.api.event.OneClientEvent;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public final class SimpleEventBus implements EventBus {
    private final List<Listener<?>> listeners = new CopyOnWriteArrayList<>();
    private final PlatformLogger logger;

    public SimpleEventBus(PlatformLogger logger) {
        this.logger = logger;
    }

    @Override
    public <E extends OneClientEvent> Subscription subscribe(Class<E> type, Consumer<? super E> listener) {
        Listener<E> entry = new Listener<>(type, listener);
        listeners.add(entry);
        return () -> listeners.remove(entry);
    }

    public void post(OneClientEvent event) {
        for (Listener<?> listener : listeners) {
            try {
                listener.accept(event);
            } catch (Throwable t) {
                logger.error("Listener for " + event.getClass().getSimpleName() + " threw an exception", t);
            }
        }
    }

    private static final class Listener<E extends OneClientEvent> {
        private final Class<E> type;
        private final Consumer<? super E> consumer;

        Listener(Class<E> type, Consumer<? super E> consumer) {
            this.type = type;
            this.consumer = consumer;
        }

        void accept(OneClientEvent event) {
            if (type.isInstance(event)) {
                consumer.accept(type.cast(event));
            }
        }
    }
}
