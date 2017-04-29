/**
 * @author Aleksey Terzi
 *
 */

package com.lishid.orebfuscator.chunkmap;

import java.util.List;

import com.comphenix.protocol.wrappers.nbt.NbtCompound;

public class ChunkData {
	public int chunkX;
	public int chunkZ;
	public boolean groundUpContinuous;
	public int primaryBitMask;
	public byte[] data;
	public boolean isOverworld;
	public boolean useCache;
	public List<NbtCompound> blockEntities;
}
