package lishid.orebfuscator;

import lishid.orebfuscator.utils.Calculations;

import org.bukkit.block.Block;
import org.bukkit.event.entity.*;

public class OrebfuscatorEntityListener extends EntityListener{
	Orebfuscator plugin;
	public OrebfuscatorEntityListener(Orebfuscator scrap) {
		plugin = scrap;
	}
	
	@Override
	public void onEntityExplode(EntityExplodeEvent event)
	{
		if (event.isCancelled() || !OrebfuscatorConfig.UpdateOnExplosion() || !OrebfuscatorConfig.Enabled())
			return;
		
		for (Block block : event.blockList()) {
	        Calculations.UpdateBlocksNearby(block);
		}
	}
}