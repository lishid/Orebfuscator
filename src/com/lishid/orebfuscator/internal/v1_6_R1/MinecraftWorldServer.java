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

package com.lishid.orebfuscator.internal.v1_6_R1;

import com.lishid.orebfuscator.internal.IMinecraftWorldServer;
import com.lishid.orebfuscator.internal.InternalAccessor;

//Volatile
import net.minecraft.server.v1_6_R1.*;
import org.bukkit.craftbukkit.v1_6_R1.*;

public class MinecraftWorldServer implements IMinecraftWorldServer {
    public void Notify(Object world, int x, int y, int z) {
        if (world instanceof CraftWorld) {
            WorldServer server = (WorldServer) ((CraftWorld) world).getHandle();
            server.notify(x, y, z);
        }
        else {
            InternalAccessor.Instance.PrintError();
        }
    }
}