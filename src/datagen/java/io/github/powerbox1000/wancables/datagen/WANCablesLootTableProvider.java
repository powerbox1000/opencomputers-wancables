package io.github.powerbox1000.wancables.datagen;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import io.github.powerbox1000.wancables.datagen.loot.*;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

public class WANCablesLootTableProvider extends LootTableProvider {
    private static List<SubProviderEntry> providers = List.of(
        new SubProviderEntry(WANCablesBlockLootSubProvider::new, LootContextParamSets.BLOCK)
    );

    public WANCablesLootTableProvider(PackOutput output, CompletableFuture<Provider> lookupProvider) {
        super(output, Set.of(), providers, lookupProvider);
    }
}
