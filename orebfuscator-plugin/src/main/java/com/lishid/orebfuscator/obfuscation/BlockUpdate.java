/*
 * Copyright (C) 2011-2014 lishid.  All rights reserved.
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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import com.lishid.orebfuscator.Orebfuscator;

import net.imprex.orebfuscator.NmsInstance;
import net.imprex.orebfuscator.cache.ChunkCache;
import net.imprex.orebfuscator.config.OrebfuscatorConfig;
import net.imprex.orebfuscator.config.WorldConfig;
import net.imprex.orebfuscator.nms.BlockStateHolder;
import net.imprex.orebfuscator.util.ChunkPosition;
import net.imprex.orebfuscator.util.MaterialUtil;

public class BlockUpdate {

	private static OrebfuscatorConfig config;
	private static ChunkCache chunkCache;

	public static void initialize(Orebfuscator orebfuscator) {
		BlockUpdate.config = orebfuscator.getOrebfuscatorConfig();
		BlockUpdate.chunkCache = orebfuscator.getChunkCache();
	}

	public static boolean needsUpdate(Block block) {
		int materialId = NmsInstance.get().getMaterialIds(block.getType()).iterator().next();
		return !MaterialUtil.isTransparent(materialId);
	}

	public static void update(Block block) {
		if (!needsUpdate(block)) {
			return;
		}

		update(Arrays.asList(new Block[] { block }));
	}

	public static void update(List<Block> blocks) {
		if (blocks.isEmpty()) {
			return;
		}

		World world = blocks.get(0).getWorld();
		WorldConfig worldConfig = BlockUpdate.config.world(world);
		String worldName = world.getName();

		HashSet<BlockStateHolder> updateBlocks = new HashSet<>();
		HashSet<ChunkPosition> invalidChunks = new HashSet<>();
		int updateRadius = BlockUpdate.config.general().updateRadius();

		for (Block block : blocks) {
			if (needsUpdate(block)) {
				BlockStateHolder blockState = NmsInstance.get().getBlockState(world, block.getX(), block.getY(),
						block.getZ());

				getAdjacentBlocks(updateBlocks, world, worldConfig, blockState, updateRadius);

				if (blockState != null) {
					if ((blockState.getX() & 0xf) == 0) {
						invalidChunks.add(
								new ChunkPosition(worldName, (blockState.getX() >> 4) - 1, blockState.getZ() >> 4));
					} else if ((blockState.getX() + 1 & 0xf) == 0) {
						invalidChunks.add(
								new ChunkPosition(worldName, (blockState.getX() >> 4) + 1, blockState.getZ() >> 4));
					} else if ((blockState.getZ() & 0xf) == 0) {
						invalidChunks.add(
								new ChunkPosition(worldName, blockState.getX() >> 4, (blockState.getZ() >> 4) - 1));
					} else if ((blockState.getZ() + 1 & 0xf) == 0) {
						invalidChunks.add(
								new ChunkPosition(worldName, blockState.getX() >> 4, (blockState.getZ() >> 4) + 1));
					}
				}
			}
		}

		sendUpdates(world, updateBlocks);

		invalidateCachedChunks(invalidChunks);
	}

	// This method is used in CastleGates plugin
	public static void updateByLocations(List<Location> locations, int updateRadius) {
		if (locations.isEmpty()) {
			return;
		}

		World world = locations.get(0).getWorld();
		WorldConfig worldConfig = BlockUpdate.config.world(world);
		String worldName = world.getName();

		HashSet<BlockStateHolder> updateBlocks = new HashSet<>();
		HashSet<ChunkPosition> invalidChunks = new HashSet<>();

		for (Location location : locations) {
			BlockStateHolder blockState = NmsInstance.get().getBlockState(world, location.getBlockX(),
					location.getBlockY(), location.getBlockZ());

			getAdjacentBlocks(updateBlocks, world, worldConfig, blockState, updateRadius);

			if (blockState != null) {
				if ((blockState.getX() & 0xf) == 0) {
					invalidChunks.add(new ChunkPosition(worldName, (blockState.getX() >> 4) - 1, blockState.getZ() >> 4));
				} else if ((blockState.getX() + 1 & 0xf) == 0) {
					invalidChunks.add(new ChunkPosition(worldName, (blockState.getX() >> 4) + 1, blockState.getZ() >> 4));
				} else if ((blockState.getZ() & 0xf) == 0) {
					invalidChunks.add(new ChunkPosition(worldName, blockState.getX() >> 4, (blockState.getZ() >> 4) - 1));
				} else if ((blockState.getZ() + 1 & 0xf) == 0) {
					invalidChunks.add(new ChunkPosition(worldName, blockState.getX() >> 4, (blockState.getZ() >> 4) + 1));
				}
			}
		}

		sendUpdates(world, updateBlocks);

		invalidateCachedChunks(invalidChunks);
	}

	private static void sendUpdates(World world, Set<BlockStateHolder> blocks) {
		// Orebfuscator.log("Notify block change for " + blocks.size() + "
		// blocks");/*debug*/

		for (BlockStateHolder blockState : blocks) {
			blockState.notifyBlockChange();
		}
	}

	private static void invalidateCachedChunks(Set<ChunkPosition> invalidChunks) {
		if (invalidChunks.isEmpty() || !BlockUpdate.config.cache().enabled()) {
			return;
		}

		for (ChunkPosition chunk : invalidChunks) {
			BlockUpdate.chunkCache.invalidate(chunk);
		}
	}

	private static void getAdjacentBlocks(HashSet<BlockStateHolder> allBlocks, World world, WorldConfig worldConfig,
			BlockStateHolder blockState, int countdown) {
		if (blockState == null) {
			return;
		}

		int blockId = blockState.getBlockId();

		if ((worldConfig.blockmask(blockId) & WorldConfig.BLOCK_MASK_OBFUSCATE) != 0) {
			allBlocks.add(blockState);
		}

		if (countdown > 0) {
			countdown--;
			getAdjacentBlocks(allBlocks, world, worldConfig,
					NmsInstance.get().getBlockState(world, blockState.getX() + 1, blockState.getY(), blockState.getZ()),
					countdown);
			getAdjacentBlocks(allBlocks, world, worldConfig,
					NmsInstance.get().getBlockState(world, blockState.getX() - 1, blockState.getY(), blockState.getZ()),
					countdown);
			getAdjacentBlocks(allBlocks, world, worldConfig,
					NmsInstance.get().getBlockState(world, blockState.getX(), blockState.getY() + 1, blockState.getZ()),
					countdown);
			getAdjacentBlocks(allBlocks, world, worldConfig,
					NmsInstance.get().getBlockState(world, blockState.getX(), blockState.getY() - 1, blockState.getZ()),
					countdown);
			getAdjacentBlocks(allBlocks, world, worldConfig,
					NmsInstance.get().getBlockState(world, blockState.getX(), blockState.getY(), blockState.getZ() + 1),
					countdown);
			getAdjacentBlocks(allBlocks, world, worldConfig,
					NmsInstance.get().getBlockState(world, blockState.getX(), blockState.getY(), blockState.getZ() - 1),
					countdown);
		}
	}
}
