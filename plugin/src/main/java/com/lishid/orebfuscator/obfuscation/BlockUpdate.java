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

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import com.lishid.orebfuscator.api.Orebfuscator;
import com.lishid.orebfuscator.api.config.IConfigManager;
import com.lishid.orebfuscator.api.config.IOrebfuscatorConfig;
import com.lishid.orebfuscator.api.config.IWorldConfig;
import com.lishid.orebfuscator.api.nms.IBlockInfo;
import com.lishid.orebfuscator.api.nms.INmsManager;
import com.lishid.orebfuscator.api.types.ChunkCoord;
import com.lishid.orebfuscator.api.utils.Globals;
import com.lishid.orebfuscator.api.utils.IBlockUpdate;
import com.lishid.orebfuscator.cache.ObfuscatedCachedChunk;
import com.lishid.orebfuscator.handler.CraftHandler;

public class BlockUpdate extends CraftHandler implements IBlockUpdate {

	private INmsManager nmsManager;
	private IOrebfuscatorConfig config;
	private IConfigManager configManager;

	public BlockUpdate(Orebfuscator plugin) {
		super(plugin);
	}

	@Override
	public void onInit() {
		this.nmsManager = this.plugin.getNmsManager();
		this.config = this.plugin.getConfigHandler().getConfig();
		this.configManager = this.plugin.getConfigHandler().getConfigManager();
	}

	public boolean needsUpdate(Block block) {
		int materialId = this.nmsManager.getMaterialIds(block.getType()).iterator().next();
		return !this.config.isBlockTransparent(materialId);
	}

	public void update(Block block) {
		if (!this.needsUpdate(block)) {
			return;
		}

		this.update(Arrays.asList(new Block[] { block }));
	}

	public void update(List<Block> blocks) {
		if (blocks.isEmpty()) {
			return;
		}

		World world = blocks.get(0).getWorld();
		IWorldConfig worldConfig = this.configManager.getWorld(world);
		HashSet<IBlockInfo> updateBlocks = new HashSet<IBlockInfo>();
		HashSet<ChunkCoord> invalidChunks = new HashSet<ChunkCoord>();
		int updateRadius = this.config.getUpdateRadius();

		for (Block block : blocks) {
			if (this.needsUpdate(block)) {
				IBlockInfo blockInfo = this.nmsManager.getBlockInfo(world, block.getX(), block.getY(), block.getZ());

				this.getAdjacentBlocks(updateBlocks, world, worldConfig, blockInfo, updateRadius);

				if (blockInfo != null) {
					if ((blockInfo.getX() & 0xf) == 0) {
						invalidChunks.add(new ChunkCoord((blockInfo.getX() >> 4) - 1, blockInfo.getZ() >> 4));
					} else if (((blockInfo.getX() + 1) & 0xf) == 0) {
						invalidChunks.add(new ChunkCoord((blockInfo.getX() >> 4) + 1, blockInfo.getZ() >> 4));
					} else if (((blockInfo.getZ()) & 0xf) == 0) {
						invalidChunks.add(new ChunkCoord(blockInfo.getX() >> 4, (blockInfo.getZ() >> 4) - 1));
					} else if (((blockInfo.getZ() + 1) & 0xf) == 0) {
						invalidChunks.add(new ChunkCoord(blockInfo.getX() >> 4, (blockInfo.getZ() >> 4) + 1));
					}
				}
			}
		}

		this.sendUpdates(world, updateBlocks);
		this.invalidateCachedChunks(world, invalidChunks);
	}

