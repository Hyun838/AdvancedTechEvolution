package com.advancedtech.block;

import com.advancedtech.AdvancedTech;
import com.advancedtech.energy.EnergyTier;
import com.advancedtech.tile.TileCable;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.energy.CapabilityEnergy;

public class BlockCable extends Block {
    /** Индекс = EnumFacing.getIndex(): DOWN, UP, NORTH, SOUTH, WEST, EAST. */
    public static final PropertyBool[] CONN = {
            PropertyBool.create("down"), PropertyBool.create("up"), PropertyBool.create("north"),
            PropertyBool.create("south"), PropertyBool.create("west"), PropertyBool.create("east")};

    private final EnergyTier tier;

    public BlockCable(String name, EnergyTier tier) {
        super(Material.IRON);
        this.tier = tier;
        setRegistryName(AdvancedTech.MODID, name);
        setUnlocalizedName(AdvancedTech.MODID + "." + name);
        setCreativeTab(CreativeTabs.REDSTONE);
        setHardness(1.0f);
        setSoundType(SoundType.METAL);
    }

    public EnergyTier getTier() { return tier; }

    @Override protected BlockStateContainer createBlockState() { return new BlockStateContainer(this, CONN); }
    @Override public int getMetaFromState(IBlockState state) { return 0; }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess w, BlockPos pos) {
        for (EnumFacing f : EnumFacing.VALUES) {
            TileEntity te = w.getTileEntity(pos.offset(f));
            boolean c = te != null && te.hasCapability(CapabilityEnergy.ENERGY, f.getOpposite());
            state = state.withProperty(CONN[f.getIndex()], c);
        }
        return state;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess src, BlockPos pos) {
        state = getActualState(state, src, pos);
        double mn = 5 / 16d, mx = 11 / 16d;
        return new AxisAlignedBB(
                state.getValue(CONN[4]) ? 0 : mn, state.getValue(CONN[0]) ? 0 : mn, state.getValue(CONN[2]) ? 0 : mn,
                state.getValue(CONN[5]) ? 1 : mx, state.getValue(CONN[1]) ? 1 : mx, state.getValue(CONN[3]) ? 1 : mx);
    }

    @Override public boolean isOpaqueCube(IBlockState s) { return false; }
    @Override public boolean isFullCube(IBlockState s) { return false; }
    @Override public BlockFaceShape getBlockFaceShape(IBlockAccess w, IBlockState s, BlockPos p, EnumFacing f) {
        return BlockFaceShape.UNDEFINED;
    }

    @Override public boolean hasTileEntity(IBlockState state) { return true; }
    @Override public TileEntity createTileEntity(World w, IBlockState state) { return new TileCable(tier); }
}
