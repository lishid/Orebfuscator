/**
 * @author lishid
 * @author Aleksey Terzi
 *
 */

package com.lishid.orebfuscator.nms.v1_13_R1;

import net.minecraft.server.v1_13_R1.Block;
import net.minecraft.server.v1_13_R1.BlockPosition;
import net.minecraft.server.v1_13_R1.Chunk;
import net.minecraft.server.v1_13_R1.ChunkProviderServer;
import net.minecraft.server.v1_13_R1.IBlockData;
import net.minecraft.server.v1_13_R1.IChatBaseComponent;
import net.minecraft.server.v1_13_R1.Item;
import net.minecraft.server.v1_13_R1.Packet;
import net.minecraft.server.v1_13_R1.TileEntity;
import net.minecraft.server.v1_13_R1.WorldServer;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_13_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_13_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_13_R1.util.CraftChatMessage;
import org.bukkit.craftbukkit.v1_13_R1.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.v1_13_R1.block.data.CraftBlockData;
import org.bukkit.entity.Player;

import com.lishid.orebfuscator.nms.IBlockInfo;
import com.lishid.orebfuscator.nms.IChunkCache;
import com.lishid.orebfuscator.nms.INBT;
import com.lishid.orebfuscator.nms.INmsManager;
import com.lishid.orebfuscator.types.BlockCoord;
import com.lishid.orebfuscator.types.BlockState;

public class NmsManager implements INmsManager {
	private int maxLoadedCacheFiles;
	
	public void setMaxLoadedCacheFiles(int value) {
		this.maxLoadedCacheFiles = value;
	}
	
	public INBT createNBT() {
		return new NBT();
	}
	
	@Override
	public IChunkCache createChunkCache() {
		return new ChunkCache(this.maxLoadedCacheFiles);
	}
	
	@Override
    public void updateBlockTileEntity(BlockCoord blockCoord, Player player) {
        CraftWorld world = (CraftWorld)player.getWorld();
        TileEntity tileEntity = world.getTileEntityAt(blockCoord.x, blockCoord.y, blockCoord.z);
        
        if (tileEntity == null) {
            return;
        }
        
        Packet<?> packet = tileEntity.getUpdatePacket();
        
        if (packet != null) {
            CraftPlayer player2 = (CraftPlayer)player;
            player2.getHandle().playerConnection.sendPacket(packet);
        }
    }

	@Override
    public void notifyBlockChange(World world, IBlockInfo blockInfo) {
    	BlockPosition blockPosition = new BlockPosition(blockInfo.getX(), blockInfo.getY(), blockInfo.getZ());
    	IBlockData blockData = ((BlockInfo)blockInfo).getBlockData();
    	
        ((CraftWorld)world).getHandle().notify(blockPosition, blockData, blockData, 0);
    }
    
	@Override
    public int getBlockLightLevel(World world, int x, int y, int z) {
		return ((CraftWorld)world).getHandle().getLightLevel(new BlockPosition(x, y, z));
    }
    
	@Override
	public void setBlockStateFromID(int id, BlockState blockState) {
		blockState.id = id;
		IBlockData block = Block.getByCombinedId(id);
		CraftBlockData cBlock = CraftBlockData.fromData(block); 
		blockState.type = cBlock.getMaterial();
	}
	
	@Override
	public void setBlockStateFromMaterial(Material type, BlockState blockState) {
		blockState.type = type;
		if (type.isBlock()) {
			Block block = CraftMagicNumbers.getBlock(type);
			blockState.id = Block.getCombinedId(block.getBlockData());
		} else {
			Item item = CraftMagicNumbers.getItem(type);
			blockState.id = Item.getId(item);
		}
	}
	
	@Override
	public IBlockInfo getBlockInfo(World world, int x, int y, int z) {
		IBlockData blockData = getBlockData(world, x, y, z, false);
		
		return blockData != null
				? new BlockInfo(x, y, z, blockData)
				: null;
	}
	
	@Override
	public BlockState getBlockState(World world, int x, int y, int z) {
		IBlockData blockData = getBlockData(world, x, y, z, false);
		
		if(blockData == null) return null;
		
		//Block block = blockData.getBlock();
		
		BlockState blockState = new BlockState();
		blockState.type = world.getBlockAt(x, y, z).getType();
		blockState.id = Block.getCombinedId(blockData);
		//blockState.meta = block.toLegacyData(blockData);
		
		return blockState;
	}
	
	@Override
	public int getBlockId(World world, int x, int y, int z) {
		IBlockData blockData = getBlockData(world, x, y, z, false);
		return blockData != null ? Block.getCombinedId(blockData): -1;
	}

	@Override
	public BlockData getBlockDataFromBlockState(BlockState blockState) {
		IBlockData block = Block.getByCombinedId(blockState.id);
		CraftBlockData cBlock = CraftBlockData.fromData(block); 		
		return cBlock;
	}
	
	@Override
	public int loadChunkAndGetBlockId(World world, int x, int y, int z) {
		IBlockData blockData = getBlockData(world, x, y, z, true);
		return blockData != null ? Block.getCombinedId(blockData): -1;
	}
	
	@Override
	public String getTextFromChatComponent(String json) {
		IChatBaseComponent component = IChatBaseComponent.ChatSerializer.a(json);
		return CraftChatMessage.fromComponent(component);
	}
	
	private static IBlockData getBlockData(World world, int x, int y, int z, boolean loadChunk) {
		int chunkX = x >> 4;
		int chunkZ = z >> 4;

		WorldServer worldServer = ((CraftWorld)world).getHandle();
		ChunkProviderServer chunkProviderServer = worldServer.getChunkProviderServer();
		
		if(!loadChunk && !chunkProviderServer.isLoaded(chunkX, chunkZ)) return null;
		
		Chunk chunk = chunkProviderServer.getOrLoadChunkAt(chunkX, chunkZ);
		
		return chunk != null ? chunk.getType(new BlockPosition(x, y, z)) : null;
	}
}
