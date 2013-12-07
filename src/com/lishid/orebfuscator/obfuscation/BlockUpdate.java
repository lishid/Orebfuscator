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

package com.lishid.orebfuscator.obfuscation;

import java.util.List;
import java.util.HashSet;

import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;

import com.lishid.orebfuscator.OrebfuscatorConfig;
import com.lishid.orebfuscator.internal.IMinecraftWorldServer;
import com.lishid.orebfuscator.internal.InternalAccessor;

public class BlockUpdate {
    private static IMinecraftWorldServer worldServerAccessor;

    private static IMinecraftWorldServer getWorldServer() {
        if (worldServerAccessor == null)
            worldServerAccessor = InternalAccessor.Instance.newMinecraftWorldServer();

        return worldServerAccessor;
    }

    public static boolean needsUpdate(Block block) {
        return !OrebfuscatorConfig.isBlockTransparent((short) block.getTypeId());
    }

    public static void Update(Block block) {
        if (!needsUpdate(block))
            return;

        HashSet<Block> updateBlocks = GetAjacentBlocks(block.getWorld(), new HashSet<Block>(), block, OrebfuscatorConfig.UpdateRadius);

        World world = block.getWorld();
        IMinecraftWorldServer worldServer = getWorldServer();

        for (Block nearbyBlock : updateBlocks) {
            worldServer.Notify(world, nearbyBlock.getX(), nearbyBlock.getY(), nearbyBlock.getZ());
        }
    }

    public static void Update(List<Block> blocks) {
        if (blocks.size() <= 0)
            return;

        HashSet<Block> updateBlocks = new HashSet<Block>();
        for (Block block : blocks) {
            if (needsUpdate(block)) {
                updateBlocks.addAll(GetAjacentBlocks(block.getWorld(), new HashSet<Block>(), block, OrebfuscatorConfig.UpdateRadius));
            }
        }

        World world = blocks.get(0).getWorld();
        IMinecraftWorldServer worldServer = getWorldServer();

        for (Block nearbyBlock : updateBlocks) {
            worldServer.Notify(world, nearbyBlock.getX(), nearbyBlock.getY(), nearbyBlock.getZ());
        }
    }

    public static HashSet<Block> GetAjacentBlocks(World world, HashSet<Block> allBlocks, Block block, int countdown) {
        if (block == null)
            return allBlocks;

        AddBlockCheck(allBlocks, block);

        if (countdown == 0)
            return allBlocks;

        GetAjacentBlocks(world, allBlocks, CalculationsUtil.getBlockAt(world, block.getX() + 1, block.getY(), block.getZ()), countdown - 1);
        GetAjacentBlocks(world, allBlocks, CalculationsUtil.getBlockAt(world, block.getX() - 1, block.getY(), block.getZ()), countdown - 1);
        GetAjacentBlocks(world, allBlocks, CalculationsUtil.getBlockAt(world, block.getX(), block.getY() + 1, block.getZ()), countdown - 1);
        GetAjacentBlocks(world, allBlocks, CalculationsUtil.getBlockAt(world, block.getX(), block.getY() - 1, block.getZ()), countdown - 1);
        GetAjacentBlocks(world, allBlocks, CalculationsUtil.getBlockAt(world, block.getX(), block.getY(), block.getZ() + 1), countdown - 1);
        GetAjacentBlocks(world, allBlocks, CalculationsUtil.getBlockAt(world, block.getX(), block.getY(), block.getZ() - 1), countdown - 1);

        return allBlocks;
    }

    public static void AddBlockCheck(HashSet<Block> allBlocks, Block block) {
        if ((OrebfuscatorConfig.isObfuscated((byte) block.getTypeId(), block.getWorld().getEnvironment() == Environment.NETHER) || OrebfuscatorConfig.isDarknessObfuscated((byte) block.getTypeId()))) {
            allBlocks.add(block);
        }
    }
}
