package org.example;

import java.util.Optional;

public interface OffsetStore extends GenericOffsetStore<BinlogFileOffset> {

    Optional<BinlogFileOffset> getLastBinlogFileOffset();

    void save(BinlogFileOffset binlogFileOffset);
}
