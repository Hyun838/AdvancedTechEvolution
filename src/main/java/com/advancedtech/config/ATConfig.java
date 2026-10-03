package com.advancedtech.config;

import com.advancedtech.AdvancedTech;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = AdvancedTech.MODID, name = "AdvancedTechEvolution")
public class ATConfig {

    @Config.Comment("Сложность рецептов: 0 = лёгкая, 1 = обычная, 2 = сложная (читается загрузчиком рецептов / CraftTweaker)")
    @Config.RangeInt(min = 0, max = 2)
    public static int recipeDifficulty = 1;

    @Config.Comment("Включить систему тепла и перегрев машин")
    public static boolean enableHeat = true;

    @Config.Comment("Включить потери энергии в кабелях")
    public static boolean enableCableLoss = true;

    @Config.Comment("Множитель потерь в кабелях")
    @Config.RangeDouble(min = 0, max = 10)
    public static double cableLossMultiplier = 1.0;

    @Config.Comment("Включить загрязнение чанков")
    public static boolean enablePollution = true;

    @Config.Comment("Включить систему исследований (если false — всё разблокировано)")
    public static boolean enableResearch = true;

    @Config.Comment("Множитель очков исследований")
    @Config.RangeDouble(min = 0.1, max = 10)
    public static double researchPointMultiplier = 1.0;

    @Config.Comment("Редкие блоки, добыча которых впервые даёт очки исследований")
    public static String[] rareOres = {
            "minecraft:diamond_ore", "minecraft:emerald_ore", "minecraft:lapis_ore",
            "minecraft:gold_ore", "minecraft:redstone_ore"
    };

    @Mod.EventBusSubscriber(modid = AdvancedTech.MODID)
    public static class Sync {
        @SubscribeEvent
        public static void onChanged(ConfigChangedEvent.OnConfigChangedEvent e) {
            if (e.getModID().equals(AdvancedTech.MODID)) {
                ConfigManager.sync(AdvancedTech.MODID, Config.Type.INSTANCE);
            }
        }
    }
}
