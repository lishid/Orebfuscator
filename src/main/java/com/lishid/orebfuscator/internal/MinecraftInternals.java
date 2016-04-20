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

import net.minecraft.server.v1_9_R1.BlockPosition;
import net.minecraft.server.v1_9_R1.IBlockData;
import net.minecraft.server.v1_9_R1.Packet;
import net.minecraft.server.v1_9_R1.TileEntity;

import org.bukkit.craftbukkit.v1_9_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_9_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_9_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

//Volatile

public class MinecraftInternals {
    public static void updateBlockTileEntity(org.bukkit.block.Block block, Player player) {
        CraftWorld world = (CraftWorld) block.getWorld();
        TileEntity tileEntity = world.getTileEntityAt(block.getX(), block.getY(), block.getZ());
        if (tileEntity == null) {
            return;
        }
        Packet<?> packet = tileEntity.getUpdatePacket();
        if (packet != null) {
            CraftPlayer player2 = (CraftPlayer) player;
            player2.getHandle().playerConnection.sendPacket(packet);
        }
    }

    public static void notifyBlockChange(org.bukkit.World world, CraftBlock block) {
    	BlockPosition blockPosition = new BlockPosition(block.getX(), block.getY(), block.getZ());
    	IBlockData blockData = ((CraftChunk)block.getChunk()).getHandle().getBlockData(blockPosition);
    	
        ((CraftWorld) world).getHandle().notify(blockPosition, blockData, blockData, 0);
    }
}
