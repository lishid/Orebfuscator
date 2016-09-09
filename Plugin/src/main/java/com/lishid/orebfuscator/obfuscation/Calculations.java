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

import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;

import com.lishid.orebfuscator.Orebfuscator;
import com.lishid.orebfuscator.OrebfuscatorConfig;
import com.lishid.orebfuscator.cache.ObfuscatedCachedChunk;
import com.lishid.orebfuscator.chunkmap.ChunkData;
import com.lishid.orebfuscator.chunkmap.ChunkMapManager;
import com.lishid.orebfuscator.types.BlockCoord;
import com.lishid.orebfuscator.types.BlockState;

public class Calculations {

    public static byte[] obfuscateOrUseCache(ChunkData chunkData, Player player) throws IOException {
    	if(chunkData.primaryBitMask == 0) return null;
    	
        if (!OrebfuscatorConfig.Enabled // Plugin enabled
        		|| OrebfuscatorConfig.isWorldDisabled(player.getWorld().getName()) // World not enabled
                || !OrebfuscatorConfig.obfuscateForPlayer(player)) // Should the player have obfuscation?
        {
            return null; 
        }
        
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
    	Environment environment = player.getWorld().getEnvironment();
    	int initialRadius = OrebfuscatorConfig.InitialRadius;

        // Track of pseudo-randomly assigned randomBlock
        int randomIncrement = 0;
        int randomIncrement2 = 0;
        int randomCave = 0;
        // Track of whether a block should be obfuscated or not
        boolean obfuscate = false;
        boolean specialObfuscate = false;

        int engineMode = OrebfuscatorConfig.EngineMode;
        int maxChance = OrebfuscatorConfig.AirGeneratorMaxChance;
        int incrementMax = maxChance;

        int randomBlocksLength = OrebfuscatorConfig.getRandomBlocks(false, environment).length;
        boolean randomAlternate = false;

		int startX = chunkData.chunkX << 4;
		int startZ = chunkData.chunkZ << 4;

		BlockState blockState = new BlockState();

		ChunkMapManager manager = new ChunkMapManager(chunkData);
		manager.init();
		
		for(int i = 0; i < manager.getSectionCount(); i++) {
            OrebfuscatorConfig.shuffleRandomBlocks();

            for(int offsetY = 0; offsetY < 16; offsetY++) {
            	for(int offsetZ = 0; offsetZ < 16; offsetZ++) {
                    incrementMax = (maxChance + OrebfuscatorConfig.random(maxChance)) / 2;
                    
                    for(int offsetX = 0; offsetX < 16; offsetX++) {
                    	int blockData = manager.readNextBlock();
                    	
                    	ChunkMapManager.blockDataToState(blockData, blockState);

                        if (blockState.id < 256) {
							int x = startX | offsetX;
							int y = manager.getY();
							int z = startZ | offsetZ;
	
	                        // Initialize data
	                        obfuscate = false;
	                        specialObfuscate = false;
	
	                        // Check if the block should be obfuscated for the default engine modes
	                        if (OrebfuscatorConfig.isObfuscated(blockState.id, environment)) {
	                            if (initialRadius == 0) {
	                                // Do not interfere with PH
	                                if (OrebfuscatorConfig.UseProximityHider && OrebfuscatorConfig.isProximityObfuscated(y, blockState.id)) {
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
	                        if (!obfuscate && OrebfuscatorConfig.UseProximityHider && OrebfuscatorConfig.isProximityObfuscated(y, blockState.id)) {
	                            if (OrebfuscatorConfig.isProximityHiderOn(y, blockState.id)) {
	                            	BlockCoord block = new BlockCoord(x, y, z);
	                                if (block != null) {
	                                    proximityBlocks.add(block);
	                                }
	                                
	                                obfuscate = true;
	                                if (OrebfuscatorConfig.UseSpecialBlockForProximityHider) {
	                                    specialObfuscate = true;
	                                }
	                            }
	                        }
	
	                        // Check if the block is obfuscated
	                        if (obfuscate) {
	                            if (specialObfuscate) {
	                                // Proximity hider
	                                blockState.id = OrebfuscatorConfig.ProximityHiderID;
	                            } else {
	                                randomIncrement2 = OrebfuscatorConfig.random(incrementMax);
	
	                                if (engineMode == 1) {
	                                    // Engine mode 1, replace with stone
	                                	blockState.id = (environment == Environment.NETHER ? 87 : 1);
	                                } else if (engineMode == 2) {
	                                    // Ending mode 2, replace with random block
	                                    if (randomBlocksLength > 1) {
	                                        randomIncrement = CalculationsUtil.increment(randomIncrement, randomBlocksLength);
	                                    }
	                                    
	                                    blockState.id = OrebfuscatorConfig.getRandomBlock(randomIncrement, randomAlternate, environment);
	                                    randomAlternate = !randomAlternate;
	                                }
	                                // Anti texturepack and freecam
	                                if (OrebfuscatorConfig.AntiTexturePackAndFreecam) {
	                                // Add random air blocks
	                                    if (randomIncrement2 == 0) {
	                                        randomCave = 1 + OrebfuscatorConfig.random(3);
	                                    }
	
	                                    if (randomCave > 0) {
	                                    	blockState.id = 0;
	                                        randomCave--;
	                                    }
	                                }
	                            }
	
	                            blockState.meta = 0;
	                        }
	
	                        // Check if the block should be obfuscated because of the darkness
	                        if (!obfuscate && OrebfuscatorConfig.DarknessHideBlocks && OrebfuscatorConfig.isDarknessObfuscated(blockState.id)) {
	                            if (!areAjacentBlocksBright(player.getWorld(), x, y, z, 1)) {
	                                // Hide block, setting it to air
	                            	blockState.id = 0;
	                            	blockState.meta = 0;
	                            }
	                        }
	                        
	                        blockData = ChunkMapManager.blockStateToData(blockState);
                        } else {
                        	blockData = 0;
                        }
                        
						if(offsetY == 0 && offsetZ == 0 && offsetX == 0) {
							manager.finalizeOutput();							
							manager.initOutputPalette();
							addBlocksToPalette(manager, environment);
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
    
    private static void addBlocksToPalette(ChunkMapManager manager, Environment environment) {
    	if(!manager.inputHasNonAirBlock()) {
    		return;
    	}

    	int[] list = environment == Environment.NETHER
    			? OrebfuscatorConfig.NetherPaletteBlocks
    			: OrebfuscatorConfig.NormalPaletteBlocks;
    	
    	for(int id : list) {
    		int blockData = ChunkMapManager.getBlockDataFromId(id);
    		manager.addToOutputPalette(blockData);
    	}
    }
    
    private static ObfuscatedCachedChunk tryUseCache(ChunkData chunkData, Player player) {
        if (!OrebfuscatorConfig.UseCache) return null;
        
        chunkData.useCache = true;
        
        // Hash the chunk
        long hash = CalculationsUtil.Hash(chunkData.data, chunkData.data.length);
        // Get cache folder
        File cacheFolder = new File(OrebfuscatorConfig.getCacheFolder(), player.getWorld().getName());
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
	        int id;
	
	        if (blockData < 0) {
	        	id = Orebfuscator.nms.getBlockId(world, x, y, z);
	        	
	            if (id < 0) {
	                id = 1;
	                chunkData.useCache = false;
	            }
	        } else {
	        	id = ChunkMapManager.getBlockIdFromData(blockData);
	        }
	
	        if (OrebfuscatorConfig.isBlockTransparent(id)) {
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

    //16 bit char for block data, including 12 bits for block id
    /*
    private static final int BLOCKID_MAX = 4096;
    private static char[] cacheMap = new char[BLOCKID_MAX];

    static {
        buildCacheMap();
    }

    public static void buildCacheMap() {
        for (int i = 0; i < 4096; i++) {
            cacheMap[i] = (char) i;
            if (OrebfuscatorConfig.isBlockTransparent((short) i) && !isBlockSpecialObfuscated(64, (char) i)) {
                cacheMap[i] = 0;
            }
        }
    }

    private static void PrepareBufferForCaching(byte[] data, int bytes) {
        for (int i = 0; i < bytes / 2; i++) {
            int blockId = chunkGetBlockId(data, i);

            blockId = cacheMap[blockId % BLOCKID_MAX];

            chunkSetBlockId(data, i, blockId);
        }
    }

    private static boolean isBlockSpecialObfuscated(int y, char id) {
        if (OrebfuscatorConfig.DarknessHideBlocks && OrebfuscatorConfig.isDarknessObfuscated(id)) {
            return true;
        }
        if (OrebfuscatorConfig.UseProximityHider && OrebfuscatorConfig.isProximityObfuscated(y, id)) {
            return true;
        }
        return false;
    }

    private static void RepaintChunkToBuffer(byte[] data, ChunkInfo info) {
        byte[] original = info.original;
        int bytes = info.bytes;

        for (int i = 0; i < bytes / 2; i++) {
            int newId = chunkGetBlockId(data, i);
            int originalId = chunkGetBlockId(original, i);

            if (newId == 0 && originalId != 0) {
                if (OrebfuscatorConfig.isBlockTransparent((short) originalId)) {
                    if (!isBlockSpecialObfuscated(0, (char) originalId)) {
                        chunkSetBlockId(data, i, originalId);
                    }
                }
            }
        }
    }
    */
}