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

import com.lishid.orebfuscator.Orebfuscator;
import com.lishid.orebfuscator.utils.ReflectionHelper;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

//Volatile

public class MinecraftInternals {
    public static boolean isBlockTransparent(int id) {
        return Block.getById(id).u();
    }

    public static void updateBlockTileEntity(org.bukkit.block.Block block, Player player) {
        CraftWorld world = (CraftWorld) block.getWorld();
        TileEntity tileEntity = world.getTileEntityAt(block.getX(), block.getY(), block.getZ());
        if (tileEntity == null) {
            return;
        }
        Packet packet = tileEntity.getUpdatePacket();
        if (packet != null) {
            CraftPlayer player2 = (CraftPlayer) player;
            player2.getHandle().playerConnection.sendPacket(packet);
        }
    }

    public static void notifyBlockChange(org.bukkit.World world, int x, int y, int z) {
        ((CraftWorld) world).getHandle().notify(new BlockPosition(x, y, z));
    }

    public static void tryDisableSpigotAntiXray(org.bukkit.World world) {
        try {
            World mcworld = ((CraftWorld) world).getHandle();
            Object spigotWorldConfig = World.class.getField("spigotConfig").get(mcworld);
            ReflectionHelper.setPrivateField(spigotWorldConfig, "antiXray", false);
        } catch (Exception e) {
            Orebfuscator.log(e);
        }
    }
}
