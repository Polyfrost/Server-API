package org.polyfrost.oneclient.api;

import java.util.UUID;

public final class OneClient {
    private static volatile OneClientApi api;

    public static OneClientApi getApi() {
        OneClientApi current = api;
        if (current == null) {
            throw new IllegalStateException("The OneClient server API has not been loaded yet");
        }
        return current;
    }

    public static boolean isOneClient(UUID uniqueId) {
        OneClientApi current = api;
        return current != null && current.isOneClient(uniqueId);
    }

    public static synchronized void register(OneClientApi implementation) {
        if (api != null) {
            throw new IllegalStateException("The OneClient server API is already registered");
        }
        api = implementation;
    }

    public static synchronized void unregister() {
        api = null;
    }
}
