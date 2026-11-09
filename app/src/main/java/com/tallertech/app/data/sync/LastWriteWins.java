package com.tallertech.app.data.sync;

public final class LastWriteWins {
    private LastWriteWins() {}
    public static boolean remoteWins(long local, long remote) { return remote >= local; }
    public static long nextLocalTimestamp(long previous) {
        return Math.max(System.currentTimeMillis(), previous + 1);
    }
}
