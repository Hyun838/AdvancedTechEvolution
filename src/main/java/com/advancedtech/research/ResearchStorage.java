package com.advancedtech.research;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

public class ResearchStorage implements Capability.IStorage<IResearchData> {
    @Override
    public NBTBase writeNBT(Capability<IResearchData> cap, IResearchData inst, EnumFacing side) {
        return inst.serializeNBT();
    }

    @Override
    public void readNBT(Capability<IResearchData> cap, IResearchData inst, EnumFacing side, NBTBase nbt) {
        if (nbt instanceof NBTTagCompound) inst.deserializeNBT((NBTTagCompound) nbt);
    }
}
