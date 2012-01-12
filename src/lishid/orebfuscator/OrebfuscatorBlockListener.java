package lishid.orebfuscator;

import java.util.HashMap;

import lishid.orebfuscator.utils.Calculations;
import lishid.orebfuscator.utils.OrebfuscatorConfig;

import org.bukkit.Material;
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
        if(event.getBlock().getType() != Material.SAND && event.getBlock().getType() != Material.GRAVEL)
        	return;
        if(!applyphysics(event.getBlock()))
        	return;
        Calculations.UpdateBlocksNearby(event.getBlock());
    }
    
    private boolean applyphysics(Block block)
    {
        int l = block.getWorld().getBlockTypeIdAt(block.getX(), block.getY() - 1, block.getZ());

        if (l == 0) {
            return true;
        } else if (l == net.minecraft.server.Block.FIRE.id) {
            return true;
        } else {
        	net.minecraft.server.Material material = net.minecraft.server.Block.byId[l].material;

            return material == net.minecraft.server.Material.WATER ? true : material == net.minecraft.server.Material.LAVA;
        }
    }
}