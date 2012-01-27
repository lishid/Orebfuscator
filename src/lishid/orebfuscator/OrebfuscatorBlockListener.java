package lishid.orebfuscator;

import java.util.HashMap;

import lishid.orebfuscator.threading.OrebfuscatorThreadUpdate;
import lishid.orebfuscator.utils.OrebfuscatorConfig;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;

public class OrebfuscatorBlockListener implements Listener {
	public static HashMap<Player, Block> blockLog = new HashMap<Player, Block>();
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled() || !OrebfuscatorConfig.getUpdateOnBreak())
        	return;
        
        OrebfuscatorThreadUpdate.Queue(event.getBlock());
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockDamage(BlockDamageEvent event) {
    	if (event.isCancelled() || !OrebfuscatorConfig.getUpdateOnDamage())
        	return;
        
		if(blockLog.containsKey(event.getPlayer()) && blockLog.get(event.getPlayer()).equals(event.getBlock()))
		{
			return;
		}
		
		blockLog.put(event.getPlayer(), event.getBlock());

		OrebfuscatorThreadUpdate.Queue(event.getBlock());
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockPhysics(BlockPhysicsEvent event) {
        if (event.isCancelled() || !OrebfuscatorConfig.getUpdateOnPhysics())
        	return;
        if(event.getBlock().getType() != Material.SAND && event.getBlock().getType() != Material.GRAVEL)
        	return;
        if(!applyphysics(event.getBlock()))
        	return;
        OrebfuscatorThreadUpdate.Queue(event.getBlock());
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