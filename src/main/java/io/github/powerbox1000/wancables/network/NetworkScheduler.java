package io.github.powerbox1000.wancables.network;

import java.util.Comparator;

import static io.github.powerbox1000.wancables.WANCables.LOGGER;

import io.github.powerbox1000.wancables.ServerConfig;
import io.github.powerbox1000.wancables.WANCables;
import io.github.powerbox1000.wancables.blocks.blockentity.ModemBlockEntity;
import it.unimi.dsi.fastutil.objects.ObjectHeapPriorityQueue;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = WANCables.MODID)
public class NetworkScheduler {
    public static final TicketType<BlockPos> WAN_MODEM_TICKET = TicketType.create("wanmodem", Vec3i::compareTo, 100);

    private record ScheduledTask(ResourceKey<Level> dimension, long modem, int delay, byte[] data) {}
    private static ObjectHeapPriorityQueue<ScheduledTask> scheduleQueue = new ObjectHeapPriorityQueue<>(Comparator.comparingInt(task -> task.delay));

    @SubscribeEvent
    private static void onServerPostTick(ServerTickEvent.Post event) {
        var server = event.getServer();
        while (scheduleQueue.size() > 0 && scheduleQueue.first().delay <= server.getTickCount()) {
            var task = scheduleQueue.dequeue();
            var blockPos = BlockPos.of(task.modem);
            var level = server.getLevel(task.dimension);

            LOGGER.debug("Dispatching message at pos {} {} {}", blockPos.getX(), blockPos.getY(), blockPos.getZ());

            level.getChunkSource().addRegionTicket(WAN_MODEM_TICKET, new ChunkPos(blockPos), 3, blockPos, true);
            ((ModemBlockEntity)level.getBlockEntity(blockPos)).queueMessage(task.data);
        }
    }

    public static void schedulePacketDispatch(ResourceKey<Level> dimension, long modem, int distance, byte[] data, int currTickCount) {
        if (distance <= 0) throw new IllegalArgumentException("Distance must be >0!");
        int delay = (distance / ServerConfig.DATA_TRANSFER_RATE.getAsInt()) + currTickCount;
        if (delay < 0) throw new IllegalStateException("The current tick count has overflowed");
        scheduleQueue.enqueue(new ScheduledTask(dimension, modem, delay, data));
    }
}
