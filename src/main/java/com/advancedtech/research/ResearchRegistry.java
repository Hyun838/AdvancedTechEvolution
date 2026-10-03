package com.advancedtech.research;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/** Технологическое древо и таблица блокировок предметов. Публичный API — можно вызывать из CraftTweaker. */
public class ResearchRegistry {
    private static final Map<String, ResearchNode> NODES = new LinkedHashMap<>();
    private static final Map<ResourceLocation, String> LOCKS = new LinkedHashMap<>();

    static {
        add(new ResearchNode("basic_machines", 0));
        add(new ResearchNode("electric_circuits", 25, "basic_machines"));
        add(new ResearchNode("processing", 30, "basic_machines"));
        add(new ResearchNode("pollution_control", 80, "electric_circuits"));
        add(new ResearchNode("automation", 120, "electric_circuits", "processing"));
        add(new ResearchNode("mv_tier", 150, "automation"));
        add(new ResearchNode("hv_tier", 300, "mv_tier"));
        add(new ResearchNode("quantum_tech", 600, "hv_tier"));
    }

    public static void add(ResearchNode n) { NODES.put(n.id, n); }

    @Nullable public static ResearchNode get(String id) { return NODES.get(id); }

    public static Collection<ResearchNode> all() { return NODES.values(); }

    /** Заблокировать крафт предмета/блока до изучения исследования. */
    public static void lock(String itemRegistryName, String researchId) {
        LOCKS.put(new ResourceLocation(itemRegistryName), researchId);
    }

    /** id требуемого исследования или null, если предмет свободен. */
    @Nullable
    public static String getRequirement(ItemStack stack) {
        if (stack.isEmpty() || stack.getItem().getRegistryName() == null) return null;
        return LOCKS.get(stack.getItem().getRegistryName());
    }
}
