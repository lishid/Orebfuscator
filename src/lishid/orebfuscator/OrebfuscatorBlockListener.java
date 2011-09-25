<<<<<<< HEAD
package lishid.orebfuscator;

import java.util.ArrayList;
import java.util.HashMap;

import lishid.orebfuscator.utils.Calculations;


import org.bukkit.block.Block;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.block.*;

public class OrebfuscatorBlockListener extends BlockListener {
	public static HashMap<Player, Block> playerLog = new HashMap<Player, Block>();
    Orebfuscator plugin;
    public OrebfuscatorBlockListener(Orebfuscator plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled() || !OrebfuscatorConfig.UpdateOnBreak() || !OrebfuscatorConfig.Enabled())
        	return;
        
        if (OrebfuscatorConfig.isTransparent((byte)event.getBlock().getTypeId()))
        	return;
        
        CraftBlock eventBlock = (CraftBlock) event.getBlock();
        
        ArrayList<Block> blocks = Calculations.GetAjacentBlocks(eventBlock.getWorld(),
        		new ArrayList<Block>(), event.getBlock(), OrebfuscatorConfig.UpdateRadius());
        
        for(Block block : blocks)
        {
        	Calculations.UpdateBlock(block);
        }
    }
    
    @Override
    public void onBlockDamage(BlockDamageEvent event) {
        if (event.isCancelled() || !OrebfuscatorConfig.UpdateOnDamage() || !OrebfuscatorConfig.Enabled())
        	return;
        
		if(playerLog.containsKey(event.getPlayer()) &&
				playerLog.get(event.getPlayer()).equals(event.getBlock()))
		{
			return;
		}
		
		playerLog.put(event.getPlayer(), event.getBlock());

        if (OrebfuscatorConfig.isTransparent((byte)event.getBlock().getTypeId()))
        	return;

        CraftBlock eventBlock = (CraftBlock) event.getBlock();
        
        ArrayList<Block> blocks = Calculations.GetAjacentBlocks(eventBlock.getWorld(),
        		new ArrayList<Block>(), event.getBlock(), OrebfuscatorConfig.UpdateRadius());
        for(Block block : blocks)
        {
            Calculations.UpdateBlock(block);
        }
    }
    
    @Override
    public void onBlockPhysics(BlockPhysicsEvent event) {
        if (event.isCancelled() || !OrebfuscatorConfig.UpdateOnPhysics() || !OrebfuscatorConfig.Enabled())
        	return;
        
        if (OrebfuscatorConfig.isTransparent((byte)event.getBlock().getTypeId()))
        	return;

        CraftBlock eventBlock = (CraftBlock) event.getBlock();
        
        ArrayList<Block> blocks = Calculations.GetAjacentBlocks(eventBlock.getWorld(),
        		new ArrayList<Block>(), event.getBlock(), OrebfuscatorConfig.UpdateRadius());
        for(Block block : blocks)
        {
            Calculations.UpdateBlock(block);
        }
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
=======
package lishid.orebfuscator;

import java.util.ArrayList;
import java.util.HashMap;

import lishid.orebfuscator.utils.Calculations;


import org.bukkit.block.Block;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.block.*;

public class OrebfuscatorBlockListener extends BlockListener {
	public static HashMap<Player, Block> playerLog = new HashMap<Player, Block>();
    Orebfuscator plugin;
    public OrebfuscatorBlockListener(Orebfuscator plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled() || !OrebfuscatorConfig.UpdateOnBreak() || !OrebfuscatorConfig.Enabled())
        	return;
        
        if (OrebfuscatorConfig.isTransparent((byte)event.getBlock().getTypeId()))
        	return;
        
        CraftBlock eventBlock = (CraftBlock) event.getBlock();
        
        ArrayList<Block> blocks = Calculations.GetAjacentBlocks(eventBlock.getWorld(),
        		new ArrayList<Block>(), event.getBlock(), OrebfuscatorConfig.UpdateRadius());
        
        for(Block block : blocks)
        {
        	Calculations.UpdateBlock(block);
        }
    }
    
    @Override
    public void onBlockDamage(BlockDamageEvent event) {
        if (event.isCancelled() || !OrebfuscatorConfig.UpdateOnDamage() || !OrebfuscatorConfig.Enabled())
        	return;
        
		if(playerLog.containsKey(event.getPlayer()) &&
				playerLog.get(event.getPlayer()).equals(event.getBlock()))
		{
			return;
		}
		
		playerLog.put(event.getPlayer(), event.getBlock());

        if (OrebfuscatorConfig.isTransparent((byte)event.getBlock().getTypeId()))
        	return;

        CraftBlock eventBlock = (CraftBlock) event.getBlock();
        
        ArrayList<Block> blocks = Calculations.GetAjacentBlocks(eventBlock.getWorld(),
        		new ArrayList<Block>(), event.getBlock(), OrebfuscatorConfig.UpdateRadius());
        for(Block block : blocks)
        {
            Calculations.UpdateBlock(block);
        }
    }
    
    @Override
    public void onBlockPhysics(BlockPhysicsEvent event) {
        if (event.isCancelled() || !OrebfuscatorConfig.UpdateOnPhysics() || !OrebfuscatorConfig.Enabled())
        	return;
        
        if (OrebfuscatorConfig.isTransparent((byte)event.getBlock().getTypeId()))
        	return;

        CraftBlock eventBlock = (CraftBlock) event.getBlock();
        
        ArrayList<Block> blocks = Calculations.GetAjacentBlocks(eventBlock.getWorld(),
        		new ArrayList<Block>(), event.getBlock(), OrebfuscatorConfig.UpdateRadius());
        for(Block block : blocks)
        {
            Calculations.UpdateBlock(block);
        }
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
>>>>>>> f2a74dba45cffa3cfa52db44f62e029351ddccac
}