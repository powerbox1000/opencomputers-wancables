package io.github.powerbox1000.wancables;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

import io.github.powerbox1000.wancables.blocks.*;
import io.github.powerbox1000.wancables.blocks.blockentity.*;

@EventBusSubscriber(modid = WANCables.MODID)
public class Registry {
    public static record DeferredBlockItemPairHolder<T extends Block>(DeferredBlock<T> block, DeferredItem<BlockItem> item) {}

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(WANCables.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(WANCables.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, WANCables.MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, WANCables.MODID);

    public static final DeferredBlockItemPairHolder<CableBlock> CABLE_BLOCK = registerBlockItemPair("cable", () -> new CableBlock(
        BlockBehaviour.Properties.of()
            .requiresCorrectToolForDrops()
            .strength(2.0F, 6.0F)
            .mapColor(MapColor.METAL)
            .sound(SoundType.METAL)
            .noOcclusion()
            .pushReaction(PushReaction.DESTROY)
    ));

    public static final DeferredBlockItemPairHolder<ModemBlock> MODEM_BLOCK = registerBlockItemPair("modem", () -> new ModemBlock(
        BlockBehaviour.Properties.of()
            .requiresCorrectToolForDrops()
            .strength(3.0F, 6.0F)
            .mapColor(MapColor.METAL)
            .sound(SoundType.METAL)
            .pushReaction(PushReaction.BLOCK)
    ));
    public static final Supplier<BlockEntityType<ModemBlockEntity>> MODEM_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("modem", () -> BlockEntityType.Builder.of(ModemBlockEntity::new, MODEM_BLOCK.block().get()).build(null));

    private static <T extends Block> DeferredBlockItemPairHolder<T> registerBlockItemPair(String id, Supplier<T> blockInstanceSupplier) {
        final DeferredBlock<T> block = BLOCKS.register(id, blockInstanceSupplier);
        final DeferredItem<BlockItem> item = ITEMS.registerSimpleBlockItem(id, block);
        return new DeferredBlockItemPairHolder<T>(block, item);
    }

    private static void registerCreativeTabs() {
        CREATIVE_MODE_TABS.register("wancables_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.wancables"))
            .icon(() -> CABLE_BLOCK.item().get().getDefaultInstance()) // TODO change icon
            .displayItems((parameters, output) -> {
                for (var item : ITEMS.getEntries()) {
                    output.accept(item.get());
                }
            })
            .build()
        );
    }
    
    public static void registerAll(IEventBus modEventBus) {
        registerCreativeTabs();

        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITY_TYPES.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
    }

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(li.cil.oc.common.Capabilities.EnvironmentCapability(), MODEM_BLOCK_ENTITY.get(), (be, side) -> be);
    }
}
