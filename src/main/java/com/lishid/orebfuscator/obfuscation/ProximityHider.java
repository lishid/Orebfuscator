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
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.lishid.orebfuscator.DeprecatedMethods;
import com.lishid.orebfuscator.Orebfuscator;
import com.lishid.orebfuscator.OrebfuscatorConfig;
import com.lishid.orebfuscator.types.BlockCoord;
import com.lishid.orebfuscator.types.BlockState;

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
                
                int checkRadius = OrebfuscatorConfig.ProximityHiderDistance >> 4;
                
                if((OrebfuscatorConfig.ProximityHiderDistance & 0xf) != 0) {
                	checkRadius++;
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
                        	if(!localPlayerInfo.getWorld().equals(playerInfo.getWorld())) {
                        		localPlayerInfo.setWorld(playerInfo.getWorld());
                        		localPlayerInfo.clearChunks();
                        	}
                        	
                        	localPlayerInfo.copyChunks(playerInfo);
                            playerInfo.clearChunks();
                        }
                    }
                    
                    if (OrebfuscatorConfig.skipProximityHiderCheck(y)) continue;
                    
                    if(localPlayerInfo.getWorld() == null || p.getWorld() == null || !p.getWorld().equals(localPlayerInfo.getWorld())) {
                    	localPlayerInfo.clearChunks();
                        continue;
                    }
                    
                    ArrayList<BlockCoord> removedBlocks = new ArrayList<BlockCoord>();
                    Location playerLocation = p.getLocation();
                    int minChunkX = (playerLocation.getBlockX() >> 4) - checkRadius;
                    int maxChunkX = minChunkX + (checkRadius << 1);
                    int minChunkZ = (playerLocation.getBlockZ() >> 4) - checkRadius;
                    int maxChunkZ = minChunkZ + (checkRadius << 1);
                    
                    for(int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
	                    for(int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
	                    	ArrayList<BlockCoord> blocks = localPlayerInfo.getBlocks(chunkX, chunkZ);
	                    	
	                    	if(blocks == null) continue;
	                    	
	                    	removedBlocks.clear();
                    
		                    for (BlockCoord b : blocks) {
		                        if (b == null) {
		                            removedBlocks.add(b);
		                            continue;
		                        }
		                        
		                        Location blockLocation = new Location(localPlayerInfo.getWorld(), b.x, b.y, b.z);
		                        
		                        if (OrebfuscatorConfig.proximityHiderDeobfuscate() || playerLocation.distanceSquared(blockLocation) < distanceSquared) {
		                            removedBlocks.add(b);
		                            
		                            BlockState blockState = Orebfuscator.nms.getBlockState(localPlayerInfo.getWorld(), b.x, b.y, b.z);
		
		                            if (blockState != null) {
		                            	DeprecatedMethods.sendBlockChange(p, blockLocation, blockState);
		                                final BlockCoord block = b;
		                                final Player player = p;
		                                Orebfuscator.instance.runTask(new Runnable() {
		                                    @Override
		                                    public void run() {
		                                    	Orebfuscator.nms.updateBlockTileEntity(block, player);
		                                    }
		                                });
		                            }
		                        }
		                    }
		                    
		                    if(blocks.size() == removedBlocks.size()) {
		                    	localPlayerInfo.removeChunk(chunkX, chunkZ);
		                    } else {
		                    	blocks.removeAll(removedBlocks);
		                    }
	                    }
                    }
                }
            } catch (Exception e) {
                Orebfuscator.log(e);
            }
        }

        running = false;
    }

    private static void restart() {
        synchronized (thread) {
            if (thread.isInterrupted() || !thread.isAlive())
                running = false;

            if (!running && OrebfuscatorConfig.UseProximityHider) {
                // Load ProximityHider
                ProximityHider.Load();
            }
        }
    }

    public static void addProximityBlocks(Player player, int chunkX, int chunkZ, ArrayList<BlockCoord> blocks) {
        if (!OrebfuscatorConfig.UseProximityHider) return;
        
        restart();
        
        synchronized (proximityHiderTracker) {
        	ProximityHiderPlayer playerInfo = proximityHiderTracker.get(player);
        	World world = player.getWorld();
        	
            if (playerInfo == null) {
                proximityHiderTracker.put(player, playerInfo = new ProximityHiderPlayer(world));
            } else if(!playerInfo.getWorld().equals(world)) {
        		playerInfo.setWorld(world);
        		playerInfo.clearChunks();
            }
            
            if(blocks.size() > 0) {
            	playerInfo.putBlocks(chunkX, chunkZ, blocks);
            } else {
            	playerInfo.removeChunk(chunkX, chunkZ);
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
        		
        		if(!playerInfo.getWorld().equals(world)) {
                	playerInfo.setWorld(world);
                    playerInfo.clearChunks();
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