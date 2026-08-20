package io.github.powerbox1000.wancables.network;

import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

/**
 * A graph of a dimension's WAN network.
 * Each wire and modem is a node with an ID computed via {@link net.minecraft.core.BlockPos#asLong()}.
 */
public class NetworkGraph {
    private final Long2ObjectOpenHashMap<LongOpenHashSet> adjacencyMap;
    private final LongOpenHashSet modemSet;

    public NetworkGraph() {
        this(new Long2ObjectOpenHashMap<>(), new LongOpenHashSet());
    }

    public NetworkGraph(Long2ObjectOpenHashMap<LongOpenHashSet> adjacencyMap, LongOpenHashSet modemSet) {
        this.adjacencyMap = adjacencyMap;
        this.modemSet = modemSet;
    }

    /**
     * Get the internal adjacency map
     * @return The internal adjacency map
     */
    public Long2ObjectOpenHashMap<LongOpenHashSet> getAdjecencyMap() {
        return adjacencyMap;
    }

    /**
     * Get the internal modem set
     * @return The internal modem set
     */
    public LongOpenHashSet getModemSet() {
        return modemSet;
    }

    /**
     * Check if a node is on the graph
     * @param node The node to check
     * @return If the node exists on the graph
     */
    public boolean exists(long node) {
        return adjacencyMap.containsKey(node);
    }

    /**
     * Check if two nodes are connected
     * @param node1 The first node
     * @param node2 The second node
     * @return If the first node is connected to the second node
     * @throws IllegalArgumentException if either node is not on the graph
     */
    public boolean isConnected(long node1, long node2) {
        if (!adjacencyMap.containsKey(node1)) throw new IllegalArgumentException("Node with id %d does not exist on the graph (caused by argument 1)".formatted(node1));
        if (!adjacencyMap.containsKey(node2)) throw new IllegalArgumentException("Node with id %d does not exist on the graph (caused by argument 2)".formatted(node2));

        return adjacencyMap.get(node1).contains(node2);
    }

    /**
     * Insert a node into the graph
     * @param id The node's id
     * @param isModem Whether the node is a modem or not
     * @see #insertNode(long id, LongOpenHashSet adjacencyList, boolean isModem)
     */
    public void insertNode(long id, boolean isModem) {
        insertNode(id, new LongOpenHashSet(), isModem);
    }

    /**
     * Insert a node into the graph
     * @param id The node's id
     * @param adjacencyList The initial adjacency list for the node
     * @param isModem Whether the node is a modem or not
     */
    public void insertNode(long id, LongOpenHashSet adjacencyList, boolean isModem) {
        adjacencyMap.put(id, adjacencyList);
        if (isModem) modemSet.add(id);
    }

    /**
     * Remove a node and any connections to it from the graph
     * @param id The id to remove
     */
    public void removeNode(long id) {
        adjacencyMap.remove(id);
        modemSet.remove(id);

        for (var node : adjacencyMap.values()) {
            node.remove(id);
        }
    }

    /**
     * Connect two nodes on the graph
     * @param node1 The first node
     * @param node2 The second node
     * @throws IllegalArgumentException if either node is not on the graph
     */
    public void connectNodes(long node1, long node2) {
        if (!adjacencyMap.containsKey(node1)) throw new IllegalArgumentException("Node with id %d does not exist on the graph (caused by argument 1)".formatted(node1));
        if (!adjacencyMap.containsKey(node2)) throw new IllegalArgumentException("Node with id %d does not exist on the graph (caused by argument 2)".formatted(node2));

        adjacencyMap.get(node1).add(node2);
        adjacencyMap.get(node2).add(node1);
    }

    /**
     * Disconnect two nodes on the graph
     * @param node1 The first node
     * @param node2 The second node
     * @throws IllegalArgumentException if either node is not on the graph
     * @apiNote This method does not throw if the nodes aren't connected, so long as they both exist
     */
    public void disconnectNodes(long node1, long node2) {
        if (!adjacencyMap.containsKey(node1)) throw new IllegalArgumentException("Node with id %d does not exist on the graph (caused by argument 1)".formatted(node1));
        if (!adjacencyMap.containsKey(node2)) throw new IllegalArgumentException("Node with id %d does not exist on the graph (caused by argument 2)".formatted(node2));

        adjacencyMap.get(node1).remove(node2);
        adjacencyMap.get(node2).remove(node1);
    }

    /**
     * Find all modems reachable by the given modem
     * @param modem The modem to start the search at
     * @return An map of all reachable modems (excluding the starting modem) to their traversal distance in blocks
     * @throws IllegalArgumentException if the given node either doesn't exist or isn't a modem
     * @apiNote The search is performed with BFS, so all paths are guaranteed to be the shortest possible path
     */
    public Long2IntOpenHashMap findConnectedModems(long modem) {
        if (!adjacencyMap.containsKey(modem)) throw new IllegalArgumentException("Node with id %d does not exist on the graph".formatted(modem));
        if (!modemSet.contains(modem)) throw new IllegalArgumentException("Node with id %d is not a modem".formatted(modem));

        Long2IntOpenHashMap distanceMap = new Long2IntOpenHashMap();
        LongArrayFIFOQueue frontier = new LongArrayFIFOQueue();

        distanceMap.put(modem, 0);
        frontier.enqueue(modem);

        while (frontier.size() > 0) {
            long currentNode = frontier.dequeueLong();
            int newDistance = distanceMap.get(currentNode) + 1;

            for (long neighbor : adjacencyMap.get(currentNode)) {
                if (distanceMap.containsKey(neighbor)) continue;

                frontier.enqueue(neighbor);
                distanceMap.put(neighbor, newDistance);
            }
        }

        distanceMap.remove(modem);
        var iterator = distanceMap.keySet().iterator();
        while (iterator.hasNext()) {
            long node = iterator.nextLong();
            if (!modemSet.contains(node)) {
                iterator.remove();
            }
        }

        return distanceMap;
    }
}
