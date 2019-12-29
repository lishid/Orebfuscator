package com.lishid.orebfuscator.proximityhider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.lishid.orebfuscator.api.IProximityHiderHandler;
import com.lishid.orebfuscator.api.Orebfuscator;
import com.lishid.orebfuscator.api.config.IConfigManager;
import com.lishid.orebfuscator.api.config.IOrebfuscatorConfig;
import com.lishid.orebfuscator.api.config.IProximityHiderConfig;
import com.lishid.orebfuscator.api.config.IWorldConfig;
import com.lishid.orebfuscator.api.logger.OFCLogger;
import com.lishid.orebfuscator.api.nms.IBlockInfo;
import com.lishid.orebfuscator.api.nms.INmsManager;
import com.lishid.orebfuscator.api.types.BlockCoord;
import com.lishid.orebfuscator.handler.CraftHandler;

public class ProximityHiderHandler extends CraftHandler implements IProximityHiderHandler {

	private final Map<Player, ProximityHiderPlayer> proximityHiderTracker = new HashMap<Player, ProximityHiderPlayer>();
	private final Map<Player, Location> playersToCheck = new HashMap<Player, Location>();
	private final HashSet<Player> playersToReload = new HashSet<Player>();

	private final Map<Player, ProximityHiderPlayer> proximityPlayers = new HashMap<Player, ProximityHiderPlayer>();
	private final AtomicBoolean running = new AtomicBoolean(true);

	private INmsManager nmsManager;
	private IOrebfuscatorConfig config;
	private IConfigManager configManager;

	private long lastExecute = System.currentTimeMillis();

	private Thread thread;

	public ProximityHiderHandler(Orebfuscator plugin) {
		super(plugin);
	}

	@Override
	public void onInit() {
		this.nmsManager = this.plugin.getNmsManager();
		this.config = this.plugin.getConfigHandler().getConfig();
		this.configManager = this.plugin.getConfigHandler().getConfigManager();
	}

	@Override
	public void onEnable() {
		this.running.compareAndSet(this.running.get(), true);

		if (this.thread == null || this.thread.isInterrupted() || !this.thread.isAlive()) {
			this.thread = new Thread(new ProximityHiderRunner());
			this.thread.setName("Orebfuscator ProximityHider Thread");
			this.thread.setPriority(Thread.MIN_PRIORITY);
			this.thread.setDaemon(true);
			this.thread.start();
		}
	}

	@Override
	public void onDisable() {
		this.running.compareAndSet(this.running.get(), false);

		if (this.thread != null && !this.thread.isInterrupted()) {
			try {
				this.thread.interrupt();
			} catch(Exception e) {
				OFCLogger.log(e);
			}
		}

		this.thread = null;
	}

	@Override
	public boolean enableHandler() {
		return this.config.isProximityHiderEnabled();
	}

	public void restartThread() {
		synchronized (this.thread) {
			if (this.thread.isInterrupted() || this.thread.isAlive()) {
				this.running.compareAndSet(this.running.get(), false);

				if (!this.running.get() && this.enableHandler()) {
					this.onEnable();
				}
			}
		}
	}

	/**
	 * Basic idea here is to take some rays from the considered block to the
	 * player's eyes, and decide if any of those rays can reach the eyes unimpeded.
	 * 
	 * @param block  the starting block
	 * @param eyes   the destination eyes
	 * @param player the player world we are testing for
	 * @return true if unimpeded path, false otherwise
	 */
	private boolean doFastCheck(Location block, Location eyes, World player) {
		double ex = eyes.getX();
		double ey = eyes.getY();
		double ez = eyes.getZ();
		double x = block.getBlockX();
		double y = block.getBlockY();
		double z = block.getBlockZ();
		return // midfaces
		fastAABBRayCheck(x, y, z, x, y + 0.5, z + 0.5, ex, ey, ez, player)
				|| fastAABBRayCheck(x, y, z, x + 0.5, y, z + 0.5, ex, ey, ez, player)
				|| fastAABBRayCheck(x, y, z, x + 0.5, y + 0.5, z, ex, ey, ez, player)
				|| fastAABBRayCheck(x, y, z, x + 0.5, y + 1.0, z + 0.5, ex, ey, ez, player)
				|| fastAABBRayCheck(x, y, z, x + 0.5, y + 0.5, z + 1.0, ex, ey, ez, player)
				|| fastAABBRayCheck(x, y, z, x + 1.0, y + 0.5, z + 0.5, ex, ey, ez, player) ||
				// corners
				fastAABBRayCheck(x, y, z, x, y, z, ex, ey, ez, player)
				|| fastAABBRayCheck(x, y, z, x + 1, y, z, ex, ey, ez, player)
				|| fastAABBRayCheck(x, y, z, x, y + 1, z, ex, ey, ez, player)
				|| fastAABBRayCheck(x, y, z, x + 1, y + 1, z, ex, ey, ez, player)
				|| fastAABBRayCheck(x, y, z, x, y, z + 1, ex, ey, ez, player)
				|| fastAABBRayCheck(x, y, z, x + 1, y, z + 1, ex, ey, ez, player)
				|| fastAABBRayCheck(x, y, z, x, y + 1, z + 1, ex, ey, ez, player)
				|| fastAABBRayCheck(x, y, z, x + 1, y + 1, z + 1, ex, ey, ez, player);
	}

