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
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import com.lishid.orebfuscator.api.IProximityHiderHandler;
import com.lishid.orebfuscator.api.Orebfuscator;
import com.lishid.orebfuscator.api.config.IOrebfuscatorConfig;
import com.lishid.orebfuscator.api.hithack.IBlockHitHandler;
import com.lishid.orebfuscator.api.logger.OFCLogger;
import com.lishid.orebfuscator.api.nms.INmsManager;
import com.lishid.orebfuscator.api.utils.IBlockUpdate;

public class OrebfuscatorPlayerListener implements Listener {

	private final Orebfuscator plugin;
	private final IProximityHiderHandler proximityHiderHandler;
	private final IBlockHitHandler blockHitHandler;
	private final IBlockUpdate blockUpdate;
	private final INmsManager nmsManager;
	private final IOrebfuscatorConfig config;

	public OrebfuscatorPlayerListener(Orebfuscator plugin) {
		this.plugin = plugin;

		this.proximityHiderHandler = this.plugin.getProximityHiderHandler();
		this.blockHitHandler = this.plugin.getBlockHitHandler();
		this.blockUpdate = this.plugin.getBlockUpdate();
		this.nmsManager = this.plugin.getNmsManager();
		this.config = this.plugin.getConfigHandler().getConfig();
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();

		if (this.config.isLoginNotification()) {
			if (this.config.playerBypassOp(player)) {
				OFCLogger.message(player, "Orebfuscator bypassed because you are OP.");
			} else if (this.config.playerBypassPerms(player)) {
				OFCLogger.message(player, "Orebfuscator bypassed because you have permission.");
			}
		}

		if (this.config.isProximityHiderEnabled()) {
			this.proximityHiderHandler.addPlayerToCheck(event.getPlayer(), null);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerQuit(PlayerQuitEvent event) {
		this.blockHitHandler.clearHistory(event.getPlayer());
		if (this.config.isProximityHiderEnabled()) {
			this.proximityHiderHandler.clearPlayer(event.getPlayer());
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.useInteractedBlock() != Result.DENY) {
			if (event.getItem() != null && event.getItem().getType() != null
					&& (event.getMaterial() == Material.DIRT || event.getMaterial() == Material.GRASS)
					&& this.nmsManager.isHoe(event.getItem().getType())) {
				this.blockUpdate.update(event.getClickedBlock());
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
		this.blockHitHandler.clearHistory(event.getPlayer());

		if (this.config.isProximityHiderEnabled()) {
			this.proximityHiderHandler.clearBlocksForOldWorld(event.getPlayer());
			this.proximityHiderHandler.addPlayerToCheck(event.getPlayer(), null);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		if (this.config.isProximityHiderEnabled()) {
			if (event.getCause() != TeleportCause.END_PORTAL && event.getCause() != TeleportCause.NETHER_PORTAL) {
				this.proximityHiderHandler.addPlayerToCheck(event.getPlayer(), null);
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerMove(PlayerMoveEvent event) {
		if (this.config.isProximityHiderEnabled()) {
			this.proximityHiderHandler.addPlayerToCheck(event.getPlayer(), event.getFrom());
		}
	}
}
