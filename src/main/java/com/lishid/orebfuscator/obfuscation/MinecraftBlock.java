package com.lishid.orebfuscator.obfuscation;

import org.bukkit.block.Block;

public class MinecraftBlock {
    int x, y, z;

    public MinecraftBlock(Block b) {
        this(b.getX(), b.getY(), b.getZ());
    }
    
    public MinecraftBlock(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null || !(other instanceof MinecraftBlock)) {
            return false;
        }
        MinecraftBlock object = (MinecraftBlock) other;
        return this.x == object.x && this.y == object.y && this.z == object.z;
    }

    @Override
    public int hashCode() {
        return x ^ y ^ z;
    }

    @Override
    public String toString() {
        return x + " " + y + " " + z;
    }
}
