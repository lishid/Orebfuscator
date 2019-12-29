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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import org.bukkit.World;
import org.bukkit.entity.Player;

import com.comphenix.protocol.wrappers.nbt.NbtBase;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtType;
import com.lishid.orebfuscator.api.Orebfuscator;
import com.lishid.orebfuscator.api.chunk.ChunkData;
import com.lishid.orebfuscator.api.chunk.IChunkMap;
import com.lishid.orebfuscator.api.config.IOrebfuscatorConfig;
import com.lishid.orebfuscator.api.config.IProximityHiderConfig;
import com.lishid.orebfuscator.api.config.IWorldConfig;
import com.lishid.orebfuscator.api.nms.INmsManager;
import com.lishid.orebfuscator.api.types.BlockCoord;
import com.lishid.orebfuscator.api.utils.Globals;
import com.lishid.orebfuscator.api.utils.ICalculations;
import com.lishid.orebfuscator.cache.ObfuscatedCachedChunk;
import com.lishid.orebfuscator.handler.CraftHandler;

public class Calculations extends CraftHandler implements ICalculations {

	private final Random random = new Random();

	private IOrebfuscatorConfig config;
	private INmsManager nmsManager;

	public Calculations(Orebfuscator plugin) {
		super(plugin);
	}

	@Override
	public void onInit() {
		this.config = this.plugin.getConfigHandler().getConfig();
		this.nmsManager = this.plugin.getNmsManager();
	}

	public Result obfuscateOrUseCache(ChunkData chunkData, Player player, IWorldConfig worldConfig) throws Exception {
		if (chunkData.primaryBitMask == 0)
			return null;

		byte[] output;
		ArrayList<BlockCoord> removedEntities;
		ObfuscatedCachedChunk cache = this.tryUseCache(chunkData, player);

		if (cache != null && cache.data != null) {
			output = cache.data;
			removedEntities = this.getCoordFromArray(cache.removedEntityList);
		} else {
			// Blocks kept track for ProximityHider
			ArrayList<BlockCoord> proximityBlocks = new ArrayList<>();

			removedEntities = new ArrayList<>();
			output = this.obfuscate(worldConfig, chunkData, player, proximityBlocks, removedEntities);

			if (cache != null) {
				// If cache is still allowed
				if (chunkData.useCache) {
					// Save cache
					int[] proximityList = this.getArrayFromCoord(proximityBlocks);
					int[] removedEntityList = this.getArrayFromCoord(removedEntities);

					cache.write(cache.hash, output, proximityList, removedEntityList);
				}
				cache.free();
			}
		}

		Result result = new Result();
		result.output = output;
		result.removedEntities = removedEntities;

		return result;
	}

	private int[] getArrayFromCoord(ArrayList<BlockCoord> coords) {
		int[] list = new int[coords.size() * 3];
		int index = 0;

		for (int i = 0; i < coords.size(); i++) {
			BlockCoord b = coords.get(i);
			if (b != null) {
				list[index++] = b.x;
				list[index++] = b.y;
				list[index++] = b.z;
			}
		}

		return list;
	}

	private ArrayList<BlockCoord> getCoordFromArray(int[] array) {
		ArrayList<BlockCoord> list = new ArrayList<>();

		// Decrypt chest list
		if (array != null) {
			int index = 0;

			while (index < array.length) {
				int x = array[index++];
				int y = array[index++];
				int z = array[index++];
				BlockCoord b = new BlockCoord(x, y, z);

				if (b != null) {
					list.add(b);
				}
			}
		}
		return list;
	}

