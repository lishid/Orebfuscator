package lishid.orebfuscator.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Deflater;

import lishid.orebfuscator.Orebfuscator;
import lishid.orebfuscator.OrebfuscatorConfig;

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
	public static ArrayList <Block> GetAjacentBlocks(World world, ArrayList <Block> allBlocks, Block block, int countdown)
	{
		if (allBlocks == null)
			allBlocks = new ArrayList <Block> ();
		
		AddBlockCheck(allBlocks, block);
		
		if (countdown <= 0)
			return allBlocks;

		AddBlockCheck(allBlocks, block.getRelative(BlockFace.UP));
		AddBlockCheck(allBlocks, block.getRelative(BlockFace.DOWN));
		AddBlockCheck(allBlocks, block.getRelative(BlockFace.NORTH));
		AddBlockCheck(allBlocks, block.getRelative(BlockFace.SOUTH));
		AddBlockCheck(allBlocks, block.getRelative(BlockFace.EAST));
		AddBlockCheck(allBlocks, block.getRelative(BlockFace.WEST));

		GetAjacentBlocks(world, allBlocks, block.getRelative(BlockFace.UP), countdown - 1);
		GetAjacentBlocks(world, allBlocks, block.getRelative(BlockFace.DOWN), countdown - 1);
		GetAjacentBlocks(world, allBlocks, block.getRelative(BlockFace.NORTH), countdown - 1);
		GetAjacentBlocks(world, allBlocks, block.getRelative(BlockFace.SOUTH), countdown - 1);
		GetAjacentBlocks(world, allBlocks, block.getRelative(BlockFace.EAST), countdown - 1);
		GetAjacentBlocks(world, allBlocks, block.getRelative(BlockFace.WEST), countdown - 1);
		
		return allBlocks;
	}

	public static void AddBlockCheck(ArrayList <Block> allBlocks, Block block)
	{
		if (block == null) return;
		if (!allBlocks.contains(block) && 
				(OrebfuscatorConfig.isObfuscated((byte)block.getTypeId()) 
						|| OrebfuscatorConfig.isDarknessObfuscated((byte)block.getTypeId())))
		{
			allBlocks.add(block);
		}
	}

	public static void UpdateBlock(Block block)
	{
		if (block == null) return;

        ArrayList<CraftPlayer> players = new ArrayList<CraftPlayer>();
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

	private static boolean isTransparentId(byte id)
	{
		//return id==0;
		return OrebfuscatorConfig.isTransparent(id);
	}
	
	public static ArrayList<Byte> GetAjacentBlocksTypeID(BlockInfo info, ArrayList<Byte> IDPool, int index, int x, int y, int z, int countdown)
	{
		if (IDPool == null)
			IDPool = new ArrayList<Byte>();
		
		byte id = 1;
		
		if(y <= info.sizeY - 1 && 
			y >= 0 && 
			x <= info.sizeX - 1 && 
			x >= 0 && 
			z < info.sizeZ - 1 && 
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

		if(!IDPool.contains(id))
		{
			IDPool.add(id);
		}
		
		if (countdown <= 0)
			return IDPool;
		
		GetAjacentBlocksTypeID(info, IDPool, index + 1, x, y + 1, z, countdown - 1);
		GetAjacentBlocksTypeID(info, IDPool, index - 1, x, y - 1, z, countdown - 1);
		GetAjacentBlocksTypeID(info, IDPool, index + info.sizeY * info.sizeZ, x + 1, y, z, countdown - 1);
		GetAjacentBlocksTypeID(info, IDPool, index - info.sizeY * info.sizeZ, x - 1, y, z, countdown - 1);
		GetAjacentBlocksTypeID(info, IDPool, index + info.sizeY, x, y, z + 1, countdown - 1);
		GetAjacentBlocksTypeID(info, IDPool, index - info.sizeY, x, y, z - 1, countdown - 1);
		
		return IDPool;
	}
	
	public static boolean GetAjacentBlocksHaveLight(BlockInfo info, int index, int x, int y, int z, int countdown)
	{
		if(info.world.getLightLevel(x + info.startX, y + info.startY, z + info.startZ) > 0)
		{
			return true;
		}
		
		if (countdown <= 0)
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
		
		if(((!OrebfuscatorConfig.NoObfuscationForPermission() || !PermissionRelay.hasPermission(player, "Orebfuscator.deobfuscate")) &&
				(!OrebfuscatorConfig.NoObfuscationForOps() || !((Player)player).isOp()) &&
				OrebfuscatorConfig.Enabled()))
		{
			
			info.original = new byte[packet.rawData.length];
			System.arraycopy(packet.rawData, 0, info.original, 0, packet.rawData.length);
			
			if (info.sizeY > 1)
			{
				//The number of blocks
				/*int blocks = info.sizeX * info.sizeY * info.sizeZ;
				
				boolean half_byte = false;
				
				//Lighting data offset
				int light_offset = blocks + blocks / 2;
				
				//Sky-Lighting data offset
				int sky_light_offset = blocks * 2;
				
				//Bytes to store lighting levels
				info.lightingArray = new byte[blocks];
				
				//Get lighting for every block
				for (int index = 0; index < blocks; index++)
				{
					int lighting;
					//Sky lighting -> stored in first nibble
					if (!half_byte)
					{
						//First nibble
						lighting = packet.rawData[sky_light_offset] & 0x0F;
					}
					else
					{
						//Last nibble
						lighting = packet.rawData[sky_light_offset] >> 4;
						//Next byte
						sky_light_offset++;
					}
	
					//Lighting -> stored in last nibble
					if (!half_byte)
					{
						//First nibble
						lighting |= (packet.rawData[light_offset] & 0x0F) << 4;
					}
					else
					{
						//Last nibble
						lighting |= packet.rawData[light_offset] & 0xF0;
						//Next byte
						light_offset++;
					}
					
					//if (lighting <= 0);
					info.lightingArray[index] = (byte)lighting;
					
					//Next half byte
					half_byte = !half_byte;
				}
				*/
				//For every block
				for (int x = 0; x < info.sizeX; x++)
				{
					for (int z = 0; z < info.sizeZ; z++)
					{
						for (int y = 0; y < info.sizeY; y++)
						{
							//Get block index
							int index = y + z * info.sizeY + x * info.sizeY * info.sizeZ;
							
							boolean Obfuscate = false;
							
							//Check if the block belongs to obfuscated blocks
							if(OrebfuscatorConfig.isObfuscated(info.original[index]))
							{
								//Get all block IDs nearby
								ArrayList<Byte> IDs = GetAjacentBlocksTypeID(info, null, index, x, y, z, OrebfuscatorConfig.InitialRadius());
								
								Obfuscate = true;
								
								//Go through to see if we should hide the block
								for(byte id : IDs)
								{
									//Transparent block found nearby, do not hide
									if(isTransparentId(id))
									{
										Obfuscate = false;
										break;
									}
								}
							}
							
							if (OrebfuscatorConfig.DarknessHideBlocks() && OrebfuscatorConfig.isDarknessObfuscated(info.original[index]))
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
						}
					}
				}
			}
		}
		
		//Conpression
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
		info.startX = packet.a;
		info.startY = packet.b;
		info.startZ = packet.c;
		info.sizeX = packet.d;
		info.sizeY = packet.e;
		info.sizeZ = packet.f;

		@SuppressWarnings({ "rawtypes" })
		List list = info.world.getTileEntities(info.startX, info.startY, info.startZ, info.startX + info.sizeX, info.startY + info.sizeY, info.startZ + info.sizeZ);
        for (int i = 0; i < list.size(); ++i) {
        	TileEntity tileentity = (TileEntity) list.get(i);
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
}