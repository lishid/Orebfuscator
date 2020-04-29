package net.imprex.orebfuscator.proximityhider;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.bukkit.World;

import net.imprex.orebfuscator.util.BlockCoords;

public class ProximityWorldData {

	public static long getKey(int chunkX, int chunkZ) {
		return (chunkZ & 0xffffffffL) << 32 | chunkX & 0xffffffffL;
	}

	private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock(true);

	private final World world;
	private final Map<Long, Set<BlockCoords>> chunks = new HashMap<>();

	public ProximityWorldData(World world) {
		this.world = world;
	}

	public void putBlocks(int chunkX, int chunkZ, Set<BlockCoords> blocks) {
		long key = ProximityWorldData.getKey(chunkX, chunkZ);

		this.readWriteLock.writeLock().lock();
		try {
			this.chunks.put(key, new HashSet<>(blocks));
		} finally {
			this.readWriteLock.writeLock().unlock();
		}
	}

	public Set<BlockCoords> getBlocks(int chunkX, int chunkZ) {
		long key = ProximityWorldData.getKey(chunkX, chunkZ);

		this.readWriteLock.readLock().lock();
		try {
			return this.chunks.get(key);
		} finally {
			this.readWriteLock.readLock().unlock();
		}
	}

	public void removeChunk(int chunkX, int chunkZ) {
		long key = ProximityWorldData.getKey(chunkX, chunkZ);

		this.readWriteLock.writeLock().lock();
		try {
			this.chunks.remove(key);
		} finally {
			this.readWriteLock.writeLock().unlock();
		}
	}

	public World getWorld() {
		return this.world;
	}
}