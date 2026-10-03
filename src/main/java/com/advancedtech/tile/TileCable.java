package com.advancedtech.tile;

import com.advancedtech.energy.EnergyTier;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Кабель: у каждого блока свой маленький буфер. Энергия течёт по градиенту (от полного к пустому),
 * на каждом шаге между кабелями часть теряется. Если за тик в кабель пришло больше, чем выдерживает
 * тир, копится перегрузка — через 2 секунды кабель плавится.
 */
public class TileCable extends TileEntity implements ITickable {
    public static final int MELT_TICKS = 40;

    private EnergyTier tier = EnergyTier.LV;
    private final CableStorage storage = new CableStorage();
    private int received;       // принято с прошлого тика
    private int overloadTicks;

    public TileCable() { storage.configure(tier); }
    public TileCable(EnergyTier tier) { this.tier = tier; storage.configure(tier); }

    private class CableStorage extends EnergyStorage {
        CableStorage() { super(1, 1, 1); }

        void configure(EnergyTier t) {
            capacity = t.maxTransfer * 2;
            maxReceive = t.maxTransfer * 4;   // принимаем больше нормы, чтобы перегрузка была возможна
            maxExtract = t.maxTransfer;
            energy = Math.min(energy, capacity);
        }

        @Override
        public int receiveEnergy(int max, boolean simulate) {
            int a = super.receiveEnergy(max, simulate);
            if (!simulate) received += a;
            return a;
        }

        void drain(int n) { energy = Math.max(0, energy - n); }
    }

    @Override
    public void update() {
        if (world.isRemote) return;

        // --- перегрузка ---
        if (received > tier.maxTransfer) {
            if (++overloadTicks >= MELT_TICKS) { melt(); return; }
        } else if (overloadTicks > 0) {
            overloadTicks--;
        }
        received = 0;

        // --- раздача ---
        int stored = storage.getEnergyStored();
        if (stored <= 0) return;
        int budget = Math.min(stored, tier.maxTransfer);

        for (EnumFacing f : EnumFacing.VALUES) {
            if (budget <= 0) break;
            TileEntity te = world.getTileEntity(pos.offset(f));
            if (te == null) continue;
            IEnergyStorage target = te.getCapability(CapabilityEnergy.ENERGY, f.getOpposite());
            if (target == null || !target.canReceive()) continue;

            if (te instanceof TileCable) {
                TileCable o = (TileCable) te;
                int diff = stored - o.storage.getEnergyStored();
                if (diff < 2) continue;
                int send = Math.min(budget, diff / 2);
                int offered = tier.applyCableLoss(send, 1);       // потери на шаге
                if (offered <= 0) continue;
                int acc = o.storage.receiveEnergy(offered, false);
                int used = acc >= offered ? send : Math.round((float) acc / offered * send);
                storage.drain(used);
                budget -= used;
                stored -= used;
            } else {
                TileEntityMachine m = te instanceof TileEntityMachine ? (TileEntityMachine) te : null;
                if (m != null && !m.acceptsEnergy()) continue;
                int acc = target.receiveEnergy(budget, false);
                if (acc <= 0) continue;
                storage.drain(acc);
                budget -= acc;
                stored -= acc;
                // Кабель высшего тира → машина низшего греется
                if (m != null && m.getEnergy().getTier().ordinal() < tier.ordinal()) m.overvolt();
            }
        }
    }

    private void melt() {
        world.playSound(null, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 1f, 0.6f);
        world.setBlockState(pos, Blocks.FIRE.getDefaultState());
    }

    public EnergyTier getTier() { return tier; }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> c, @Nullable EnumFacing f) {
        return c == CapabilityEnergy.ENERGY || super.hasCapability(c, f);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> c, @Nullable EnumFacing f) {
        return c == CapabilityEnergy.ENERGY ? (T) storage : super.getCapability(c, f);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound t) {
        super.writeToNBT(t);
        t.setInteger("Tier", tier.ordinal());
        t.setInteger("Energy", storage.getEnergyStored());
        t.setInteger("Overload", overloadTicks);
        return t;
    }

    @Override
    public void readFromNBT(NBTTagCompound t) {
        super.readFromNBT(t);
        tier = EnergyTier.byOrdinal(t.getInteger("Tier"));
        storage.configure(tier);
        storage.drain(storage.getEnergyStored());
        storage.receiveEnergy(t.getInteger("Energy"), false);
        received = 0;
        overloadTicks = t.getInteger("Overload");
    }
}
