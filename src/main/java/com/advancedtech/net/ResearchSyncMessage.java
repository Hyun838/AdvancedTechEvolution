package com.advancedtech.net;

import com.advancedtech.research.IResearchData;
import com.advancedtech.research.ResearchCapability;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class ResearchSyncMessage implements IMessage {
    private NBTTagCompound tag;

    public ResearchSyncMessage() {}
    public ResearchSyncMessage(NBTTagCompound tag) { this.tag = tag; }

    @Override public void fromBytes(ByteBuf buf) { tag = ByteBufUtils.readTag(buf); }
    @Override public void toBytes(ByteBuf buf) { ByteBufUtils.writeTag(buf, tag); }

    /** Регистрируется только на клиенте (Side.CLIENT). */
    public static class Handler implements IMessageHandler<ResearchSyncMessage, IMessage> {
        @Override
        public IMessage onMessage(ResearchSyncMessage msg, MessageContext ctx) {
            Minecraft mc = Minecraft.getMinecraft();
            mc.addScheduledTask(() -> {
                IResearchData d = ResearchCapability.get(mc.player);
                if (d != null && msg.tag != null) d.deserializeNBT(msg.tag);
            });
            return null;
        }
    }
}
