package com.arcana.mod.wand;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// Debug toggle: players in this set get a step-by-step trace of the engine in chat each time
// they use a wand. Toggled with /arcana trace; not persisted across restarts.
public final class SpellTrace {
    private static final Set<UUID> TRACING = ConcurrentHashMap.newKeySet();

    private SpellTrace() {}

    public static boolean isTracing(UUID player) {
        return TRACING.contains(player);
    }

    // Returns the new state
    public static boolean toggle(UUID player) {
        if (TRACING.remove(player)) return false;
        TRACING.add(player);
        return true;
    }
}
