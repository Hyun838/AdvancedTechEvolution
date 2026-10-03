package com.advancedtech.research;

public class ResearchNode {
    public final String id;
    public final int cost;
    public final String[] parents;

    public ResearchNode(String id, int cost, String... parents) {
        this.id = id;
        this.cost = cost;
        this.parents = parents;
    }

    /** Стартовое исследование: бесплатное и без родителей. */
    public boolean isFree() { return cost == 0 && parents.length == 0; }

    public String getNameKey() { return "research.advancedtech." + id; }
}
