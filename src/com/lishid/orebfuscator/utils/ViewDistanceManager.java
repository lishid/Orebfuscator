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

package com.lishid.orebfuscator.utils;

import java.util.List;

import net.minecraft.server.v1_4_5.PlayerManager;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_4_5.CraftWorld;

public class ViewDistanceManager
{
    public static void setDynamicViewDistance(int distance)
    {
        List<World> worlds = Bukkit.getWorlds();
        for (World world : worlds)
        {
            PlayerManager pm = ((CraftWorld) world).getHandle().getPlayerManager();
            ReflectionHelper.setPrivateField(pm, "e", distance);
        }
    }
}
