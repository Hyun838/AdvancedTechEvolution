package com.advancedtech.pollution;

import com.advancedtech.config.ATConfig;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;

public class PollutionManager {

    public static PollutionData data(World w) {
        MapStorage st = w.getPerWorldStorage();
        PollutionData d = (PollutionData) st.getOrLoadData(PollutionData.class, PollutionData.ID);
        if (d == null) {
            d = new PollutionData();
            st.setData(PollutionData.ID, d);
        }
        return d;
    }

    public static void add(World w, BlockPos pos, float amount) {
        if (!ATConfig.enablePollution || w.isRemote || amount <= 0) return;
        PollutionData d = data(w);
        d.map.merge(ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4), amount, Float::sum);
        d.markDirty();
    }

    public static float get(World w, BlockPos pos) {
        if (w.isRemote) return 0;
        Float v = data(w).map.get(ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4));
        return v == null ? 0 : v;
    }
}
