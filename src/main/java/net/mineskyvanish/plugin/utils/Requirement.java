package net.mineskyvanish.plugin.utils;

public interface Requirement<T> {
    boolean fulfilledBy(T testSubject);
}
