package net.imprex.orebfuscator.config;

import java.util.List;

import org.bukkit.World;

public interface ProximityWorldConfig {

	boolean enabled();

	List<World> worlds();

	int distance();

	int distanceSquared();

	boolean useFastGazeCheck();

	int randomBlockId();
}
