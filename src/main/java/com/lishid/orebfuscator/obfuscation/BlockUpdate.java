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

import net.minecraft.server.v1_9_R1.BlockPosition;
import net.minecraft.server.v1_9_R1.IBlockData;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_9_R1.CraftChunk;

import com.lishid.orebfuscator.OrebfuscatorConfig;
import com.lishid.orebfuscator.internal.BlockInfo;
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

        World world = blocks.get(0).getWorld();
        HashSet<BlockInfo> updateBlocks = new HashSet<BlockInfo>();
        
        for (Block block : blocks) {
            if (needsUpdate(block)) {
            	BlockInfo blockInfo = new BlockInfo();
            	blockInfo.x = block.getX();
            	blockInfo.y = block.getY();
            	blockInfo.z = block.getZ();
            	
            	BlockPosition blockPosition = new BlockPosition(blockInfo.x, blockInfo.y, blockInfo.z);
            	IBlockData blockData = ((CraftChunk)block.getChunk()).getHandle().getBlockData(blockPosition);
            	
            	blockInfo.blockData = blockData;

            	GetAjacentBlocks(updateBlocks, world, blockInfo, OrebfuscatorConfig.UpdateRadius);
            }
        }

        sendUpdates(world, updateBlocks);
    }

    private static void sendUpdates(World world, Set<BlockInfo> blocks) {
        for (BlockInfo blockInfo : blocks) {
            MinecraftInternals.notifyBlockChange(world, blockInfo);
        }
    }

    private static void GetAjacentBlocks(HashSet<BlockInfo> allBlocks, World world, BlockInfo blockInfo, int countdown) {
        if (blockInfo == null) return;
        
        int blockId = blockInfo.getTypeId();

        if ((OrebfuscatorConfig.isObfuscated(blockId, world.getEnvironment())
                || OrebfuscatorConfig.isDarknessObfuscated(blockId)))
        {
            allBlocks.add(blockInfo);
        }

        if (countdown > 0) {
            countdown--;
            GetAjacentBlocks(allBlocks, world, MinecraftInternals.getBlockInfo(world, blockInfo.x + 1, blockInfo.y, blockInfo.z), countdown);
            GetAjacentBlocks(allBlocks, world, MinecraftInternals.getBlockInfo(world, blockInfo.x - 1, blockInfo.y, blockInfo.z), countdown);
            GetAjacentBlocks(allBlocks, world, MinecraftInternals.getBlockInfo(world, blockInfo.x, blockInfo.y + 1, blockInfo.z), countdown);
            GetAjacentBlocks(allBlocks, world, MinecraftInternals.getBlockInfo(world, blockInfo.x, blockInfo.y - 1, blockInfo.z), countdown);
            GetAjacentBlocks(allBlocks, world, MinecraftInternals.getBlockInfo(world, blockInfo.x, blockInfo.y, blockInfo.z + 1), countdown);
            GetAjacentBlocks(allBlocks, world, MinecraftInternals.getBlockInfo(world, blockInfo.x, blockInfo.y, blockInfo.z - 1), countdown);
        }
    }
}
