/*
 * Copyright (C) 2011-2014 lishid.  All rights reserved.
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

package com.lishid.orebfuscator.obfuscation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.lishid.orebfuscator.Orebfuscator;
import com.lishid.orebfuscator.OrebfuscatorConfig;
import com.lishid.orebfuscator.internal.MinecraftInternals;

public class ProximityHider extends Thread implements Runnable {
    private static final Map<Player, ProximityHiderPlayer> proximityHiderTracker = new WeakHashMap<Player, ProximityHiderPlayer>();
    private static final Map<Player, Location> playersToCheck = new HashMap<Player, Location>();

    private static ProximityHider thread = new ProximityHider();

    private Map<Player, ProximityHiderPlayer> proximityHiderTrackerLocal = new WeakHashMap<Player, ProximityHiderPlayer>();
    private long lastExecute = System.currentTimeMillis();
    private AtomicBoolean kill = new AtomicBoolean(false);
    private static boolean running = false;

    public static void Load() {
        running = true;
        if (thread == null || thread.isInterrupted() || !thread.isAlive()) {
            thread = new ProximityHider();
            thread.setName("Orebfuscator ProximityHider Thread");
            thread.setPriority(Thread.MIN_PRIORITY);
            thread.start();
        }
    }

    public static void terminate() {
        if (thread != null) {
            thread.kill.set(true);
        }
    }

    public void run() {
        while (!this.isInterrupted() && !kill.get()) {
            try {
                // Wait until necessary
                long timeWait = lastExecute + OrebfuscatorConfig.ProximityHiderRate - System.currentTimeMillis();
                lastExecute = System.currentTimeMillis();
                if (timeWait > 0) {
                    Thread.sleep(timeWait);
                }

                if (!OrebfuscatorConfig.UseProximityHider) {
                    running = false;
                    return;
                }
                
                HashMap<Player, Location> checkPlayers = new HashMap<Player, Location>();

                synchronized (playersToCheck) {
                    checkPlayers.putAll(playersToCheck);
                    playersToCheck.clear();
                }
                
                int distance = OrebfuscatorConfig.ProximityHiderDistance;
                int distanceSquared = distance * distance;

                for (Player p : checkPlayers.keySet()) {

                    if (p == null) {
                        continue;
                    }

                    synchronized (proximityHiderTracker) {
                        if (!proximityHiderTracker.containsKey(p)) {
                            continue;
                        }
                    }

                    Location oldLocation = checkPlayers.get(p);
                    
                    if(oldLocation != null) {
	                    Location curLocation = p.getLocation();
	
	                    // Player didn't actually move
	                    if (curLocation.getBlockX() == oldLocation.getBlockX() && curLocation.getBlockY() == oldLocation.getBlockY() && curLocation.getBlockZ() == oldLocation.getBlockZ()) {
	                        continue;
	                    }
                    }
                    
                    ProximityHiderPlayer localPlayerInfo = proximityHiderTrackerLocal.get(p);
                    
                    if(localPlayerInfo == null) {
                    	proximityHiderTrackerLocal.put(p, localPlayerInfo = new ProximityHiderPlayer(p.getWorld()));
                    }

                    int y = (int) Math.floor(p.getLocation().getY());

                    synchronized (proximityHiderTracker) {
                    	ProximityHiderPlayer playerInfo = proximityHiderTracker.get(p);

                        if (playerInfo != null) {
                        	if(!localPlayerInfo.world.equals(playerInfo.world)) {
                        		localPlayerInfo.world = playerInfo.world;
                        		localPlayerInfo.blocks.clear();
                        	}
                        	
                        	localPlayerInfo.blocks.addAll(playerInfo.blocks);
                            playerInfo.blocks.clear();
                        }
                    }
                    
                    if (OrebfuscatorConfig.skipProximityHiderCheck(y)) continue;
                    
                    if(localPlayerInfo.world == null || p.getWorld() == null || !p.getWorld().equals(localPlayerInfo.world)) {
                    	localPlayerInfo.blocks.clear();
                        continue;
                    }
                    
                    Set<ProximityHiderBlock> removedBlocks = new HashSet<ProximityHiderBlock>();
                    
                    for (ProximityHiderBlock b : localPlayerInfo.blocks) {
                        if (b == null) {
                            removedBlocks.add(b);
                            continue;
                        }
                        
                        Location blockLocation = new Location(localPlayerInfo.world, b.x, b.y, b.z);

                        if (OrebfuscatorConfig.proximityHiderDeobfuscate() || p.getLocation().distanceSquared(blockLocation) < distanceSquared) {
                            removedBlocks.add(b);

                            if (CalculationsUtil.isChunkLoaded(localPlayerInfo.world, b.x >> 4, b.z >> 4)) {
                                p.sendBlockChange(blockLocation, b.getId(), (byte)b.getMeta());
                                final ProximityHiderBlock block = b;
                                final Player player = p;
                                Orebfuscator.instance.runTask(new Runnable() {
                                    @Override
                                    public void run() {
                                        MinecraftInternals.updateBlockTileEntity(block.x, block.y, block.z, player);
                                    }
                                });
                            }
                        }
                    }

                    for (ProximityHiderBlock b : removedBlocks) {
                    	localPlayerInfo.blocks.remove(b);
                    }
                }
            } catch (Exception e) {
                Orebfuscator.log(e);
            }
        }

        running = false;
    }

    public static void restart() {
        synchronized (thread) {
            if (thread.isInterrupted() || !thread.isAlive())
                running = false;

            if (!running && OrebfuscatorConfig.UseProximityHider) {
                // Load ProximityHider
                ProximityHider.Load();
            }
        }
    }

    public static void addProximityBlocks(Player player, ArrayList<ProximityHiderBlock> blocks) {
        if (!OrebfuscatorConfig.UseProximityHider) return;
        
        restart();
        
        synchronized (proximityHiderTracker) {
        	ProximityHiderPlayer playerInfo = proximityHiderTracker.get(player);
        	World world = player.getWorld();
        	
            if (playerInfo == null) {
                proximityHiderTracker.put(player, playerInfo = new ProximityHiderPlayer(world));
            } else if(!playerInfo.world.equals(world)) {
        		playerInfo.world = world;
        		playerInfo.blocks.clear();
            }
            
            for (ProximityHiderBlock b : blocks) {
                playerInfo.blocks.add(b);
            }
        }
    }

    public static void clearPlayer(Player player) {
        synchronized (ProximityHider.proximityHiderTracker) {
            ProximityHider.proximityHiderTracker.remove(player);
        }
    }

    public static void clearBlocksForOldWorld(Player player) {
        synchronized (ProximityHider.proximityHiderTracker) {
    		ProximityHiderPlayer playerInfo = ProximityHider.proximityHiderTracker.get(player);

    		if(playerInfo != null) {
        		World world = player.getWorld();
        		
        		if(!playerInfo.world.equals(world)) {
                	playerInfo.world = world;
                    playerInfo.blocks.clear();
        		}
        	}
        }
    }

    public static void addPlayerToCheck(Player player, Location location) {
        synchronized (ProximityHider.playersToCheck) {
            if (!ProximityHider.playersToCheck.containsKey(player)) {
                ProximityHider.playersToCheck.put(player, location);
            }
        }
    }
}