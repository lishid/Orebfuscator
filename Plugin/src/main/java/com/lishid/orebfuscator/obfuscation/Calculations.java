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

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.comphenix.protocol.wrappers.nbt.NbtBase;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtType;
import com.lishid.orebfuscator.DeprecatedMethods;
import com.lishid.orebfuscator.Orebfuscator;
import com.lishid.orebfuscator.cache.ObfuscatedCachedChunk;
import com.lishid.orebfuscator.cache.ObfuscatedDataCache;
import com.lishid.orebfuscator.chunkmap.ChunkData;
import com.lishid.orebfuscator.chunkmap.ChunkMapManager;
import com.lishid.orebfuscator.config.ProximityHiderConfig;
import com.lishid.orebfuscator.config.WorldConfig;
import com.lishid.orebfuscator.types.BlockCoord;
import com.lishid.orebfuscator.types.BlockState;

public class Calculations {
	private static Random random = new Random();

    public static byte[] obfuscateOrUseCache(ChunkData chunkData, Player player, WorldConfig worldConfig) throws IOException {
    	if(chunkData.primaryBitMask == 0) return null;
    	
        byte[] output;
        
        ObfuscatedCachedChunk cache = tryUseCache(chunkData, player);
        
        if(cache != null && cache.data != null) {
        	//Orebfuscator.log("Read from cache");/*debug*/
        	output = cache.data;
        } else {
	        // Blocks kept track for ProximityHider
	        ArrayList<BlockCoord> proximityBlocks = new ArrayList<BlockCoord>();
	        
	        output = obfuscate(chunkData, player, proximityBlocks);
	
	        if (cache != null) {
	            // If cache is still allowed
	        	if(chunkData.useCache) {
		            // Save cache
		            int[] proximityList = new int[proximityBlocks.size() * 3];
		            int index = 0;
		            
		            for (int i = 0; i < proximityBlocks.size(); i++) {
		            	BlockCoord b = proximityBlocks.get(i);
		                if (b != null) {
		                    proximityList[index++] = b.x;
		                    proximityList[index++] = b.y;
		                    proximityList[index++] = b.z;
		                }
		            }
		            
		            cache.write(cache.hash, output, proximityList);
		            
		            //Orebfuscator.log("Write to cache");/*debug*/
	        	}
	        	
	            cache.free();
	        }
        }
        
        //Orebfuscator.log("Send chunk x = " + chunkData.chunkX + ", z = " + chunkData.chunkZ + " to player " + player.getName());/*debug*/

        return output;
    }
    
