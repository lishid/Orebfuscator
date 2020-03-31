package net.imprex.orebfuscator.cache;

import java.util.ArrayList;
import java.util.List;

import net.imprex.orebfuscator.util.BlockCoords;

public class ChunkCacheEntry {

	private final long hash;
	private final byte[] data;

	private final List<BlockCoords> proximityBlocks;
	private final List<BlockCoords> removedEntities;

	public ChunkCacheEntry(long hash, byte[] data) {
		this(hash, data, new ArrayList<>(), new ArrayList<>());
	}

	public ChunkCacheEntry(long hash, byte[] data, List<BlockCoords> proximityBlocks,
			List<BlockCoords> removedEntities) {
		this.hash = hash;
		this.data = data;
		this.proximityBlocks = proximityBlocks;
		this.removedEntities = removedEntities;
	}

	public long getHash() {
		return this.hash;
	}

	public byte[] getData() {
		return this.data;
	}

	public List<BlockCoords> getProximityBlocks() {
		return this.proximityBlocks;
	}

	public List<BlockCoords> getRemovedEntities() {
		return this.removedEntities;
	}
}
