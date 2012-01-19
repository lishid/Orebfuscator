package lishid.orebfuscator.chunkscrambler;

import lishid.orebfuscator.Orebfuscator;

import org.bukkit.World;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldListener;

public class ChunkScramblerWorldListener extends WorldListener{
	
	public ChunkScramblerWorldListener() { }
	
	public void onWorldInit(WorldInitEvent e)
	{
		World world = e.getWorld();
		Orebfuscator.ReplaceWorldChunkManager(world);
	}
}
