/**
 * @author Aleksey Terzi
 *
 */

package com.lishid.orebfuscator.obfuscation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.World;
import org.bukkit.block.Block;

public class ProximityHiderPlayer {
	private World world; 
	private Map<Long, ArrayList<Block>> chunks;
	
	public ProximityHiderPlayer(World world) {
		this.world = world;
		this.chunks = new HashMap<Long, ArrayList<Block>>();
	}
	
	public World getWorld() {
		return this.world;
	}
	
	public void setWorld(World world) {
		this.world = world;
	}
	
	public void clearChunks() {
		this.chunks.clear();
	}
	
	public void putBlocks(int chunkX, int chunkZ, ArrayList<Block> blocks) {
		long key = getKey(chunkX, chunkZ);
		this.chunks.put(key, blocks);
	}
	
	public ArrayList<Block> getBlocks(int chunkX, int chunkZ) {
		long key = getKey(chunkX, chunkZ);
		return this.chunks.get(key);
	}
	
	public void copyChunks(ProximityHiderPlayer playerInfo) {
		this.chunks.putAll(playerInfo.chunks);
	}
	
	public void removeChunk(int chunkX, int chunkZ) {
		long key = getKey(chunkX, chunkZ);
		this.chunks.remove(key);
	}
	
	private static long getKey(int chunkX, int chunkZ) {
		return ((chunkZ & 0xffffffffL) << 32) | (chunkX & 0xffffffffL);
	}
}
