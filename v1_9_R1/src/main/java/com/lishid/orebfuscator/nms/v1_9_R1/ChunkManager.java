/**
 * @author Aleksey Terzi
 *
 */

package com.lishid.orebfuscator.nms.v1_9_R1;

import java.util.HashSet;

import net.minecraft.server.v1_9_R1.BlockPosition;
import net.minecraft.server.v1_9_R1.Blocks;
import net.minecraft.server.v1_9_R1.ChunkProviderServer;
import net.minecraft.server.v1_9_R1.EntityPlayer;
import net.minecraft.server.v1_9_R1.PlayerChunk;
import net.minecraft.server.v1_9_R1.PlayerChunkMap;
import net.minecraft.server.v1_9_R1.WorldServer;

import org.bukkit.entity.Player;

import com.lishid.orebfuscator.nms.IChunkManager;

public class ChunkManager implements IChunkManager {
	private PlayerChunkMap chunkMap;
	private ChunkProviderServer chunkProvider;
	
	public ChunkManager(PlayerChunkMap chunkMap) {
		this.chunkMap = chunkMap;
		this.chunkProvider = this.chunkMap.getWorld().getChunkProviderServer(); 
	}
	
	public boolean canResendChunk(int chunkX, int chunkZ) {
		if(!this.chunkProvider.isChunkLoaded(chunkX, chunkZ) || !this.chunkMap.isChunkInUse(chunkX, chunkZ)) return false;
		
		PlayerChunk playerChunk = this.chunkMap.b(chunkX, chunkZ);
		
		return playerChunk != null && playerChunk.chunk != null && playerChunk.chunk.isReady();
	}
	
	public void resendChunk(int chunkX, int chunkZ, HashSet<Player> affectedPlayers) {
		if(!this.chunkMap.isChunkInUse(chunkX, chunkZ)) return;
		
		PlayerChunk playerChunk = this.chunkMap.b(chunkX, chunkZ);
		
		if(playerChunk == null || playerChunk.chunk == null || !playerChunk.chunk.isReady()) return;
		
		WorldServer world = this.chunkMap.getWorld();
		
		int px = chunkX << 4;
		int pz = chunkZ << 4;
		      
		int height = world.getHeight() / 16;
		
		for (int idx = 0; idx < 64; idx++) {
			world.notify(new BlockPosition(px + idx / height, idx % height * 16, pz), Blocks.AIR.getBlockData(), Blocks.STONE.getBlockData(), 3);
		}
		world.notify(new BlockPosition(px + 15, height * 16 - 1, pz + 15), Blocks.AIR.getBlockData(), Blocks.STONE.getBlockData(), 3);
		
		for(EntityPlayer player : playerChunk.c) {
			affectedPlayers.add(player.getBukkitEntity());
		}
	}
}