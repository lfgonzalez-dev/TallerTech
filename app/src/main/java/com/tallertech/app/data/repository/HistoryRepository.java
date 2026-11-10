package com.tallertech.app.data.repository;

import android.content.Context;
import com.tallertech.app.data.local.*;
import java.util.List;

public final class HistoryRepository {
    private final TallerTechDatabase db;
    public HistoryRepository(Context context) { db = TallerTechDatabase.getInstance(context); }
    public void load(ResultCallback<List<HistoryEntry>> callback) {
        RepositoryExecutor.run(() -> db.serviceOrderDao().getHistory(), callback);
    }
}
