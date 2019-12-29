/**
 * @author Aleksey Terzi
 *
 */

package com.lishid.orebfuscator.nms.v1_13_R1;

import com.lishid.orebfuscator.api.nms.IBlockInfo;

import net.minecraft.server.v1_13_R1.Block;
import net.minecraft.server.v1_13_R1.IBlockData;

public class BlockInfo implements IBlockInfo {
	private int x;
	private int y;
	private int z;
	private IBlockData blockData;
	
	public BlockInfo(int x, int y, int z, IBlockData blockData) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.blockData = blockData;
	}
	
	public int getX() {
		return this.x;
	}
	
	public int getY() {
		return this.y;
	}	
	
	public int getZ() {
		return this.z;
	}
	
	public int getCombinedId() {
		return Block.getCombinedId(this.blockData);
	}

	public IBlockData getBlockData() {
		return this.blockData;
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
