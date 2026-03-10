package io.drahlek.hearthguard.entity;

public interface SilentStateTracker {
    boolean hearthguard$getOriginalSilent();

    void hearthguard$setOriginalSilent(boolean originalSilent);

    boolean hearthguard$isForcedSilent();

    void hearthguard$setForcedSilent(boolean forcedSilent);
}
