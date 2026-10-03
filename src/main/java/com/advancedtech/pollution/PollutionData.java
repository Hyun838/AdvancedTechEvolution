package com.advancedtech.pollution;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;

import java.util.HashMap;
import java.util.Map;

/** Уровень загрязнения по чанкам (ключ — ChunkPos.asLong). Хранится в мире. */
public class PollutionData extends WorldSavedData {
    public static final String ID = "advancedtech_pollution";
    public final Map<Long, Float> map = new HashMap<>();

    public PollutionData() { super(ID); }
    public PollutionData(String id) { super(id); }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        map.clear();
        NBTTagList l = nbt.getTagList("c", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < l.tagCount(); i++) {
            NBTTagCompound t = l.getCompoundTagAt(i);
            map.put(ChunkPos.asLong(t.getInteger("x"), t.getInteger("z")), t.getFloat("v"));
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        NBTTagList l = new NBTTagList();
        for (Map.Entry<Long, Float> e : map.entrySet()) {
            NBTTagCompound t = new NBTTagCompound();
            t.setInteger("x", (int) (long) e.getKey());
            t.setInteger("z", (int) (e.getKey() >> 32));
            t.setFloat("v", e.getValue());
            l.appendTag(t);
        }
        nbt.setTag("c", l);
        return nbt;
    }
}
