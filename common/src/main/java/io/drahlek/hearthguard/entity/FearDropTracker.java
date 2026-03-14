package io.drahlek.hearthguard.entity;

public interface FearDropTracker {
    boolean hearthguard$hasDroppedFearItem();

    void hearthguard$setDroppedFearItem(boolean droppedFearItem);
}
