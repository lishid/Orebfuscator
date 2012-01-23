package lishid.orebfuscator;

import lishid.orebfuscator.threading.OrebfuscatorThreadUpdate;
import lishid.orebfuscator.utils.OrebfuscatorConfig;

import org.bukkit.block.Block;
import org.bukkit.event.entity.*;

public class OrebfuscatorEntityListener extends EntityListener{
	@Override
	public void onEntityExplode(EntityExplodeEvent event)
	{
		if (event.isCancelled() || !OrebfuscatorConfig.getUpdateOnExplosion() || !OrebfuscatorConfig.getEnabled())
			return;
		
		for (Block block : event.blockList()) {
			OrebfuscatorThreadUpdate.Queue(block);
		}
	}
}