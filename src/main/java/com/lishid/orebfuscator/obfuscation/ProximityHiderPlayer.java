/**
 * @author Aleksey Terzi
 *
 */

package com.lishid.orebfuscator.obfuscation;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.block.Block;

public class ProximityHiderPlayer {
	public UUID worldUID; 
	public Set<Block> blocks;
	
	public ProximityHiderPlayer(UUID worldUID) {
		this.worldUID = worldUID;
		this.blocks = new HashSet<Block>();
	}
}
