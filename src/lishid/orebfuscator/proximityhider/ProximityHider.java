package lishid.orebfuscator.proximityhider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import lishid.orebfuscator.Orebfuscator;
import lishid.orebfuscator.OrebfuscatorConfig;

import org.bukkit.block.Block;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class ProximityHider {
    public static HashMap<Player, HashSet<Block>> proximityHiderTracker = new HashMap<Player, HashSet<Block>>();
    public static HashSet<Player> playersToCheck = new HashSet<Player>();
	public static final Object Lock = new Object();
    
	public static void Load()
	{
		Orebfuscator.instance.getServer().getScheduler().scheduleAsyncRepeatingTask(Orebfuscator.instance, new Runnable() {
		    public void run() {
		    	if(!OrebfuscatorConfig.getUseProximityHider())
		    		return;
		    	synchronized(Lock)
		    	{
			    	for(Player p : playersToCheck)
			    	{
			    		if(p != null && proximityHiderTracker.containsKey(p))
			    		{
				    		HashSet<Block> blocksToRemove = new HashSet<Block>();
				    		HashSet<Block> blocks = proximityHiderTracker.get(p);
				    		for(Block b : blocks)
				    		{
				    			if(!p.getWorld().equals(b.getWorld()))
				    			{
					    			blocksToRemove.add(b);
				    			}
				    			else if(p.getLocation().distance(b.getLocation()) < OrebfuscatorConfig.getProximityHiderDistance())
				    			{
					    			blocksToRemove.add(b);
					    			
					                p.sendBlockChange(b.getLocation(), b.getTypeId(), b.getData());
					    			//HashSet<CraftPlayer> players = new HashSet<CraftPlayer>();
					    			//players.add((CraftPlayer)p);
					    			//Calculations.UpdateBlock(b, players);
				    			}
				    		}
				    		for(Block b : blocksToRemove)
				    		{
				    			blocks.remove(b);
				    		}
			    		}
			    	}
			    	playersToCheck.clear();
		    	}
		    }
		}, 10L, 10L);
	}
    
	public static void AddProximityBlocks(CraftPlayer player, ArrayList<Block> blocks)
	{
    	synchronized(Lock)
    	{
			if(!proximityHiderTracker.containsKey(player))
			{
				proximityHiderTracker.put(player, new HashSet<Block>());
			}
			for(Block b : blocks)
			{
				proximityHiderTracker.get(player).add(b);
			}
    	}
	}
}
