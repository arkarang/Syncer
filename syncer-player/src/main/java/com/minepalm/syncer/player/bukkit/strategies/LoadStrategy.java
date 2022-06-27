package com.minepalm.syncer.player.bukkit.strategies;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface LoadStrategy {

    CompletableFuture<Void> onLoad(UUID uuid);

    CompletableFuture<Void> onUnload(UUID uuid);

    void onApply(UUID uuid);
}
