package io.github.powerbox1000.wancables;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(WANCables.MODID)
public class WANCables {
    public static final String MODID = "wancables";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    public WANCables(IEventBus modEventBus, ModContainer modContainer) {
        Registry.registerAll(modEventBus);
        modContainer.registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC);
    }
}
