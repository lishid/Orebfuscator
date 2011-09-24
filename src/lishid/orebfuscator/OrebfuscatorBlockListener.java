package lishid.orebfuscator;

import java.util.ArrayList;

import lishid.orebfuscator.utils.Calculations;


import org.bukkit.block.Block;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.event.block.*;

public class OrebfuscatorBlockListener extends BlockListener {
    Orebfuscator plugin;
    public OrebfuscatorBlockListener(Orebfuscator plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled() || !OrebfuscatorConfig.UpdateOnBreak())
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
        if (event.isCancelled() || !OrebfuscatorConfig.UpdateOnDamage())
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
        if (event.isCancelled() || !OrebfuscatorConfig.DarknessHideBlocks())
        	return;
		Calculations.LightingUpdate(event.getBlock(), false);
    }
    @Override
    public void onBlockIgnite(BlockIgniteEvent event)
    {
        if (event.isCancelled() || !OrebfuscatorConfig.DarknessHideBlocks())
        	return;
		Calculations.LightingUpdate(event.getBlock(), true);
    }
}