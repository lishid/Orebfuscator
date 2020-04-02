package net.imprex.orebfuscator.cache;

import java.util.HashSet;
import java.util.Set;

import net.imprex.orebfuscator.util.BlockCoords;

public class ChunkCacheEntry {

	private final long hash;
	private final byte[] data;

	private final Set<BlockCoords> proximityBlocks;
	private final Set<BlockCoords> removedTileEntities;

	public ChunkCacheEntry(long hash, byte[] data) {
		this(hash, data, new HashSet<>(), new HashSet<>());
	}

	public ChunkCacheEntry(long hash, byte[] data, Set<BlockCoords> proximityBlocks,
			Set<BlockCoords> removedTileEntities) {
		this.hash = hash;
		this.data = data;
		this.proximityBlocks = proximityBlocks;
		this.removedTileEntities = removedTileEntities;
	}

	public long getHash() {
		return this.hash;
	}

	public byte[] getData() {
		return this.data;
	}

	public Set<BlockCoords> getProximityBlocks() {
		return this.proximityBlocks;
	}

	public Set<BlockCoords> getRemovedTileEntities() {
		return this.removedTileEntities;
	}
}