	private byte[] obfuscate(IWorldConfig worldConfig, ChunkData chunkData, Player player, ArrayList<BlockCoord> proximityBlocks, ArrayList<BlockCoord> removedEntities) throws Exception {
		IProximityHiderConfig proximityHider = worldConfig.getProximityHiderConfig();
		int initialRadius = this.config.getInitialRadius();

		// Track of pseudo-randomly assigned randomBlock
		int randomIncrement = 0;
		int randomIncrement2 = 0;
		int randomCave = 0;

		int engineMode = this.config.getEngineMode();
		int maxChance = worldConfig.getAirGeneratorMaxChance();

		int randomBlocksLength = worldConfig.getRandomBlocks().length;
		boolean randomAlternate = false;

		int startX = chunkData.chunkX << 4;
		int startZ = chunkData.chunkZ << 4;

		byte[] output;

		try (IChunkMap manager = this.plugin.getChunkMapHandler().create(chunkData)) {
			for (int i = 0; i < manager.getSectionCount(); i++) {
				worldConfig.shuffleRandomBlocks();

				for (int offsetY = 0; offsetY < 16; offsetY++) {
					for (int offsetZ = 0; offsetZ < 16; offsetZ++) {
						int incrementMax = (maxChance + this.random(maxChance)) / 2;

						for (int offsetX = 0; offsetX < 16; offsetX++) {
							int blockData = manager.readNextBlock();
							int x = startX | offsetX;
							int y = manager.getY();
							int z = startZ | offsetZ;

							// Initialize data
							int obfuscateBits = worldConfig.getObfuscatedBits(blockData);
							boolean obfuscateFlag = (obfuscateBits & Globals.MASK_OBFUSCATE) != 0;
							boolean proximityHiderFlag = (obfuscateBits & Globals.MASK_PROXIMITYHIDER) != 0;
							boolean darknessBlockFlag = (obfuscateBits & Globals.MASK_DARKNESSBLOCK) != 0;
							boolean tileEntityFlag = (obfuscateBits & Globals.MASK_TILEENTITY) != 0;

							boolean obfuscate = false;
							boolean specialObfuscate = false;

							// Check if the block should be obfuscated for the default engine modes
							if (obfuscateFlag) {
								if (initialRadius == 0) {
									// Do not interfere with PH
									if (proximityHiderFlag && proximityHider.isEnabled()
											&& proximityHider.isProximityObfuscated(y, blockData)) {
										if (!this.areAjacentBlocksTransparent(manager, player.getWorld(), false, x, y, z, 1)) {
											obfuscate = true;
										}
									} else {
										// Obfuscate all blocks
										obfuscate = true;
									}
								} else {
									// Check if any nearby blocks are transparent
									if (!this.areAjacentBlocksTransparent(manager, player.getWorld(), false, x, y, z, initialRadius)) {
										obfuscate = true;
									}
								}
							}

							// Check if the block should be obfuscated because of proximity check
							if (!obfuscate && proximityHiderFlag && proximityHider.isEnabled() && proximityHider.isProximityObfuscated(y, blockData)) {
								BlockCoord block = new BlockCoord(x, y, z);
								if (block != null) {
									proximityBlocks.add(block);
								}

								obfuscate = true;
								if (proximityHider.isUseSpecialBlock()) {
									specialObfuscate = true;
								}
							}

							// Check if the block is obfuscated
							if (obfuscate && (!worldConfig.isBypassObfuscationForSignsWithText() || this.canObfuscate(chunkData, x, y, z, blockData))) {
								if (specialObfuscate) {
									// Proximity hider
									blockData = proximityHider.getSpecialBlockID();
								} else {
									if (engineMode == 1) {
										// Engine mode 1, replace with stone
										blockData = worldConfig.getMode1BlockId();
									} else if (engineMode == 2) {
										// Ending mode 2, replace with random block
										if (randomBlocksLength > 1) {
											randomIncrement = CalculationsUtil.increment(randomIncrement, randomBlocksLength);
										}

										blockData = worldConfig.getRandomBlock(randomIncrement, randomAlternate);
										randomAlternate = !randomAlternate;
									}
									// Anti texturepack and freecam
									if (worldConfig.isAntiTexturePackAndFreecam()) {
										// Add random air blocks
										randomIncrement2 = this.random(incrementMax);

										if (randomIncrement2 == 0) {
											randomCave = 1 + this.random(3);
										}

										if (randomCave > 0) {
											blockData = this.nmsManager.getCaveAirBlockId();
											randomCave--;
										}
									}
								}
							}

							// Check if the block should be obfuscated because of the darkness
							if (!obfuscate && darknessBlockFlag && worldConfig.isDarknessHideBlocks()) {
								if (!this.areAjacentBlocksBright(player.getWorld(), x, y, z, 1)) {
									// Hide block, setting it to air
									blockData = this.nmsManager.getCaveAirBlockId();
									obfuscate = true;
								}
							}

							if (obfuscate && tileEntityFlag) {
								removedEntities.add(new BlockCoord(x, y, z));
							}

							if (offsetY == 0 && offsetZ == 0 && offsetX == 0) {
								manager.finalizeOutput();
								manager.initOutputPalette();
								this.addBlocksToPalette(manager, worldConfig);
								manager.initOutputSection();
							}

							manager.writeOutputBlock(blockData);
						}
					}
				}
			}

			manager.finalizeOutput();
			output = manager.createOutput();
		}

		this.plugin.getProximityHiderHandler().addProximityBlocks(player, chunkData.chunkX, chunkData.chunkZ, proximityBlocks);
		return output;
	}

	private boolean canObfuscate(ChunkData chunkData, int x, int y, int z, int blockData) {
		if (!this.nmsManager.isSign(blockData)) {
			return true;
		}

		NbtCompound tag = this.getBlockEntity(chunkData, x, y, z);

		return tag == null || this.isSignTextEmpty(tag, "Text1") && this.isSignTextEmpty(tag, "Text2")
				&& this.isSignTextEmpty(tag, "Text3") && this.isSignTextEmpty(tag, "Text4");
	}