	private boolean fastAABBRayCheck(double bx, double by, double bz, double x, double y, double z, double ex,
			double ey, double ez, World world) {
		double fx = ex - x;
		double fy = ey - y;
		double fz = ez - z;
		double absFx = Math.abs(fx);
		double absFy = Math.abs(fy);
		double absFz = Math.abs(fz);
		double s = Math.max(absFx, Math.max(absFy, absFz));

		if (s < 1) {
			return true; // on top / inside
		}

		double lx, ly, lz;

		fx = fx / s; // units of change along vector
		fy = fy / s;
		fz = fz / s;

		while (s > 0) {
			ex = ex - fx; // move along vector, we start _at_ the eye and move towards b
			ey = ey - fy;
			ez = ez - fz;
			lx = Math.floor(ex);
			ly = Math.floor(ey);
			lz = Math.floor(ez);
			if (lx == bx && ly == by && lz == bz)
				return true; // we've reached our starting block, don't test it.

			IBlockInfo between = this.nmsManager.getBlockInfo(world, (int) lx, (int) ly, (int) lz);
			if (between != null && !this.config.isBlockTransparent(between.getCombinedId())) {
				return false; // fail on first hit, this ray is "blocked"
			}

			s--; // we stop
		}
		return true;
	}

	public void addProximityBlocks(Player player, int chunkX, int chunkZ, ArrayList<BlockCoord> blocks) {
		IProximityHiderConfig proximityHider = this.configManager.getWorld(player.getWorld()).getProximityHiderConfig();

		if (!proximityHider.isEnabled())
			return;

		this.restartThread();

		synchronized (this.proximityHiderTracker) {
			ProximityHiderPlayer playerInfo = this.proximityHiderTracker.get(player);
			World world = player.getWorld();

			if (playerInfo == null) {
				this.proximityHiderTracker.put(player, playerInfo = new ProximityHiderPlayer(world));
			} else if (!playerInfo.getWorld().equals(world)) {
				playerInfo.setWorld(world);
				playerInfo.clearChunks();
			}

			if (blocks.size() > 0) {
				playerInfo.putBlocks(chunkX, chunkZ, blocks);
			} else {
				playerInfo.removeChunk(chunkX, chunkZ);
			}
		}

		boolean isPlayerToReload;

		synchronized (this.playersToReload) {
			isPlayerToReload = this.playersToReload.remove(player);
		}

		if (isPlayerToReload) {
			this.addPlayerToCheck(player, null);
		}
	}

	public void clearPlayer(Player player) {
		synchronized (this.proximityHiderTracker) {
			this.proximityHiderTracker.remove(player);
		}

		synchronized (this.playersToCheck) {
			this.playersToCheck.remove(player);
		}

		synchronized (this.playersToReload) {
			this.playersToReload.remove(player);
		}
	}

	public void clearBlocksForOldWorld(Player player) {
		synchronized (this.proximityHiderTracker) {
			ProximityHiderPlayer playerInfo = this.proximityHiderTracker.get(player);

			if (playerInfo != null) {
				World world = player.getWorld();

				if (!playerInfo.getWorld().equals(world)) {
					playerInfo.setWorld(world);
					playerInfo.clearChunks();
				}
			}
		}
	}

	public void addPlayerToCheck(Player player, Location location) {
		synchronized (this.playersToCheck) {
			if (!this.playersToCheck.containsKey(player)) {
				playersToCheck.put(player, location);
			}
		}
	}

	public void addPlayersToReload(HashSet<Player> players) {
		if (!this.config.isProximityHiderEnabled())
			return;

		synchronized (this.playersToReload) {
			this.playersToReload.addAll(players);
		}
	}

	private class ProximityHiderRunner extends Thread implements Runnable {

