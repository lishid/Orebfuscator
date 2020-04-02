/*
// * Copyright (C) 2011-2014 lishid.  All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation,  version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.lishid.orebfuscator.obfuscation;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.bukkit.World;
import org.bukkit.entity.Player;

import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.lishid.orebfuscator.Orebfuscator;
import com.lishid.orebfuscator.chunkmap.ChunkData;
import com.lishid.orebfuscator.chunkmap.ChunkMapManager;

import net.imprex.orebfuscator.NmsInstance;
import net.imprex.orebfuscator.cache.ChunkCache;
import net.imprex.orebfuscator.cache.ChunkCacheEntry;
import net.imprex.orebfuscator.config.OrebfuscatorConfig;
import net.imprex.orebfuscator.config.WorldConfig;
import net.imprex.orebfuscator.util.BlockCoords;
import net.imprex.orebfuscator.util.ChunkPosition;

public class Calculations {

	private static OrebfuscatorConfig config;
	private static ChunkCache chunkCache;

	public static void initialize(Orebfuscator orebfuscator) {
		Calculations.config = orebfuscator.getOrebfuscatorConfig();
		Calculations.chunkCache = orebfuscator.getChunkCache();
	}

	public static ChunkCacheEntry obfuscateChunk(ChunkData chunkData, Player player, WorldConfig worldConfig,
			long hash) {
		List<BlockCoords> proximityBlocks = new ArrayList<>();
		List<BlockCoords> removedEntities = new ArrayList<>();
		try {
			byte[] data = obfuscate(worldConfig, chunkData, player, proximityBlocks, removedEntities);

			ChunkCacheEntry chunkCacheEntry = new ChunkCacheEntry(hash, data);
			chunkCacheEntry.getProximityBlocks().addAll(proximityBlocks);
			chunkCacheEntry.getRemovedTileEntities().addAll(removedEntities);
			return chunkCacheEntry;
		} catch (Exception e) {
			throw new RuntimeException("Can't obfuscate chunk " + chunkData.chunkX + ", " + chunkData.chunkZ, e);
		}
	}

	private static LinkedList<Long> avgTimes = new LinkedList<>();
	private static double calls = 0;
	private static DecimalFormat formatter = new DecimalFormat("###,###,###,###.00");

	public static Result obfuscateOrUseCache(ChunkData chunkData, Player player, WorldConfig worldConfig) {
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

	public static Result obfuscateOrUseCache0(ChunkData chunkData, Player player, WorldConfig worldConfig) {
		if (chunkData.primaryBitMask == 0) {
			return null;
		}

		ChunkPosition position = new ChunkPosition(player.getWorld().getName(), chunkData.chunkX, chunkData.chunkZ);
		ChunkCacheEntry cacheEntry = null;

		final long hash = CalculationsUtil.Hash(chunkData.data, chunkData.data.length, Calculations.config.hash());

		if (Calculations.config.cache().enabled()) {
			cacheEntry = Calculations.chunkCache.get(position, hash,
					key -> obfuscateChunk(chunkData, player, worldConfig, hash));
		} else {
			cacheEntry = obfuscateChunk(chunkData, player, worldConfig, hash);
		}

//		ProximityHider.addProximityBlocks(player, chunkData.chunkX, chunkData.chunkZ, cacheEntry.getProximityBlocks());

		return new Result(cacheEntry.getData(), cacheEntry.getRemovedTileEntities());
	}

	private static byte[] obfuscate(WorldConfig worldConfig, ChunkData chunkData, Player player,
			List<BlockCoords> proximityBlocks, List<BlockCoords> removedEntities) throws IOException {

//		final ProximityConfig proximityConfig = config.proximity(player.getWorld());
////		ProximityHiderConfig proximityHider = worldConfig.getProximityHiderConfig();
//		int initialRadius = Calculations.config.general().initialRadius();
//
//		// Track of pseudo-randomly assigned randomBlock
////		int randomIncrement = 0;
////		int randomIncrement2 = 0;
////		int randomCave = 0;
//
////		int engineMode = Calculations.configManager.getConfig().getEngineMode();
////		int maxChance = worldConfig.getAirGeneratorMaxChance();
//
////		int randomBlocksLength = worldConfig.getRandomBlocks().length;
////		boolean randomAlternate = false;
//
//		int startX = chunkData.chunkX << 4;
//		int startZ = chunkData.chunkZ << 4;
//
//		byte[] output;
//
//		try (ChunkMapManager manager = ChunkMapManager.create(chunkData)) {
//			for (int i = 0; i < manager.getSectionCount(); i++) {
////				worldConfig.shuffleRandomBlocks();
//
//				for (int offsetY = 0; offsetY < 16; offsetY++) {
//					for (int offsetZ = 0; offsetZ < 16; offsetZ++) {
////						int incrementMax = (maxChance + random(maxChance)) / 2;
//
//						for (int offsetX = 0; offsetX < 16; offsetX++) {
//							int blockData = manager.readNextBlock();
//							int x = startX | offsetX;
//							int y = manager.getY();
//							int z = startZ | offsetZ;
//
//							// Initialize data
//							int obfuscateBits = worldConfig.blockmask(blockData);
//							boolean obfuscateFlag = (obfuscateBits & WorldConfig.BLOCK_MASK_OBFUSCATE) != 0;
//							boolean darknessBlockFlag = (obfuscateBits & WorldConfig.BLOCK_MASK_DARKNESS) != 0;
//							boolean tileEntityFlag = (obfuscateBits & WorldConfig.BLOCK_MASK_TILEENTITY) != 0;
//							boolean proximityHiderFlag = proximityConfig != null
//									&& proximityConfig.shouldHide(y, blockData);
//
//							boolean obfuscate = false;
////							boolean specialObfuscate = false;
//
//							// Check if the block should be obfuscated for the default engine modes
//							if (obfuscateFlag) {
//								if (initialRadius == 0) {
//									// Do not interfere with PH
//									if (proximityHiderFlag && proximityConfig.enabled()) {
//										if (!areAjacentBlocksTransparent(manager, player.getWorld(), false, x, y, z,
//												1)) {
//											obfuscate = true;
//										}
//									} else {
//										// Obfuscate all blocks
//										obfuscate = true;
//									}
//								} else {
//									// Check if any nearby blocks are transparent
//									if (!areAjacentBlocksTransparent(manager, player.getWorld(), false, x, y, z,
//											initialRadius)) {
//										obfuscate = true;
//									}
//								}
//							}
//
//							// Check if the block should be obfuscated because of proximity check
//							if (!obfuscate && proximityHiderFlag && proximityConfig.enabled()
//									&& proximityConfig.shouldHide(y, blockData)) {
//								BlockCoords block = new BlockCoords(x, y, z);
//								if (block != null) {
//									proximityBlocks.add(block);
//								}
//
//								obfuscate = true;
////								if (proximityHider.isUseSpecialBlock()) {
////									specialObfuscate = true;
////								}
//							}
//
//							// Check if the block is obfuscated
//							if (obfuscate && /*(!worldConfig.isBypassObfuscationForSignsWithText()
//									||*/ canObfuscate(chunkData, x, y, z, blockData)/*)*/) {
//								if (proximityHiderFlag) {
//									// Proximity hider
//									blockData = proximityConfig.randomBlockId();
//								} else {
//									blockData = worldConfig.randomBlockId();
////									if (engineMode == 1) {
//									// Engine mode 1, replace with stone
////										blockData = worldConfig.getMode1BlockId();
////									} else if (engineMode == 2) {
//									// Ending mode 2, replace with random block
////									if (randomBlocksLength > 1) {
////										randomIncrement = CalculationsUtil.increment(randomIncrement,
////												randomBlocksLength);
////									}
////
////									blockData = worldConfig.getRandomBlock(randomIncrement, randomAlternate);
////									randomAlternate = !randomAlternate;
////									}
//									// Anti texturepack and freecam
////									if (worldConfig.isAntiTexturePackAndFreecam()) {
////										// Add random air blocks
////										randomIncrement2 = random(incrementMax);
////
////										if (randomIncrement2 == 0) {
////											randomCave = 1 + random(3);
////										}
////
////										if (randomCave > 0) {
////											blockData = NmsInstance.get().getCaveAirBlockId();
////											randomCave--;
////										}
////									}
//								}
//							}
//
//							// Check if the block should be obfuscated because of the darkness
//							if (!obfuscate && darknessBlockFlag && worldConfig.darknessBlocksEnabled()) {
//								if (!areAjacentBlocksBright(player.getWorld(), x, y, z, 1)) {
//									// Hide block, setting it to air
//									blockData = NmsInstance.get().getCaveAirBlockId();
//									obfuscate = true;
//								}
//							}
//
//							if (obfuscate && tileEntityFlag) {
//								removedEntities.add(new BlockCoords(x, y, z));
//							}
//
//							if (offsetY == 0 && offsetZ == 0 && offsetX == 0) {
//								manager.finalizeOutput();
//								manager.initOutputPalette();
//
//								manager.addToOutputPalette(NmsInstance.get().getCaveAirBlockId());
//								for (int blockId : worldConfig.randomBlocks()) {
//									manager.addToOutputPalette(blockId);
//								}
//								if (proximityConfig.enabled()) {
////									for (int blockId : proximityConfig.randomBlocks()) {
////										manager.addToOutputPalette(blockId);
////									}
//								}
//
//								manager.initOutputSection();
//							}
//
//							manager.writeOutputBlock(blockData);
//						}
//					}
//				}
//			}
//
//			manager.finalizeOutput();
//
//			output = manager.createOutput();
//		}
//
//		// Orebfuscator.log("Create new chunk data for x = " + chunkData.chunkX + ", z =
//		// " + chunkData.chunkZ);/*debug*/
//
//		return output;
		return null;
	}

	private static boolean canObfuscate(ChunkData chunkData, int x, int y, int z, int blockData) {
		return getBlockEntity(chunkData, x, y, z) == null;
	}

	private static NbtCompound getBlockEntity(ChunkData chunkData, int x, int y, int z) {
		for (NbtCompound tag : chunkData.blockEntities) {
			if (tag != null && x == tag.getInteger("x") && y == tag.getInteger("y") && z == tag.getInteger("z")) {
				return tag;
			}
		}

		return null;
	}

	public static boolean areAjacentBlocksTransparent(ChunkMapManager manager, World world, boolean checkCurrentBlock,
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

//			if (blockData >= 0 && MaterialUtil.isTransparent(blockData)) {
//				return true;
//			}
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

	public static boolean areAjacentBlocksBright(World world, int x, int y, int z, int countdown) {
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

	public static class Result {

		public final byte[] output;
		public final Set<BlockCoords> removedEntities;

		public Result(byte[] output, Set<BlockCoords> removedEntities) {
			this.output = output;
			this.removedEntities = removedEntities;
		}
	}
}