	private boolean isSignTextEmpty(NbtCompound compound, String key) {
		NbtBase<?> tag = compound.getValue(key);

		if (tag == null || tag.getType() != NbtType.TAG_STRING) {
			return true;
		}

		String json = (String) tag.getValue();

		if (json == null || json.isEmpty()) {
			return true;
		}

		String text = this.nmsManager.getTextFromChatComponent(json);

		return text == null || text.isEmpty();
	}

	private NbtCompound getBlockEntity(ChunkData chunkData, int x, int y, int z) {
		for (NbtCompound tag : chunkData.blockEntities) {
			if (tag != null && x == tag.getInteger("x") && y == tag.getInteger("y") && z == tag.getInteger("z")) {
				return tag;
			}
		}

		return null;
	}

	private void addBlocksToPalette(IChunkMap manager, IWorldConfig worldConfig) {
		if (!manager.inputHasNonAirBlock()) {
			return;
		}

		for (int id : worldConfig.getPaletteBlocks()) {
			manager.addToOutputPalette(id);
		}
	}

	private ObfuscatedCachedChunk tryUseCache(ChunkData chunkData, Player player) {
		if (!this.config.isUseCache())
			return null;

		chunkData.useCache = true;

		// Hash the chunk
		long hash = CalculationsUtil.Hash(chunkData.data, chunkData.data.length);
		// Get cache folder
		File cacheFolder = new File(this.plugin.getObfuscatedDataCacheHandler().getCacheFolder(), player.getWorld().getName());
		// Create cache objects
		ObfuscatedCachedChunk cache = new ObfuscatedCachedChunk(this.plugin, cacheFolder, chunkData.chunkX, chunkData.chunkZ);

		// Check if hash is consistent
		cache.read();

		long storedHash = cache.getHash();

		if (storedHash == hash && cache.data != null) {
			int[] proximityList = cache.proximityList;
			ArrayList<BlockCoord> proximityBlocks = this.getCoordFromArray(proximityList);

			// ProximityHider add blocks
			this.plugin.getProximityHiderHandler().addProximityBlocks(player, chunkData.chunkX, chunkData.chunkZ, proximityBlocks);

			// Hash match, use the cached data instead and skip calculations
			return cache;
		}

		cache.hash = hash;
		cache.data = null;

		return cache;
	}

	public boolean areAjacentBlocksTransparent(IChunkMap manager, World world, boolean checkCurrentBlock, int x, int y, int z, int countdown) throws IOException {
		if (y >= world.getMaxHeight() || y < 0) {
			return true;
		}

		if (checkCurrentBlock) {
			ChunkData chunkData = manager.getChunkData();
			int blockData = manager.get(x, y, z);

			if (blockData < 0) {
				blockData = this.nmsManager.loadChunkAndGetBlockId(world, x, y, z);

				if (blockData < 0) {
					chunkData.useCache = false;
				}
			}

			if (blockData >= 0 && this.config.isBlockTransparent(blockData)) {
				return true;
			}
		}

		if (countdown == 0)
			return false;

		if (this.areAjacentBlocksTransparent(manager, world, true, x, y + 1, z, countdown - 1))
			return true;
		if (this.areAjacentBlocksTransparent(manager, world, true, x, y - 1, z, countdown - 1))
			return true;
		if (this.areAjacentBlocksTransparent(manager, world, true, x + 1, y, z, countdown - 1))
			return true;
		if (this.areAjacentBlocksTransparent(manager, world, true, x - 1, y, z, countdown - 1))
			return true;
		if (this.areAjacentBlocksTransparent(manager, world, true, x, y, z + 1, countdown - 1))
			return true;
		if (this.areAjacentBlocksTransparent(manager, world, true, x, y, z - 1, countdown - 1))
			return true;

		return false;
	}

	public boolean areAjacentBlocksBright(World world, int x, int y, int z, int countdown) {
		if (this.nmsManager.getBlockLightLevel(world, x, y, z) > 0) {
			return true;
		}

		if (countdown == 0)
			return false;

		if (this.areAjacentBlocksBright(world, x, y + 1, z, countdown - 1))
			return true;
		if (this.areAjacentBlocksBright(world, x, y - 1, z, countdown - 1))
			return true;
		if (this.areAjacentBlocksBright(world, x + 1, y, z, countdown - 1))
			return true;
		if (this.areAjacentBlocksBright(world, x - 1, y, z, countdown - 1))
			return true;
		if (this.areAjacentBlocksBright(world, x, y, z + 1, countdown - 1))
			return true;
		if (this.areAjacentBlocksBright(world, x, y, z - 1, countdown - 1))
			return true;

		return false;
	}

	private int random(int max) {
		return this.random.nextInt(max);
	}
}