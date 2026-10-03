package com.advancedtech.tile;

import com.advancedtech.energy.EnergyTier;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;

/** Пример конкретной машины: слот 0 — вход, слот 1 — выход. */
public class TileElectricFurnace extends TileEntityMachine {
    public static final int IN = 0, OUT = 1;

    public TileElectricFurnace() { super(2, EnergyTier.LV); }

    private ItemStack result() {
        ItemStack in = inventory.getStackInSlot(IN);
        return in.isEmpty() ? ItemStack.EMPTY : FurnaceRecipes.instance().getSmeltingResult(in);
    }

    @Override public boolean canProcess() {
        ItemStack r = result();
        return !r.isEmpty() && canOutput(OUT, r);
    }

    @Override public void finishProcess() {
        ItemStack r = result();
        if (r.isEmpty()) return;
        output(OUT, r);
        inventory.getStackInSlot(IN).shrink(1);
        markDirty();
    }

    @Override public int getBaseProcessTime() { return 160; }
    @Override public int getEnergyPerTick() { return 20; }

    @Override public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return slot == IN && !FurnaceRecipes.instance().getSmeltingResult(stack).isEmpty();
    }

    @Override public int[][] getSlotLayout() { return new int[][]{{56, 35}, {116, 35}}; }
}
