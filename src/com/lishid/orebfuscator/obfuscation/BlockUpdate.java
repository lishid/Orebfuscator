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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import com.lishid.orebfuscator.Orebfuscator;
import com.lishid.orebfuscator.OrebfuscatorConfig;

public class BlockUpdate
{
    public static void UpdateBlocksNearby(Block block)
    {
        HashSet<Block> blocks = GetAjacentBlocks(block.getWorld(), new HashSet<Block>(), block, OrebfuscatorConfig.getUpdateRadius());
        
        HashSet<CraftPlayer> players = new HashSet<CraftPlayer>();
        
        List<Player> playerList = getPlayers(block.getWorld());
        
        for (Player player : playerList)
        {
            double dx = Math.abs((((int)player.getLocation().getX()) >> 4) - (block.getX() >> 4));
            double dz = Math.abs((((int)player.getLocation().getZ()) >> 4) - (block.getZ() >> 4));
            double dist = Bukkit.getServer().getViewDistance();
            if (dx <= dist && dz <= dist)
            {
                players.add((CraftPlayer) player);
            }
        }
        
        blocks.remove(block);
        
        for (Block nearbyBlock : blocks)
        {
            UpdateBlock(nearbyBlock, players);
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
    
    public static List<Player> getPlayers(World world)
    {
        List<Player> players = new ArrayList<Player>();
        
        synchronized (Orebfuscator.players)
        {
            for (Player p : Orebfuscator.players.keySet())
            {
                if (p.getWorld().getName().equals(world.getName()))
                    players.add(p);
            }
        }
        
        return players;
    }
    
    public static void UpdateBlock(Block block, HashSet<CraftPlayer> players)
    {
        if (block == null)
            return;
        
        for (CraftPlayer player : players)
        {
            player.sendBlockChange(block.getLocation(), block.getTypeId(), block.getData());
        }
    }
}
