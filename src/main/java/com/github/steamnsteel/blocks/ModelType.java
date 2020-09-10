package com.github.steamnsteel.blocks;

import net.minecraft.util.IStringSerializable;

public enum ModelType implements IStringSerializable {
    STRAIGHT,
    STRAIGHT_TERMINUS,
    ELBOW,
    MULTI;

    @Override
    public String getString() {
        return this.name().toLowerCase();
    }
}
