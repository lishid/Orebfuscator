package lishid.orebfuscator.utils;

import gnu.trove.set.hash.TByteHashSet;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.zip.Deflater;

import lishid.orebfuscator.Orebfuscator;

import net.minecraft.server.NetServerHandler;
import net.minecraft.server.Packet;
import net.minecraft.server.Packet51MapChunk;
import net.minecraft.server.TileEntity;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class Calculations
{
	public static void UpdateBlocksNearby(Block block)
	{
        if (!OrebfuscatorConfig.Enabled() || 
        		OrebfuscatorConfig.isTransparent((byte)block.getTypeId()))
        	return;
        
        HashSet<Block> blocks = Calculations.GetAjacentBlocks(block.getWorld(),
        		new HashSet<Block>(), block, OrebfuscatorConfig.UpdateRadius());
        
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
		
		if(CheckID(IDPool, id))
			return true;
		
		if (countdown == 0)
			return false;
		
		if(GetAjacentBlocksTypeID(info, IDPool, index + 1, x, y + 1, z, countdown - 1))
			return true;
		if(GetAjacentBlocksTypeID(info, IDPool, index - 1, x, y - 1, z, countdown - 1))
			return true;
		if(GetAjacentBlocksTypeID(info, IDPool, index + info.sizeY * info.sizeZ, x + 1, y, z, countdown - 1)) 
			return true;
		if(GetAjacentBlocksTypeID(info, IDPool, index - info.sizeY * info.sizeZ, x - 1, y, z, countdown - 1))
			return true;
		if(GetAjacentBlocksTypeID(info, IDPool, index + info.sizeY, x, y, z + 1, countdown - 1))
			return true;
		if(GetAjacentBlocksTypeID(info, IDPool, index - info.sizeY, x, y, z - 1, countdown - 1))
			return true;
		
		return false;
	}
	
	private static boolean CheckID(TByteHashSet IDPool, byte id)
	{
		if(IDPool.contains(id))
			return false;
		if(OrebfuscatorConfig.isTransparent(id))
			return true;
		IDPool.add(id);
		return false;
	}
	
	public static boolean GetAjacentBlocksHaveLight(BlockInfo info, int index, int x, int y, int z, int countdown)
	{
		if(info.world.getLightLevel(x + info.startX, y + info.startY, z + info.startZ) > 0)
			return true;
		
		if (countdown == 0)
			return false;

		if(GetAjacentBlocksHaveLight(info, index + 1, x, y + 1, z, countdown - 1))
			return true;
		if(GetAjacentBlocksHaveLight(info, index - 1, x, y - 1, z, countdown - 1))
			return true;
		if(GetAjacentBlocksHaveLight(info, index + info.sizeY * info.sizeZ, x + 1, y, z, countdown - 1))
			return true;
		if(GetAjacentBlocksHaveLight(info, index - info.sizeY * info.sizeZ, x - 1, y, z, countdown - 1))
			return true;
		if(GetAjacentBlocksHaveLight(info, index + info.sizeY, x, y, z + 1, countdown - 1))
			return true;
		if(GetAjacentBlocksHaveLight(info, index - info.sizeY, x, y, z - 1, countdown - 1))
			return true;
			
		return false;
	}

	public static void Obfuscate(Packet51MapChunk packet, CraftPlayer player)
	{
		NetServerHandler handler = player.getHandle().netServerHandler;
		if(!Orebfuscator.usingSpout)
		{
			packet.k = false;
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

		if(((!OrebfuscatorConfig.NoObfuscationForPermission() || !PermissionRelay.hasPermission(player, "Orebfuscator.deobfuscate")) &&
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
								//Get all block IDs nearby
								Obfuscate = !GetAjacentBlocksTypeID(info, blockList, index, x, y, z, OrebfuscatorConfig.InitialRadius());
							}
							
							if (!Obfuscate && OrebfuscatorConfig.DarknessHideBlocks() && OrebfuscatorConfig.isDarknessObfuscated(info.original[index]))
							{
								if(!GetAjacentBlocksHaveLight(info, index, x, y, z, OrebfuscatorConfig.InitialRadius()))
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
		Deflater deflater = new Deflater();
		byte[] deflateBuffer = new byte[82020];

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

	    packet.g = new byte[size];
	    packet.h = size;
	    System.arraycopy(deflateBuffer, 0, packet.g, 0, size);
		
		//Send it
		handler.sendPacket(packet);
		
		//Send packets for sign changes
		Object[] list = info.world.getTileEntities(info.startX, info.startY, info.startZ, info.startX + info.sizeX, info.startY + info.sizeY, info.startZ + info.sizeZ).toArray();
        for (int i = 0; i < list.length; ++i) {
        	TileEntity tileentity = (TileEntity) list[i];
            if (tileentity != null) {
            	Packet p = tileentity.l();
            	if(p!=null)
            	{
            		handler.sendPacket(p);
            	}
            }
        }
	}

	public static void LightingUpdate(Block block, boolean skipCheck)
	{
		if(OrebfuscatorConfig.emitsLight((byte)block.getTypeId()) || skipCheck)
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
		}
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