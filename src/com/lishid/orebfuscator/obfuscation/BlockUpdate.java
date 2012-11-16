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

import net.minecraft.server.WorldServer;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.CraftWorld;

import com.lishid.orebfuscator.OrebfuscatorConfig;

public class BlockUpdate
{
    public static boolean needsUpdate(Block block)
    {
        byte id = (byte) block.getTypeId();
        return !OrebfuscatorConfig.isBlockTransparent(id);
    }
    
    public static void Update(Block block)
    {
        if (!needsUpdate(block))
            return;
        
        HashSet<Block> updateBlocks = GetAjacentBlocks(block.getWorld(), new HashSet<Block>(), block, OrebfuscatorConfig.getUpdateRadius());
        
        WorldServer worldServer = ((CraftWorld) block.getWorld()).getHandle();
        
        updateBlocks.remove(block);
        
        for (Block nearbyBlock : updateBlocks)
        {
            worldServer.notify(nearbyBlock.getX(), nearbyBlock.getY(), nearbyBlock.getZ());
        }
    }
    
    public static void Update(List<Block> blocks)
    {
        if (blocks.size() <= 0)
            return;
        
        HashSet<Block> updateBlocks = new HashSet<Block>();
        for (Block block : blocks)
        {
            if (needsUpdate(block))
            {
                updateBlocks.addAll(GetAjacentBlocks(block.getWorld(), new HashSet<Block>(), block, OrebfuscatorConfig.getUpdateRadius()));
            }
        }
        
        WorldServer worldServer = ((CraftWorld) blocks.get(0).getWorld()).getHandle();
        
        for (Block nearbyBlock : updateBlocks)
        {
            worldServer.notify(nearbyBlock.getX(), nearbyBlock.getY(), nearbyBlock.getZ());
        }
    }
    
    public static HashSet<Block> GetAjacentBlocks(World world, HashSet<Block> allBlocks, Block block, int countdown)
    {
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
    
    public static void AddBlockCheck(HashSet<Block> allBlocks, Block block)
    {
        if ((OrebfuscatorConfig.isObfuscated((byte) block.getTypeId()) || OrebfuscatorConfig.isDarknessObfuscated((byte) block.getTypeId())))
        {
            allBlocks.add(block);
        }
    }
}
