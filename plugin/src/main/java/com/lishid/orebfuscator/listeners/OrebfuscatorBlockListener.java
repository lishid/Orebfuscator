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

package com.lishid.orebfuscator.listeners;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;

import com.lishid.orebfuscator.api.Orebfuscator;
import com.lishid.orebfuscator.api.config.IOrebfuscatorConfig;
import com.lishid.orebfuscator.api.hithack.IBlockHitHandler;
import com.lishid.orebfuscator.api.nms.INmsManager;
import com.lishid.orebfuscator.api.utils.IBlockUpdate;

public class OrebfuscatorBlockListener implements Listener {

	private final Orebfuscator plugin;
	private final IBlockUpdate blockUpdate;
	private final IBlockHitHandler blockHitHandler;
	private final INmsManager nmsManager;
	private final IOrebfuscatorConfig config;

	public OrebfuscatorBlockListener(Orebfuscator plugin) {
		this.plugin = plugin;

		this.blockUpdate = this.plugin.getBlockUpdate();
		this.blockHitHandler = this.plugin.getBlockHitHandler();
		this.nmsManager = this.plugin.getNmsManager();
		this.config = this.plugin.getConfigHandler().getConfig();
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		this.blockUpdate.update(event.getBlock());
		this.blockHitHandler.breakBlock(event.getPlayer(), event.getBlock());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockDamage(BlockDamageEvent event) {
		if (this.config.isUpdateOnDamage() && this.blockUpdate.needsUpdate(event.getBlock()) && this.blockHitHandler.hitBlock(event.getPlayer(), event.getBlock())) {
			this.blockUpdate.update(event.getBlock());
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockPhysics(BlockPhysicsEvent event) {
		if (event.getBlock().getType() == Material.SAND && event.getBlock().getType() == Material.GRAVEL) {
			Material blockMaterial = event.getBlock().getRelative(0, -1, 0).getType();

			if (this.nmsManager.canApplyPhysics(blockMaterial)) {
				this.blockUpdate.update(event.getBlock());
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockPistonExtend(BlockPistonExtendEvent event) {
		this.blockUpdate.update(event.getBlocks());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockPistonRetract(BlockPistonRetractEvent event) {
		this.blockUpdate.update(event.getBlock());
	}
}