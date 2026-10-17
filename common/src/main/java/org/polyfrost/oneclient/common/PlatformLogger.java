package org.polyfrost.oneclient.common;

import java.util.logging.Level;
import java.util.logging.Logger;

public interface PlatformLogger {
    void debug(String message);

    void error(String message, Throwable thrown);

    static PlatformLogger of(Logger logger) {
        return new PlatformLogger() {
            @Override
            public void debug(String message) {
                logger.log(Level.FINE, message);
            }

            @Override
            public void error(String message, Throwable thrown) {
                logger.log(Level.SEVERE, message, thrown);
            }
        };
    }
}
