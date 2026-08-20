package io.github.powerbox1000.wancables.datagen;

import java.util.concurrent.CompletableFuture;

import io.github.powerbox1000.wancables.Registry;
import li.cil.oc.common.init.OCBlocks;
import li.cil.oc.common.init.OCItems;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;

class WANCablesRecipeProvider extends RecipeProvider {
    public WANCablesRecipeProvider(PackOutput output, CompletableFuture<Provider> registries) {
        super(output, registries);
    }
    
    @Override
    protected void buildRecipes(RecipeOutput output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Registry.MODEM_BLOCK.item())
            .pattern("ITI")
            .pattern("DMR")
            .pattern("ITI")
            .define('I', Items.IRON_NUGGET)
            .define('T', OCItems.Transistor())
            .define('D', OCItems.DataCardTier1())
            .define('M', OCItems.ChipTier1())
            .define('R', OCItems.RAMTier1())
            .unlockedBy("has_item", has(OCItems.ChipTier1()))
            .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Registry.CABLE_BLOCK.item(), 4)
            .pattern(" I ")
            .pattern("IRI")
            .pattern(" I ")
            .define('I', Items.IRON_INGOT)
            .define('R', Items.REDSTONE_BLOCK)
            .unlockedBy("has_item", has(OCBlocks.Cable().asItem()))
            .save(output);
    }
}
