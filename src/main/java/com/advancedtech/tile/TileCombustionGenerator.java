package com.advancedtech.tile;

import com.advancedtech.config.ATConfig;
import com.advancedtech.energy.EnergyTier;
import com.advancedtech.pollution.PollutionManager;
import com.advancedtech.registry.ModItems;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

/** Топливный генератор: сжигает топливо из печи, выдаёт FE соседям, загрязняет чанк (фильтр режет выбросы на 90%). */
public class TileCombustionGenerator extends TileEntityMachine {
    public static final int FUEL = 0, FILTER = 1;
    public static final int OUTPUT = 24;             // FE/t
    public static final float EMISSION = 3f;         // загрязнение за секунду горения
    public static final float FILTERED_FACTOR = 0.1f;

    private int burnLeft, burnMax, emitTimer;

    public TileCombustionGenerator() { super(2, EnergyTier.LV); }

    @Override
    public void update() {
        if (world.isRemote) return;

        if (burnLeft <= 0) {
            ItemStack fuel = inventory.getStackInSlot(FUEL);
            int bt = fuel.isEmpty() ? 0 : TileEntityFurnace.getItemBurnTime(fuel);
            if (bt > 0 && energy.getEnergyStored() + OUTPUT <= energy.getMaxEnergyStored()) {
                burnMax = burnLeft = bt;
                if (fuel.getCount() == 1 && fuel.getItem().hasContainerItem(fuel))
                    inventory.setStackInSlot(FUEL, fuel.getItem().getContainerItem(fuel));
                else fuel.shrink(1);
                markDirty();
            }
        }
        // горим только если есть место в буфере — топливо не пропадает зря
        if (burnLeft > 0 && energy.getEnergyStored() + OUTPUT <= energy.getMaxEnergyStored()) {
            energy.generate(OUTPUT);
            burnLeft--;
            emit();
        }
        progress = burnMax - burnLeft;
        pushEnergy();
        if (world.getTotalWorldTime() % 20 == 0) markDirty();
    }

    private void emit() {
        if (++emitTimer < 20) return;
        emitTimer = 0;
        if (!ATConfig.enablePollution) return;
        float amount = EMISSION;
        ItemStack f = inventory.getStackInSlot(FILTER);
        if (!f.isEmpty() && f.getItem() == ModItems.AIR_FILTER) {
            amount *= FILTERED_FACTOR;
            f.setItemDamage(f.getItemDamage() + 1);
            if (f.getItemDamage() >= f.getMaxDamage()) inventory.setStackInSlot(FILTER, ItemStack.EMPTY);
        }
        PollutionManager.add(world, pos, amount);
    }

    private void pushEnergy() {
        for (EnumFacing f : EnumFacing.VALUES) {
            int avail = Math.min(energy.getEnergyStored(), energy.getTier().maxTransfer);
            if (avail <= 0) return;
            TileEntity te = world.getTileEntity(pos.offset(f));
            if (te == null) continue;
            if (te instanceof TileEntityMachine && !((TileEntityMachine) te).acceptsEnergy()) continue;
            IEnergyStorage t = te.getCapability(CapabilityEnergy.ENERGY, f.getOpposite());
            if (t == null || !t.canReceive()) continue;
            energy.consume(t.receiveEnergy(avail, false));
        }
    }

    @Override protected void tickHeat(boolean working) { /* генератор не греется */ }
    @Override public boolean acceptsEnergy() { return false; }
    @Override public boolean canProcess() { return false; }
    @Override public void finishProcess() { }
    @Override public int getBaseProcessTime() { return 1; }
    @Override public int getProcessTime() { return Math.max(1, burnMax); }   // для шкалы горения в GUI
    @Override public int getEnergyPerTick() { return 0; }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        if (slot == FUEL) return TileEntityFurnace.getItemBurnTime(stack) > 0;
        return slot == FILTER && stack.getItem() == ModItems.AIR_FILTER;
    }

    @Override public int[][] getSlotLayout() { return new int[][]{{56, 35}, {116, 35}}; }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound t) {
        super.writeToNBT(t);
        t.setInteger("BurnLeft", burnLeft);
        t.setInteger("BurnMax", burnMax);
        t.setInteger("EmitTimer", emitTimer);
        return t;
    }

    @Override
    public void readFromNBT(NBTTagCompound t) {
        super.readFromNBT(t);
        burnLeft = t.getInteger("BurnLeft");
        burnMax = t.getInteger("BurnMax");
        emitTimer = t.getInteger("EmitTimer");
    }
}
