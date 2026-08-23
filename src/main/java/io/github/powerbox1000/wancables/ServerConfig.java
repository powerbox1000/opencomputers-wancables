package io.github.powerbox1000.wancables;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ServerConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    /*
        For convenience, 1 MTU occupies 1 block.
        The defaults currently are:
        - Transmission rate: 16 block/tick (1 chunk/tick), or 320 blocks/sec (40 chunk/sec)
        - Maximum transmission unit: 8,192 bytes (8 kb)
    */

    public static final ModConfigSpec.IntValue DATA_TRANSFER_RATE = BUILDER
            .comment("The rate of data transfer across a WAN network, in blocks per tick")
            .defineInRange("dataTransferRate", 16, 1, Integer.MAX_VALUE);
    
    public static final ModConfigSpec.IntValue MAXIMUM_TRANSMISSION_UNIT = BUILDER
        .comment("The maximum amount of bytes that can be transferred in a single data packet")
        .defineInRange("maximumTransmissionUnit", 8192, 1, Integer.MAX_VALUE);

    // TODO impl
    // public static final ModConfigSpec.BooleanValue LOAD_CHUNKS_ON_MESSAGE = BUILDER
    //     .comment("Whether to load an area of 3x3 entity ticking chunks around a modem when it recieves a message")
    //     .define("loadChunksOnMessage", true);

    static final ModConfigSpec SPEC = BUILDER.build();
}
