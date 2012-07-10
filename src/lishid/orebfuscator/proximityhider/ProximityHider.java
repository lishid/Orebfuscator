package lishid.orebfuscator.proximityhider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import lishid.orebfuscator.Orebfuscator;
import lishid.orebfuscator.OrebfuscatorConfig;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class ProximityHider
{
    public static HashMap<Player, HashSet<Block>> proximityHiderTracker = new HashMap<Player, HashSet<Block>>();
    public static HashMap<Player, Location> playersToCheck = new HashMap<Player, Location>();
    public static final Object PlayerLock = new Object();
    public static final Object BlockLock = new Object();
    
    public static void Load()
    {
        Orebfuscator.instance.getServer().getScheduler().scheduleAsyncRepeatingTask(Orebfuscator.instance, new Runnable()
        {
            public void run()
            {
                if (!OrebfuscatorConfig.getUseProximityHider())
                    return;
                
                HashMap<Player, Location> newPlayers = new HashMap<Player, Location>();
                
                synchronized (PlayerLock)
                {
                    newPlayers.putAll(playersToCheck);
                    playersToCheck.clear();
                }
                
                for (Player p : newPlayers.keySet())
                {
                    if (p != null && proximityHiderTracker.containsKey(p))
                    {
                        Location loc1 = p.getLocation();
                        Location loc2 = (newPlayers.get(p));
                        
                        if(!loc1.getWorld().getName().equalsIgnoreCase(loc2.getWorld().getName()))
                            continue;
                        
                        if(loc1.getBlock().getLocation().distance(loc2.getBlock().getLocation()) < 0.9)
                        {
                            continue;
                        }
                        
                        HashSet<Block> blocks = new HashSet<Block>();
                        
                        synchronized (BlockLock)
                        {
                            blocks.addAll(proximityHiderTracker.get(p));
                        }
                        
                        for (Block b : blocks)
                        {
                            if (b == null || p == null || b.getWorld() == null || p.getWorld() == null)
                            {
                                removeBlock(p, b);
                                continue;
                            }
                            if (!p.getWorld().equals(b.getWorld()))
                            {
                                removeBlock(p, b);
                            }
                            else if (p.getLocation().distance(b.getLocation()) < OrebfuscatorConfig.getProximityHiderDistance())
                            {
                                removeBlock(p, b);
                                
                                p.sendBlockChange(b.getLocation(), b.getTypeId(), b.getData());
                            }
                        }
                    }
                }
            }
        }, 10L, 10L);
    }
    
    private static void removeBlock(Player p, Block b)
    {
        synchronized (BlockLock)
        {
            if (proximityHiderTracker.get(p) != null)
                proximityHiderTracker.get(p).remove(b);
        }
    }
    
    public static void AddProximityBlocks(CraftPlayer player, ArrayList<Block> blocks)
    {
        synchronized (BlockLock)
        {
            if (!proximityHiderTracker.containsKey(player))
            {
                proximityHiderTracker.put(player, new HashSet<Block>());
            }
            for (Block b : blocks)
            {
                proximityHiderTracker.get(player).add(b);
            }
        }
    }
}
