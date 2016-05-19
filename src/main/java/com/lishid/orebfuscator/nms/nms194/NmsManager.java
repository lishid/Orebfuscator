/**
 * @author Aleksey Terzi
 *
 */

package com.lishid.orebfuscator.nms.nms194;

import net.minecraft.server.v1_9_R2.Block;
import net.minecraft.server.v1_9_R2.BlockPosition;
import net.minecraft.server.v1_9_R2.Chunk;
import net.minecraft.server.v1_9_R2.ChunkProviderServer;
import net.minecraft.server.v1_9_R2.IBlockData;
import net.minecraft.server.v1_9_R2.Packet;
import net.minecraft.server.v1_9_R2.PlayerChunkMap;
import net.minecraft.server.v1_9_R2.TileEntity;
import net.minecraft.server.v1_9_R2.WorldServer;

import org.bukkit.World;
import org.bukkit.craftbukkit.v1_9_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

import com.lishid.orebfuscator.nms.IBlockInfo;
import com.lishid.orebfuscator.nms.IChunkCache;
import com.lishid.orebfuscator.nms.IChunkManager;
import com.lishid.orebfuscator.nms.INBT;
import com.lishid.orebfuscator.nms.INmsManager;
import com.lishid.orebfuscator.types.BlockCoord;
import com.lishid.orebfuscator.types.BlockState;

public class NmsManager implements INmsManager {
	@Override
	public INBT createNBT() {
		return new NBT();
	}
	
	@Override
	public IChunkCache createChunkCache() {
		return new ChunkCache();
	}
	
	@Override
	public IChunkManager getChunkManager(World world) {
    	WorldServer worldServer = ((CraftWorld)world).getHandle();
    	PlayerChunkMap chunkMap = worldServer.getPlayerChunkMap();
    	
    	return new ChunkManager(chunkMap);
	}
	
	@Override
    public void updateBlockTileEntity(BlockCoord blockCoord, Player player) {
        CraftWorld world = (CraftWorld)player.getWorld();
        TileEntity tileEntity = world.getTileEntityAt(blockCoord.x, blockCoord.y, blockCoord.y);
        
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
	public IBlockInfo getBlockInfo(World world, int x, int y, int z) {
		IBlockData blockData = getBlockData(world, x, y, z);
		
		return blockData != null
				? new BlockInfo(x, y, z, blockData)
				: null;
	}
	
	@Override
	public BlockState getBlockState(World world, int x, int y, int z) {
		IBlockData blockData = getBlockData(world, x, y, z);
		
		if(blockData == null) return null;
		
		Block block = blockData.getBlock();
		
		BlockState blockState = new BlockState();
		blockState.id = Block.getId(block);
		blockState.meta = block.toLegacyData(blockData);
		
		return blockState;
	}
	
	@Override
	public int getBlockId(World world, int x, int y, int z) {
		IBlockData blockData = getBlockData(world, x, y, z);
		
		return blockData != null ? Block.getId(blockData.getBlock()): -1;
	}
	
	private static IBlockData getBlockData(World world, int x, int y, int z) {
		int chunkX = x >> 4;
		int chunkZ = z >> 4;

		WorldServer worldServer = ((CraftWorld)world).getHandle();
		ChunkProviderServer chunkProviderServer = worldServer.getChunkProviderServer();
		
		if(!chunkProviderServer.isLoaded(chunkX, chunkZ)) return null;
		
		Chunk chunk = chunkProviderServer.getOrLoadChunkAt(chunkX, chunkZ);
		
		return chunk.getBlockData(new BlockPosition(x, y, z));
	}
}
