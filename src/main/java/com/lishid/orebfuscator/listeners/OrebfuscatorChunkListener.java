/**
 * @author Aleksey Terzi
 *
 */

package com.lishid.orebfuscator.listeners;

import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import com.lishid.orebfuscator.obfuscation.ChunkReloader;

public class OrebfuscatorChunkListener implements Listener {
	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled = true)
	public void onChunkLoad(ChunkLoadEvent event) {
		Chunk chunk = event.getChunk();
		ChunkReloader.addLoadedChunk(event.getWorld(), chunk.getX(), chunk.getZ());
		
		//Orebfuscator.log("Chunk x = " + chunk.getX() + ", z = " + chunk.getZ() + " is loaded");/*debug*/
	}

	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled = true)
	public void onChunkUnload(ChunkUnloadEvent event) {
		Chunk chunk = event.getChunk();
		ChunkReloader.addUnloadedChunk(event.getWorld(), chunk.getX(), chunk.getZ());
		
		//Orebfuscator.log("Chunk x = " + chunk.getX() + ", z = " + chunk.getZ() + " is unloaded");/*debug*/
	}
}
