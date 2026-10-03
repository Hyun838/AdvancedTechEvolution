package com.advancedtech.research;

import com.advancedtech.AdvancedTech;
import com.advancedtech.config.ATConfig;
import com.advancedtech.net.Network;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.Arrays;

@Mod.EventBusSubscriber(modid = AdvancedTech.MODID)
public class ResearchEvents {
    private static final ResourceLocation KEY = new ResourceLocation(AdvancedTech.MODID, "research");

    public static final int POINTS_CRAFT = 5, POINTS_MOB = 10, POINTS_ORE = 8;

    // ---------- Жизненный цикл capability ----------
    @SubscribeEvent
    public static void attach(AttachCapabilitiesEvent<Entity> e) {
        if (e.getObject() instanceof EntityPlayer) e.addCapability(KEY, new ResearchProvider());
    }

    @SubscribeEvent
    public static void clone(net.minecraftforge.event.entity.player.PlayerEvent.Clone e) {
        IResearchData oldD = ResearchCapability.get(e.getOriginal());
        IResearchData newD = ResearchCapability.get(e.getEntityPlayer());
        if (oldD != null && newD != null) newD.deserializeNBT(oldD.serializeNBT()); // сохраняем и после смерти
    }

    @SubscribeEvent
    public static void login(PlayerEvent.PlayerLoggedInEvent e) { sync(e.player); }
    @SubscribeEvent
    public static void respawn(PlayerEvent.PlayerRespawnEvent e) { sync(e.player); }
    @SubscribeEvent
    public static void dim(PlayerEvent.PlayerChangedDimensionEvent e) { sync(e.player); }

    private static void sync(EntityPlayer p) {
        if (p instanceof EntityPlayerMP) Network.syncResearch((EntityPlayerMP) p);
    }

    // ---------- Получение очков ----------
    public static void award(EntityPlayer player, int base, String keyForActionBar) {
        if (!(player instanceof EntityPlayerMP) || player instanceof FakePlayer) return;
        IResearchData d = ResearchCapability.get(player);
        if (d == null) return;
        int pts = Math.max(1, (int) Math.round(base * ATConfig.researchPointMultiplier));
        d.addPoints(pts);
        player.sendStatusMessage(new TextComponentString("+" + pts + " очк. исследований (" + keyForActionBar + ")"), true);
        Network.syncResearch((EntityPlayerMP) player);
    }

    private static boolean first(EntityPlayer p, String key) {
        IResearchData d = ResearchCapability.get(p);
        return d != null && d.discover(key);
    }

    /** Первый крафт нового предмета. */
    @SubscribeEvent
    public static void crafted(PlayerEvent.ItemCraftedEvent e) {
        if (e.player.world.isRemote || e.crafting.isEmpty()) return;
        ResourceLocation id = e.crafting.getItem().getRegistryName();
        if (id != null && first(e.player, "craft:" + id)) award(e.player, POINTS_CRAFT, "крафт");
    }

    /** Убийство нового типа моба. */
    @SubscribeEvent
    public static void kill(LivingDeathEvent e) {
        Entity src = e.getSource().getTrueSource();
        if (!(src instanceof EntityPlayerMP) || src instanceof FakePlayer) return;
        ResourceLocation id = EntityList.getKey(e.getEntityLiving());
        if (id != null && first((EntityPlayer) src, "mob:" + id)) award((EntityPlayer) src, POINTS_MOB, "новый моб");
    }

    /** Добыча редкой руды (первая каждого вида). */
    @SubscribeEvent
    public static void harvest(BlockEvent.HarvestDropsEvent e) {
        EntityPlayer p = e.getHarvester();
        if (p == null || p instanceof FakePlayer || e.getWorld().isRemote) return;
        ResourceLocation id = e.getState().getBlock().getRegistryName();
        if (id == null || !Arrays.asList(ATConfig.rareOres).contains(id.toString())) return;
        if (first(p, "ore:" + id)) award(p, POINTS_ORE, "редкая руда");
    }

    // ---------- Блокировка крафта ----------
    /**
     * Если результат верстака/2×2-крафта требует неизученного исследования — убираем результат.
     * Для «жёсткой» защиты (шифт-клик в одном тике) используйте собственный IRecipe / CraftTweaker.
     */
    @SubscribeEvent
    public static void tick(TickEvent.PlayerTickEvent e) {
        if (e.phase != TickEvent.Phase.END || e.side != Side.SERVER || !ATConfig.enableResearch) return;
        Container c = e.player.openContainer;
        if (!(c instanceof ContainerWorkbench) && !(c instanceof ContainerPlayer)) return;

        Slot out = c.getSlot(0);
        ItemStack s = out.getStack();
        if (s.isEmpty()) return;

        String req = ResearchRegistry.getRequirement(s);
        IResearchData d = ResearchCapability.get(e.player);
        if (req != null && (d == null || !d.isUnlocked(req))) {
            out.putStack(ItemStack.EMPTY);
            if (e.player.ticksExisted % 40 == 0)
                e.player.sendStatusMessage(new TextComponentTranslation("msg.advancedtech.locked", req), true);
        }
    }
}
