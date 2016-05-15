package com.lishid.orebfuscator.internal;

import net.minecraft.server.v1_9_R1.Block;
import net.minecraft.server.v1_9_R1.IBlockData;

public class BlockInfo {
	public int x;
	public int y;
	public int z;
	public IBlockData blockData;
	
	public int getTypeId() {
		return Block.getId(this.blockData.getBlock());
	}
	
	@Override
	public boolean equals(Object other) {
		if (other == null || !(other instanceof BlockInfo)) {
			return false;
		}

		BlockInfo object = (BlockInfo) other;
		
		return this.x == object.x && this.y == object.y && this.z == object.z;
	}
	
	@Override
	public int hashCode() {
		return this.x ^ this.y ^ this.z;
	}
	
	@Override
	public String toString() {
		return this.x + " " + this.y + " " + this.z;
	}
}
