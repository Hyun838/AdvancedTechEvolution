package com.advancedtech.pollution;

import com.advancedtech.AdvancedTech;
import com.advancedtech.config.ATConfig;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockGrass;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockReed;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Раз в 5 секунд: распад, расползание по соседним чанкам и последствия.
 *   > 200 — гибнут растения; > 300 — листва; > 400 — трава → земля; > 600 — яд для игроков.
 */
@Mod.EventBusSubscriber(modid = AdvancedTech.MODID)
public class PollutionEvents {
    private static final int[][] DIRS = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

    @SubscribeEvent
    public static void tick(TickEvent.WorldTickEvent e) {
        World w = e.world;
        if (e.phase != TickEvent.Phase.END || w.isRemote || !ATConfig.enablePollution) return;
        if (w.getTotalWorldTime() % 100 != 0) return;

        PollutionData d = PollutionManager.data(w);
        if (d.map.isEmpty()) return;

        Map<Long, Float> delta = new HashMap<>();
        for (Map.Entry<Long, Float> en : d.map.entrySet()) {
            long k = en.getKey();
            float v = en.getValue();
            int cx = (int) k, cz = (int) (k >> 32);

            if (v > 50f) {                                   // расползание: 4% соседу
                float share = v * 0.04f;
                for (int[] dir : DIRS) delta.merge(ChunkPos.asLong(cx + dir[0], cz + dir[1]), share, Float::sum);
                delta.merge(k, -share * 4, Float::sum);
            }
            delta.merge(k, -(v * 0.02f + 0.5f), Float::sum); // естественный распад

            applyEffects(w, cx, cz, v);
        }
        for (Map.Entry<Long, Float> en : delta.entrySet()) {
            float nv = d.map.getOrDefault(en.getKey(), 0f) + en.getValue();
            if (nv < 1f) d.map.remove(en.getKey()); else d.map.put(en.getKey(), nv);
        }
        d.markDirty();
    }

    private static void applyEffects(World w, int cx, int cz, float v) {
        if (v < 200f || !w.isBlockLoaded(new BlockPos(cx * 16 + 8, 64, cz * 16 + 8))) return;
        Random r = w.rand;
        int attempts = v > 600f ? 12 : 6;
        for (int i = 0; i < attempts; i++) {
            BlockPos top = w.getHeight(new BlockPos(cx * 16 + r.nextInt(16), 0, cz * 16 + r.nextInt(16)));
            for (int dy = 2; dy >= -6; dy--) {
                BlockPos p = top.up(dy);
                IBlockState s = w.getBlockState(p);
                Block b = s.getBlock();
                if (b instanceof BlockBush || b instanceof BlockReed || (b instanceof BlockLeaves && v > 300f)) {
                    w.setBlockToAir(p);
                    break;
                }
                if (b instanceof BlockGrass && v > 400f) {
                    w.setBlockState(p, Blocks.DIRT.getDefaultState());
                    break;
                }
            }
        }
        if (v > 600f) {
            for (EntityPlayer pl : w.playerEntities) {
                if ((((int) Math.floor(pl.posX)) >> 4) == cx && (((int) Math.floor(pl.posZ)) >> 4) == cz)
                    pl.addPotionEffect(new PotionEffect(MobEffects.POISON, 120, 0));
            }
        }
    }
}
