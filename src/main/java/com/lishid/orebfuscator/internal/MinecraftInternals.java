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

package com.lishid.orebfuscator.internal;

import net.minecraft.server.v1_9_R2.Block;
import net.minecraft.server.v1_9_R2.BlockPosition;
import net.minecraft.server.v1_9_R2.Chunk;
import net.minecraft.server.v1_9_R2.ChunkProviderServer;
import net.minecraft.server.v1_9_R2.IBlockData;
import net.minecraft.server.v1_9_R2.Packet;
import net.minecraft.server.v1_9_R2.TileEntity;
import net.minecraft.server.v1_9_R2.WorldServer;

import org.bukkit.World;
import org.bukkit.craftbukkit.v1_9_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

import com.lishid.orebfuscator.chunkmap.BlockState;

public class MinecraftInternals {
    public static void updateBlockTileEntity(BlockCoord blockCoord, Player player) {
        CraftWorld world = (CraftWorld) player.getWorld();
        TileEntity tileEntity = world.getTileEntityAt(blockCoord.x, blockCoord.y, blockCoord.y);
        
        if (tileEntity == null) {
            return;
        }
        
        Packet<?> packet = tileEntity.getUpdatePacket();
        if (packet != null) {
            CraftPlayer player2 = (CraftPlayer) player;
            player2.getHandle().playerConnection.sendPacket(packet);
        }
    }

    public static void notifyBlockChange(org.bukkit.World world, BlockInfo blockInfo) {
    	BlockPosition blockPosition = new BlockPosition(blockInfo.x, blockInfo.y, blockInfo.z);
    	
        ((CraftWorld) world).getHandle().notify(blockPosition, blockInfo.blockData, blockInfo.blockData, 0);
    }
    
    public static int getBlockLightLevel(World world, int x, int y, int z) {
		return ((CraftWorld)world).getHandle().getLightLevel(new BlockPosition(x, y, z));
    }
    
	public static BlockInfo getBlockInfo(World world, int x, int y, int z) {
		IBlockData blockData = getBlockData(world, x, y, z);
		
		if(blockData == null) return null;
		
		BlockInfo block = new BlockInfo();
		block.x = x;
		block.y = y;
		block.z = z;
		block.blockData = blockData;
		
		return block;
	}
	
	public static BlockState getBlockState(World world, int x, int y, int z) {
		IBlockData blockData = getBlockData(world, x, y, z);
		
		if(blockData == null) return null;
		
		Block block = blockData.getBlock();
		
		BlockState blockState = new BlockState();
		blockState.id = Block.getId(block);
		blockState.meta = block.toLegacyData(blockData);
		
		return blockState;
	}
	
	public static int getBlockId(World world, int x, int y, int z) {
		IBlockData blockData = getBlockData(world, x, y, z);
		
		return blockData != null ? Block.getId(blockData.getBlock()): -1;
	}
	
	public static IBlockData getBlockData(World world, int x, int y, int z) {
		int chunkX = x >> 4;
		int chunkZ = z >> 4;

		WorldServer worldServer = ((CraftWorld)world).getHandle();
		ChunkProviderServer chunkProviderServer = worldServer.getChunkProviderServer();
		
		if(!chunkProviderServer.isLoaded(chunkX, chunkZ)) return null;
		
		Chunk chunk = chunkProviderServer.getOrLoadChunkAt(chunkX, chunkZ);
		
		return chunk.getBlockData(new BlockPosition(x, y, z));
	}
}