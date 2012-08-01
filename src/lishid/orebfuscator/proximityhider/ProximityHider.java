package lishid.orebfuscator.proximityhider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicBoolean;

import lishid.orebfuscator.Orebfuscator;
import lishid.orebfuscator.OrebfuscatorConfig;
import lishid.orebfuscator.obfuscation.Calculations;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class ProximityHider extends Thread implements Runnable
{
    public static HashMap<Player, HashSet<Block>> proximityHiderTracker = new HashMap<Player, HashSet<Block>>();
    public static HashMap<Player, Location> playersToCheck = new HashMap<Player, Location>();
    public static final Object PlayerLock = new Object();
    public static final Object BlockLock = new Object();
    
    public static ProximityHider thread = new ProximityHider();
    
    public long lastExecute = System.currentTimeMillis();
    public AtomicBoolean kill =  new AtomicBoolean(false);
    
    public static void Load()
    {
        if (thread == null || thread.isInterrupted() || !thread.isAlive())
        {
            thread = new ProximityHider();
            thread.setName("ProximityHider Thread");
            thread.setPriority(Thread.MIN_PRIORITY);
            thread.start();
        }
    }
    
    public static void terminate()
    {
        if (thread != null)
            thread.kill.set(true);
    }
    
    public void run()
    {
        while (!this.isInterrupted() && !kill.get())
        {
            try
            {
                //Wait until necessary
                long timeWait = lastExecute + 500 - System.currentTimeMillis();
                lastExecute = System.currentTimeMillis();
                if(timeWait > 0)
                {
                    Thread.sleep(timeWait);
                }
                
                if (!OrebfuscatorConfig.getUseProximityHider())
                    continue;
                
                HashMap<Player, Location> newPlayers = new HashMap<Player, Location>();
                
                synchronized (PlayerLock)
                {
                    newPlayers.putAll(playersToCheck);
                    playersToCheck.clear();
                }
                
                for (Player p : newPlayers.keySet())
                {
                    if (p == null || !proximityHiderTracker.containsKey(p))
                    {
                        continue;
                    }
                    
                    Location loc1 = p.getLocation();
                    Location loc2 = newPlayers.get(p);
                    
                    // If player changed world
                    if (!loc1.getWorld().equals(loc2.getWorld()))
                    {
                        proximityHiderTracker.remove(p);
                        continue;
                    }
                    
                    // Player didn't actually move
                    if (loc1.getBlock().equals((loc2.getBlock())))
                    {
                        continue;
                    }
                    
                    HashSet<Block> blocks = new HashSet<Block>();
                    HashSet<Block> removedBlocks = new HashSet<Block>();
                    
                    synchronized (BlockLock)
                    {
                        if (proximityHiderTracker.get(p) != null)
                            blocks.addAll(proximityHiderTracker.get(p));
                    }
                    
                    for (Block b : blocks)
                    {
                        if (b == null || p == null || b.getWorld() == null || p.getWorld() == null)
                        {
                            removedBlocks.add(b);
                            continue;
                        }
                        
                        if (!p.getWorld().equals(b.getWorld()))
                        {
                            removedBlocks.add(b);
                            continue;
                        }
                        
                        if (p.getLocation().distance(b.getLocation()) < OrebfuscatorConfig.getProximityHiderDistance())
                        {
                            removedBlocks.add(b);
                            
                            if (Calculations.isChunkLoaded(b.getWorld(), b.getChunk().getX(), b.getChunk().getZ()))
                            {
                                p.sendBlockChange(b.getLocation(), b.getTypeId(), b.getData());
                            }
                        }
                    }
                    
                    synchronized (BlockLock)
                    {
                        for (Block b : removedBlocks)
                        {
                            proximityHiderTracker.get(p).remove(b);
                        }
                    }
                }
            }
            catch (Exception e)
            {
                Orebfuscator.log(e);
            }
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
