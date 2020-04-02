package net.imprex.orebfuscator.config;

import java.util.List;

import org.bukkit.World;

public interface ProximityConfig {

	boolean enabled();

	List<World> worlds();

	int distance();

	int distanceSquared();

	boolean useFastGazeCheck();

	int randomBlockId();
}
