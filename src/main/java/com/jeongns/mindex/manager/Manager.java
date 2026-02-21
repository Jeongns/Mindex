package com.jeongns.mindex.manager;

public interface Manager {
    default void initialize() {
    }

    default void reload() {
    }

    default void shutdown() {
    }
}
