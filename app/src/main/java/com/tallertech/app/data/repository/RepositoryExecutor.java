package com.tallertech.app.data.repository;

import android.os.Handler;
import android.os.Looper;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class RepositoryExecutor {
    static final ExecutorService IO = Executors.newSingleThreadExecutor();
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    public static <T> void run(Callable<T> task, ResultCallback<T> callback) {
        IO.execute(() -> {
            try {
                T result = task.call();
                MAIN.post(() -> callback.onResult(result));
            } catch (Exception e) {
                MAIN.post(() -> callback.onError(e));
            }
        });
    }
}
