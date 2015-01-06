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

package com.lishid.orebfuscator.obfuscation;

import com.lishid.orebfuscator.OrebfuscatorConfig;
import com.lishid.orebfuscator.internal.MinecraftWorldServer;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;

import java.util.HashSet;
import java.util.List;

public class BlockUpdate {

    public static boolean needsUpdate(Block block) {
        return !OrebfuscatorConfig.isBlockTransparent((short) block.getTypeId());
    }

    public static void Update(Block block) {
        if (!needsUpdate(block))
            return;

        HashSet<Block> updateBlocks = GetAjacentBlocks(block.getWorld(), new HashSet<Block>(), block, OrebfuscatorConfig.UpdateRadius);

        World world = block.getWorld();

        for (Block nearbyBlock : updateBlocks) {
            MinecraftWorldServer.Notify(world, nearbyBlock.getX(), nearbyBlock.getY(), nearbyBlock.getZ());
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

        for (Block nearbyBlock : updateBlocks) {
            MinecraftWorldServer.Notify(world, nearbyBlock.getX(), nearbyBlock.getY(), nearbyBlock.getZ());
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
