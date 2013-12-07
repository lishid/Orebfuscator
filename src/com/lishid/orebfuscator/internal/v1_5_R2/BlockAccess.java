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

package com.lishid.orebfuscator.internal.v1_5_R2;

import com.lishid.orebfuscator.internal.IBlockAccess;
import org.bukkit.entity.Player;

//Volatile
import net.minecraft.server.v1_5_R2.*;
import org.bukkit.craftbukkit.v1_5_R2.entity.*;
import org.bukkit.craftbukkit.v1_5_R2.*;

public class BlockAccess implements IBlockAccess {
    @Override
    public boolean isBlockTransparent(int id) {
        return !Block.l(id);
    }

    @Override
    public void updateBlockTileEntity(org.bukkit.block.Block block, Player player) {
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
}