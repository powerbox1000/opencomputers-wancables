package io.github.powerbox1000.wancables.datagen;

import java.util.concurrent.CompletableFuture;

import io.github.powerbox1000.wancables.Registry;
import io.github.powerbox1000.wancables.WANCables;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class WANCablesBlockTagProvider extends BlockTagsProvider {
    public WANCablesBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, WANCables.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider lookupProvider) {
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
            .add(Registry.CABLE_BLOCK.block().get())
            .add(Registry.MODEM_BLOCK.block().get());

        tag(BlockTags.NEEDS_IRON_TOOL)
            .add(Registry.CABLE_BLOCK.block().get())
            .add(Registry.MODEM_BLOCK.block().get());
    }
}
