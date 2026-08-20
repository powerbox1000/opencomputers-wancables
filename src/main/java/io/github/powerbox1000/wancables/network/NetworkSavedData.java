package io.github.powerbox1000.wancables.network;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * Represents a saved network graph.
 *
 * Data is stored in the following format:
 * <pre>
 * {@code
 *   tag = {
 *      "adjacencyKeys": LongArrayTag,
 *      "adjacencyValues": ListTag<LongArrayTag>,
 *      "modemSet": LongArrayTag
 *   }
 * }
 * </pre>
 */
public class NetworkSavedData extends SavedData {
    public static final Factory<NetworkSavedData> FACTORY = new Factory<>(NetworkSavedData::create, NetworkSavedData::load);

    public final NetworkGraph graph;

    private NetworkSavedData(NetworkGraph graph) {
        this.graph = graph;
    }

    public static NetworkSavedData create() {
        return new NetworkSavedData(new NetworkGraph());
    }

    public static NetworkSavedData load(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        long[] adjacencyKeys = tag.getLongArray("adjacencyKeys");
        LongOpenHashSet[] adjacencyValues = new LongOpenHashSet[adjacencyKeys.length];

        ListTag adjacencyValuesRaw = tag.getList("adjacencyValues", Tag.TAG_LONG_ARRAY);
        for (int i = 0; i < adjacencyValues.length; i++) {
            adjacencyValues[i] = new LongOpenHashSet(adjacencyValuesRaw.getLongArray(i));
        }

        Long2ObjectOpenHashMap<LongOpenHashSet> adjacencyMap = new Long2ObjectOpenHashMap<LongOpenHashSet>(adjacencyKeys, adjacencyValues);
        LongOpenHashSet modemSet = new LongOpenHashSet(tag.getLongArray("modemSet"));

        return new NetworkSavedData(new NetworkGraph(adjacencyMap, modemSet));
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        var adjacencyMap = graph.getAdjecencyMap();
        var modemSet = graph.getModemSet();

        tag.putLongArray("adjacencyKeys", adjacencyMap.keySet().toLongArray());

        ListTag adjacencyMapValues = new ListTag(adjacencyMap.size());
        for (var value : adjacencyMap.values()) {
            adjacencyMapValues.add(new LongArrayTag(value));
        }
        tag.put("adjacencyValues", adjacencyMapValues);

        tag.putLongArray("modemSet", modemSet.toLongArray());

        return tag;
    }
}
