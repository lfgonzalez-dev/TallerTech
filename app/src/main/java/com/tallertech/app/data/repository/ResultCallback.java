package com.tallertech.app.data.repository;

public interface ResultCallback<T> {
    void onResult(T result);
    void onError(Exception e);
}
