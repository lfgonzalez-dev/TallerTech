package com.tallertech.app.data.sync;

import java.io.IOException;
import java.util.List;

abstract class SyncResource<E> {
    final String name;
    SyncResource(String name) { this.name = name; }
    abstract String id(E entity);
    abstract long time(E entity);
    abstract String status(E entity);
    abstract E local(String id);
    abstract List<E> pending();
    abstract void insert(E entity);
    abstract void update(E entity);
    abstract void mark(String id, long time, String status);
    abstract List<E> download() throws IOException;
    abstract E remote(String id) throws IOException;
    abstract E upload(E entity, boolean exists) throws IOException;
    boolean parentsReady(E entity) { return true; }
}
