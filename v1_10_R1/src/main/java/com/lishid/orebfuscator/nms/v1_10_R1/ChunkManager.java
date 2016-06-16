/**
 * @author Aleksey Terzi
 *
 */

package com.lishid.orebfuscator.nms.v1_10_R1;

import java.util.HashSet;

import net.minecraft.server.v1_10_R1.ChunkProviderServer;
import net.minecraft.server.v1_10_R1.EntityPlayer;
import net.minecraft.server.v1_10_R1.PacketPlayOutMapChunk;
import net.minecraft.server.v1_10_R1.PacketPlayOutUnloadChunk;
import net.minecraft.server.v1_10_R1.PlayerChunk;
import net.minecraft.server.v1_10_R1.PlayerChunkMap;

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
		if(!this.chunkProvider.isLoaded(chunkX, chunkZ) || !this.chunkMap.isChunkInUse(chunkX, chunkZ)) return false;
		
		PlayerChunk playerChunk = this.chunkMap.getChunk(chunkX, chunkZ);
		
		return playerChunk != null && playerChunk.chunk != null && playerChunk.chunk.isReady();	
	}
	
	public void resendChunk(int chunkX, int chunkZ, HashSet<Player> affectedPlayers) {
		if(!this.chunkMap.isChunkInUse(chunkX, chunkZ)) return;
		
		PlayerChunk playerChunk = this.chunkMap.getChunk(chunkX, chunkZ);
		
		if(playerChunk == null || playerChunk.chunk == null || !playerChunk.chunk.isReady()) return;
		
		for(EntityPlayer player : playerChunk.c) {
			player.playerConnection.sendPacket(new PacketPlayOutUnloadChunk(chunkX, chunkZ));
			player.playerConnection.sendPacket(new PacketPlayOutMapChunk(playerChunk.chunk, 0xffff));
			
			affectedPlayers.add(player.getBukkitEntity());
		}
	}
}
