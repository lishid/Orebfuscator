<<<<<<< HEAD
package lishid.orebfuscator;

import java.util.ArrayList;

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
		ArrayList<Block> blocks = new ArrayList<Block>();
		
		for (Block block : event.blockList()) {
			Calculations.GetAjacentBlocks(block.getWorld(), blocks, block, 1);
		}

        for (Block block : blocks)
        {
        	Calculations.UpdateBlock(block);
        }
	}
}
=======
package lishid.orebfuscator;

import java.util.ArrayList;

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
		ArrayList<Block> blocks = new ArrayList<Block>();
		
		for (Block block : event.blockList()) {
			Calculations.GetAjacentBlocks(block.getWorld(), blocks, block, 1);
		}

        for (Block block : blocks)
        {
        	Calculations.UpdateBlock(block);
        }
	}
}
>>>>>>> f2a74dba45cffa3cfa52db44f62e029351ddccac
