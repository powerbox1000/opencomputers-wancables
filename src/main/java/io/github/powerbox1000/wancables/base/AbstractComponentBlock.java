package io.github.powerbox1000.wancables.base;

import static io.github.powerbox1000.wancables.WANCables.LOGGER;

import li.cil.oc.api.Network;
import li.cil.oc.api.UnrecoverablePersistanceException;
import li.cil.oc.api.driver.DeviceInfo;
import li.cil.oc.api.network.Connector;
import li.cil.oc.api.network.Visibility;
import li.cil.oc.api.prefab.BlockEntityEnvironment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/*
 * Based on https://github.com/PC-Logix/OpenSecurity/blob/1.21.1/src/main/java/pcl/opensecurity/blockentity/SecurityBlockEntity.java by PC-Logix
 * Licensed under the MIT License
 */
public abstract class AbstractComponentBlock extends BlockEntityEnvironment implements DeviceInfo {
    private static final String OC_NODE_TAG = "oc:node";

    protected AbstractComponentBlock(BlockEntityType<?> type, BlockPos pos, BlockState state, String componentName, double buffer) {
        this(type, pos, state, componentName, buffer, Visibility.Network, Visibility.Network);
    }

    protected AbstractComponentBlock(BlockEntityType<?> type, BlockPos pos, BlockState state, String componentName, double buffer, Visibility nodeVisibility, Visibility componentVisibility) {
        super(type, pos, state);
        node = Network.newNode(this, nodeVisibility)
            .withComponent(componentName, componentVisibility)
            .withConnector(buffer)
            .create();
    }

    @Override
    public void onLoad(){
        if (node == null) return;
        super.onLoad();
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        if (node != null && node.host() == this && tag.contains(OC_NODE_TAG, Tag.TAG_COMPOUND)) {
            try {
                node.loadData(tag.getCompound(OC_NODE_TAG), provider);
            } catch (UnrecoverablePersistanceException exception) {
                LOGGER.warn("Could not restore the OpenComputers component address at {}", worldPosition, exception);
            }
        }
    }

    protected boolean consumeEnergy(double amount) {
        return node instanceof Connector connector && connector.tryChangeBuffer(-amount);
    }
}