package com.advancedtech.research;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.Set;

public interface IResearchData extends INBTSerializable<NBTTagCompound> {
    int getPoints();
    void addPoints(int amount);

    boolean isUnlocked(String id);
    /** Все родители изучены и хватает очков. */
    boolean canUnlock(String id);
    /** Изучить (списывает очки). */
    boolean unlock(String id);
    Set<String> getUnlocked();

    /** Запоминает «первое событие» (крафт/моб/руда). true — событие новое. */
    boolean discover(String key);
}
