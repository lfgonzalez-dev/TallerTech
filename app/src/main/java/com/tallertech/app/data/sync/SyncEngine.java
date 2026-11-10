package com.tallertech.app.data.sync;

import android.database.sqlite.SQLiteConstraintException;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

final class SyncEngine {
    interface Transaction { void run(Runnable operation); }
    interface SessionCheck { void check() throws IOException; }
    private final List<SyncResource<?>> resources;
    private final Transaction transaction;
    private final SessionCheck session;
    private final Consumer<String> problem;
    private boolean issues;
    SyncEngine(List<SyncResource<?>> resources, Transaction transaction, SessionCheck session, Consumer<String> problem) {
        this.resources = resources; this.transaction = transaction; this.session = session; this.problem = problem;
    }
    boolean synchronize() throws IOException {
        for (SyncResource<?> resource : resources) { checkSession(); synchronize(resource); }
        return !issues;
    }
    private void checkSession() throws IOException { session.check(); }
    private <E> void synchronize(SyncResource<E> resource) throws IOException {
        for (E remote : resource.download()) {
            checkSession();
            try { merge(resource, remote); }
            catch (SQLiteConstraintException | IllegalArgumentException ex) {
                issues = true;
                problem.accept(resource.name + ": integridad al descargar");
            }
        }
        for (E snapshot : resource.pending()) {
            checkSession();
            if (!resource.parentsReady(snapshot)) { issues = true; continue; }
            try {
                E remote = resource.remote(resource.id(snapshot));
                if (remote != null && LastWriteWins.remoteWins(resource.time(snapshot), resource.time(remote))) {
                    merge(resource, remote);
                    continue;
                }
                E confirmed;
                try { confirmed = resource.upload(snapshot, remote != null); }
                catch (SyncHttp.Failure ex) {
                    // A concurrent POST may have created the same UUID after GET.
                    if (ex.code != 409 || remote != null) throw ex;
                    E concurrent = resource.remote(resource.id(snapshot));
                    if (concurrent == null) throw ex;
                    confirmed = LastWriteWins.remoteWins(resource.time(snapshot), resource.time(concurrent))
                            ? concurrent : resource.upload(snapshot, true);
                }
                if (!resource.id(snapshot).equals(resource.id(confirmed))
                        || resource.time(confirmed) < resource.time(snapshot)) throw new IOException("Confirmación de versión inválida");
                merge(resource, confirmed);
            } catch (SyncHttp.Failure ex) {
                if (!ex.recordError()) throw ex;
                issues = true;
                transaction.run(() -> resource.mark(resource.id(snapshot), resource.time(snapshot), SyncStatus.ERROR));
                problem.accept(resource.name + " " + resource.id(snapshot) + ": datos rechazados por el servidor (HTTP " + ex.code + ")");
            } catch (SQLiteConstraintException | IllegalArgumentException ex) {
                issues = true;
                transaction.run(() -> resource.mark(resource.id(snapshot), resource.time(snapshot), SyncStatus.ERROR));
                problem.accept(resource.name + " " + resource.id(snapshot) + ": conflicto de integridad de datos");
            }
        }
    }
    private <E> void merge(SyncResource<E> resource, E remote) {
        transaction.run(() -> {
            E current = resource.local(resource.id(remote));
            if (current == null) resource.insert(remote);
            else if (LastWriteWins.remoteWins(resource.time(current), resource.time(remote))) resource.update(remote);
            else if (SyncStatus.SYNCED.equals(resource.status(current)))
                resource.mark(resource.id(current), resource.time(current), SyncStatus.PENDING);
        });
    }
}
