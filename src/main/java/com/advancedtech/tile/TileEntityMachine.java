package com.advancedtech.tile;

import com.advancedtech.config.ATConfig;
import com.advancedtech.energy.ATEnergyStorage;
import com.advancedtech.energy.EnergyTier;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Базовая машина: слоты, FE-буфер, прогресс, тепло, 4 уровня корпуса.
 * Наследник реализует только рецепт (canProcess / finishProcess / время / энергия).
 */
public abstract class TileEntityMachine extends TileEntity implements ITickable {

    public static final float AMBIENT = 20f;     // температура среды
    public static final float OVERHEAT = 100f;   // остановка
    public static final float RECOVER = 70f;     // возобновление после перегрева
    public static final float EXPLODE = 150f;    // взрыв
    public static final float HEAT_DIVISOR = 16f;   // тепло/тик = энергия/тик / HEAT_DIVISOR
    public static final float COOL_COEFF = 0.02f;   // доля остывания за тик
    /** Индекс = уровень корпуса: базовый, улучшенный, индустриальный, квантовый. */
    public static final float[] SPEED = {1f, 1.5f, 2.5f, 4f};
    public static final float[] COOLING = {1f, 1.5f, 2.5f, 4f};

    protected final ItemStackHandler inventory;
    protected final ATEnergyStorage energy;
    protected int casing = 0;
    protected float heat = AMBIENT;
    protected int progress = 0;
    protected boolean overheated = false;

    // Клиентская копия для GUI (приходит через ContainerMachine)
    private final int[] wire = new int[7];
    private int cEnergy, cMaxEnergy, cHeat10, cProgress, cMaxProgress;

    protected TileEntityMachine(int slots, EnergyTier tier) {
        this.energy = new ATEnergyStorage(tier);
        this.inventory = new ItemStackHandler(slots) {
            @Override protected void onContentsChanged(int slot) { markDirty(); }
            @Nonnull @Override
            public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
                // Автоматизация (воронки/трубы) не может класть в выходные слоты
                if (!isItemValidForSlot(slot, stack)) return stack;
                return super.insertItem(slot, stack, simulate);
            }
        };
    }

    // ---------- API для наследников ----------
    public abstract boolean canProcess();
    public abstract void finishProcess();
    public abstract int getBaseProcessTime();
    public abstract int getEnergyPerTick();
    public abstract boolean isItemValidForSlot(int slot, ItemStack stack);
    /** Координаты слотов в GUI {{x,y},...}. */
    public abstract int[][] getSlotLayout();

    // ---------- Логика ----------
    public int getProcessTime() {
        return Math.max(1, Math.round(getBaseProcessTime() / SPEED[casing]));
    }

    @Override
    public void update() {
        if (world.isRemote) return;

        boolean working = false;
        if (!overheated && canProcess() && energy.consume(getEnergyPerTick())) {
            working = true;
            progress++;
            if (progress >= getProcessTime()) {
                finishProcess();
                progress = 0;
            }
        } else if (!canProcess()) {
            progress = 0;
        }
        tickHeat(working);
        if (working || world.getTotalWorldTime() % 20 == 0) markDirty();
    }

    protected void tickHeat(boolean working) {
        if (!ATConfig.enableHeat) { heat = AMBIENT; overheated = false; return; }
        if (working) heat += getEnergyPerTick() / HEAT_DIVISOR;
        heat -= (heat - AMBIENT) * COOL_COEFF * COOLING[casing];

        if (heat >= OVERHEAT) overheated = true;
        else if (overheated && heat <= RECOVER) overheated = false;

        if (heat >= EXPLODE) {
            world.setBlockToAir(pos);
            world.createExplosion(null, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5, 2.5f, true);
        }
    }

    // ---------- Утилиты выходных слотов ----------
    protected boolean canOutput(int slot, ItemStack s) {
        ItemStack cur = inventory.getStackInSlot(slot);
        if (cur.isEmpty()) return true;
        return ItemHandlerHelper.canItemStacksStack(cur, s)
                && cur.getCount() + s.getCount() <= cur.getMaxStackSize();
    }

    protected void output(int slot, ItemStack s) {
        ItemStack cur = inventory.getStackInSlot(slot);
        if (cur.isEmpty()) inventory.setStackInSlot(slot, s.copy());
        else cur.grow(s.getCount());
        markDirty();
    }

    // ---------- Геттеры/апгрейды ----------
    public ItemStackHandler getInventory() { return inventory; }
    public ATEnergyStorage getEnergy() { return energy; }
    public int getCasing() { return casing; }

    public boolean upgradeCasing() {
        if (casing >= 3) return false;
        casing++;
        markDirty();
        return true;
    }

    public void setTier(EnergyTier t) { energy.setTier(t); markDirty(); }

    /** Принимает ли устройство энергию из сети (генераторы — нет). */
    public boolean acceptsEnergy() { return true; }

    /** Перенапряжение: кабель более высокого тира греет машину (дальше работает система перегрева). */
    public void overvolt() { heat += 30f; markDirty(); }

    // ---------- Синхронизация GUI (short-пакеты → энергия режется на 2 части) ----------
    public int[] packSync() {
        int e = energy.getEnergyStored(), m = energy.getMaxEnergyStored();
        return new int[]{e & 0xFFFF, e >>> 16, m & 0xFFFF, m >>> 16,
                Math.round(heat * 10), progress, getProcessTime()};
    }

    public void applySync(int id, int value) {
        wire[id] = value & 0xFFFF;
        cEnergy = wire[0] | (wire[1] << 16);
        cMaxEnergy = wire[2] | (wire[3] << 16);
        cHeat10 = wire[4];
        cProgress = wire[5];
        cMaxProgress = wire[6];
    }

    public int guiEnergy() { return world.isRemote ? cEnergy : energy.getEnergyStored(); }
    public int guiMaxEnergy() { return world.isRemote ? cMaxEnergy : energy.getMaxEnergyStored(); }
    public float guiHeat() { return world.isRemote ? cHeat10 / 10f : heat; }
    public int guiProgress() { return world.isRemote ? cProgress : progress; }
    public int guiMaxProgress() { return world.isRemote ? cMaxProgress : getProcessTime(); }

    // ---------- Capabilities (FE + предметы) ----------
    @Override
    public boolean hasCapability(@Nonnull Capability<?> cap, @Nullable EnumFacing f) {
        return cap == CapabilityEnergy.ENERGY || cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY
                || super.hasCapability(cap, f);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> cap, @Nullable EnumFacing f) {
        if (cap == CapabilityEnergy.ENERGY) return (T) energy;
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) return (T) inventory;
        return super.getCapability(cap, f);
    }

    // ---------- NBT ----------
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound t) {
        super.writeToNBT(t);
        t.setTag("Inv", inventory.serializeNBT());
        t.setTag("Energy", energy.serializeNBT());
        t.setInteger("Casing", casing);
        t.setFloat("Heat", heat);
        t.setInteger("Progress", progress);
        t.setBoolean("Overheated", overheated);
        return t;
    }

    @Override
    public void readFromNBT(NBTTagCompound t) {
        super.readFromNBT(t);
        inventory.deserializeNBT(t.getCompoundTag("Inv"));
        energy.deserializeNBT(t.getCompoundTag("Energy"));
        casing = Math.max(0, Math.min(3, t.getInteger("Casing")));
        heat = t.hasKey("Heat") ? t.getFloat("Heat") : AMBIENT;
        progress = t.getInteger("Progress");
        overheated = t.getBoolean("Overheated");
    }
}