	// This method is used in CastleGates plugin
	public void updateByLocations(List<Location> locations, int updateRadius) {
		if (locations.isEmpty()) {
			return;
		}

		World world = locations.get(0).getWorld();
		IWorldConfig worldConfig = this.configManager.getWorld(world);
		HashSet<IBlockInfo> updateBlocks = new HashSet<IBlockInfo>();
		HashSet<ChunkCoord> invalidChunks = new HashSet<ChunkCoord>();

		for (Location location : locations) {
			IBlockInfo blockInfo = this.nmsManager.getBlockInfo(world, location.getBlockX(), location.getBlockY(),
					location.getBlockZ());

			this.getAdjacentBlocks(updateBlocks, world, worldConfig, blockInfo, updateRadius);

			if (blockInfo != null) {
				if ((blockInfo.getX() & 0xf) == 0) {
					invalidChunks.add(new ChunkCoord((blockInfo.getX() >> 4) - 1, blockInfo.getZ() >> 4));
				} else if (((blockInfo.getX() + 1) & 0xf) == 0) {
					invalidChunks.add(new ChunkCoord((blockInfo.getX() >> 4) + 1, blockInfo.getZ() >> 4));
				} else if (((blockInfo.getZ()) & 0xf) == 0) {
					invalidChunks.add(new ChunkCoord(blockInfo.getX() >> 4, (blockInfo.getZ() >> 4) - 1));
				} else if (((blockInfo.getZ() + 1) & 0xf) == 0) {
					invalidChunks.add(new ChunkCoord(blockInfo.getX() >> 4, (blockInfo.getZ() >> 4) + 1));
				}
			}
		}

		this.sendUpdates(world, updateBlocks);
		this.invalidateCachedChunks(world, invalidChunks);
	}

	private void sendUpdates(World world, Set<IBlockInfo> blocks) {
		for (IBlockInfo blockInfo : blocks) {
			this.nmsManager.notifyBlockChange(world, blockInfo);
		}
	}

	private void invalidateCachedChunks(World world, Set<ChunkCoord> invalidChunks) {
		if (invalidChunks.isEmpty() || !this.config.isUseCache())
			return;

		File cacheFolder = new File(this.plugin.getObfuscatedDataCacheHandler().getCacheFolder(), world.getName());

		for (ChunkCoord chunk : invalidChunks) {
			ObfuscatedCachedChunk cache = new ObfuscatedCachedChunk(this.plugin, cacheFolder, chunk.x, chunk.z);
			cache.invalidate();
		}
	}

	private void getAdjacentBlocks(HashSet<IBlockInfo> allBlocks, World world, IWorldConfig worldConfig, IBlockInfo blockInfo, int countdown) {
		if (blockInfo == null)
			return;

		int blockId = blockInfo.getCombinedId();

		if ((worldConfig.getObfuscatedBits(blockId) & Globals.MASK_OBFUSCATE) != 0) {
			allBlocks.add(blockInfo);
		}

		if (countdown > 0) {
			countdown--;
			this.getAdjacentBlocks(allBlocks, world, worldConfig,
					this.nmsManager.getBlockInfo(world, blockInfo.getX() + 1, blockInfo.getY(), blockInfo.getZ()),
					countdown);
			this.getAdjacentBlocks(allBlocks, world, worldConfig,
					this.nmsManager.getBlockInfo(world, blockInfo.getX() - 1, blockInfo.getY(), blockInfo.getZ()),
					countdown);
			this.getAdjacentBlocks(allBlocks, world, worldConfig,
					this.nmsManager.getBlockInfo(world, blockInfo.getX(), blockInfo.getY() + 1, blockInfo.getZ()),
					countdown);
			this.getAdjacentBlocks(allBlocks, world, worldConfig,
					this.nmsManager.getBlockInfo(world, blockInfo.getX(), blockInfo.getY() - 1, blockInfo.getZ()),
					countdown);
			this.getAdjacentBlocks(allBlocks, world, worldConfig,
					this.nmsManager.getBlockInfo(world, blockInfo.getX(), blockInfo.getY(), blockInfo.getZ() + 1),
					countdown);
			this.getAdjacentBlocks(allBlocks, world, worldConfig,
					this.nmsManager.getBlockInfo(world, blockInfo.getX(), blockInfo.getY(), blockInfo.getZ() - 1),
					countdown);
		}
	}
}
