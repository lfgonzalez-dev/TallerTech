package com.tallertech.backend.service;

public final class LastWriteWins {
    private LastWriteWins() {}

    // Equality keeps the stored version, making retries idempotent.
    public static boolean accepts(Long incoming, long stored) {
        return incoming == null || incoming > stored;
    }

    // Legacy API clients without a timestamp still perform a server-timed edit.
    public static long timestamp(Long incoming, long stored) {
        return incoming == null ? Math.max(System.currentTimeMillis(), stored + 1) : incoming;
    }
}
