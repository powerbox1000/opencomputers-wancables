package io.github.powerbox1000.wancables.datagen.loot;

import java.util.Set;

import io.github.powerbox1000.wancables.Registry;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;

public class WANCablesBlockLootSubProvider extends BlockLootSubProvider {
    public WANCablesBlockLootSubProvider(HolderLookup.Provider lookupProvider) {
        super(Set.of(), FeatureFlags.DEFAULT_FLAGS, lookupProvider);
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return Registry.BLOCKS.getEntries()
            .stream()
            .map(e -> (Block) e.value())
            .toList();
    }

    @Override
    protected void generate() {
        dropSelf(Registry.CABLE_BLOCK.block().get());
        dropSelf(Registry.MODEM_BLOCK.block().get());
    }
}
