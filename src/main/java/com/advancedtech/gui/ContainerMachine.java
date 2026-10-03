package com.advancedtech.gui;

import com.advancedtech.tile.TileEntityMachine;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerMachine extends Container {
    private final TileEntityMachine tile;
    private final int[] last = new int[7];

    public ContainerMachine(InventoryPlayer inv, TileEntityMachine tile) {
        this.tile = tile;
        java.util.Arrays.fill(last, Integer.MIN_VALUE);

        IItemHandler h = tile.getInventory();
        int[][] layout = tile.getSlotLayout();
        for (int i = 0; i < layout.length; i++)
            addSlotToContainer(new SlotItemHandler(h, i, layout[i][0], layout[i][1]));

        for (int r = 0; r < 3; r++)
            for (int c = 0; c < 9; c++)
                addSlotToContainer(new Slot(inv, c + r * 9 + 9, 8 + c * 18, 84 + r * 18));
        for (int c = 0; c < 9; c++)
            addSlotToContainer(new Slot(inv, c, 8 + c * 18, 142));
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        int[] cur = tile.packSync();
        for (int i = 0; i < cur.length; i++) {
            if (cur[i] != last[i]) {
                for (IContainerListener l : listeners) l.sendWindowProperty(this, i, cur[i]);
                last[i] = cur[i];
            }
        }
    }

    @Override
    public void updateProgressBar(int id, int data) {
        tile.applySync(id, data);
    }

    public TileEntityMachine getTile() { return tile; }

    @Override
    public boolean canInteractWith(EntityPlayer p) {
        return tile.getWorld().getTileEntity(tile.getPos()) == tile
                && p.getDistanceSq(tile.getPos().getX() + .5, tile.getPos().getY() + .5, tile.getPos().getZ() + .5) <= 64;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer p, int index) {
        ItemStack ret = ItemStack.EMPTY;
        Slot slot = inventorySlots.get(index);
        if (slot != null && slot.getHasStack()) {
            ItemStack s = slot.getStack();
            ret = s.copy();
            int n = tile.getInventory().getSlots();
            if (index < n) {
                if (!mergeItemStack(s, n, inventorySlots.size(), true)) return ItemStack.EMPTY;
            } else if (!mergeItemStack(s, 0, n, false)) {
                return ItemStack.EMPTY;
            }
            if (s.isEmpty()) slot.putStack(ItemStack.EMPTY); else slot.onSlotChanged();
        }
        return ret;
    }
}
