package lishid.orebfuscator;

import lishid.orebfuscator.threading.OrebfuscatorThreadUpdate;
import lishid.orebfuscator.utils.OrebfuscatorConfig;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;

public class OrebfuscatorEntityListener implements Listener{
	@EventHandler(priority = EventPriority.MONITOR)
	public void onEntityExplode(EntityExplodeEvent event)
	{
		if (event.isCancelled() || !OrebfuscatorConfig.getUpdateOnExplosion() || !OrebfuscatorConfig.getEnabled())
			return;
		
		for (Block block : event.blockList()) {
			OrebfuscatorThreadUpdate.Queue(block);
		}
	}
}