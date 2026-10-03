package com.advancedtech.net;

import com.advancedtech.AdvancedTech;
import com.advancedtech.research.IResearchData;
import com.advancedtech.research.ResearchCapability;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class Network {
    public static SimpleNetworkWrapper CHANNEL;

    public static void init() {
        CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel(AdvancedTech.MODID);
        CHANNEL.registerMessage(ResearchSyncMessage.Handler.class, ResearchSyncMessage.class, 0, Side.CLIENT);
    }

    /** Отправить игроку актуальные данные исследований. */
    public static void syncResearch(EntityPlayerMP player) {
        IResearchData data = ResearchCapability.get(player);
        if (data != null) CHANNEL.sendTo(new ResearchSyncMessage(data.serializeNBT()), player);
    }
}
