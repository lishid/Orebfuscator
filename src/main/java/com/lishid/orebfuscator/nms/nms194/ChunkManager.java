/**
 * @author Aleksey Terzi
 *
 */

package com.lishid.orebfuscator.nms.nms194;

import net.minecraft.server.v1_9_R2.EntityPlayer;
import net.minecraft.server.v1_9_R2.PacketPlayOutMapChunk;
import net.minecraft.server.v1_9_R2.PacketPlayOutUnloadChunk;
import net.minecraft.server.v1_9_R2.PlayerChunk;
import net.minecraft.server.v1_9_R2.PlayerChunkMap;

import com.lishid.orebfuscator.nms.IChunkManager;

public class ChunkManager implements IChunkManager {
	private PlayerChunkMap chunkMap;
	
	public ChunkManager(PlayerChunkMap chunkMap) {
		this.chunkMap = chunkMap;
	}
	
	@Override
	public boolean canResendChunk(int chunkX, int chunkZ) {
		if(!this.chunkMap.isChunkInUse(chunkX, chunkZ)) return false;
		
		PlayerChunk playerChunk = this.chunkMap.getChunk(chunkX, chunkZ);
		
		return playerChunk != null && playerChunk.chunk != null && !playerChunk.chunk.isReady();		
	}
	
	@Override
	public void resendChunk(int chunkX, int chunkZ) {
		if(!this.chunkMap.isChunkInUse(chunkX, chunkZ)) return;
		
		PlayerChunk playerChunk = this.chunkMap.getChunk(chunkX, chunkZ);
		
		if(playerChunk == null || playerChunk.chunk == null || !playerChunk.chunk.isReady()) return;
		
		for(EntityPlayer player : playerChunk.c) {
			player.playerConnection.sendPacket(new PacketPlayOutUnloadChunk(chunkX, chunkZ));
			player.playerConnection.sendPacket(new PacketPlayOutMapChunk(playerChunk.chunk, 0xffff));
		}
	}
}
