package com.lishid.orebfuscator.obfuscation;

import com.lishid.orebfuscator.chunkmap.ChunkMapManager;

public class ProximityHiderBlock {
	int blockData;
    int x, y, z;

    public ProximityHiderBlock(int blockData, int x, int y, int z) {
    	this.blockData = blockData;
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public int getId() {
    	return ChunkMapManager.getBlockIdFromData(blockData);
    }
    
    public int getMeta() {
    	return ChunkMapManager.getBlockMetaFromData(blockData);
    }

    @Override
    public boolean equals(Object other) {
        if (other == null || !(other instanceof ProximityHiderBlock)) {
            return false;
        }
        ProximityHiderBlock object = (ProximityHiderBlock) other;
        return this.blockData == object.blockData && this.x == object.x && this.y == object.y && this.z == object.z;
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
