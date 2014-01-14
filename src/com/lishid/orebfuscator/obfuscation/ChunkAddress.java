package com.lishid.orebfuscator.obfuscation;

public class ChunkAddress {
    public int x;
    public int z;

    public ChunkAddress(int x, int z) {
        this.x = x;
        this.z = z;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ChunkAddress)) {
            return false;
        }
        ChunkAddress object = (ChunkAddress) obj;
        return this.x == object.x && this.z == object.z;
    }

    @Override
    public int hashCode() {
        return x ^ z;
    }

}
