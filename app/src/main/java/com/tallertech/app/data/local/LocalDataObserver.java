package com.tallertech.app.data.local;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.*;
import androidx.room.InvalidationTracker;
import java.util.Set;

public final class LocalDataObserver {
    private LocalDataObserver() {}
    public static void observe(Fragment fragment, Runnable reload) {
        TallerTechDatabase db = TallerTechDatabase.getInstance(fragment.requireContext());
        Lifecycle lifecycle = fragment.getViewLifecycleOwner().getLifecycle();
        Handler main = new Handler(Looper.getMainLooper());
        InvalidationTracker.Observer observer = new InvalidationTracker.Observer(
                "clients", "equipment", "service_orders", "diagnoses", "repairs") {
            @Override public void onInvalidated(@NonNull Set<String> tables) {
                main.post(() -> {
                    if (lifecycle.getCurrentState().isAtLeast(Lifecycle.State.STARTED)) reload.run();
                });
            }
        };
        lifecycle.addObserver(new DefaultLifecycleObserver() {
            @Override public void onStart(@NonNull LifecycleOwner owner) { db.getInvalidationTracker().addObserver(observer); reload.run(); }
            @Override public void onStop(@NonNull LifecycleOwner owner) { db.getInvalidationTracker().removeObserver(observer); }
        });
    }
}
