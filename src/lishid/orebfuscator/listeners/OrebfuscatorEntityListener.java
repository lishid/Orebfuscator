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

package lishid.orebfuscator.listeners;

import lishid.orebfuscator.OrebfuscatorConfig;
import lishid.orebfuscator.threading.OrebfuscatorThreadUpdate;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;

public class OrebfuscatorEntityListener implements Listener
{
    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityExplode(EntityExplodeEvent event)
    {
        if (event.isCancelled() || !OrebfuscatorConfig.getUpdateOnExplosion() || !OrebfuscatorConfig.getEnabled())
        {
            return;
        }
        
        for (Block block : event.blockList())
        {
            OrebfuscatorThreadUpdate.Queue(block);
        }
    }
}