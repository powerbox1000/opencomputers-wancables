package io.github.powerbox1000.wancables.datagen;

import io.github.powerbox1000.wancables.Registry;
import io.github.powerbox1000.wancables.WANCables;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class WANCablesLanguageProvider extends LanguageProvider {
    public WANCablesLanguageProvider(PackOutput output) {
        super(output, WANCables.MODID, "en_us");
    }

    @Override
    protected void addTranslations() {
        // Blocks
        addBlock(Registry.CABLE_BLOCK.block(), "WAN Cable");
        addBlock(Registry.MODEM_BLOCK.block(), "Modem");

        // Config
        add("wancables.configuration.title", "WAN Cables Configs");
        add("wancables.configuration.section.wancables.common.toml", "WAN Cables Configs");
        add("wancables.configuration.section.wancables.common.toml.title", "WAN Cables Configs");
        add("wancables.configuration.dataTransferRate", "Data Transfer Rate (blocks/tick)");
        add("wancables.configuration.maximumTransmissionUnit", "Maximum Transmission Unit (bytes)");
        add("wancables.configuration.loadChunksOnMessage", "Load Chunks on Message Receipt");

        // Misc
        add("itemGroup.wancables", "WAN Cables");
    }
}
