package com.advancedtech.registry;

import com.advancedtech.AdvancedTech;
import com.advancedtech.block.BlockCable;
import com.advancedtech.block.BlockMachine;
import com.advancedtech.energy.EnergyTier;
import com.advancedtech.item.ItemEngineerBook;
import com.advancedtech.research.ResearchRegistry;
import com.advancedtech.tile.TileCable;
import com.advancedtech.tile.TileCombustionGenerator;
import com.advancedtech.tile.TileElectricFurnace;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod.EventBusSubscriber(modid = AdvancedTech.MODID)
public class RegistryHandler {

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> e) {
        ModBlocks.ELECTRIC_FURNACE = new BlockMachine("electric_furnace", TileElectricFurnace::new, "processing");
        ModBlocks.COMBUSTION_GENERATOR = new BlockMachine("combustion_generator", TileCombustionGenerator::new, "electric_circuits");
        e.getRegistry().registerAll(ModBlocks.ELECTRIC_FURNACE, ModBlocks.COMBUSTION_GENERATOR);

        for (EnergyTier t : EnergyTier.values()) {
            BlockCable c = new BlockCable("cable_" + t.name().toLowerCase(), t);
            ModBlocks.CABLES[t.ordinal()] = c;
            e.getRegistry().register(c);
        }

        GameRegistry.registerTileEntity(TileElectricFurnace.class, new ResourceLocation(AdvancedTech.MODID, "electric_furnace"));
        GameRegistry.registerTileEntity(TileCombustionGenerator.class, new ResourceLocation(AdvancedTech.MODID, "combustion_generator"));
        GameRegistry.registerTileEntity(TileCable.class, new ResourceLocation(AdvancedTech.MODID, "cable"));

        // Блокировки крафта до изучения
        ResearchRegistry.lock("advancedtech:electric_furnace", "processing");
        ResearchRegistry.lock("advancedtech:combustion_generator", "electric_circuits");
        ResearchRegistry.lock("advancedtech:air_filter", "pollution_control");
        ResearchRegistry.lock("advancedtech:cable_mv", "mv_tier");
        ResearchRegistry.lock("advancedtech:cable_hv", "hv_tier");
        ResearchRegistry.lock("advancedtech:cable_ev", "quantum_tech");
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> e) {
        ModItems.ENGINEER_BOOK = new ItemEngineerBook();
        ModItems.CIRCUIT = new Item()
                .setRegistryName(AdvancedTech.MODID, "circuit")
                .setUnlocalizedName(AdvancedTech.MODID + ".circuit")
                .setCreativeTab(CreativeTabs.MATERIALS);
        ModItems.AIR_FILTER = new Item()
                .setRegistryName(AdvancedTech.MODID, "air_filter")
                .setUnlocalizedName(AdvancedTech.MODID + ".air_filter")
                .setCreativeTab(CreativeTabs.MISC)
                .setMaxStackSize(1)
                .setMaxDamage(1200);   // ~20 минут работы генератора

        e.getRegistry().registerAll(ModItems.ENGINEER_BOOK, ModItems.CIRCUIT, ModItems.AIR_FILTER,
                itemBlock(ModBlocks.ELECTRIC_FURNACE), itemBlock(ModBlocks.COMBUSTION_GENERATOR));
        for (BlockCable c : ModBlocks.CABLES) e.getRegistry().register(itemBlock(c));
    }

    private static Item itemBlock(Block b) {
        return new ItemBlock(b).setRegistryName(b.getRegistryName());
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent e) {
        model(ModItems.ENGINEER_BOOK);
        model(ModItems.CIRCUIT);
        model(ModItems.AIR_FILTER);
        model(Item.getItemFromBlock(ModBlocks.ELECTRIC_FURNACE));
        model(Item.getItemFromBlock(ModBlocks.COMBUSTION_GENERATOR));
        for (BlockCable c : ModBlocks.CABLES) model(Item.getItemFromBlock(c));
    }

    @SideOnly(Side.CLIENT)
    private static void model(Item item) {
        ModelLoader.setCustomModelResourceLocation(item, 0,
                new ModelResourceLocation(item.getRegistryName(), "inventory"));
    }
}
