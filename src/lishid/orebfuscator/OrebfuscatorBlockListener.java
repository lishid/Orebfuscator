/*
 * Copyright (C) 2011-2012 lishid.  All rights reserved.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation,  version 3.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

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
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockPistonExtend(BlockPistonExtendEvent event) {
        if (event.isCancelled() || !OrebfuscatorConfig.getUpdateOnPiston())
        	return;
        for(Block b : event.getBlocks())
        {
        	OrebfuscatorThreadUpdate.Queue(b);
        }
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