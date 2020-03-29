package net.imprex.orebfuscator.obfuscation;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Consumer;

import com.lishid.orebfuscator.Orebfuscator;
import com.lishid.orebfuscator.chunkmap.ChunkData;
import com.lishid.orebfuscator.chunkmap.ChunkMapManager;
import com.lishid.orebfuscator.obfuscation.CalculationsUtil;
import com.lishid.orebfuscator.obfuscation.ProximityHider;

import net.imprex.orebfuscator.NmsInstance;
import net.imprex.orebfuscator.cache.ChunkCache;
import net.imprex.orebfuscator.cache.ChunkCacheEntry;
import net.imprex.orebfuscator.config.OrebfuscatorConfig;
import net.imprex.orebfuscator.config.ProximityConfig;
import net.imprex.orebfuscator.config.WorldConfig;
import net.imprex.orebfuscator.util.BlockCoords;
import net.imprex.orebfuscator.util.ChunkPosition;
import net.imprex.orebfuscator.util.MaterialUtil;

public class Calculations {

	private final OrebfuscatorConfig config;
	private final ChunkCache chunkCache;

	public Calculations(Orebfuscator orebfuscator) {
		this.config = orebfuscator.getOrebfuscatorConfig();
		this.chunkCache = orebfuscator.getChunkCache();
	}

	private LinkedList<Long> avgTimes = new LinkedList<>();
	private double calls = 0;
	private DecimalFormat formatter = new DecimalFormat("###,###,###,###.00");

	public Result obfuscateOrUseCache(ChunkData chunkData, Player player, WorldConfig worldConfig) {
		long time = System.nanoTime();
		Result result = obfuscateOrUseCache0(chunkData, player, worldConfig);
		long diff = System.nanoTime() - time;

		avgTimes.add(diff);
		if (avgTimes.size() > 1000) {
			avgTimes.removeFirst();
		}

		if (calls++ % 100 == 0) {
			System.out.println("avg: "
					+ formatter.format(
							((double) avgTimes.stream().reduce(0L, Long::sum) / (double) avgTimes.size()) / 1000D)
					+ "μs");
		}

		return result;
	}

	public Result obfuscateOrUseCache0(ChunkData chunkData, Player player, WorldConfig worldConfig) {
		if (chunkData.primaryBitMask == 0) {
			return null;
		}

		ChunkPosition position = new ChunkPosition(player.getWorld().getName(), chunkData.chunkX, chunkData.chunkZ);
		ChunkCacheEntry cacheEntry = null;

		final long hash = CalculationsUtil.Hash(chunkData.data, chunkData.data.length, this.config.hash());

		if (this.config.cache().enabled()) {
			cacheEntry = this.chunkCache.get(position, hash,
					key -> this.obfuscate(worldConfig, chunkData, player, hash));
		} else {
			cacheEntry = this.obfuscate(worldConfig, chunkData, player, hash);
		}

		ProximityHider.addProximityBlocks(player, chunkData.chunkX, chunkData.chunkZ, cacheEntry.getProximityBlocks());

		return new Result(cacheEntry.getData(), cacheEntry.getRemovedEntities());
	}

