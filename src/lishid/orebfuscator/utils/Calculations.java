/*
 * Copyright (C) 2011-2012 lishid.  All rights reserved.
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

package lishid.orebfuscator.utils;

import gnu.trove.set.hash.TByteHashSet;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.Deflater;

import lishid.orebfuscator.Orebfuscator;
import lishid.orebfuscator.cache.ObfuscatedChunkCache;

import net.minecraft.server.NetServerHandler;
import net.minecraft.server.Packet;
import net.minecraft.server.Packet51MapChunk;
import net.minecraft.server.TileEntity;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.craftbukkit.CraftWorld;

public class Calculations
{
    private final static int CHUNK_SIZE = 16 * 128 * 16 * 5 / 2;
    
	private static Deflater deflater = new Deflater();
    private static byte[] deflateBuffer = new byte[CHUNK_SIZE + 100];
    
    public static int ChunksCalculated = 0;
    
	public static void UpdateBlocksNearby(Block block)
	{
        HashSet<Block> blocks = Calculations.GetAjacentBlocks(block.getWorld(), new HashSet<Block>(), block, OrebfuscatorConfig.getUpdateRadius());

		HashSet<CraftPlayer> players = new HashSet<CraftPlayer>();
		
		List<Player> playerList = getPlayers(block.getWorld());
		
        for (Player player : playerList) {
        	double dx = Math.abs(player.getLocation().getX() - block.getX());
        	double dz = Math.abs(player.getLocation().getZ() - block.getZ());
        	double dist = Bukkit.getServer().getViewDistance() * 16;
            if (dx < dist && dz < dist)
            {
            	players.add((CraftPlayer) player);
            }
        }
        
        for(Block nearbyBlock : blocks)
        {
        	Calculations.UpdateBlock(nearbyBlock, players);
        }
	}
	
	public static HashSet<Block> GetAjacentBlocks(World world, HashSet<Block> allBlocks, Block block, int countdown)
	{
		AddBlockCheck(allBlocks, block);
		
		if (countdown == 0)
			return allBlocks;
		
		GetAjacentBlocks(world, allBlocks, block.getRelative(BlockFace.UP), countdown - 1);
		GetAjacentBlocks(world, allBlocks, block.getRelative(BlockFace.DOWN), countdown - 1);
		GetAjacentBlocks(world, allBlocks, block.getRelative(BlockFace.NORTH), countdown - 1);
		GetAjacentBlocks(world, allBlocks, block.getRelative(BlockFace.SOUTH), countdown - 1);
		GetAjacentBlocks(world, allBlocks, block.getRelative(BlockFace.EAST), countdown - 1);
		GetAjacentBlocks(world, allBlocks, block.getRelative(BlockFace.WEST), countdown - 1);
		
		return allBlocks;
	}

	public static void AddBlockCheck(HashSet<Block> allBlocks, Block block)
	{
		if (block == null) return;
		if ((OrebfuscatorConfig.isObfuscated((byte)block.getTypeId()) 
				|| OrebfuscatorConfig.isDarknessObfuscated((byte)block.getTypeId())))
		{
			allBlocks.add(block);
		}
	}

	public static void UpdateBlock(Block block, HashSet<CraftPlayer> players)
	{
		if (block == null) return;
		
        Packet p = null;
        while(true)
        {
            try
            {
            	TileEntity te = ((CraftWorld)block.getWorld()).getHandle().getTileEntity(block.getX(), block.getY(), block.getZ());
            	if(te != null)
            	{
            		p = te.d();
        		}
            	break;
            }
            catch (Exception e) { } //ConcurrentModificationException
        }
        
        for (CraftPlayer player : players) {
            player.sendBlockChange(block.getLocation(), block.getTypeId(), block.getData());
            
            if(p != null)
            {
        		player.getHandle().netServerHandler.sendPacket(p);
            }
        }
	}
	
	public static List<Player> getPlayers(World world)
	{
		List<Player> players = new ArrayList<Player>();
		
		synchronized(Orebfuscator.players)
		{
			for(Player p : Orebfuscator.players.keySet())
			{
				if(p.getWorld().getName().equals(world.getName()))
					players.add(p);
			}
		}
		
		return players;
	}
	
	public static boolean GetAjacentBlocksTypeID(ChunkInfo info, TByteHashSet IDPool, int x, int y, int z, int countdown)
	{
		byte id = 0;
		
		if(y >= info.world.getHeight())
			return true;
		
		int section = y >> 4;
        if ((info.chunkMask & (1 << section)) > 0 && x >> 4 == info.chunkX && z >> 4 == info.chunkZ)
		{
            int cX = x % 16;
            if(cX < 0)
            	cX += 16;
            int cZ = z % 16;
            if(cZ < 0)
            	cZ += 16;
            int index = section * 4096 + (y % 16 << 8) + (cZ << 4) + cX;
            try{
            	id = info.data[index];
            }
            catch(Exception e)
            {
            	Orebfuscator.log("Small problem, plz report to dev: " + x + " " + y + " " + z + " " + section + " " + index);
				id = (byte)info.world.getTypeId(x, y, z);
        	}
		}
		else
		{
			if(!info.world.isLoaded(x, y, z))
			{
				id = 1;
				info.useCache = false;
			}
			else
			{
				id = (byte)info.world.getTypeId(x, y, z);
			}
		}
		
		boolean isTested = IDPool.contains(id);
		if(!isTested && OrebfuscatorConfig.isTransparent(id))
		{
			return true;
		}
		else if(!isTested)
		{
			IDPool.add(id);
		}
		
		if (countdown == 0)
			return false;
		
		if(GetAjacentBlocksTypeID(info, IDPool, x, y + 1, z, countdown - 1)) return true;
		if(GetAjacentBlocksTypeID(info, IDPool, x, y - 1, z, countdown - 1)) return true;
		if(GetAjacentBlocksTypeID(info, IDPool, x + 1, y, z, countdown - 1)) return true;
		if(GetAjacentBlocksTypeID(info, IDPool, x - 1, y, z, countdown - 1)) return true;
		if(GetAjacentBlocksTypeID(info, IDPool, x, y, z + 1, countdown - 1)) return true;
		if(GetAjacentBlocksTypeID(info, IDPool, x, y, z - 1, countdown - 1)) return true;
		
		return false;
	}
	
	public static boolean GetAjacentBlocksHaveLight(ChunkInfo info, int x, int y, int z, int countdown)
	{
		if(info.world.getLightLevel(x, y, z) > 0)
			return true;
		
		if (countdown == 0)
			return false;

		if(GetAjacentBlocksHaveLight(info, x, y + 1, z, countdown - 1)) return true;
		if(GetAjacentBlocksHaveLight(info, x, y - 1, z, countdown - 1)) return true;
		if(GetAjacentBlocksHaveLight(info, x + 1, y, z, countdown - 1)) return true;
		if(GetAjacentBlocksHaveLight(info, x - 1, y, z, countdown - 1)) return true;
		if(GetAjacentBlocksHaveLight(info, x, y, z + 1, countdown - 1)) return true;
		if(GetAjacentBlocksHaveLight(info, x, y, z - 1, countdown - 1)) return true;
			
		return false;
	}

	public static void Obfuscate(Packet51MapChunk packet, CraftPlayer player, boolean sendPacket, byte[] chunkBuffer)
	{
		NetServerHandler nsh = player.getHandle().netServerHandler;
		if(nsh == null || nsh.disconnected || nsh.networkManager == null)
			return;
		
		ChunkInfo info = new ChunkInfo();
		info.world = player.getHandle().world.getWorld().getHandle();
		info.chunkX = packet.a;
		info.chunkZ = packet.b;
		info.chunkMask = packet.c;
		info.extraMask = packet.d;
		info.buffer = chunkBuffer;
		info.data = packet.rawData;
		
		//Compute chunk number
		for (int i = 0; i < 16; i++)
		{
		    if ((info.chunkMask & 1 << i) > 0)
		    	info.chunkSectionNumber++;
		    if ((info.extraMask & 1 << i) > 0)
		    	info.extraSectionNumber++;
		}
		
		//Obfuscate
		if(info.world.getWorld().getEnvironment() == Environment.NORMAL && //Environment.NORMAL = overworld
			!OrebfuscatorConfig.isWorldDisabled(info.world.getWorld().getName()) && //World not disabled
			OrebfuscatorConfig.obfuscateForPlayer(player) &&
				OrebfuscatorConfig.getEnabled()) //Plugin enabled
		{
			byte[] obfuscated = Obfuscate(info);
			System.arraycopy(obfuscated, 0, packet.rawData, 0, info.chunkSectionNumber * 4096);
		}
		
		if(sendPacket)
		{
			try{
				synchronized(deflateBuffer)
				{
					//Compression
			        int dataSize = packet.rawData.length;
			        if (deflateBuffer.length < dataSize + 100) {
			            deflateBuffer = new byte[dataSize + 100];
			        }
			        
			        deflater.reset();
			        deflater.setLevel(dataSize < 20480 ? 1 : 6);
			        deflater.setInput(packet.rawData);
			        deflater.finish();
			        int size = deflater.deflate(deflateBuffer);
			        if (size == 0) {
			            size = deflater.deflate(deflateBuffer);
			        }
		        	
			        // copy compressed data to packet
			        packet.buffer = new byte[size];
			        packet.size = size;
			        System.arraycopy(deflateBuffer, 0, packet.buffer, 0, size);
				}
			} catch (Exception e) { Orebfuscator.log(e); }
			
			//Send it
			nsh.networkManager.queue(packet);
		}
		
		//Send tile entities if not using spout
		if(sendPacket)
		{
			//Send TileEntities
			int i = info.chunkX * 16;
			int j = info.chunkZ * 16;
			for (int k = 0; k < 16; ++k) {
                if ((info.chunkMask & 1 << k) != 0) {
                    int l = k << 4;
                    Object[] list = info.world.getTileEntities(i, l, j, i + 16, l + 16, j + 16).toArray();

                    for (int i1 = 0; i1 < list.length; i1++) {
        	        	TileEntity tileentity = (TileEntity) list[i1];
        	            if (tileentity != null) {
        	            	Packet p = tileentity.d();
        	            	if(p!=null)
        	            	{
        	            		nsh.sendPacket(p);
        	            	}
        	            }
                    }
                }
            }
		}
	}
	
	public static byte[] Obfuscate(ChunkInfo info)
	{
		File cacheFolder = new File(OrebfuscatorConfig.getCacheFolder(), info.world.getWorld().getName());
		ObfuscatedChunkCache cache = new ObfuscatedChunkCache(cacheFolder, info.chunkX, info.chunkZ, OrebfuscatorConfig.getInitialRadius());
		TByteHashSet blockList = new TByteHashSet();
		int tmp = 0;
		boolean Obfuscate = false;
		info.useCache = false;
		if(info.chunkSectionNumber * 4096 != info.buffer.length)
			info.buffer = new byte[info.chunkSectionNumber * 4096];
		System.arraycopy(info.data, 0, info.buffer, 0, info.chunkSectionNumber * 4096);
		long hash = Hash(info.buffer);
		
		//Caching
		if(info.data.length == 2048 * (5 * info.chunkSectionNumber + info.extraSectionNumber) + 256 && OrebfuscatorConfig.getUseCache())
		{
			info.useCache = true;
			
			long storedHash = cache.getHash();
			if(storedHash != 0L && hash == storedHash)
			{
				byte[] data = cache.getData();
				if(data != null)
				{
					if(OrebfuscatorConfig.getVerboseMode())
					{
						Orebfuscator.log("Cache found.");
					}
					
					//Hash match, use the cached data instead
					System.arraycopy(data, 0, info.buffer, 0, data.length);
					//Skip calculations
					return info.buffer;
				}
			}
			
			if(OrebfuscatorConfig.getVerboseMode())
			{
				if(storedHash == 0L)
					Orebfuscator.log("Cache not found.");
				else if(hash != storedHash)
					Orebfuscator.log("Cache hash does not match: " + hash + " " + storedHash);
				else
					Orebfuscator.log("Cache data inconsistent.");
			}
		}
		
		if(OrebfuscatorConfig.getVerboseMode() && OrebfuscatorConfig.getUseCache() && !info.useCache)
		{
			Orebfuscator.log("Cache not used.");
		}
		
		if(OrebfuscatorConfig.getVerboseMode())
		{
			ChunksCalculated++;
		}
		
		
		//Loop over 16x16x16 chunks in the 16x256x16 column
		int dataIndexModifier = 0;
		//int extraIndexModifier = 0;
		//int extraIndexStart = totalChunks * (4096 + 2048 + 2048 + 2048);
		int startX = info.chunkX << 4;
		int startZ = info.chunkZ << 4;
		for (int i = 0; i < 16; i++)
		{
		    //If the bitmask indicates this chunk is sent...
		    if ((info.chunkMask & 1 << i) > 0)
		    {
		        int indexDataStart = dataIndexModifier * 4096;
		        //boolean useExtraData = (info.chunkExtra & 1 << i) > 0;
		        //int indexExtraStart = extraIndexModifier * 2048;
		        
		        int tempIndex = 0;

				OrebfuscatorConfig.shuffleRandomBlocks();
				
		        for (int y = 0; y < 16; y++)
		        {
			        for (int z = 0; z < 16; z++)
			        {
				        for (int x = 0; x < 16; x++)
				        {
				            int index = indexDataStart + tempIndex;
				            byte data = info.data[index];
				            /*
				            byte extra = 0;
				            if(useExtraData)
			            	{
				            	if(tempIndex % 2 == 0)
				            		extra = (byte) (info.data[extraIndexStart + indexExtraStart + (tempIndex >> 1)] & 0x0F);
				            	else
				            		extra = (byte) (info.data[extraIndexStart + indexExtraStart + (tempIndex >> 1)] >> 4);
			            	}*/
				            

							//Initialize objects
							Obfuscate = false;
							blockList.clear();
							
							/*
							//Check if the block should be obfuscated because of proximity check
							if (!Obfuscate && OrebfuscatorConfig.getUseProximityHider() && OrebfuscatorConfig.isProximityObfuscated(info.data[index]))
							{
								Obfuscate = true;
							}*/

							//Check if the block should be obfuscated because of being behind stuff
							if(OrebfuscatorConfig.isObfuscated(data))
							{
								if(OrebfuscatorConfig.getInitialRadius() == 0)
								{
									//Obfuscate anyways
									Obfuscate = true;
								}
								else
								{
									//Get all block IDs nearby
									Obfuscate = !GetAjacentBlocksTypeID(info, blockList, startX + x, (i << 4) + y, startZ + z, OrebfuscatorConfig.getInitialRadius());
								}
							}
							
							//Check if the block should be obfuscated because of darkness
							if (!Obfuscate && OrebfuscatorConfig.getDarknessHideBlocks() && OrebfuscatorConfig.isDarknessObfuscated(data))
							{
								if(OrebfuscatorConfig.getInitialRadius() == 0)
								{
									Obfuscate = true;
								}
								else if(!GetAjacentBlocksHaveLight(info, x, y, z, OrebfuscatorConfig.getInitialRadius()))
								{
									Obfuscate = true;
								}
							}
							
							//If the block should be obfuscated
							if(Obfuscate)
							{
								if(OrebfuscatorConfig.getEngineMode() == 1)
								{
									//Engine mode 1, replace with stone
									info.buffer[index] = 1;
								}
								else if(OrebfuscatorConfig.getEngineMode() == 2)
								{
									//Ending mode 2, replace with random block
						            tmp = tmp % (OrebfuscatorConfig.getRandomBlocks().size() - 1) + 1;
						            info.buffer[index] = (byte)(int)OrebfuscatorConfig.getRandomBlocks().get(tmp);
								}
							}
				            
				            tempIndex++;
				        }
			        }
		        }
		        
		        dataIndexModifier++;
		        //if(useExtraData)
		        //{
		        //	extraIndexModifier++;
	        	//}
		    }
		}
		
		//If cache is allowed
		if(info.useCache)
		{
			//Save cache
			cache.initialRadius = OrebfuscatorConfig.getInitialRadius();
			cache.Write(hash, info.buffer);
		}
		
		return info.buffer;
	}
	
	public static long Hash(byte[] data)
	{
		CRC32 crc = new CRC32();
		crc.reset();
		crc.update(data);
		long hash = crc.getValue();
		return hash;
	}
	
	public int getIndex(int x, int y, int z) {
		return (x & 0xF) << 11 | (z & 0xF) << 7 | (y & 0x7F);
	}
}