package com.advancedtech.gui;

import com.advancedtech.tile.TileEntityMachine;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler {
    public static final int MACHINE = 0;

    @Override
    public Object getServerGuiElement(int id, EntityPlayer p, World w, int x, int y, int z) {
        TileEntity te = w.getTileEntity(new BlockPos(x, y, z));
        if (id == MACHINE && te instanceof TileEntityMachine)
            return new ContainerMachine(p.inventory, (TileEntityMachine) te);
        return null;
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer p, World w, int x, int y, int z) {
        TileEntity te = w.getTileEntity(new BlockPos(x, y, z));
        if (id == MACHINE && te instanceof TileEntityMachine)
            return new GuiMachine(p.inventory, (TileEntityMachine) te);
        return null;
    }
}
