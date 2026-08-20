package io.github.powerbox1000.wancables.blocks.blockentity;

import static io.github.powerbox1000.wancables.WANCables.LOGGER;

import io.github.powerbox1000.wancables.ServerConfig;
import io.github.powerbox1000.wancables.Registry;
import io.github.powerbox1000.wancables.base.AbstractComponentBlock;
import io.github.powerbox1000.wancables.network.NetworkGraph;
import io.github.powerbox1000.wancables.network.NetworkManager;
import io.github.powerbox1000.wancables.network.NetworkScheduler;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;

import java.util.Map;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.Connector;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

public class ModemBlockEntity extends AbstractComponentBlock {
    private static final double ENERGY_PER_TRANSMISSION = 5.0;
    private static final int MAX_BUFFER_SIZE = 5;

    private static final Map<String, String> DEVICE_INFO = Map.of(
        DeviceAttribute.Class, DeviceClass.Network,
        DeviceAttribute.Description, "WAN modem for long-distance wired communication",
        DeviceAttribute.Vendor, "Powerbox1000",
        DeviceAttribute.Product, "You"
    );

    private int bytesThisTick = 0;
    private ObjectArrayFIFOQueue<byte[]> messageBuffer = new ObjectArrayFIFOQueue<>();

    public ModemBlockEntity(BlockPos pos, BlockState blockState) {
        super(Registry.MODEM_BLOCK_ENTITY.get(), pos, blockState, "wan_modem", 32);
    }

    @Override
    public Map<String, String> getDeviceInfo() {
        return DEVICE_INFO;
    }

    // The block place trigger is in the block itself bc neighbor updates occur before the ModemBlockEntity is instantiated
    // This is only here for organization
    public static void blockPlaced(BlockPlaceContext blockPlaceContext) {
        if (blockPlaceContext.getLevel() instanceof ServerLevel level) {
            NetworkManager.getGraphForDimension(level.dimension()).insertNode(blockPlaceContext.getClickedPos().asLong(), true);
            NetworkManager.getSavedDataForDimension(level.dimension()).setDirty();
        }
    }

    public void blockBroken(BlockState oldState, BlockState newState, LevelAccessor levelAccess, BlockPos blockPos) {
        if (levelAccess instanceof ServerLevel level) {
            NetworkManager.getGraphForDimension(level.dimension()).removeNode(blockPos.asLong());
            NetworkManager.getSavedDataForDimension(level.dimension()).setDirty();
        }
    }

    public void tick() {
        if (!(level instanceof ServerLevel serverLevel)) return;
        if (messageBuffer.size() == 0) return;
        
        LOGGER.debug("Attempting to drain message queue of size {}", messageBuffer.size());

        bytesThisTick = 0;
        long self = worldPosition.asLong();
        NetworkGraph graph = NetworkManager.getGraphForDimension(level.dimension());
        Long2IntOpenHashMap reachableModems = graph.findConnectedModems(self);

        LOGGER.debug("Found {} reachable modems", reachableModems.size());

        while (messageBuffer.size() > 0 && (bytesThisTick + messageBuffer.first().length) <= ServerConfig.MAXIMUM_TRANSMISSION_UNIT.getAsInt()) {
            byte[] data = messageBuffer.dequeue();

            for (var modemDistancePair : reachableModems.long2IntEntrySet()) {
                NetworkScheduler.schedulePacketDispatch(serverLevel.dimension(), modemDistancePair.getLongKey(), modemDistancePair.getIntValue(), data, serverLevel.getServer().getTickCount());
            }

            bytesThisTick += data.length;
        }

        LOGGER.debug("Processed {} bytes this tick", bytesThisTick);

        if (bytesThisTick > 0 && messageBuffer.size() == 0) {
            node.sendToReachable("computer.signal", "wan_modem_buffer_drained");
        }
    }

    // API
    public void queueMessage(byte[] message) {
        node.sendToReachable("computer.signal", "wan_modem_message", message);
    }

    // Methods
    @Callback(doc = "function getTransferRate(): number -- Returns the data transfer rate, in blocks/tick", direct = true)
    public Object[] getTransferRate(Context ctx, Arguments args) {
        return new Object[]{ServerConfig.DATA_TRANSFER_RATE.get()};
    }

    @Callback(doc = "function getMTU(): number -- Returns the maximum transmission unit, in bytes", direct = true)
    public Object[] getMTU(Context ctx, Arguments args) {
        return new Object[]{ServerConfig.MAXIMUM_TRANSMISSION_UNIT.get()};
    }

    @Callback(doc = "function send(string data): boolean, string | nil -- Add the given data to the transmission queue")
    public Object[] send(Context ctx, Arguments args) {
        byte[] data = args.checkByteArray(0);

        if (level.isClientSide()) throw new IllegalStateException("OC lua executor not running on server");

        if (!(node instanceof Connector connector && connector.tryChangeBuffer(-ENERGY_PER_TRANSMISSION))) {
            return new Object[]{false, "not enough power"};
        } else if (data.length > ServerConfig.MAXIMUM_TRANSMISSION_UNIT.getAsInt()) {
            return new Object[]{false, "packet too large"};
        } else if (messageBuffer.size() >= MAX_BUFFER_SIZE) {
            return new Object[]{false, "internal buffer full"};
        }

        messageBuffer.enqueue(data);

        return new Object[]{true};
    }
}
