package com.advancedtech.item;

import com.advancedtech.AdvancedTech;
import com.advancedtech.net.Network;
import com.advancedtech.research.IResearchData;
import com.advancedtech.research.ResearchCapability;
import com.advancedtech.research.ResearchNode;
import com.advancedtech.research.ResearchRegistry;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

/**
 * Временный интерфейс (до GUI): ПКМ — показать статус в чате, Shift+ПКМ — изучить первое доступное.
 * TODO: заменить на GuiResearchBook с вкладками.
 */
public class ItemEngineerBook extends Item {
    public ItemEngineerBook() {
        setRegistryName(AdvancedTech.MODID, "engineer_book");
        setUnlocalizedName(AdvancedTech.MODID + ".engineer_book");
        setCreativeTab(CreativeTabs.MISC);
        setMaxStackSize(1);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World w, EntityPlayer p, EnumHand hand) {
        ItemStack stack = p.getHeldItem(hand);
        if (w.isRemote) return new ActionResult<>(EnumActionResult.SUCCESS, stack);

        IResearchData d = ResearchCapability.get(p);
        if (d == null) return new ActionResult<>(EnumActionResult.FAIL, stack);

        if (p.isSneaking()) {
            for (ResearchNode n : ResearchRegistry.all()) {
                if (d.unlock(n.id)) {
                    p.sendMessage(new TextComponentString(TextFormatting.GREEN + "Изучено: " + n.id));
                    break;
                }
            }
        }
        p.sendMessage(new TextComponentString(TextFormatting.AQUA + "Очки исследований: " + d.getPoints()));
        p.sendMessage(new TextComponentString(TextFormatting.GOLD + "Загрязнение чанка: "
                + Math.round(com.advancedtech.pollution.PollutionManager.get(w, p.getPosition()))));
        for (ResearchNode n : ResearchRegistry.all()) {
            TextFormatting c = d.isUnlocked(n.id) ? TextFormatting.GREEN
                    : d.canUnlock(n.id) ? TextFormatting.YELLOW : TextFormatting.DARK_GRAY;
            p.sendMessage(new TextComponentString(c + " • " + n.id + " (" + n.cost + ")"));
        }
        if (p instanceof EntityPlayerMP) Network.syncResearch((EntityPlayerMP) p);
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }
}
