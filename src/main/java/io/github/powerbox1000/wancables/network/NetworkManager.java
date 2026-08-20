package io.github.powerbox1000.wancables.network;

import java.util.HashMap;

import io.github.powerbox1000.wancables.WANCables;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.LevelEvent;

/**
 * Manages instances of NetworkSavedData & NetworkGraph per-dimension
 */
@EventBusSubscriber(modid = WANCables.MODID)
public class NetworkManager {
    private static final HashMap<ResourceKey<Level>, NetworkSavedData> saveMap = new HashMap<>();
    private static final HashMap<ResourceKey<Level>, NetworkGraph> graphMap = new HashMap<>();

    @SubscribeEvent
    private static void onLevelLoad(LevelEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel level) {
            saveMap.put(
                level.dimension(),
                level.getDataStorage().computeIfAbsent(NetworkSavedData.FACTORY, "wan_network")
            );

            graphMap.put(level.dimension(), saveMap.get(level.dimension()).graph);
        }
    }

    @SubscribeEvent
    private static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level) {
            saveMap.remove(level.dimension());
            graphMap.remove(level.dimension());
        }
    }

    /**
     * Get the dimension's NetworkSavedData instance
     * @param dimension The dimension to get the NetworkSavedData instance from
     * @return The NetworkSavedData instance, or null if the dimension is not loaded
     * @apiNote This should only be called on the server
     */
    public static NetworkSavedData getSavedDataForDimension(ResourceKey<Level> dimension) {
        return saveMap.get(dimension);
    }

    /**
     * Get the dimension's NetworkGraph instance
     * @param dimension The dimension to get the NetworkGraph instance from
     * @return The NetworkGraph instance, or null if the dimension is not loaded
     * @apiNote This should only be called on the server
     */
    public static NetworkGraph getGraphForDimension(ResourceKey<Level> dimension) {
        return graphMap.get(dimension);
    }
}