	private ChunkCacheEntry obfuscate(WorldConfig worldConfig, ChunkData chunkData, Player player, long hash) {
		try {
			final ProximityConfig proximityConfig = this.config.proximity(player.getWorld());
			final List<BlockCoords> proximityBlocks = new ArrayList<>();
			final List<BlockCoords> removedEntities = new ArrayList<>();
			final boolean proximityHiderEnabled = proximityConfig != null && proximityConfig.enabled();

			final int startX = chunkData.chunkX << 4;
			final int startZ = chunkData.chunkZ << 4;

			final int caveAirId = NmsInstance.get().getCaveAirBlockId();
			final List<Integer> randomBlocks = new ArrayList<Integer>();
			byte[] output;

			randomBlocks.addAll(worldConfig.randomBlocks());
			if (proximityHiderEnabled) {
				randomBlocks.addAll(proximityConfig.randomBlocks());
			}

			try (ChunkMapManager chunkMap = ChunkMapManager.create(chunkData)) {
				for (int offsetY = 0; offsetY < 16; offsetY++) {
					for (int offsetX = 0; offsetX < 16; offsetX++) {
						for (int offsetZ = 0; offsetZ < 16; offsetZ++) {
							int blockData = chunkMap.readNextBlock();
							int x = startX | offsetX;
							int y = chunkMap.getY();
							int z = startZ | offsetZ;

							int obfuscateBits = worldConfig.blockmask(blockData);
							boolean obfuscateFlag = (obfuscateBits & WorldConfig.BLOCK_MASK_OBFUSCATE) != 0;
							boolean darknessBlockFlag = (obfuscateBits & WorldConfig.BLOCK_MASK_DARKNESS) != 0;
							boolean tileEntityFlag = (obfuscateBits & WorldConfig.BLOCK_MASK_TILEENTITY) != 0;
							boolean proximityHiderFlag = proximityHiderEnabled && proximityConfig.shouldHide(y, blockData);

							boolean obfuscate = false;

							if (obfuscateFlag) {
								
							}
							// ProximityHider
							if (!obfuscate && proximityHiderFlag) {
								BlockCoords blockCoords = new BlockCoords(x, y, z);
								proximityBlocks.add(blockCoords);

								blockData = proximityConfig.randomBlockId();
							}

							// Darkness blocks
							if (!obfuscate && darknessBlockFlag && worldConfig.darknessBlocksEnabled()) {
								if (!this.areAjacentBlocksBright(player.getWorld(), x, y, z, 1)) {
									blockData = NmsInstance.get().getCaveAirBlockId();
								}
							}

							if (offsetY == 0 && offsetZ == 0 && offsetX == 0) {
								chunkMap.finalizeOutput();
								chunkMap.initOutputPalette();
								chunkMap.addToOutputPalette(caveAirId);

								for (int randomBlockId : randomBlocks) {
									chunkMap.addToOutputPalette(randomBlockId);
								}

								chunkMap.initOutputSection();
							}

							chunkMap.writeOutputBlock(blockData);
						}
					}
				}

				chunkMap.finalizeOutput();
				output = chunkMap.createOutput();
			}

			return new ChunkCacheEntry(hash, output, proximityBlocks, removedEntities);
		} catch (Exception e) {
			throw new RuntimeException("Can't obfuscate chunk " + chunkData.chunkX + ", " + chunkData.chunkZ, e);
		}
	}

	public boolean areAjacentBlocksTransparent(ChunkMapManager manager, World world, boolean checkCurrentBlock,
			int x, int y, int z, int countdown) throws IOException {
		if (y >= world.getMaxHeight() || y < 0) {
			return true;
		}

		if (checkCurrentBlock) {
			ChunkData chunkData = manager.getChunkData();
			int blockData = manager.get(x, y, z);

			if (blockData < 0) {
				blockData = NmsInstance.get().loadChunkAndGetBlockId(world, x, y, z);

				if (blockData < 0) {
					chunkData.useCache = false;
				}
			}

			if (blockData >= 0 && MaterialUtil.isTransparent(blockData)) {
				return true;
			}
		}

		if (countdown == 0) {
			return false;
		}

		if (areAjacentBlocksTransparent(manager, world, true, x, y + 1, z, countdown - 1)) {
			return true;
		}
		if (areAjacentBlocksTransparent(manager, world, true, x, y - 1, z, countdown - 1)) {
			return true;
		}
		if (areAjacentBlocksTransparent(manager, world, true, x + 1, y, z, countdown - 1)) {
			return true;
		}
		if (areAjacentBlocksTransparent(manager, world, true, x - 1, y, z, countdown - 1)) {
			return true;
		}
		if (areAjacentBlocksTransparent(manager, world, true, x, y, z + 1, countdown - 1)) {
			return true;
		}
		if (areAjacentBlocksTransparent(manager, world, true, x, y, z - 1, countdown - 1)) {
			return true;
		}

		return false;
	}

	public boolean areAjacentBlocksBright(World world, int x, int y, int z, int countdown) {
		if (NmsInstance.get().getBlockLightLevel(world, x, y, z) > 0) {
			return true;
		}

		if (countdown == 0) {
			return false;
		}

		if (areAjacentBlocksBright(world, x, y + 1, z, countdown - 1)) {
			return true;
		}
		if (areAjacentBlocksBright(world, x, y - 1, z, countdown - 1)) {
			return true;
		}
		if (areAjacentBlocksBright(world, x + 1, y, z, countdown - 1)) {
			return true;
		}
		if (areAjacentBlocksBright(world, x - 1, y, z, countdown - 1)) {
			return true;
		}
		if (areAjacentBlocksBright(world, x, y, z + 1, countdown - 1)) {
			return true;
		}
		if (areAjacentBlocksBright(world, x, y, z - 1, countdown - 1)) {
			return true;
		}

		return false;
	}

	private class Result {

		public final byte[] output;
		public final List<BlockCoords> removedEntities;

		public Result(byte[] output, List<BlockCoords> removedEntities) {
			this.output = output;
			this.removedEntities = removedEntities;
		}
	}
}
