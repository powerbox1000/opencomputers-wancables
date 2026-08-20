package io.github.powerbox1000.wancables.blocks;

import com.mojang.serialization.MapCodec;

import io.github.powerbox1000.wancables.Registry;
import io.github.powerbox1000.wancables.network.NetworkGraph;
import io.github.powerbox1000.wancables.network.NetworkManager;
import io.github.powerbox1000.wancables.network.NetworkSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

import static net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED;

public class CableBlock extends PipeBlock implements SimpleWaterloggedBlock {
    public static final MapCodec<CableBlock> CODEC = simpleCodec(CableBlock::new);

    @Override
    protected MapCodec<? extends PipeBlock> codec() {
        return CODEC;
    }

    public CableBlock(BlockBehaviour.Properties properties) {
        super(0.125F, properties);
        registerDefaultState(
            stateDefinition.any()
                .setValue(NORTH, false)
                .setValue(EAST, false)
                .setValue(SOUTH, false)
                .setValue(WEST, false)
                .setValue(UP, false)
                .setValue(DOWN, false)
                .setValue(WATERLOGGED, false)
        );
    }

    private void onCreate(LevelAccessor levelAccess, BlockPos placedAt) {
        // Only need to add self to graph; neighbors will connect to us
        if (levelAccess instanceof ServerLevel level) {
            NetworkManager.getGraphForDimension(level.dimension()).insertNode(placedAt.asLong(), false);
            NetworkManager.getSavedDataForDimension(level.dimension()).setDirty();
        }
    }

    private void onConnectionsUpdated(LevelAccessor levelAccess, BlockPos ownPos, Direction changedDirection, boolean wasConnected, boolean isNowConnected) {
        if (levelAccess instanceof ServerLevel level && wasConnected != isNowConnected) {
            NetworkSavedData savedData = NetworkManager.getSavedDataForDimension(level.dimension());
            NetworkGraph graph = NetworkManager.getGraphForDimension(level.dimension());
            long self = ownPos.asLong();
            long neighbor = ownPos.relative(changedDirection).asLong();
            
            if (isNowConnected) {
                graph.connectNodes(self, neighbor);
                savedData.setDirty();
            } else if (graph.exists(neighbor) && graph.isConnected(self, neighbor)) {
                graph.disconnectNodes(self, neighbor);
                savedData.setDirty();
            }
        }
    }

    public void blockBroken(BlockState oldState, BlockState newState, LevelAccessor levelAccess, BlockPos blockPos) {
        if (levelAccess instanceof ServerLevel level) {
            NetworkManager.getGraphForDimension(level.dimension()).removeNode(blockPos.asLong());
            NetworkManager.getSavedDataForDimension(level.dimension()).setDirty();
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN, WATERLOGGED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        FluidState fluidState = blockPlaceContext.getLevel().getFluidState(blockPlaceContext.getClickedPos());
        var state = getStateWithConnections(blockPlaceContext.getLevel(), blockPlaceContext.getClickedPos(), this.defaultBlockState())
            .setValue(WATERLOGGED, fluidState.is(Fluids.WATER));
        try {
            onCreate(blockPlaceContext.getLevel(), blockPlaceContext.getClickedPos());
        } catch (Exception e) {
            throw new Error(e);
        }
        return state;
    }

    private static boolean canConnectTo(BlockState blockState, BlockState otherBlockState, BlockGetter blockGetter, BlockPos blockPos) {
        // TODO there HAS to be a better way
        return otherBlockState.is(Registry.CABLE_BLOCK.block()) || (otherBlockState.is(Registry.MODEM_BLOCK.block()) && (
               (otherBlockState.getValue(FACING) == Direction.NORTH && blockGetter.getBlockState(blockPos.north()) == otherBlockState)
            || (otherBlockState.getValue(FACING) == Direction.SOUTH && blockGetter.getBlockState(blockPos.south()) == otherBlockState)
            || (otherBlockState.getValue(FACING) == Direction.EAST && blockGetter.getBlockState(blockPos.east()) == otherBlockState)
            || (otherBlockState.getValue(FACING) == Direction.WEST && blockGetter.getBlockState(blockPos.west()) == otherBlockState)
        ));
    }

    private static BlockState getStateWithConnections(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState) {
        BlockState up = blockGetter.getBlockState(blockPos.above());
        BlockState down = blockGetter.getBlockState(blockPos.below());
        BlockState north = blockGetter.getBlockState(blockPos.north());
        BlockState south = blockGetter.getBlockState(blockPos.south());
        BlockState east = blockGetter.getBlockState(blockPos.east());
        BlockState west = blockGetter.getBlockState(blockPos.west());

        return blockState
            .trySetValue(UP, canConnectTo(blockState, up, blockGetter, blockPos))
            .trySetValue(DOWN, canConnectTo(blockState, down, blockGetter, blockPos))
            .trySetValue(NORTH, canConnectTo(blockState, north, blockGetter, blockPos))
            .trySetValue(SOUTH, canConnectTo(blockState, south, blockGetter, blockPos))
            .trySetValue(EAST, canConnectTo(blockState, east, blockGetter, blockPos))
            .trySetValue(WEST, canConnectTo(blockState, west, blockGetter, blockPos));
    }

    @Override
    protected BlockState updateShape(BlockState blockState, Direction direction, BlockState otherBlockState, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos otherBlockPos) {
        if (blockState.getValue(WATERLOGGED)) {
            levelAccessor.scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelAccessor));
        }

        boolean wasConnected = blockState.getValue(PROPERTY_BY_DIRECTION.get(direction));
        boolean canConnect = canConnectTo(blockState, otherBlockState, levelAccessor, blockPos);
        BlockState state = blockState.setValue(PROPERTY_BY_DIRECTION.get(direction), canConnect);
        try {
            onConnectionsUpdated(levelAccessor, blockPos, direction, wasConnected, canConnect);
        } catch (Exception e) {
            throw new Error(e);
        }
        return state;
    }

    protected FluidState getFluidState(BlockState blockState) {
        return blockState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(true) : super.getFluidState(blockState);
    }
}
