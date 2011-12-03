package lishid.orebfuscator.utils;

import gnu.trove.set.hash.TByteHashSet;

import java.lang.reflect.Field;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.zip.Deflater;

import lishid.orebfuscator.Orebfuscator;

import net.minecraft.server.NetServerHandler;
import net.minecraft.server.NetworkManager;
import net.minecraft.server.Packet;
import net.minecraft.server.Packet51MapChunk;
import net.minecraft.server.TileEntity;

import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class Calculations
{
    private final static int CHUNK_SIZE = 16 * 128 * 16 * 5 / 2;
    private final static int REDUCED_DEFLATE_THRESHOLD = CHUNK_SIZE / 4;
    private final static int DEFLATE_LEVEL_CHUNKS = 6;
    private final static int DEFLATE_LEVEL_PARTS = 1;
    
	private static Deflater deflater = new Deflater();
    private static byte[] deflateBuffer = new byte[CHUNK_SIZE + 100];
	
	public static void UpdateBlocksNearby(Block block)
	{
        if (!OrebfuscatorConfig.Enabled() || 
        		OrebfuscatorConfig.isTransparent((byte)block.getTypeId()))
        	return;
        
        HashSet<Block> blocks = Calculations.GetAjacentBlocks(block.getWorld(),
        		new HashSet<Block>(), block, OrebfuscatorConfig.UpdateRadius());
        
        Calculations.UpdateBlock(block);
        
        for(Block nearbyBlock : blocks)
        {
        	Calculations.UpdateBlock(nearbyBlock);
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

	public static void UpdateBlock(Block block)
	{
		if (block == null) return;

        HashSet<CraftPlayer> players = new HashSet<CraftPlayer>();
        for (Player player : block.getWorld().getPlayers()) {
            if ((Math.abs(player.getLocation().getX() - block.getX()) < 176) &&
            		(Math.abs(player.getLocation().getZ() - block.getZ()) < 176)) {
            	players.add((CraftPlayer) player);
            }
        }
        
        for (CraftPlayer player : players) {
            player.sendBlockChange(block.getLocation(), block.getTypeId(), block.getData());
        }
	}
	
	public static boolean GetAjacentBlocksTypeID(BlockInfo info, TByteHashSet IDPool, int index, int x, int y, int z, int countdown)
	{
		byte id = 0;
		
		if(y > 126)
			return true;
		
		if(y < info.sizeY && 
			y >= 0 && 
			x < info.sizeX && 
			x >= 0 && 
			z < info.sizeZ && 
			z >= 0 &&
			index > 0 &&
			info.original.length > index)
		{
			id = info.original[index];
		}
		else
		{
			if(info.startY >= 0)
			{
				id = (byte)info.world.getTypeId(x + info.startX, y + info.startY, z + info.startZ);
			}
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
	
	public static boolean GetAjacentBlocksHaveLight(BlockInfo info, int index, int x, int y, int z, int countdown)
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

	public static void Obfuscate(Packet51MapChunk packet, CraftPlayer player)
	{
		NetServerHandler handler = player.getHandle().netServerHandler;
		if(!Orebfuscator.usingSpout)
		{
			packet.l = false;
		}
		
		//String hash = MD5(packet.rawData);
		
		BlockInfo info = new BlockInfo();
		info.world = player.getHandle().world.getWorld().getHandle();
		info.startX = packet.a;
		info.startY = packet.b;
		info.startZ = packet.c;
		info.sizeX = packet.d;
		info.sizeY = packet.e;
		info.sizeZ = packet.f;
		
		TByteHashSet blockList = new TByteHashSet();
		
		if(info.world.getWorld().getEnvironment() == Environment.NORMAL &&
			!OrebfuscatorConfig.worldDisabled(info.world.getServer().getName()) &&
				((!OrebfuscatorConfig.NoObfuscationForPermission() || !PermissionRelay.hasPermission(player, "Orebfuscator.deobfuscate")) &&
				(!OrebfuscatorConfig.NoObfuscationForOps() || !((Player)player).isOp()) &&
				OrebfuscatorConfig.Enabled()))
		{
			info.original = new byte[packet.rawData.length];
			System.arraycopy(packet.rawData, 0, info.original, 0, packet.rawData.length);
			
			if (info.sizeY > 1)
			{
				int index = 0;
				//For every block
				for (int x = 0; x < info.sizeX; x++)
				{
					for (int z = 0; z < info.sizeZ; z++)
					{
						for (int y = 0; y < info.sizeY; y++)
						{
							boolean Obfuscate = false;
							blockList.clear();
							
							
							//Check if the block belongs to obfuscated blocks
							if(OrebfuscatorConfig.isObfuscated(info.original[index]))
							{
								if(OrebfuscatorConfig.InitialRadius() == 0)
								{
									Obfuscate = true;
								}
								else
								{
									//Get all block IDs nearby
									Obfuscate = !GetAjacentBlocksTypeID(info, blockList, index, x, y, z, OrebfuscatorConfig.InitialRadius());
								}
							}
							
							if (!Obfuscate && OrebfuscatorConfig.DarknessHideBlocks() && OrebfuscatorConfig.isDarknessObfuscated(info.original[index]))
							{
								if(OrebfuscatorConfig.InitialRadius() == 0)
								{
									Obfuscate = true;
								}
								else if(!GetAjacentBlocksHaveLight(info, index, x, y, z, OrebfuscatorConfig.InitialRadius()))
								{
									Obfuscate = true;
								}
							}
							
							if(Obfuscate)
							{
								//Hide this block
								if(OrebfuscatorConfig.EngineMode() == 1)
								{
									//Engine mode 1, replace with stone
									packet.rawData[index] = 1;
								}
								else if(OrebfuscatorConfig.EngineMode() == 2)
								{
									//Ending mode 2, replace with random block
									packet.rawData[index] = OrebfuscatorConfig.GenerateRandomBlock();
								}
							}

							//Increment index
							index++;
						}
					}
				}
			}
		}
		
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
        packet.g = new byte[size];
        packet.h = size;
        System.arraycopy(deflateBuffer, 0, packet.g, 0, size);
		
		//Send it
        while(!GetNetworkManagerQueue(handler.networkManager, 1048576 - 2*(18 + packet.h)))
        {
        	try
        	{
        		Thread.sleep(5);
        	}catch(Exception e){}
        }
		handler.networkManager.queue(packet);
		
		//Send packets for sign changes
		Object[] list = info.world.getTileEntities(info.startX, info.startY, info.startZ, info.startX + info.sizeX, info.startY + info.sizeY, info.startZ + info.sizeZ).toArray();
        for (int i = 0; i < list.length; ++i) {
        	TileEntity tileentity = (TileEntity) list[i];
            if (tileentity != null) {
            	Packet p = tileentity.k();
            	if(p!=null)
            	{
            		handler.sendPacket(p);
            	}
            }
        }
	}
	
	public static boolean GetNetworkManagerQueue(NetworkManager networkManager, int number)
	{
		try {
	        Field p = networkManager.getClass().getDeclaredField("x");
			p.setAccessible(true);
			return (Integer.parseInt(p.get(networkManager).toString()) < number);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public static void LightingUpdate(Block block, boolean skipCheck)
	{/*
		if(skipCheck || OrebfuscatorConfig.emitsLight((byte)block.getTypeId()))
		{
		    int x = block.getWorld().getChunkAt(block.getLocation()).getX();
		    int z = block.getWorld().getChunkAt(block.getLocation()).getZ();
		    block.getWorld().refreshChunk(x, z);
		    block.getWorld().refreshChunk(x, z+1);
		    block.getWorld().refreshChunk(x, z-1);
		    block.getWorld().refreshChunk(x+1, z);
		    block.getWorld().refreshChunk(x+1, z+1);
		    block.getWorld().refreshChunk(x+1, z-1);
		    block.getWorld().refreshChunk(x-1, z);
		    block.getWorld().refreshChunk(x-1, z+1);
		    block.getWorld().refreshChunk(x-1, z-1);
		}*/
	}
	
	public static String MD5(byte[] data)
	{
		try{
			MessageDigest algorithm = MessageDigest.getInstance("MD5");
			algorithm.reset();
			algorithm.update(data);
			byte messageDigest[] = algorithm.digest();
			StringBuffer hexString = new StringBuffer();
			for (int i=0;i<messageDigest.length;i++) {
				hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
			}
			return hexString.toString();
		}catch(NoSuchAlgorithmException e){
			e.printStackTrace();
		}
		return "";
	}
	
	public int getIndex(int x, int y, int z) {
		return (x & 0xF) << 11 | (z & 0xF) << 7 | (y & 0x7F);
	}
}