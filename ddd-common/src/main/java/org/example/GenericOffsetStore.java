package org.example;

public interface GenericOffsetStore<OFFSET> {
    void save(OFFSET offset);
}
