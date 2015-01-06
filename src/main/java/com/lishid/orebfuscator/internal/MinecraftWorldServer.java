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

package com.lishid.orebfuscator.internal;

import net.minecraft.server.v1_8_R1.BlockPosition;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R1.CraftWorld;

//Volatile

public class MinecraftWorldServer {
    public static void Notify(World world, int x, int y, int z) {
        ((CraftWorld) world).getHandle().notify(new BlockPosition(x, y, z));
    }
}