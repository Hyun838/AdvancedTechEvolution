package com.advancedtech.research;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.common.util.Constants;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class ResearchData implements IResearchData {
    private int points;
    private final Set<String> unlocked = new LinkedHashSet<>();
    private final Set<String> discovered = new HashSet<>();

    @Override public int getPoints() { return points; }
    @Override public void addPoints(int amount) { points = Math.max(0, points + amount); }

    @Override
    public boolean isUnlocked(String id) {
        ResearchNode n = ResearchRegistry.get(id);
        return n != null && (n.isFree() || unlocked.contains(id));
    }

    @Override
    public boolean canUnlock(String id) {
        ResearchNode n = ResearchRegistry.get(id);
        if (n == null || isUnlocked(id) || points < n.cost) return false;
        for (String parent : n.parents) if (!isUnlocked(parent)) return false;
        return true;
    }

    @Override
    public boolean unlock(String id) {
        if (!canUnlock(id)) return false;
        points -= ResearchRegistry.get(id).cost;
        unlocked.add(id);
        return true;
    }

    @Override public Set<String> getUnlocked() { return Collections.unmodifiableSet(unlocked); }
    @Override public boolean discover(String key) { return discovered.add(key); }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound t = new NBTTagCompound();
        t.setInteger("Points", points);
        t.setTag("Unlocked", toList(unlocked));
        t.setTag("Discovered", toList(discovered));
        return t;
    }

    @Override
    public void deserializeNBT(NBTTagCompound t) {
        points = t.getInteger("Points");
        fromList(t.getTagList("Unlocked", Constants.NBT.TAG_STRING), unlocked);
        fromList(t.getTagList("Discovered", Constants.NBT.TAG_STRING), discovered);
    }

    private static NBTTagList toList(Set<String> set) {
        NBTTagList l = new NBTTagList();
        for (String s : set) l.appendTag(new NBTTagString(s));
        return l;
    }

    private static void fromList(NBTTagList l, Set<String> out) {
        out.clear();
        for (int i = 0; i < l.tagCount(); i++) out.add(l.getStringTagAt(i));
    }
}