		@Override
		public void run() {
			while (!this.isInterrupted() && running.get()) {
				try {
					// Wait until necessary
					long timeWait = lastExecute + config.getProximityHiderRate() - System.currentTimeMillis();
					lastExecute = System.currentTimeMillis();
					if (timeWait > 0) {
						Thread.sleep(timeWait);
					}

					HashMap<Player, Location> checkPlayers = new HashMap<Player, Location>();

					synchronized (playersToCheck) {
						checkPlayers.putAll(playersToCheck);
						playersToCheck.clear();
					}

					for (Player player : checkPlayers.keySet()) {
						if (player == null) {
							continue;
						}

						synchronized (proximityHiderTracker) {
							if (!proximityHiderTracker.containsKey(player)) {
								continue;
							}
						}

						Location oldLocation = checkPlayers.get(player);

						if (oldLocation != null) {
							Location curLocation = player.getLocation();

							// Player didn't actually move
							if (curLocation.getBlockX() == oldLocation.getBlockX()
									&& curLocation.getBlockY() == oldLocation.getBlockY()
									&& curLocation.getBlockZ() == oldLocation.getBlockZ()) {
								continue;
							}
						}

						ProximityHiderPlayer localPlayerInfo = proximityPlayers.get(player);

						if (localPlayerInfo == null) {
							proximityPlayers.put(player, localPlayerInfo = new ProximityHiderPlayer(player.getWorld()));
						}

						synchronized (proximityHiderTracker) {
							ProximityHiderPlayer playerInfo = proximityHiderTracker.get(player);

							if (playerInfo != null) {
								if (!localPlayerInfo.getWorld().equals(playerInfo.getWorld())) {
									localPlayerInfo.setWorld(playerInfo.getWorld());
									localPlayerInfo.clearChunks();
								}

								localPlayerInfo.copyChunks(playerInfo);
								playerInfo.clearChunks();
							}
						}

						if (localPlayerInfo.getWorld() == null || player.getWorld() == null
								|| !player.getWorld().equals(localPlayerInfo.getWorld())) {
							localPlayerInfo.clearChunks();
							continue;
						}

						IWorldConfig worldConfig = configManager.getWorld(player.getWorld());
						IProximityHiderConfig proximityHider = worldConfig.getProximityHiderConfig();

						int checkRadius = proximityHider.getDistance() >> 4;

						if ((proximityHider.getDistance() & 0xf) != 0) {
							checkRadius++;
						}

						int distanceSquared = proximityHider.getDistanceSquared();

						ArrayList<BlockCoord> removedBlocks = new ArrayList<BlockCoord>();
						Location playerLocation = player.getLocation();
						// 4.3.1 -- GAZE CHECK
						Location playerEyes = player.getEyeLocation();
						// 4.3.1 -- GAZE CHECK END
						int minChunkX = (playerLocation.getBlockX() >> 4) - checkRadius;
						int maxChunkX = minChunkX + (checkRadius << 1);
						int minChunkZ = (playerLocation.getBlockZ() >> 4) - checkRadius;
						int maxChunkZ = minChunkZ + (checkRadius << 1);

						for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
							for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
								ArrayList<BlockCoord> blocks = localPlayerInfo.getBlocks(chunkX, chunkZ);

								if (blocks == null)
									continue;

								removedBlocks.clear();

								for (BlockCoord block : blocks) {
									if (block == null) {
										removedBlocks.add(block);
										continue;
									}

									Location blockLocation = new Location(localPlayerInfo.getWorld(), block.x, block.y,
											block.z);

									if (proximityHider.isObfuscateAboveY()
											|| playerLocation.distanceSquared(blockLocation) < distanceSquared) {
										// 4.3.1 -- GAZE CHECK
										if (!proximityHider.isUseFastGazeCheck() || doFastCheck(blockLocation, playerEyes, localPlayerInfo.getWorld())) {
											// 4.3.1 -- GAZE CHECK END
											removedBlocks.add(block);

											if (nmsManager.sendBlockChange(player, blockLocation)) {
												Bukkit.getScheduler().runTask(plugin, new Runnable() {

													public void run() {
														nmsManager.updateBlockTileEntity(block, player);
													}
												});
											}
										}
									}
								}

								if (blocks.size() == removedBlocks.size()) {
									localPlayerInfo.removeChunk(chunkX, chunkZ);
								} else {
									blocks.removeAll(removedBlocks);
								}
							}
						}
					}
				} catch (Exception e) {
					OFCLogger.log(e);
				}
			}
		}
	}
}