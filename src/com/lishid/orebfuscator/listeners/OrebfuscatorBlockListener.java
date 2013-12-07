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

package com.lishid.orebfuscator.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;

import com.lishid.orebfuscator.OrebfuscatorConfig;
import com.lishid.orebfuscator.hithack.BlockHitManager;
import com.lishid.orebfuscator.obfuscation.BlockUpdate;

public class OrebfuscatorBlockListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) {
            return;
        }

        BlockUpdate.Update(event.getBlock());
        BlockHitManager.breakBlock(event.getPlayer(), event.getBlock());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockDamage(BlockDamageEvent event) {
        if (event.isCancelled() || !OrebfuscatorConfig.UpdateOnDamage) {
            return;
        }

        if (!BlockUpdate.needsUpdate(event.getBlock())) {
            return;
        }

        if (!BlockHitManager.hitBlock(event.getPlayer(), event.getBlock())) {
            return;
        }

        BlockUpdate.Update(event.getBlock());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockPhysics(BlockPhysicsEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (event.getBlock().getType() != Material.SAND && event.getBlock().getType() != Material.GRAVEL) {
            return;
        }

        if (!applyphysics(event.getBlock())) {
            return;
        }

        BlockUpdate.Update(event.getBlock());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockPistonExtend(BlockPistonExtendEvent event) {
        if (event.isCancelled()) {
            return;
        }

        for (Block b : event.getBlocks()) {
            BlockUpdate.Update(b);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockPistonRetract(BlockPistonRetractEvent event) {
        if (event.isCancelled()) {
            return;
        }

        BlockUpdate.Update(event.getBlock());
    }

    private boolean applyphysics(Block block) {
        // See net.minecraft.server.v1_4_5.BlockSand.canFall(World world, int i, int j, int k)

        int blockID = block.getRelative(0, -1, 0).getTypeId();

        int air = Material.AIR.getId();
        int fire = Material.FIRE.getId();
        int water = Material.WATER.getId();
        int water2 = Material.STATIONARY_WATER.getId();
        int lava = Material.LAVA.getId();
        int lava2 = Material.STATIONARY_LAVA.getId();

        return (blockID == air || blockID == fire || blockID == water || blockID == water2 || blockID == lava || blockID == lava2);
    }
}