package com.advancedtech.energy;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.energy.EnergyStorage;

/** Буфер FE (совместим с любым модом на Forge Energy / RF) с поддержкой тиров. */
public class ATEnergyStorage extends EnergyStorage implements INBTSerializable<NBTTagCompound> {
    private EnergyTier tier;

    public ATEnergyStorage(EnergyTier tier) {
        super(tier.capacity, tier.maxTransfer, tier.maxTransfer);
        this.tier = tier;
    }

    public EnergyTier getTier() { return tier; }

    public void setTier(EnergyTier t) {
        this.tier = t;
        this.capacity = t.capacity;
        this.maxReceive = t.maxTransfer;
        this.maxExtract = t.maxTransfer;
        this.energy = Math.min(energy, capacity);
    }

    /** Внутреннее списание (работа машины), минуя лимит maxExtract. */
    public boolean consume(int amount) {
        if (energy < amount) return false;
        energy -= amount;
        return true;
    }

    /** Внутренняя генерация. Возвращает фактически добавленное. */
    public int generate(int amount) {
        int a = Math.min(amount, capacity - energy);
        energy += a;
        return a;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound t = new NBTTagCompound();
        t.setInteger("Energy", energy);
        t.setInteger("Tier", tier.ordinal());
        return t;
    }

    @Override
    public void deserializeNBT(NBTTagCompound t) {
        setTier(EnergyTier.byOrdinal(t.getInteger("Tier")));
        energy = Math.min(t.getInteger("Energy"), capacity);
    }
}
