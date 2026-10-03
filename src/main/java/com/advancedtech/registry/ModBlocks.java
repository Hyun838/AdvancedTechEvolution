package com.advancedtech.registry;

import com.advancedtech.block.BlockCable;
import com.advancedtech.block.BlockMachine;

public class ModBlocks {
    public static BlockMachine ELECTRIC_FURNACE;
    public static BlockMachine COMBUSTION_GENERATOR;
    /** Индекс = EnergyTier.ordinal(): LV, MV, HV, EV. */
    public static final BlockCable[] CABLES = new BlockCable[4];
}
