package com.advancedtech.research;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

import javax.annotation.Nullable;

public class ResearchCapability {
    @CapabilityInject(IResearchData.class)
    public static Capability<IResearchData> RESEARCH = null;

    @Nullable
    public static IResearchData get(EntityPlayer p) {
        return p == null ? null : p.getCapability(RESEARCH, null);
    }
}
