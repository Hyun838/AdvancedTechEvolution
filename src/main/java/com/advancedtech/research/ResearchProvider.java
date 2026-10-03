package com.advancedtech.research;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Привязывает ResearchData к игроку и сохраняет в его NBT. */
public class ResearchProvider implements ICapabilitySerializable<NBTTagCompound> {
    private final IResearchData instance = new ResearchData();

    @Override public boolean hasCapability(@Nonnull Capability<?> c, @Nullable EnumFacing f) {
        return c == ResearchCapability.RESEARCH;
    }

    @Nullable @Override
    public <T> T getCapability(@Nonnull Capability<T> c, @Nullable EnumFacing f) {
        return c == ResearchCapability.RESEARCH ? ResearchCapability.RESEARCH.cast(instance) : null;
    }

    @Override public NBTTagCompound serializeNBT() { return instance.serializeNBT(); }
    @Override public void deserializeNBT(NBTTagCompound nbt) { instance.deserializeNBT(nbt); }
}
