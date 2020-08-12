package net.imprex.orebfuscator.config;

import java.util.List;

import org.bukkit.World;

public interface WorldConfig {

	boolean enabled();
	
	List<World> worlds();

	List<Integer> randomBlocks();

	int randomBlockId();
}
