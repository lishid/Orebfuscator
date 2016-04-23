/**
 * @author Aleksey Terzi
 *
 */

package com.lishid.orebfuscator.obfuscation;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.World;

public class ProximityHiderPlayer {
	public World world; 
	public Set<ProximityHiderBlock> blocks;
	
	public ProximityHiderPlayer(World world) {
		this.world = world;
		this.blocks = new HashSet<ProximityHiderBlock>();
	}
}
