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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_9_R1.block.CraftBlock;

import com.lishid.orebfuscator.OrebfuscatorConfig;
import com.lishid.orebfuscator.internal.MinecraftInternals;

public class BlockUpdate {
    public static boolean needsUpdate(Block block) {
        return !OrebfuscatorConfig.isBlockTransparent(block.getTypeId());
    }

    public static void Update(Block block) {
        if (!needsUpdate(block)) {
            return;
        }

        Update(Arrays.asList(new Block[]{block}));
    }

    public static void Update(List<Block> blocks) {
        if (blocks.isEmpty()) {
            return;
        }

        Set<Block> updateBlocks = new HashSet<Block>();
        for (Block block : blocks) {
            if (needsUpdate(block)) {
                updateBlocks.addAll(GetAjacentBlocks(new HashSet<Block>(), block, OrebfuscatorConfig.UpdateRadius));
            }
        }

        World world = blocks.get(0).getWorld();

        sendUpdates(world, updateBlocks);
    }

    private static void sendUpdates(World world, Set<Block> blocks) {
        for (Block block : blocks) {
            MinecraftInternals.notifyBlockChange(world, (CraftBlock)block);
        }
    }

    public static HashSet<Block> GetAjacentBlocks(HashSet<Block> allBlocks, Block block, int countdown) {
        if (block == null) {
            return allBlocks;
        }

        if ((OrebfuscatorConfig.isObfuscated(block.getTypeId(), block.getWorld().getEnvironment())
                || OrebfuscatorConfig.isDarknessObfuscated(block.getTypeId()))) {
            allBlocks.add(block);
        }

        if (countdown > 0) {
            countdown--;
            World world = block.getWorld();
            GetAjacentBlocks(allBlocks, CalculationsUtil.getBlockAt(world, block.getX() + 1, block.getY(), block.getZ()), countdown);
            GetAjacentBlocks(allBlocks, CalculationsUtil.getBlockAt(world, block.getX() - 1, block.getY(), block.getZ()), countdown);
            GetAjacentBlocks(allBlocks, CalculationsUtil.getBlockAt(world, block.getX(), block.getY() + 1, block.getZ()), countdown);
            GetAjacentBlocks(allBlocks, CalculationsUtil.getBlockAt(world, block.getX(), block.getY() - 1, block.getZ()), countdown);
            GetAjacentBlocks(allBlocks, CalculationsUtil.getBlockAt(world, block.getX(), block.getY(), block.getZ() + 1), countdown);
            GetAjacentBlocks(allBlocks, CalculationsUtil.getBlockAt(world, block.getX(), block.getY(), block.getZ() - 1), countdown);
        }

        return allBlocks;
    }
}
