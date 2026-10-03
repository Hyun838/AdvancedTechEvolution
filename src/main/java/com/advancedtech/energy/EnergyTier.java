package com.advancedtech.energy;

import com.advancedtech.config.ATConfig;

/** Четыре уровня напряжения. Числа — стартовый баланс, правится в одном месте. */
public enum EnergyTier {
    LV(32,   4_000,   0.0020f),
    MV(128,  16_000,  0.0015f),
    HV(512,  64_000,  0.0010f),
    EV(2048, 256_000, 0.0005f);

    /** Макс. FE/t, которое может принять/отдать устройство тира. */
    public final int maxTransfer;
    /** Буфер машины тира. */
    public final int capacity;
    /** Доля потерь на один блок кабеля. */
    public final float lossPerBlock;

    EnergyTier(int maxTransfer, int capacity, float lossPerBlock) {
        this.maxTransfer = maxTransfer;
        this.capacity = capacity;
        this.lossPerBlock = lossPerBlock;
    }

    /** Сколько энергии дойдёт через {@code distance} блоков кабеля. */
    public int applyCableLoss(int amount, int distance) {
        if (!ATConfig.enableCableLoss) return amount;
        double loss = Math.min(1.0, lossPerBlock * ATConfig.cableLossMultiplier);
        return (int) Math.floor(amount * Math.pow(1.0 - loss, distance));
    }

    /** Перегрузка кабеля: пропущено больше, чем выдерживает тир. */
    public boolean isOverload(int transferredThisTick) {
        return transferredThisTick > maxTransfer;
    }

    public static EnergyTier byOrdinal(int i) {
        EnergyTier[] v = values();
        return v[Math.max(0, Math.min(v.length - 1, i))];
    }
}
