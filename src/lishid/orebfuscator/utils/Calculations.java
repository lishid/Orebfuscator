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
    private final static int REDUCED_DEFLATE_THRESHOLD = CHUNK_SIZE / 4;
    private final static int DEFLATE_LEVEL_CHUNKS = 6;
    private final static int DEFLATE_LEVEL_PARTS = 1;
    
	private static Deflater deflater = new Deflater();
    private static byte[] deflateBuffer = new byte[CHUNK_SIZE + 100];
    
    public static int ChunksCalculated = 0;
    
	public static void UpdateBlocksNearby(Block block)
	{
        HashSet<Block> blocks = Calculations.GetAjacentBlocks(block.getWorld(), new HashSet<Block>(), block, OrebfuscatorConfig.getUpdateRadius());

		HashSet<CraftPlayer> players = new HashSet<CraftPlayer>();
		
		List<Player> playerList = getPlayers(block.getWorld());
		
        for (Player player : playerList) {
        	double dx = Math.abs(player.getLocation().getChunk().getX() - block.getChunk().getX());
        	double dz = Math.abs(player.getLocation().getChunk().getZ() - block.getChunk().getZ());
        	double dist = Bukkit.getServer().getViewDistance();
            if (dx <= dist && dz <= dist)
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
            		p = te.k();
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
			HashSet<Player> allPlayers = Orebfuscator.players;
			for(Player p : allPlayers)
			{
				if(p.getWorld().getName().equals(world.getName()))
					players.add(p);
			}
		}
		
		return players;
	}
	
	public static boolean GetAjacentBlocksTypeID(ChunkInfo info, TByteHashSet IDPool, int index, int x, int y, int z, int countdown)
	{
		byte id = 0;
		
		if(y > 126)
			return true;
		
		if(y < info.sizeY &&  y >= 0 && 
			x < info.sizeX &&  x >= 0 && 
			z < info.sizeZ &&  z >= 0 &&
			index > 0 && info.data.length > index)
		{
			id = info.data[index];
		}
		else
		{
			id = (byte)info.world.getTypeId(x + info.startX, y + info.startY, z + info.startZ);
		}

		if(!IDPool.contains(id) && OrebfuscatorConfig.isTransparent(id))
		{
			return true;
		}
		else if(!IDPool.contains(id))
		{
			IDPool.add(id);
		}
		
		if (countdown == 0)
			return false;
		
		if(GetAjacentBlocksTypeID(info, IDPool, index + 1, x, y + 1, z, countdown - 1))	return true;
		if(GetAjacentBlocksTypeID(info, IDPool, index - 1, x, y - 1, z, countdown - 1))	return true;
		if(GetAjacentBlocksTypeID(info, IDPool, index + info.sizeY * info.sizeZ, x + 1, y, z, countdown - 1)) return true;
		if(GetAjacentBlocksTypeID(info, IDPool, index - info.sizeY * info.sizeZ, x - 1, y, z, countdown - 1)) return true;
		if(GetAjacentBlocksTypeID(info, IDPool, index + info.sizeY, x, y, z + 1, countdown - 1)) return true;
		if(GetAjacentBlocksTypeID(info, IDPool, index - info.sizeY, x, y, z - 1, countdown - 1)) return true;
		
		return false;
	}
	
	public static boolean GetAjacentBlocksHaveLight(ChunkInfo info, int index, int x, int y, int z, int countdown)
	{
		if(info.world.getLightLevel(x + info.startX, y + info.startY, z + info.startZ) > 0)
			return true;
		
		if (countdown == 0)
			return false;

		if(GetAjacentBlocksHaveLight(info, index + 1, x, y + 1, z, countdown - 1)) return true;
		if(GetAjacentBlocksHaveLight(info, index - 1, x, y - 1, z, countdown - 1)) return true;
		if(GetAjacentBlocksHaveLight(info, index + info.sizeY * info.sizeZ, x + 1, y, z, countdown - 1)) return true;
		if(GetAjacentBlocksHaveLight(info, index - info.sizeY * info.sizeZ, x - 1, y, z, countdown - 1)) return true;
		if(GetAjacentBlocksHaveLight(info, index + info.sizeY, x, y, z + 1, countdown - 1)) return true;
		if(GetAjacentBlocksHaveLight(info, index - info.sizeY, x, y, z - 1, countdown - 1)) return true;
			
		return false;
	}
	/*
	public static void Obfuscate(Packet51MapChunk packet, CraftPlayer player)
	{
		Obfuscate(packet, player, true, true);
	}*/

	public static void Obfuscate(Packet51MapChunk packet, CraftPlayer player, boolean sendPacket, byte[] chunkBuffer)
	{
		NetServerHandler nsh = player.getHandle().netServerHandler;
		if(nsh == null || nsh.disconnected || nsh.networkManager == null)
			return;
		
		ChunkInfo info = new ChunkInfo();
		info.world = player.getHandle().world.getWorld().getHandle();
		info.startX = packet.a;
		info.startY = packet.b;
		info.startZ = packet.c;
		info.sizeX = packet.d;
		info.sizeY = packet.e;
		info.sizeZ = packet.f;
		info.chunkSize = info.sizeX * info.sizeY * info.sizeZ;
		info.buffer = chunkBuffer;
		
		//Obfuscate
		if(info.world.getWorld().getEnvironment() == Environment.NORMAL && //Environment.NORMAL = overworld
			!OrebfuscatorConfig.isWorldDisabled(info.world.getWorld().getName()) && //World not disabled
			OrebfuscatorConfig.obfuscateForPlayer(player) &&
				OrebfuscatorConfig.getEnabled()) //Plugin enabled
		{
			info.data = packet.rawData;
			byte[] obfuscated = Obfuscate(info);
			System.arraycopy(obfuscated, 0, packet.rawData, 0, info.chunkSize);
		}
		
		//Free memory
		info.data = null;
		
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
			        deflater.setLevel(dataSize < REDUCED_DEFLATE_THRESHOLD ? DEFLATE_LEVEL_PARTS : DEFLATE_LEVEL_CHUNKS);
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
			Object[] list = info.world.getTileEntities(info.startX, info.startY, info.startZ, info.startX + info.sizeX, info.startY + info.sizeY, info.startZ + info.sizeZ).toArray();
	        for (int i = 0; i < list.length; ++i) {
	        	TileEntity tileentity = (TileEntity) list[i];
	            if (tileentity != null) {
	            	Packet p = tileentity.k();
	            	if(p!=null)
	            	{
	            		nsh.sendPacket(p);
	            	}
	            }
	        }
		}
	}
	
	public static byte[] Obfuscate(ChunkInfo info)
	{
		File cacheFolder = new File(new File(Bukkit.getServer().getWorldContainer(), "orebfuscator_cache"), info.world.getWorld().getName());
		int chunkX = info.startX >> 4;
		int chunkZ = info.startZ >> 4;
		ObfuscatedChunkCache cache = new ObfuscatedChunkCache(cacheFolder, chunkX, chunkZ, OrebfuscatorConfig.getInitialRadius());
		TByteHashSet blockList = new TByteHashSet();
		int tmp = 0;
		boolean Obfuscate = false;
		boolean useCache = false;
		if(info.chunkSize > info.buffer.length)
			info.buffer = new byte[info.chunkSize];
		else if(info.buffer.length > 16 * 16 * 128)
			info.buffer = new byte[16 * 16 * 128];
		System.arraycopy(info.data, 0, info.buffer, 0, info.chunkSize);
		long hash = Hash(info.buffer);

		//Caching
		if(info.sizeX == 16 && info.sizeY == 128 && info.sizeZ == 16 && OrebfuscatorConfig.getUseCache())
		{
			useCache = true;
			
			long storedHash = cache.getHash();
			if(storedHash != 0L && hash == storedHash)
			{
				byte[] data = cache.getData();
				if(data != null && data.length == info.chunkSize)
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
		
		if(OrebfuscatorConfig.getVerboseMode())
		{
			ChunksCalculated++;
		}
		
		//Calculating
		if (info.sizeY > 1)
		{
			//Index to keep track of blocks
			int index = 0;
			
			//Loop through blocks
			for (int x = 0; x < info.sizeX; x++)
			{
				for (int z = 0; z < info.sizeZ; z++)
				{
					//Shuffle the random blocks
					OrebfuscatorConfig.shuffleRandomBlocks();
					
					for (int y = 0; y < info.sizeY; y++)
					{
						//Initialize objects
						Obfuscate = false;
						blockList.clear();

						//Check if the block should be obfuscated because of being behind stuff
						if(OrebfuscatorConfig.isObfuscated(info.data[index]))
						{
							if(OrebfuscatorConfig.getInitialRadius() == 0)
							{
								//Obfuscate anyways
								Obfuscate = true;
							}
							else
							{
								//Get all block IDs nearby
								Obfuscate = !GetAjacentBlocksTypeID(info, blockList, index, x, y, z, OrebfuscatorConfig.getInitialRadius());
							}
						}
						
						//Check if the block should be obfuscated because of darkness
						if (!Obfuscate && OrebfuscatorConfig.getDarknessHideBlocks() && OrebfuscatorConfig.isDarknessObfuscated(info.data[index]))
						{
							if(OrebfuscatorConfig.getInitialRadius() == 0)
							{
								Obfuscate = true;
							}
							else if(!GetAjacentBlocksHaveLight(info, index, x, y, z, OrebfuscatorConfig.getInitialRadius()))
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

						//Increment index
						index++;
					}
				}
			}
		}
		
		//If cache is allowed
		if(useCache)
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