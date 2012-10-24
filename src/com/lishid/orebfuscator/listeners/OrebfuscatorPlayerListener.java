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
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Event.Result;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.*;

import com.lishid.orebfuscator.Orebfuscator;
import com.lishid.orebfuscator.OrebfuscatorConfig;
import com.lishid.orebfuscator.hithack.BlockHitManager;
import com.lishid.orebfuscator.obfuscation.BlockDeobfuscator;
import com.lishid.orebfuscator.proximityhider.ProximityHider;

public class OrebfuscatorPlayerListener implements Listener
{
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        Player player = event.getPlayer();
        if (OrebfuscatorConfig.getLoginNotification())
        {
            if (OrebfuscatorConfig.playerBypassOp(player))
            {
                Orebfuscator.message(player, "Orebfuscator bypassed because you are OP.");
            }
            else if (OrebfuscatorConfig.playerBypassPerms(player))
            {
                Orebfuscator.message(player, "Orebfuscator bypassed because you have permission.");
            }
        }
        synchronized (Orebfuscator.players)
        {
            Orebfuscator.players.put(player, true);
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        synchronized (Orebfuscator.players)
        {
            Orebfuscator.players.remove(event.getPlayer());
        }
        BlockHitManager.clearHistory(event.getPlayer());
        if (OrebfuscatorConfig.getUseProximityHider())
        {
            synchronized (ProximityHider.BlockLock)
            {
                ProximityHider.proximityHiderTracker.remove(event.getPlayer());
            }
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.useInteractedBlock() == Result.DENY)
            return;
        
        if (event.getItem() != null
                && event.getItem().getType() != null
                && (event.getMaterial() == Material.DIRT || event.getMaterial() == Material.GRASS)
                && ((event.getItem().getType() == Material.WOOD_HOE) || (event.getItem().getType() == Material.IRON_HOE) || (event.getItem().getType() == Material.GOLD_HOE) || (event.getItem()
                        .getType() == Material.DIAMOND_HOE)))
        {
            BlockDeobfuscator.Update(event.getClickedBlock());
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event)
    {
        BlockHitManager.clearHistory(event.getPlayer());
        if (OrebfuscatorConfig.getUseProximityHider())
        {
            synchronized (ProximityHider.BlockLock)
            {
                ProximityHider.proximityHiderTracker.remove(event.getPlayer());
            }
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveEvent event)
    {
        if (event.isCancelled())
        {
            return;
        }
        
        if (OrebfuscatorConfig.getUseProximityHider())
        {
            synchronized (ProximityHider.PlayerLock)
            {
                if (!ProximityHider.playersToCheck.containsKey(event.getPlayer()))
                    ProximityHider.playersToCheck.put(event.getPlayer(), event.getFrom());
            }
        }
    }
}