    private static byte[] obfuscate(ChunkData chunkData, Player player, ArrayList<BlockCoord> proximityBlocks) throws IOException {
    	WorldConfig worldConfig = Orebfuscator.configManager.getWorld(player.getWorld());
    	ProximityHiderConfig proximityHider = worldConfig.getProximityHiderConfig();
    	int initialRadius = Orebfuscator.config.getInitialRadius();

        // Track of pseudo-randomly assigned randomBlock
        int randomIncrement = 0;
        int randomIncrement2 = 0;
        int randomCave = 0;

        int engineMode = Orebfuscator.config.getEngineMode();
        int maxChance = worldConfig.getAirGeneratorMaxChance();
        int incrementMax = maxChance;

        int randomBlocksLength = worldConfig.getRandomBlocks().length;
        boolean randomAlternate = false;

		int startX = chunkData.chunkX << 4;
		int startZ = chunkData.chunkZ << 4;

		BlockState blockState = new BlockState();

		ChunkMapManager manager = new ChunkMapManager(chunkData);
		manager.init();
		
		for(int i = 0; i < manager.getSectionCount(); i++) {
            worldConfig.shuffleRandomBlocks();

            for(int offsetY = 0; offsetY < 16; offsetY++) {
            	for(int offsetZ = 0; offsetZ < 16; offsetZ++) {
                    incrementMax = (maxChance + random(maxChance)) / 2;
                    
                    for(int offsetX = 0; offsetX < 16; offsetX++) {
                    	int blockData = manager.readNextBlock();
                    	
                    	ChunkMapManager.blockDataToState(blockData, blockState);

                        if (isNotAir(blockState.type)) {
							int x = startX | offsetX;
							int y = manager.getY();
							int z = startZ | offsetZ;
	
	                        // Initialize data
	                        boolean obfuscate = false;
	                        boolean specialObfuscate = false;
	
	                        // Check if the block should be obfuscated for the default engine modes
	                        if (worldConfig.isObfuscated(blockState.type)) {
	                            if (initialRadius == 0) {
	                                // Do not interfere with PH
	                                if (proximityHider.isEnabled() && proximityHider.isProximityObfuscated(y, blockState.type)) {
	                                    if (!areAjacentBlocksTransparent(manager, player.getWorld(), false, x, y, z, 1)) {
	                                        obfuscate = true;
	                                    }
	                                } else {
	                                    // Obfuscate all blocks
	                                    obfuscate = true;
	                                }
	                            } else {
	                                // Check if any nearby blocks are transparent
	                                if (!areAjacentBlocksTransparent(manager, player.getWorld(), false, x, y, z, initialRadius)) {
	                                    obfuscate = true;
	                                }
	                            }
	                        }
	                        
	                        // Check if the block should be obfuscated because of proximity check
	                        if (!obfuscate && proximityHider.isEnabled() && proximityHider.isProximityObfuscated(y, blockState.type)) {
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
	                        if (obfuscate && canObfuscate(chunkData, x, y, z, blockState)) {
	                            if (specialObfuscate) {
	                                // Proximity hider
	                            	Orebfuscator.nms.setBlockStateFromMaterial(proximityHider.getSpecialBlockID(), blockState);
	                            } else {
	                                if (engineMode == 1) {
	                                    // Engine mode 1, replace with stone
	                                	Orebfuscator.nms.setBlockStateFromMaterial(worldConfig.getMode1BlockId(), blockState);
	                                } else if (engineMode == 2) {
	                                    // Ending mode 2, replace with random block
	                                    if (randomBlocksLength > 1) {
	                                        randomIncrement = CalculationsUtil.increment(randomIncrement, randomBlocksLength);
	                                    }
	                                    
	                                    Orebfuscator.nms.setBlockStateFromMaterial(
	                                    			worldConfig.getRandomBlock(randomIncrement, randomAlternate), blockState);
	                                    randomAlternate = !randomAlternate;
	                                }
	                                // Anti texturepack and freecam
	                                if (worldConfig.isAntiTexturePackAndFreecam()) {
	                                	// Add random air blocks
		                                randomIncrement2 = random(incrementMax);

		                                if (randomIncrement2 == 0) {
	                                        randomCave = 1 + random(3);
	                                    }
	
	                                    if (randomCave > 0) {
	                                    	// 1.13 TODO: Cave_air?
		                                	Orebfuscator.nms.setBlockStateFromMaterial(Material.AIR, blockState);

	                                        randomCave--;
	                                    }
	                                }
	                            }
	                        }
	
	                        // Check if the block should be obfuscated because of the darkness
	                        if (!obfuscate && worldConfig.isDarknessHideBlocks() && worldConfig.isDarknessObfuscated(blockState.type)) {
	                            if (!areAjacentBlocksBright(player.getWorld(), x, y, z, 1)) {
	                                // Hide block, setting it to air
	                            	Orebfuscator.nms.setBlockStateFromMaterial(Material.AIR, blockState);
	                            	// 1.13 TODO: Cave air? void air? regular air? how to decide.
	                            }
	                        }
	                        
                        } else {
                        	Orebfuscator.nms.setBlockStateFromMaterial(Material.AIR, blockState);
                        }

                        blockData = ChunkMapManager.blockStateToData(blockState);
                        
						if(offsetY == 0 && offsetZ == 0 && offsetX == 0) {
							manager.finalizeOutput();							
							manager.initOutputPalette();
							addBlocksToPalette(manager, worldConfig);
							manager.initOutputSection();
						}
						
                        manager.writeOutputBlock(blockData);
                    }
                }
            }
        }
		
		manager.finalizeOutput();
		
		byte[] output = manager.createOutput();

        ProximityHider.addProximityBlocks(player, chunkData.chunkX, chunkData.chunkZ, proximityBlocks);
        
        //Orebfuscator.log("Create new chunk data for x = " + chunkData.chunkX + ", z = " + chunkData.chunkZ);/*debug*/
        
        return output;
    }
    
    private static boolean canObfuscate(ChunkData chunkData, int x, int y, int z, BlockState blockState) {
    	if(chunkData.blockEntities == null
    			|| (
    				!Material.WALL_SIGN.equals(blockState.type)
    				&& !Material.SIGN.equals(blockState.type)
    				)
    			)
    	{
    		return true;
    	}
    	
    	NbtCompound tag = getNbtTag(chunkData, x, y, z);
    	
    	return tag == null ||
    			isSignTextEmpty(tag, "Text1")
    			&& isSignTextEmpty(tag, "Text2")
    			&& isSignTextEmpty(tag, "Text3")
    			&& isSignTextEmpty(tag, "Text4");
    }
    
    private static boolean isSignTextEmpty(NbtCompound compound, String key) {
    	NbtBase<?> tag = compound.getValue(key);
    	
    	if(tag == null || tag.getType() != NbtType.TAG_STRING) {
    		return true;
    	}
    	
    	String json = (String)tag.getValue();
    	
    	if(json == null || json.isEmpty()) {
    		return true;
    	}
    	
    	String text = Orebfuscator.nms.getTextFromChatComponent(json);
    	
    	return text == null || text.isEmpty();
    }
    
    private static NbtCompound getNbtTag(ChunkData chunkData, int x, int y, int z) {
    	for(NbtCompound tag : chunkData.blockEntities) {
    		if(tag != null) {
	    		if(x == tag.getInteger("x")
	    				&& y == tag.getInteger("y")
	    				&& z == tag.getInteger("z")
	    			)
	    		{
	    			return tag;
	    		}
    		}
    	}
    	
    	return null;
    }
    
    private static void addBlocksToPalette(ChunkMapManager manager, WorldConfig worldConfig) {
    	if(!manager.inputHasNonAirBlock()) {
    		return;
    	}

    	for(int id : worldConfig.getPaletteBlocks()) {
    		// for 1.13, the getPaletteBlocks is pre-stuffed with the computed, NMS-specific combinedblockIDs, at startup.
    		//int blockData = ChunkMapManager.getBlockDataFromId(id);
    		manager.addToOutputPalette(id); //blockData);
    	}
    }
    
    private static ObfuscatedCachedChunk tryUseCache(ChunkData chunkData, Player player) {
        if (!Orebfuscator.config.isUseCache()) return null;
        
        chunkData.useCache = true;
        
        // Hash the chunk
        long hash = CalculationsUtil.Hash(chunkData.data, chunkData.data.length);
        // Get cache folder
        File cacheFolder = new File(ObfuscatedDataCache.getCacheFolder(), player.getWorld().getName());
        // Create cache objects
        ObfuscatedCachedChunk cache = new ObfuscatedCachedChunk(cacheFolder, chunkData.chunkX, chunkData.chunkZ);
        
        // Check if hash is consistent
        cache.read();
        
        long storedHash = cache.getHash();

        if (storedHash == hash && cache.data != null) {
            int[] proximityList = cache.proximityList;
        	ArrayList<BlockCoord> proximityBlocks = new ArrayList<BlockCoord>();
        	
            // Decrypt chest list
            if (proximityList != null) {
            	int index = 0;
            	
                while (index < proximityList.length) {
                	int x = proximityList[index++];
                	int y = proximityList[index++];
                	int z = proximityList[index++];
                	BlockCoord b = new BlockCoord(x, y, z);
                	
                	if(b != null) {
                		proximityBlocks.add(b);
                	}
                }
            }

            // ProximityHider add blocks
            ProximityHider.addProximityBlocks(player, chunkData.chunkX, chunkData.chunkZ, proximityBlocks);

            // Hash match, use the cached data instead and skip calculations
            return cache;
        }
        
        cache.hash = hash;
        cache.data = null;
        
        return cache;
    }
    
    public static boolean areAjacentBlocksTransparent(
    		ChunkMapManager manager,
    		World world,
    		boolean checkCurrentBlock,
    		int x,
    		int y,
    		int z,
    		int countdown
    		) throws IOException
    {
        if (y >= world.getMaxHeight() || y < 0)
            return true;

        if(checkCurrentBlock) {
	    	ChunkData chunkData = manager.getChunkData();
	        int blockData = manager.get(x, y, z);
	        BlockState blockState = new BlockState();
	        int id;
	
	        if (blockData < 0) {
				id = Orebfuscator.nms.loadChunkAndGetBlockId(world, x, y, z);
	        	
	            if (id < 0) {
	            	Orebfuscator.nms.setBlockStateFromMaterial(Material.STONE, blockState);
	                id = blockState.id;// Stone
	                chunkData.useCache = false;
	            }
	        } else {
	        	id = blockData;// ChunkMapManager.getBlockIdFromData(blockData);
	        }
	        Orebfuscator.nms.setBlockStateFromID(id, blockState);
	        
	        // TODO: Efficiency? Can we cache these converts? 1.13 yikes
	
	        if (Orebfuscator.config.isBlockTransparent(blockState.type)) {
	            return true;
	        }
        }

        if (countdown == 0)
            return false;

        if (areAjacentBlocksTransparent(manager, world, true, x, y + 1, z, countdown - 1))
            return true;
        if (areAjacentBlocksTransparent(manager, world, true, x, y - 1, z, countdown - 1))
            return true;
        if (areAjacentBlocksTransparent(manager, world, true, x + 1, y, z, countdown - 1))
            return true;
        if (areAjacentBlocksTransparent(manager, world, true, x - 1, y, z, countdown - 1))
            return true;
        if (areAjacentBlocksTransparent(manager, world, true, x, y, z + 1, countdown - 1))
            return true;
        if (areAjacentBlocksTransparent(manager, world, true, x, y, z - 1, countdown - 1))
            return true;

        return false;
    }

    public static boolean areAjacentBlocksBright(World world, int x, int y, int z, int countdown) {
    	if(Orebfuscator.nms.getBlockLightLevel(world, x, y, z) > 0) {
    		return true;
    	}

        if (countdown == 0)
            return false;

        if (areAjacentBlocksBright(world, x, y + 1, z, countdown - 1))
            return true;
        if (areAjacentBlocksBright(world, x, y - 1, z, countdown - 1))
            return true;
        if (areAjacentBlocksBright(world, x + 1, y, z, countdown - 1))
            return true;
        if (areAjacentBlocksBright(world, x - 1, y, z, countdown - 1))
            return true;
        if (areAjacentBlocksBright(world, x, y, z + 1, countdown - 1))
            return true;
        if (areAjacentBlocksBright(world, x, y, z - 1, countdown - 1))
            return true;

        return false;
    }
    
    private static int random(int max) {
        return random.nextInt(max);
    }
    
    private static boolean isNotAir(Material type) {
    	return !(Material.AIR.equals(type) || Material.CAVE_AIR.equals(type) || Material.VOID_AIR.equals(type));
    }
}