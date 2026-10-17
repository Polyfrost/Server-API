package org.polyfrost.oneclient.api.event;

import java.util.function.Consumer;

public interface EventBus {
    <E extends OneClientEvent> Subscription subscribe(Class<E> type, Consumer<? super E> listener);

    interface Subscription {
        void unsubscribe();
    }
}
