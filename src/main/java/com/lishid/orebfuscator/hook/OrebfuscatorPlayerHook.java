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

package com.lishid.orebfuscator.hook;

import com.lishid.orebfuscator.Orebfuscator;
import com.lishid.orebfuscator.internal.PlayerHook;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class OrebfuscatorPlayerHook implements Listener {
    private static PlayerHook playerHook;

    private static PlayerHook getPlayerHook() {
        if (playerHook == null) {
            playerHook = new PlayerHook();
        }

        return playerHook;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(final PlayerJoinEvent event) {
        PlayerHook playerHook = getPlayerHook();
        if (!Orebfuscator.usePL) {
            playerHook.HookNM(event.getPlayer());
        }
        playerHook.HookChunkQueue(event.getPlayer());
    }
}
