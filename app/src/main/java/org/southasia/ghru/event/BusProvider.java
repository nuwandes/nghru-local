package org.southasia.ghru.event;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

/**
 * Maintains a singleton instance for obtaining the bus. Ideally this would be replaced with a more efficient means
 * such as through injection directly into interested classes.
 */
public final class BusProvider {

    private static Bus bus = null;

    private BusProvider() {
    }

    public static synchronized Bus getInstance() {
        if (bus == null) {
            bus = new Bus(ThreadEnforcer.MAIN);
        }
        return bus;
    }
}