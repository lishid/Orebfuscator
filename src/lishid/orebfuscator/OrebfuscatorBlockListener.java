package lishid.orebfuscator;

import java.util.HashMap;

import lishid.orebfuscator.utils.Calculations;
import lishid.orebfuscator.utils.OrebfuscatorConfig;

import org.bukkit.block.Block;
import org.bukkit.event.block.*;

public class OrebfuscatorBlockListener extends BlockListener {
	public static HashMap<String, Block> blockLog = new HashMap<String, Block>();
    Orebfuscator plugin;
    public OrebfuscatorBlockListener(Orebfuscator plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled() || !OrebfuscatorConfig.UpdateOnBreak())
        	return;
        
        Calculations.UpdateBlocksNearby(event.getBlock());
    }
    
    @Override
    public void onBlockDamage(BlockDamageEvent event) {
    	if (event.isCancelled() || !OrebfuscatorConfig.UpdateOnDamage())
        	return;
        
		if(blockLog.containsKey(event.getPlayer().getName()) && blockLog.get(event.getPlayer().getName()).equals(event.getBlock()))
		{
			return;
		}
		
		blockLog.put(event.getPlayer().getName(), event.getBlock());

        Calculations.UpdateBlocksNearby(event.getBlock());
    }
    
    @Override
    public void onBlockPhysics(BlockPhysicsEvent event) {
        if (event.isCancelled() || !OrebfuscatorConfig.UpdateOnPhysics())
        	return;

        Calculations.UpdateBlocksNearby(event.getBlock());
    }

    @Override
    public void onBlockPlace(BlockPlaceEvent event)
    {
        if (event.isCancelled() || !OrebfuscatorConfig.DarknessHideBlocks() || !OrebfuscatorConfig.Enabled())
        	return;
		Calculations.LightingUpdate(event.getBlock(), false);
    }
    @Override
    public void onBlockIgnite(BlockIgniteEvent event)
    {
        if (event.isCancelled() || !OrebfuscatorConfig.DarknessHideBlocks() || !OrebfuscatorConfig.Enabled())
        	return;
		Calculations.LightingUpdate(event.getBlock(), true);
    }
}