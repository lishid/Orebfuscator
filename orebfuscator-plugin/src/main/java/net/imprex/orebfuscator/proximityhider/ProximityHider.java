package net.imprex.orebfuscator.proximityhider;

import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.bukkit.World;
import org.bukkit.entity.Player;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import net.imprex.orebfuscator.Orebfuscator;
import net.imprex.orebfuscator.config.OrebfuscatorConfig;
import net.imprex.orebfuscator.config.ProximityWorldConfig;
import net.imprex.orebfuscator.util.BlockCoords;

public class ProximityHider {

	private final LoadingCache<Player, ProximityWorldData> playerData = CacheBuilder.newBuilder()
			.build(new CacheLoader<Player, ProximityWorldData>() {

				@Override
				public ProximityWorldData load(Player player) throws Exception {
					return new ProximityWorldData(player.getWorld());
				}
			});

	private final Orebfuscator orebfuscator;
	private final OrebfuscatorConfig config;

	private final ProximityQueue queue = new ProximityQueue();

	private final ProximityThread[] queueThreads;

	public ProximityHider(Orebfuscator orebfuscator) {
		this.orebfuscator = orebfuscator;
		this.config = this.orebfuscator.getOrebfuscatorConfig();

		this.queueThreads = new ProximityThread[this.config.general().proximityHiderRunnerSize()];
	}

	public void start() {
		int size = 0;

		for (ProximityThread thread : this.queueThreads) {
			if (thread == null) {
				thread = new ProximityThread(this, this.orebfuscator);
				thread.setDaemon(true);
				thread.setName("OFC - ProximityHider Thread - " + ++size);
				thread.start();
			}
		}
	}

	public Player pollPlayer() {
		return this.queue.poll();
	}

	public void queuePlayer(Player player) {
		this.queue.offerAndLock(player);
	}

	public void unlockPlayer(Player player) {
		this.queue.unlock(player);
	}

	public void removePlayer(Player player) {
		this.queue.remove(player);
		this.playerData.invalidate(player);
	}

	public ProximityWorldData getPlayer(Player player) {
		try {
			ProximityWorldData proximityWorldData = playerData.get(player);

			if (proximityWorldData.getWorld() != player.getWorld()) {
				proximityWorldData = new ProximityWorldData(player.getWorld());
				playerData.put(player, proximityWorldData);
			}

			return proximityWorldData;
		} catch (ExecutionException e) {
			e.printStackTrace();
		}

		return null;
	}

	public void addProximityBlocks(Player player, World world, int chunkX, int chunkZ, Set<BlockCoords> blocks) {
		ProximityWorldConfig proximityWorldConfig = this.config.proximity(player.getWorld());

		if (proximityWorldConfig == null || !proximityWorldConfig.enabled()) {
			return;
		}

		ProximityWorldData worldData = this.getPlayer(player);

		if (blocks.size() > 0) {
			worldData.putBlocks(chunkX, chunkZ, blocks);
		} else {
			worldData.removeChunk(chunkX, chunkZ);
		}

		this.queuePlayer(player);
	}

	public void destroy() {
		for (ProximityThread thread : this.queueThreads) {
			if (thread != null) {
				thread.destroy();
			}
		}
	}
}