package lishid.orebfuscator.chunkscrambler;

import lishid.orebfuscator.Orebfuscator;

import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;

public class ChunkScramblerWorldListener implements Listener{
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onWorldInit(WorldInitEvent e)
	{
		World world = e.getWorld();
		Orebfuscator.ReplaceWorldChunkManager(world);
	}
}
