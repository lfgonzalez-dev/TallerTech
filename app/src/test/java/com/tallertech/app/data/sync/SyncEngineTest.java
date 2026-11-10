package com.tallertech.app.data.sync;

import org.junit.Test;
import java.io.IOException;
import java.util.*;
import static org.junit.Assert.*;

public class SyncEngineTest {
    static final class Record {
        final long time;
        String status;
        Record(long time, String status) { this.time = time; this.status = status; }
    }
    static final class Resource extends SyncResource<Record> {
        Record local, remote;
        IOException failure;
        IOException uploadFailure;
        Runnable duringUpload;
        boolean parentReady = true;
        int posts, puts;
        List<String> order;
        Resource(String name) { super(name); }
        String id(Record e) { return name; }
        long time(Record e) { return e.time; }
        String status(Record e) { return e.status; }
        Record local(String id) { return local; }
        List<Record> pending() { return local != null && SyncStatus.PENDING.equals(local.status) ? List.of(local) : List.of(); }
        void insert(Record e) { local = e; }
        void update(Record e) { local = e; }
        void mark(String id, long time, String status) { if (local != null && local.time == time) local.status = status; }
        List<Record> download() throws IOException {
            if (order != null) order.add(name);
            if (failure != null) throw failure;
            return remote == null ? List.of() : List.of(remote);
        }
        Record remote(String id) { return remote; }
        Record upload(Record e, boolean exists) throws IOException {
            if (exists) puts++; else posts++;
            if (duringUpload != null) duringUpload.run();
            if (uploadFailure != null) throw uploadFailure;
            remote = new Record(e.time, SyncStatus.SYNCED);
            return remote;
        }
        boolean parentsReady(Record e) { return parentReady; }
    }
    private boolean run(Resource... resources) throws IOException {
        return new SyncEngine(Arrays.asList(resources), Runnable::run, () -> {}, message -> {}).synchronize();
    }
    @Test public void timestampComparisonAndEqualityFavorServer() {
        assertTrue(LastWriteWins.remoteWins(10, 11));
        assertTrue(LastWriteWins.remoteWins(10, 10));
        assertFalse(LastWriteWins.remoteWins(11, 10));
        assertTrue(LastWriteWins.nextLocalTimestamp(Long.MAX_VALUE - 2) > Long.MAX_VALUE - 2);
    }
    @Test public void absentRemoteIsPostedWithOriginalVersion() throws Exception {
        Resource resource = new Resource("Client"); resource.local = new Record(10, SyncStatus.PENDING);
        assertTrue(run(resource)); assertEquals(1, resource.posts); assertEquals(0, resource.puts);
        assertEquals(10, resource.local.time); assertEquals(SyncStatus.SYNCED, resource.local.status);
    }
    @Test public void newerLocalUsesPut() throws Exception {
        Resource resource = new Resource("Client"); resource.local = new Record(20, SyncStatus.PENDING);
        resource.remote = new Record(10, SyncStatus.SYNCED);
        run(resource); assertEquals(1, resource.puts); assertEquals(20, resource.remote.time);
    }
    @Test public void newerRemoteAndEqualRemoteAreDownloadedWithoutUpload() throws Exception {
        for (long timestamp : new long[]{10, 20}) {
            Resource resource = new Resource("Client"); resource.local = new Record(10, SyncStatus.PENDING);
            resource.remote = new Record(timestamp, SyncStatus.SYNCED);
            run(resource); assertEquals(timestamp, resource.local.time); assertEquals(SyncStatus.SYNCED, resource.local.status);
            assertEquals(0, resource.puts + resource.posts);
        }
    }
    @Test public void editDuringUploadStaysPending() throws Exception {
        Resource resource = new Resource("Client"); resource.local = new Record(10, SyncStatus.PENDING);
        resource.duringUpload = () -> resource.local = new Record(11, SyncStatus.PENDING);
        run(resource); assertEquals(11, resource.local.time); assertEquals(SyncStatus.PENDING, resource.local.status);
    }
    @Test public void networkFailureDoesNotMarkError() {
        Resource resource = new Resource("Client"); resource.local = new Record(10, SyncStatus.PENDING);
        resource.failure = new IOException("offline");
        try { run(resource); fail(); } catch (IOException expected) { assertEquals(SyncStatus.PENDING, resource.local.status); }
    }
    @Test public void unauthorizedStopsBeforeChildren() {
        Resource parent = new Resource("Client"), child = new Resource("Equipment");
        List<String> calls = new ArrayList<>(); parent.order = calls; child.order = calls;
        parent.failure = new SyncHttp.Failure(401);
        try { run(parent, child); fail(); } catch (IOException expected) { assertEquals(List.of("Client"), calls); }
    }
    @Test public void pendingParentDefersChild() throws Exception {
        Resource child = new Resource("Equipment"); child.local = new Record(10, SyncStatus.PENDING);
        child.parentReady = false; assertFalse(run(child)); assertEquals(0, child.posts);
        assertEquals(SyncStatus.PENDING, child.local.status);
    }
    @Test public void resourceOrderIsPreserved() throws Exception {
        List<String> calls = new ArrayList<>();
        Resource[] resources = new Resource[5];
        String[] names = {"Client", "Equipment", "ServiceOrder", "Diagnosis", "Repair"};
        for (int i = 0; i < names.length; i++) { resources[i] = new Resource(names[i]); resources[i].order = calls; }
        run(resources); assertEquals(Arrays.asList(names), calls);
        List<String> production = new ArrayList<>();
        for (SyncResource<?> resource : SyncResources.ordered(null, null)) production.add(resource.name);
        assertEquals(Arrays.asList(names), production);
    }
    @Test public void invalidDataMarksOnlyUnchangedVersionAsError() throws Exception {
        Resource resource = new Resource("Client"); resource.local = new Record(10, SyncStatus.PENDING);
        resource.uploadFailure = new SyncHttp.Failure(400);
        assertFalse(run(resource)); assertEquals(SyncStatus.ERROR, resource.local.status);
        resource.local = new Record(11, SyncStatus.PENDING);
        resource.duringUpload = () -> resource.local = new Record(12, SyncStatus.PENDING);
        assertFalse(run(resource)); assertEquals(SyncStatus.PENDING, resource.local.status);
    }
    @Test public void transientUploadFailurePreservesPending() {
        Resource resource = new Resource("Client"); resource.local = new Record(10, SyncStatus.PENDING);
        resource.uploadFailure = new SyncHttp.Failure(500);
        try { run(resource); fail(); } catch (IOException expected) { assertEquals(SyncStatus.PENDING, resource.local.status); }
    }
    @Test public void httpErrorsHaveDistinctRetryAndRecordPolicies() {
        assertTrue(new SyncHttp.Failure(500).retryable()); assertTrue(new SyncHttp.Failure(429).retryable());
        assertTrue(new SyncHttp.Failure(409).recordError()); assertTrue(new SyncHttp.Failure(400).recordError());
        assertFalse(new SyncHttp.Failure(401).recordError()); assertFalse(new SyncHttp.Failure(403).retryable());
        assertFalse(new SyncHttp.Failure(500).recordError());
    }
}
