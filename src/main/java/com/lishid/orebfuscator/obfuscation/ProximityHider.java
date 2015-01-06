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
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.lishid.orebfuscator.Orebfuscator;
import com.lishid.orebfuscator.OrebfuscatorConfig;

public class ProximityHider extends Thread implements Runnable {
    public static Map<Player, Set<Block>> proximityHiderTracker = new WeakHashMap<Player, Set<Block>>();
    public Map<Player, Set<Block>> proximityHiderTrackerLocal = new WeakHashMap<Player, Set<Block>>();
    public static Map<Player, Location> playersToCheck = new HashMap<Player, Location>();

    public static ProximityHider thread = new ProximityHider();

    public long lastExecute = System.currentTimeMillis();
    public AtomicBoolean kill = new AtomicBoolean(false);
    public static boolean running = false;

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
        if (thread != null)
            thread.kill.set(true);
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

                    Location loc1 = p.getLocation();
                    Location loc2 = checkPlayers.get(p);

                    // If player changed world
                    if (!loc1.getWorld().equals(loc2.getWorld())) {
                        synchronized (proximityHiderTracker) {
                            proximityHiderTracker.remove(p);
                            proximityHiderTrackerLocal.remove(p);
                        }
                        continue;
                    }

                    // Player didn't actually move
                    if (loc1.getBlockX() == loc2.getBlockX() && loc1.getBlockY() == loc2.getBlockY() && loc1.getBlockZ() == loc2.getBlockZ()) {
                        continue;
                    }

                    Set<Block> blocks = proximityHiderTrackerLocal.get(p);
                    Set<Block> removedBlocks = new HashSet<Block>();
                    if (blocks == null) {
                        blocks = new HashSet<Block>();
                        proximityHiderTrackerLocal.put(p, blocks);
                    }

                    int y = (int) Math.floor(p.getLocation().getY());

                    boolean skip = OrebfuscatorConfig.skipProximityHiderCheck(y);

                    synchronized (proximityHiderTracker) {
                        Set<Block> synchronizedBlocks = proximityHiderTracker.get(p);
                        if (synchronizedBlocks != null) {
                            blocks.addAll(synchronizedBlocks);
                            synchronizedBlocks.clear();
                        }
                    }

                    if (!skip) {
                        for (Block b : blocks) {
                            if (b == null || b.getWorld() == null || p.getWorld() == null) {
                                removedBlocks.add(b);
                                continue;
                            }

                            if (!p.getWorld().equals(b.getWorld())) {
                                removedBlocks.add(b);
                                continue;
                            }

                            if (OrebfuscatorConfig.proximityHiderDeobfuscate(y, b) || p.getLocation().distanceSquared(b.getLocation()) < distanceSquared) {
                                removedBlocks.add(b);

                                if (CalculationsUtil.isChunkLoaded(b.getWorld(), b.getChunk().getX(), b.getChunk().getZ())) {
                                    p.sendBlockChange(b.getLocation(), b.getTypeId(), b.getData());
                                    final Block block = b;
                                    final Player player = p;
                                    Orebfuscator.instance.runTask(new Runnable() {
                                        @Override
                                        public void run() {
                                            OrebfuscatorConfig.blockAccess.updateBlockTileEntity(block, player);
                                        }
                                    });
                                }
                            }
                        }

                        for (Block b : removedBlocks) {
                            blocks.remove(b);
                        }
                    }
                }
            }
            catch (Exception e) {
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

    public static void AddProximityBlocks(Player player, ArrayList<Block> blocks) {
        if (!OrebfuscatorConfig.UseProximityHider) {
            return;
        }
        restart();
        synchronized (proximityHiderTracker) {
            if (!proximityHiderTracker.containsKey(player)) {
                proximityHiderTracker.put(player, new HashSet<Block>());
            }
            for (Block b : blocks) {
                proximityHiderTracker.get(player).add(b);
            }
        }
    }
}