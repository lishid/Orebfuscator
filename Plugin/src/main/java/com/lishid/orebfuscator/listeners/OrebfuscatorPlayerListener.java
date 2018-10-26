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

import com.lishid.orebfuscator.NmsInstance;
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

import com.lishid.orebfuscator.Orebfuscator;
import com.lishid.orebfuscator.hithack.BlockHitManager;
import com.lishid.orebfuscator.obfuscation.BlockUpdate;
import com.lishid.orebfuscator.obfuscation.ProximityHider;

public class OrebfuscatorPlayerListener implements Listener {
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        if (Orebfuscator.config.isLoginNotification()) {
            if (Orebfuscator.config.playerBypassOp(player)) {
                Orebfuscator.message(player, "Orebfuscator bypassed because you are OP.");
            } else if (Orebfuscator.config.playerBypassPerms(player)) {
                Orebfuscator.message(player, "Orebfuscator bypassed because you have permission.");
            }
        }
        
        if (Orebfuscator.config.isProximityHiderEnabled()) {
            ProximityHider.addPlayerToCheck(event.getPlayer(), null);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {
        BlockHitManager.clearHistory(event.getPlayer());
        if (Orebfuscator.config.isProximityHiderEnabled()) {
            ProximityHider.clearPlayer(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.useInteractedBlock() == Result.DENY)
            return;

        //For using a hoe for farming
        if (event.getItem() != null &&
                event.getItem().getType() != null &&
                (event.getMaterial() == Material.DIRT || event.getMaterial() == Material.GRASS) &&
                NmsInstance.current.isHoe(event.getItem().getType())
            )
        {
            BlockUpdate.update(event.getClickedBlock());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
        BlockHitManager.clearHistory(event.getPlayer());
        
        if (Orebfuscator.config.isProximityHiderEnabled()) {
            ProximityHider.clearBlocksForOldWorld(event.getPlayer());
            ProximityHider.addPlayerToCheck(event.getPlayer(), null);
        }
    }
    
	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		if (Orebfuscator.config.isProximityHiderEnabled()) {
			if(event.getCause() != TeleportCause.END_PORTAL
					&& event.getCause() != TeleportCause.NETHER_PORTAL
					)
			{
				ProximityHider.addPlayerToCheck(event.getPlayer(), null);
			}
		}
	}

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (Orebfuscator.config.isProximityHiderEnabled()) {
            ProximityHider.addPlayerToCheck(event.getPlayer(), event.getFrom());
        }
    }
}
